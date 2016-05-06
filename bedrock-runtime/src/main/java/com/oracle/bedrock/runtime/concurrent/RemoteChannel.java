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

package com.oracle.bedrock.runtime.concurrent;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;

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
import java.util.concurrent.CompletableFuture;

/**
 * Provides a means of submitting {@link RemoteCallable}s and {@link RemoteRunnable}s for
 * asynchronous remote execution, and asynchronously sending and processing
 * {@link RemoteEvent}s.
 * <p>
 * The submitted {@link RemoteCallable}s, {@link RemoteRunnable} and {@link RemoteEvent}s do not
 * necessarily need to be {@link java.io.Serializable}.
 * <p>
 * If they are, they will be serialized/deserialized using Java Serialization.  If they are not,
 * their class-names will be serialized (and then later deserialized when required).
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public interface RemoteChannel extends Closeable
{
    /**
     * An {@link Option} signifying what type of notification is
     * required when raising events.
     */
    enum AcknowledgeWhen implements Option
    {
        /**
         * The {@link CompletableFuture} returned by the {@link RemoteChannel#raise(RemoteEvent, Option...)}
         * will be completed when the event has been raised.
         */
        @Options.Default
        SENT,

        /**
         * The {@link CompletableFuture} returned by the {@link RemoteChannel#raise(RemoteEvent, Option...)}
         * will be completed when the event has been processed by the remote listeners.
         */
        PROCESSED
    }


    /**
     * Submits a {@link RemoteCallable} for asynchronous execution by the
     * {@link RemoteChannel}.
     *
     * @param callable  the {@link RemoteCallable} to be executed
     * @param options   the {@link Option}s for the {@link RemoteCallable}
     * @param <T>       the return type of the {@link RemoteCallable}
     *
     * @return  a {@link CompletableFuture} that will be completed with the result
     *                                      of the {@link RemoteCallable} execution.
     *
     * @throws IllegalStateException  if the {@link RemoteChannel} is closed or
     *                                is unable to accept the submission
     */
    public <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
                                           Option...         options) throws IllegalStateException;


    /**
     * Submits a {@link RemoteRunnable} for asynchronous execution by the
     * {@link RemoteChannel}.
     *
     * @param runnable  the {@link RemoteRunnable} to be executed
     * @param options   the {@link Option}s for the {@link RemoteRunnable}
     *
     * @return  a {@link CompletableFuture} that will be completed when the
     *                                      {@link RemoteRunnable} is executed.
     *
     * @throws IllegalStateException  if the {@link RemoteChannel} is closed or
     *                                is unable to accept the submission
     */
    public CompletableFuture<Void> submit(RemoteRunnable runnable,
                                          Option...      options) throws IllegalStateException;


    /**
     * Adds a {@link RemoteEventListener} to the {@link RemoteChannel} so that it
     * can handle and process {@link RemoteEvent}s.
     * <p>
     * The specified {@link Option}s allow customized {@link RemoteEventListener}
     * processing.  For example; processing only {@link RemoteEvent}s on
     * particular "streams" is permitted by specifying the {@link StreamName} option.
     * <p>
     * Note: {@link RemoteEventListener}s may be added before a {@link RemoteChannel}
     * has been started.
     *
     * @param listener  the {@link RemoteEventListener}
     * @param options   the {@link Option}s
     */
    public void addListener(RemoteEventListener listener,
                            Option...           options);


    /**
     * Removes a previously added {@link RemoteEventListener}.
     * <p>
     * Note: {@link RemoteEventListener}s may be removed after a {@link RemoteChannel}
     * has been stopped.
     *
     * @param listener  the {@link RemoteEventListener} to remove
     * @param options   the {@link Option}s used to add the {@link RemoteEventListener}
     */
    public void removeListener(RemoteEventListener listener,
                               Option...           options);


    /**
     * Raise an {@link RemoteEvent} on the opposite end of the {@link RemoteChannel}
     * for registered {@link RemoteEventListener}s using the provided {@link Option}s.
     * <p>
     * The specified {@link Option}s allow customized {@link RemoteEvent} deliver.
     * For example; delivering {@link RemoteEvent}s for particular "streams" is
     * permitted by specifying the {@link StreamName} option.
     *
     * @param event    the {@link RemoteEvent}
     * @param options  the {@link Option}s
     *
     * @return a {@link CompletableFuture} allowing an application to determine
     *         when the event has been raised
     */
    public CompletableFuture<Void> raise(RemoteEvent event,
                                         Option...   options);


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
        public static void injectChannel(Class<?>      targetClass,
                                         RemoteChannel channel)
        {
            if (channel == null)
            {
                return;
            }

            try
            {
                ClassLoader loader = targetClass.getClassLoader();
                Class<Annotation> annotation =
                    (Class<Annotation>) loader.loadClass(RemoteChannel.Inject.class.getName());
                Class<?> channelClass = channel.getClass();

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
}
