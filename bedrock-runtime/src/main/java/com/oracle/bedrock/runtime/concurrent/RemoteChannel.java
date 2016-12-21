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
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.JavaApplicationLauncher;
import com.oracle.bedrock.runtime.java.JavaApplicationRunner;
import com.oracle.bedrock.runtime.java.container.Container;
import com.oracle.bedrock.runtime.java.container.ContainerScope;

import java.io.Closeable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
        @OptionsByType.Default
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
     * @throws IllegalStateException     if the {@link RemoteChannel} is closed or
     *                                   is unable to accept the submission
     * @throws IllegalArgumentException  if the {@link RemoteCallable} isn't serializable, is anonymous
     *                                   or a non-static inner class
     */
    <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
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
     * @throws IllegalStateException     if the {@link RemoteChannel} is closed or
     *                                   is unable to accept the submission
     * @throws IllegalArgumentException  if the {@link RemoteCallable} isn't serializable, is anonymous
     *                                   or a non-static inner class
     */
    CompletableFuture<Void> submit(RemoteRunnable runnable,
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
    void addListener(RemoteEventListener listener,
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
    void removeListener(RemoteEventListener listener,
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
    CompletableFuture<Void> raise(RemoteEvent event,
                                  Option...   options);


    /**
     * Acquires the {@link RemoteChannel} for the {@link JavaApplication} that was launched
     * with a {@link JavaApplicationLauncher}.
     *
     * @return the {@link RemoteChannel} or <code>null</code> if it's not available
     */
    static RemoteChannel get()
    {
        // determine if we're running in a Container?
        ContainerScope containerScope = Container.getContainerScope();

        if (containerScope == null)
        {
            // not running in a container, so assume we used the JavaApplicationRunner
            return JavaApplicationRunner.channel;
        }
        else
        {
            // when in a container, acquire the remote channel from the container scope
            return (RemoteChannel) containerScope.getRemoteChannel();
        }

    }


    /**
     * Provides a mechanism to inject a {@link RemoteChannel} into a
     * {@link RemoteCallable} or {@link RemoteRunnable} prior to execution.
     * <p>
     * When used on fields, the fields should be public, non-final and
     * non-static, with the type {@link RemoteChannel}.
     * <p>
     * When used on methods, the methods should be public and non-static, with
     * a single parameter type {@link RemoteChannel}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Inject
    {
    }
}
