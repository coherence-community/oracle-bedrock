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

import com.oracle.tools.Options;

import com.oracle.tools.runtime.AbstractApplication;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationProcess;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteEventStream;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.callable.RemoteMethodInvocation;

import com.oracle.tools.util.CompletionListener;
import com.oracle.tools.util.FutureCompletionListener;
import com.oracle.tools.util.ReflectionHelper;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import java.util.Properties;

import java.util.concurrent.Callable;

/**
 * A {@link AbstractJavaApplication} is a base implementation of a {@link JavaApplication} that has
 * a Console (with standard output, standard error and standard input streams).
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplication<P extends JavaApplicationProcess> extends AbstractApplication<P>
    implements JavaApplication
{
    /**
     * Constructs a {@link AbstractJavaApplication}.
     *
     * @param platform  the {@link Platform} on which the {@link Application} was launched
     * @param process   the underlying {@link ApplicationProcess} representing the {@link Application}
     * @param options   the {@link Options} used to launch the {@link Application}
     */
    public AbstractJavaApplication(Platform platform,
                                   P        process,
                                   Options  options)
    {
        super(platform, process, options);
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
        FutureCompletionListener<T> future = new FutureCompletionListener<>();

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
        return process.getSystemProperties();
    }


    @Override
    public String getSystemProperty(String name)
    {
        return submit(new GetSystemProperty(name));
    }


    @Override
    public <T> void submit(RemoteCallable<T>     callable,
                           CompletionListener<T> listener)
    {
        process.submit(callable, listener);
    }


    @Override
    public void submit(RemoteRunnable runnable) throws IllegalStateException
    {
        process.submit(runnable);
    }


    @Override
    public RemoteEventStream ensureEventStream(String name)
    {
        return process.ensureEventStream(name);
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
                                                                              interceptor),
                                                listener);

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
