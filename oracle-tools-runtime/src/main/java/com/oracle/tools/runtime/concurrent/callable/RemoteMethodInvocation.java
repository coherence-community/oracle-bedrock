/*
 * File: RemoteMethodInvocation.java
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

package com.oracle.tools.runtime.concurrent.callable;

import com.oracle.tools.runtime.concurrent.RemoteCallable;

import com.oracle.tools.util.ReflectionHelper;

import java.io.Serializable;

import java.lang.reflect.Method;

/**
 * A {@link RemoteCallable} representing a remote method invocation against
 * a remote instance (also represented by a {@link RemoteCallable}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <T>  the type of the instance on which the method will be invoked
 */
public class RemoteMethodInvocation<T> implements RemoteCallable
{
    /**
     * The {@link RemoteCallable} that will produce the instance on which
     * to invoke the specified method.
     */
    private RemoteCallable<T> instanceProducer;

    /**
     * The name of the method to invoke on the instance.
     */
    private String methodName;

    /**
     * The arguments for the method invocation.
     */
    private Object[] arguments;

    /**
     * The optional (may be <code>null</code>) {@link Interceptor} to use
     * for intercepting {@link Method} invocations.
     */
    private Interceptor interceptor;


    /**
     * Constructs a {@link RemoteMethodInvocation}.
     *
     * @param instanceProducer  the {@link RemoteCallable} that produces the instance
     *                          on which the method should be invoked
     * @param methodName        the name of the method to invoke
     * @param arguments              the arguments to the instance
     * @param interceptor       the optional (may be null) {@link Interceptor}
     */
    public RemoteMethodInvocation(RemoteCallable<T> instanceProducer,
                                  String            methodName,
                                  Object[]          arguments,
                                  Interceptor       interceptor)
    {
        this.instanceProducer = instanceProducer;
        this.methodName       = methodName;
        this.arguments        = arguments;
        this.interceptor      = interceptor;
    }


    /**
     * Obtain the {@link Interceptor} for the {@link RemoteMethodInvocation}.
     *
     * @return  the {@link Interceptor} or <code>null</code> if not defined
     */
    public Interceptor getInterceptor()
    {
        return interceptor;
    }


    @Override
    public Object call() throws Exception
    {
        T instance = instanceProducer.call();

        if (instance == null)
        {
            throw new NullPointerException("Remote Instance is null");
        }
        else
        {
            // find the compatible method on the instance
            Method method = ReflectionHelper.getCompatibleMethod(instance.getClass(), methodName, arguments);

            if (method == null)
            {
                throw new NoSuchMethodException(methodName);
            }
            else
            {
                // make the method accessible so we can call it
                method.setAccessible(true);

                if (interceptor != null)
                {
                    interceptor.onBeforeInvocation(instance, method, arguments);
                }

                // invoke the method
                try
                {
                    Object result = method.invoke(instance, arguments);

                    // transform the result (if we have a transformer)
                    if (interceptor != null)
                    {
                        result = interceptor.onAfterInvocation(instance, method, arguments, result);
                    }

                    return result;
                }
                catch (Exception e)
                {
                    if (interceptor == null)
                    {
                        // rethrow if there's no interception happening
                        throw e;
                    }
                    else
                    {
                        throw interceptor.onInvocationException(instance, method, arguments, e);
                    }
                }
            }
        }
    }


    /**
     * Provides the ability to intercept remote {@link Method} invocations
     * so that arguments, results and exceptions may be transformed.
     */
    public static interface Interceptor extends Serializable
    {
        /**
         * Called before the {@link Method} invocation occurs and the arguments
         * are serialized for dispatching remotely (to allow transforming
         * arguments prior to execution).
         * <p>
         * By implementing this {@link Method}, developers may change the
         * arguments submitted to the remote {@link Method}.  Furthermore developers
         * may choose to throw unsupported exceptions should analysis the
         * planned invocation prove to be unsupported, for example if one or more
         * arguments aren't serializable.
         *
         * @param method     the {@link Method} to be invoked
         * @param arguments  the supplied arguments to the {@link Method}
         *                   (these may be changed if necessary)
         */
        public void onBeforeRemoteInvocation(Method   method,
                                             Object[] arguments);


        /**
         * Called after the {@link Method} invocation has occurred and the resulting
         * result has deserialized but before the result has been returned locally
         * (to allow returning an alternative result).
         * <p>
         * By implementing this {@link Method}, developers may change the
         * returned result of the {@link Method}.  Furthermore developers
         * may choose to throw various runtime time exceptions should analysis the
         * invocation result prove to be unsupported or invalid.
         *
         * @param method     the {@link Method} that was invoked
         * @param arguments  the arguments supplied to the {@link Method}
         * @param result     the returned result
         * @return   the result that should be returned
         */
        public Object onAfterRemoteInvocation(Method   method,
                                              Object[] arguments,
                                              Object   result);


        /**
         * Called after the {@link Method} invocation has occurred and the
         * resulting {@link Exception} has been deserialized and before the
         * {@link Exception} has been raised locally (to allow throwing an
         * alternative {@link Exception}).
         * <p>
         * By implementing this {@link Method}, developers may change the
         * returned result of the {@link Method}.  Furthermore developers
         * may choose to throw various runtime time exceptions should analysis the
         * invocation result prove to be unsupported or invalid.
         *
         * @param method     the {@link Method} that was invoked
         * @param arguments  the arguments supplied to the {@link Method}
         * @param exception  the {@link Exception} that occurred
         * @return  the {@link Exception} to throw
         */
        public Exception onRemoteInvocationException(Method    method,
                                                     Object[]  arguments,
                                                     Exception exception);


        /**
         * Called before the invocation of the {@link Method} on the provided
         * instance to allow transforming arguments prior to execution.
         *
         * @param instance   the instance on which the {@link Method} will be invoked
         *                   (<code>null</code> when using static methods)
         * @param method     the {@link Method} to be invoked
         * @param arguments  the arguments supplied to the {@link Method}
         */
        public void onBeforeInvocation(Object   instance,
                                       Method   method,
                                       Object[] arguments);


        /**
         * Called after the invocation of the {@link Method} on the provided
         * instance (to allow returning an alternative result).
         *
         * @param instance   the instance on which the {@link Method} was invoked
         *                   (<code>null</code> when using static methods)
         * @param method     the {@link Method} that was invoked
         * @param arguments  the arguments supplied to the {@link Method}
         * @param result     the returned result
         * @return  the result that should be returned
         */
        public Object onAfterInvocation(Object   instance,
                                        Method   method,
                                        Object[] arguments,
                                        Object   result);


        /**
         * Called after the invocation of the {@link Method} has produced an
         * {@link Exception} (to allow throwing an alternative {@link Exception}).
         *
         * @param instance   the instance on which the {@link Method} was invoked
         *                   (<code>null</code> when using static methods)
         * @param method     the {@link Method} that was invoked
         * @param arguments  the arguments supplied to the {@link Method}
         * @param exception  the {@link Exception} that occurred
         */
        public Exception onInvocationException(Object    instance,
                                               Method    method,
                                               Object[]  arguments,
                                               Exception exception);
    }
}
