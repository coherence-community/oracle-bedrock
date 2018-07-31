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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.AbstractApplication;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationProcess;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;
import com.oracle.bedrock.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteMethodInvocation;
import com.oracle.bedrock.util.ProxyHelper;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A {@link AbstractJavaApplication} is a base implementation of a {@link JavaApplication} that has
 * a Console (with standard output, standard error and standard input streams).
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public abstract class AbstractJavaApplication<P extends JavaApplicationProcess> extends AbstractApplication<P>
    implements JavaApplication
{
    /**
     * Constructs a {@link AbstractJavaApplication}.
     *
     * @param platform       the {@link Platform} on which the {@link Application} was launched
     * @param process        the underlying {@link ApplicationProcess} representing the {@link Application}
     * @param optionsByType  the {@link OptionsByType} used to launch the {@link Application}
     */
    public AbstractJavaApplication(Platform      platform,
                                   P             process,
                                   OptionsByType optionsByType)
    {
        super(platform, process, optionsByType);
    }


    @Override
    public Properties getSystemProperties()
    {
        return process.getSystemProperties();
    }


    @Override
    public String getSystemProperty(String name)
    {
        try
        {
            return submit(new GetSystemProperty(name)).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new RuntimeException("Error getting System property " + name, e);
        }
    }


    @Override
    public <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
                                           Option...         options) throws IllegalStateException
    {
        return process.submit(callable, options);
    }


    @Override
    public CompletableFuture<Void> submit(RemoteRunnable runnable,
                                          Option...      options) throws IllegalStateException
    {
        return process.submit(runnable, options);
    }


    @Override
    public void addListener(RemoteEventListener listener,
                            Option...           options)
    {
        process.addListener(listener, options);
    }


    @Override
    public void removeListener(RemoteEventListener listener,
                               Option...           options)
    {
        process.removeListener(listener, options);
    }


    @Override
    public CompletableFuture<Void> raise(RemoteEvent event,
                                         Option...   options)
    {
        return process.raise(event, options);
    }


    @Override
    public <T> T getProxyFor(Class<T>                           classToProxy,
                             RemoteCallable<T>                  instanceProducer,
                             RemoteMethodInvocation.Interceptor interceptor)
    {
        return ProxyHelper.createProxyOf(classToProxy, new ProxyMethodInterceptor(instanceProducer, interceptor));
    }


    /**
     * The interceptor for remotely proxied classes.
     *
     * @param <T>  the type of the proxied class
     */
    private class ProxyMethodInterceptor<T> implements ProxyHelper.Interceptor
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

        public Object intercept(Method method, Object[] args) throws Throwable
        {
            if (interceptor != null)
            {
                interceptor.onBeforeRemoteInvocation(method, args);
            }

            RemoteMethodInvocation<T> invocation = new RemoteMethodInvocation<>(instanceProducer,
                                                                                method.getName(),
                                                                                args,
                                                                                interceptor);

            CompletableFuture<Object> future = AbstractJavaApplication.this.submit(invocation);

            try
            {
                Object result = future.get();

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
