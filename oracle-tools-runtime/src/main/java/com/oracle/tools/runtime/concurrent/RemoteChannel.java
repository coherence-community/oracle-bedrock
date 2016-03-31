/*
 * File: RemoteChannel.java
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

package com.oracle.tools.runtime.concurrent;

import com.oracle.tools.util.CompletionListener;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;

/**
 * Provides a means of submitting {@link RemoteCallable}s and/or
 * {@link RemoteRunnable}s for asynchronous remote execution and for
 * creating {@link RemoteEventStream}s.
 * <p>
 * The submitted {@link Callable}s/{@link Runnable}s are not required to be
 * {@link java.io.Serializable}.
 * If they are, they will be serialized/deserialized.  If they are not, their
 * class-names will be serialized (and then later deserialized for execution).
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see ControllableRemoteChannel
 */
public interface RemoteChannel extends Closeable
{
    /**
     * Submits a {@link RemoteCallable} for asynchronous execution by the
     * {@link RemoteChannel}.
     *
     * @param callable  the {@link RemoteCallable} to be executed
     * @param listener  a {@link CompletionListener} to be notified upon
     *                  completed execution of the {@link RemoteCallable}
     * @param <T>       the return type of the {@link RemoteCallable}
     *
     * @throws IllegalStateException  if the {@link RemoteChannel} is closed or
     *                                is unable to accept the submission
     */
    public <T> void submit(RemoteCallable<T>     callable,
                           CompletionListener<T> listener) throws IllegalStateException;


    /**
     * Submits a {@link RemoteRunnable} for asynchronous execution by the
     * {@link RemoteChannel}.
     *
     * @param runnable  the {@link RemoteRunnable} to be executed
     *
     * @throws IllegalStateException  if the {@link RemoteChannel} is closed or
     *                                is unable to accept the submission
     */
    public void submit(RemoteRunnable runnable) throws IllegalStateException;


    /**
     * Obtain the {@link RemoteEventStream} with the specified name,
     * creating a new instance of a {@link RemoteEventStream} if one
     * does not already exist.
     *
     * @param name  the name of the {@link RemoteEventStream} to obtain
     *
     * @return  the {@link RemoteEventStream} identified by the specified name
     *
     * @throws NullPointerException if the name is null
     */
    public RemoteEventStream ensureEventStream(String name);


    /**
     * Defines how a {@link RemoteChannel} may be injected into a {@link Class}
     * that requires a reference to a {@link RemoteChannel}. The target class is
     * typically the main class that runs as an application.
     * <p>
     * If this annotation is used on fields the fields should be public
     * static and of type {@link RemoteChannel}.
     * <p>
     * If this annotation is used on methods the methods should be public
     * static and have a single parameter of type {@link RemoteChannel}.
     * <p>
     *
     * The {@link Inject} annotation can be used to specify a public static
     * field to inject a {@link RemoteChannel} reference into.
     * <pre><code>
     * public class App {
     *     ...
     *     &#64;RemoteChannel.Inject
     *     public static RemoteChannel CHANNEL;
     *     ...
     * }
     * </code></pre>
     *
     * Alternatively, the {@link Inject} annotation can be used to specify that
     * the public static method is called for injecting the value.
     * <pre><code>
     * public class App {
     *     ...
     *     &#64;RemoteChannel.Inject
     *     public static void setPublisher(RemoteChannel channel) {
     *         ...
     *     }
     *     ...
     * }
     * </code></pre>
     *
     * @see Injector#injectChannel(Class, RemoteChannel)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
    @interface Inject
    {
    }


    /**
     * An abstract class with a single method to inject a {@link RemoteChannel}
     * into static fields or static methods of other classes.
     */
    abstract class Injector
    {
        /**
         * Inject the specified {@link RemoteChannel} into static fields or static methods of
         * the specified {@link Class}.
         *
         * @param targetClass  the {@link Class} to have the {@link RemoteChannel} injected
         * @param channel      the {@link RemoteChannel} to inject
         */
        public static void injectChannel(Class<?> targetClass, RemoteChannel channel)
        {
            if (channel == null)
            {
                return;
            }

            try
            {
                ClassLoader       loader       = targetClass.getClassLoader();
                Class<Annotation> annotation   = (Class<Annotation>) loader.loadClass(RemoteChannel.Inject.class.getName());
                Class<?>          channelClass = channel.getClass();

                for (Method method : targetClass.getMethods())
                {
                    int modifiers = method.getModifiers();

                    if (method.getAnnotation(annotation) != null
                        && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].isAssignableFrom(channelClass)
                        && Modifier.isStatic(modifiers)
                        && Modifier.isPublic(modifiers))
                    {
                        try
                        {
                            method.invoke(null, channel);
                        }
                        catch (Exception e)
                        {
                            // carry on... perhaps we can use another approach?
                        }
                    }
                }


                for (Field field : targetClass.getFields())
                {
                    int modifiers = field.getModifiers();

                    if (field.getAnnotation(annotation) != null
                        && Modifier.isStatic(modifiers)
                        && Modifier.isPublic(modifiers)
                        && field.getType().isAssignableFrom(channelClass))
                    {
                        try
                        {
                            field.set(null, channel);
                        }
                        catch (Exception e)
                        {
                            // carry on... perhaps we can use another approach?
                        }
                    }
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

}
