/*
 * File: DeferredInvoke.java
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

package com.oracle.bedrock.deferred;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A {@link DeferredInvoke} is a {@link Deferred} that represents a request
 * to invoke a non-void, typically non-static method on a {@link Deferred}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <T>  the expected type of value returned by the invocation.
 *
 * @author Brian Oliver
 */
public class DeferredInvoke<T> implements Deferred<T>
{
    /**
     * The {@link Deferred} on which the {@link Method} should be invoked.
     */
    private Deferred<?> deferred;

    /**
     * The {@link Method} to invoke to produce a {@link Deferred} result.
     */
    private Method method;

    /**
     * The arguments for the {@link Method} invocation.
     */
    private Object[] arguments;


    /**
     * Constructor for a {@link DeferredInvoke}.
     *
     * @param deferred   the {@link Deferred} object on which to invoke
     *                   the specified {@link Method}
     * @param method     the {@link Method} to invoke
     * @param arguments  the arguments for the invocation
     */
    public DeferredInvoke(Deferred<?> deferred,
                          Method      method,
                          Object...   arguments)
    {
        this.deferred  = deferred;
        this.method    = method;
        this.arguments = arguments;
    }


    /**
     * Constructor for a {@link DeferredInvoke}.
     *
     * @param object     the object on which to invoke the specified {@link Method}
     * @param method     the {@link Method} to invoke
     * @param arguments  the arguments for the invocation
     */
    public DeferredInvoke(Object    object,
                          Method    method,
                          Object... arguments)
    {
        this(new Existing<Object>(object), method, arguments);
    }


    @SuppressWarnings("unchecked")
    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        // first try to get the object on which to perform the invocation
        Object object = deferred.get();

        if (object == null)
        {
            throw new PermanentlyUnavailableException(this,
                                                      new NullPointerException("The underlying deferred was null"));
        }
        else
        {
            try
            {
                // now perform the invocation
                return (T) invoke(method, object, arguments);
            }
            catch (IllegalAccessException e)
            {
                // when we can't access the method, we're doomed
                throw new PermanentlyUnavailableException(this, e);
            }
            catch (IllegalArgumentException e)
            {
                // when the method arguments are incorrect, we're doomed
                throw new PermanentlyUnavailableException(this, e);
            }
            catch (InvocationTargetException e)
            {
                // re-throw runtime exceptions (there's no need to wrap them)
                if (e.getTargetException() instanceof RuntimeException)
                {
                    throw(RuntimeException) e.getTargetException();
                }
                else
                {
                    // wrap other exceptions as runtime exceptions
                    throw new TemporarilyUnavailableException(this, e);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getDeferredClass()
    {
        return (Class<T>) method.getReturnType();
    }


    /**
     * This method is a workaround for:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4071957
     *
     * @param method  the {@link Method} you'd like to call
     *                (but may fail if it's on an inner class)
     * @param object  the {@link Object} on which to invoke the {@link Method}
     * @param args    the arguments
     *
     * @return the result of the invocation
     *
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected Object invoke(Method    method,
                            Object    object,
                            Object... args) throws InvocationTargetException, IllegalAccessException
    {
        try
        {
            // first we attempt to invoke the method directly on the object
            return method.invoke(object, args);
        }
        catch (IllegalAccessException ex)
        {
            // when we get an illegal access exception, it's highly likely
            // that the object on which we're attempting to execute the method
            // is "private", "anonymous" or and "inner-class", in which case
            // reflection will fail to find the actual method, even through it
            // exists.  so here we try to find the equivalent method on a
            // public interface, and then invoke using that.
            Class<?>[] interfaces = object.getClass().getInterfaces();
            String     methodName = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();

            for (int i = 0; i < interfaces.length; i++)
            {
                try
                {
                    Method interfaceMethod = interfaces[i].getMethod(methodName, paramTypes);

                    return interfaceMethod.invoke(object, args);
                }
                catch (NoSuchMethodException ex2)
                {
                }
            }

            throw ex;
        }
    }


    @Override
    public String toString()
    {
        StringBuilder arguments = new StringBuilder();

        for (int i = 0; i < this.arguments.length; i++)
        {
            if (i > 0)
            {
                arguments.append(", ");
            }

            arguments.append(this.arguments[i]);
        }

        return String.format("DeferredInvoke{%s.%s(%s)}", getDeferredClass(), method.getName(), arguments);
    }
}
