/*
 * File: AbstractJavaApplication.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.tools.runtime.java;

import com.oracle.tools.Option;

import com.oracle.tools.deferred.Cached;
import com.oracle.tools.deferred.Deferred;
import com.oracle.tools.deferred.NeverAvailable;
import com.oracle.tools.deferred.PermanentlyUnavailableException;

import com.oracle.tools.deferred.jmx.DeferredJMXConnector;
import com.oracle.tools.deferred.jmx.DeferredMBeanAttribute;
import com.oracle.tools.deferred.jmx.DeferredMBeanInfo;
import com.oracle.tools.deferred.jmx.DeferredMBeanProxy;

import com.oracle.tools.runtime.AbstractApplication;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationListener;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.callable.RemoteMethodInvocation;

import com.oracle.tools.util.CompletionListener;
import com.oracle.tools.util.FutureCompletionListener;
import com.oracle.tools.util.ReflectionHelper;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import static com.oracle.tools.deferred.DeferredHelper.cached;
import static com.oracle.tools.deferred.DeferredHelper.ensured;
import static com.oracle.tools.deferred.DeferredHelper.within;

import java.io.IOException;

import java.lang.reflect.Method;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.Properties;
import java.util.Set;

import java.util.concurrent.Callable;

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;

import javax.management.remote.JMXConnector;

/**
 * A {@link AbstractJavaApplication} is a base implementation of a {@link JavaApplication} that has
 * a Console (with standard output, standard error and standard input streams).
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplication<A extends AbstractJavaApplication<A, P, R>,
                                              P extends JavaApplicationProcess, R extends JavaApplicationRuntime<P>>
    extends AbstractApplication<A, P, R> implements FluentJavaApplication<A>
{
    /**
     * The {@link Cached} representing the {@link JMXConnector}.
     */
    protected Cached<JMXConnector> cachedJmxConnector;


    /**
     * Construct a {@link AbstractJavaApplication}.
     *
     * @param runtime    the {@link JavaApplicationRuntime} for the {@link JavaApplication}
     * @param listeners  the {@link ApplicationListener}s
     */
    public AbstractJavaApplication(R                                        runtime,
                                   Iterable<ApplicationListener<? super A>> listeners)
    {
        super(runtime, listeners);

        // ensure that the RMI server doesn't eagerly load JMX classes
        System.setProperty("java.rmi.server.useCodebaseOnly", "true");

        // establish our JMX connector
        if (isJMXEnabled())
        {
            // define a DeferredResource representation of a JMXConnector
            // for this application.  we use a DeferredResource as the
            // application may not have started, or be ready for JMX connections
            String url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi",
                                       getRMIServerHostName(),
                                       getRemoteJMXPort());

            // use a CachedResource as once the JMXConnector is established
            // we don't want to create another JMXConnector for the application
            cachedJmxConnector = cached(new DeferredJMXConnector(url));
        }
        else
        {
            cachedJmxConnector = cached(new NeverAvailable<JMXConnector>(JMXConnector.class));
        }
    }


    /**
     * Submits a {@link Callable} for remote execution by the
     * {@link Application} and blocks waiting for the result.
     *
     * @param callable  the {@link Callable} to execute
     * @param <T>       the type of the result
     */
    public <T> T submit(RemoteCallable<T> callable)
    {
        FutureCompletionListener<T> future = new FutureCompletionListener<T>();

        submit(callable, future);

        try
        {
            return future.get();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to execute the Callable: " + callable, e);
        }

    }


    @Override
    public Properties getSystemProperties()
    {
        return runtime.getSystemProperties();
    }


    @Override
    public String getSystemProperty(String name)
    {
        return getSystemProperties().getProperty(name);
    }


    @Override
    public boolean isJMXEnabled()
    {
        return getSystemProperties().containsKey(SUN_MANAGEMENT_JMXREMOTE_PORT);
    }


    @Override
    public int getRemoteJMXPort()
    {
        if (isJMXEnabled())
        {
            return Integer.parseInt(getSystemProperties().getProperty(SUN_MANAGEMENT_JMXREMOTE_PORT));
        }
        else
        {
            throw new UnsupportedOperationException("Application is not enabled for remote JMX management");
        }
    }


    @Override
    public String getRMIServerHostName()
    {
        String hostname = getSystemProperties().getProperty(JAVA_RMI_SERVER_HOSTNAME);

        return hostname == null ? getPlatform().getAddress().getHostAddress() : hostname;
    }


    @Override
    public Deferred<JMXConnector> getDeferredJMXConnector()
    {
        return cachedJmxConnector;
    }


    @Override
    public <T> Deferred<T> getDeferredMBeanProxy(ObjectName objectName,
                                                 Class<T>   proxyClass)
    {
        return new Cached<T>(new DeferredMBeanProxy<T>(cachedJmxConnector, objectName, proxyClass));
    }


    @Override
    public <T> T getMBeanProxy(ObjectName objectName,
                               Class<T>   proxyClass)
    {
        return ensured(getDeferredMBeanProxy(objectName, proxyClass), within(getDefaultTimeout())).get();
    }


    @Override
    public Deferred<MBeanInfo> getDeferredMBeanInfo(ObjectName objectName)
    {
        return new DeferredMBeanInfo(cachedJmxConnector, objectName);
    }


    @Override
    public MBeanInfo getMBeanInfo(ObjectName objectName)
    {
        return ensured(getDeferredMBeanInfo(objectName), within(getDefaultTimeout())).get();
    }


    @Override
    public <T> Deferred<T> getDeferredMBeanAttribute(ObjectName objectName,
                                                     String     attributeName,
                                                     Class<T>   attributeClass)
    {
        return new DeferredMBeanAttribute<T>(cachedJmxConnector, objectName, attributeName, attributeClass);
    }


    @Override
    public <T> T getMBeanAttribute(ObjectName objectName,
                                   String     attributeName,
                                   Class<T>   attributeClass)
    {
        return ensured(getDeferredMBeanAttribute(objectName, attributeName, attributeClass),
                       within(getDefaultTimeout())).get();
    }


    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name,
                                           QueryExp   query)
    {
        try
        {
            return ensured(getDeferredJMXConnector(),
                           within(getDefaultTimeout())).get().getMBeanServerConnection().queryMBeans(name, query);
        }
        catch (IOException e)
        {
            throw new PermanentlyUnavailableException(getDeferredJMXConnector(), e);
        }
    }


    @Override
    public <T> void submit(RemoteCallable<T>     callable,
                           CompletionListener<T> listener)
    {
        runtime.getApplicationProcess().submit(callable, listener);
    }


    @Override
    public void submit(RemoteRunnable runnable) throws IllegalStateException
    {
        runtime.getApplicationProcess().submit(runnable);
    }


    @Override
    public void close(Option... options)
    {
        // close the JMXConnector (if we've got one)
        JMXConnector jmxConnector = cachedJmxConnector.release();

        if (jmxConnector != null)
        {
            try
            {
                jmxConnector.close();
            }
            catch (IOException e)
            {
                // nothing to do here as we don't care
            }
        }

        super.close(options);
    }


    @Override
    public InetSocketAddress getRemoteDebugSocket()
    {
        int remoteDebuggingPort = runtime.getRemoteDebuggingPort();

        if (remoteDebuggingPort <= 0)
        {
            return null;
        }

        Platform platform = getPlatform();

        if (platform == null)
        {
            return null;
        }

        InetAddress address = platform.getAddress();

        return new InetSocketAddress(address, remoteDebuggingPort);
    }


    @Override
    public <T> T getProxyFor(Class<T>                           classToProxy,
                             RemoteCallable<T>                  instanceProducer,
                             RemoteMethodInvocation.Interceptor interceptor)
    {
        return ReflectionHelper.createProxyOf(classToProxy, new ProxyMethodInterceptor(instanceProducer, interceptor));
    }


    /**
     * The {@link MethodInterceptor} for remotely proxied classes.
     *
     * @param <T>  the type of the proxied class
     */
    private class ProxyMethodInterceptor<T> implements MethodInterceptor
    {
        /**
         * The {@link RemoteCallable} that will provide the application instance
         * on which proxy method calls should be invoked
         */
        private RemoteCallable<T> instanceProducer;

        /**
         * An optional {@link RemoteMethodInvocation.Interceptor} to
         * intercept remote {@link Method} invocations.
         */
        private RemoteMethodInvocation.Interceptor interceptor;


        /**
         * Constructs a {@link ProxyMethodInterceptor} with the ability to raise
         * {@link UnsupportedOperationException}s for {@link Method}s that can't be implemented.
         *
         * @param instanceProducer  the {@link RemoteCallable} that produces the instance on which
         *                          proxied methods should be invoked
         * @param interceptor       the optional {@link RemoteMethodInvocation.Interceptor} to
         *                          intercept remote {@link Method} invocations
         */
        public ProxyMethodInterceptor(RemoteCallable<T>                  instanceProducer,
                                      RemoteMethodInvocation.Interceptor interceptor)
        {
            this.instanceProducer = instanceProducer;
            this.interceptor      = interceptor;
        }


        @Override
        public Object intercept(Object      self,
                                Method      method,
                                Object[]    args,
                                MethodProxy methodProxy) throws Throwable
        {
            if (interceptor != null)
            {
                interceptor.onBeforeRemoteInvocation(method, args);
            }

            FutureCompletionListener listener = new FutureCompletionListener();

            AbstractJavaApplication.this.submit(new RemoteMethodInvocation<T>(instanceProducer,
                                                                              method.getName(),
                                                                              args,
                                                                              interceptor), listener);

            try
            {
                Object result = listener.get();

                if (interceptor != null)
                {
                    result = interceptor.onAfterRemoteInvocation(method, args, result);
                }

                return result;

            }
            catch (Exception e)
            {
                if (interceptor == null)
                {
                    throw e;
                }
                else
                {
                    throw interceptor.onRemoteInvocationException(method, args, e);
                }
            }
        }
    }
}
