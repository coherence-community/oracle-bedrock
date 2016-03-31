/*
 * File: BlockingQueueRemoteChannel.java
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

import com.oracle.tools.runtime.java.container.Container;
import com.oracle.tools.runtime.java.container.ContainerScope;
import com.oracle.tools.runtime.java.io.Serialization;
import com.oracle.tools.util.CompletionListener;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of a {@link RemoteChannel} that uses {@link BlockingQueue}s
 * as a pipe between applications to send serialized events.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class BlockingQueueRemoteChannel implements RemoteChannel
{
    /**
     * The {@link ExecutorService} to use to run event listener {@link Runnable}s.
     */
    private final ExecutorService executorService;

    /**
     * A {@link ConcurrentMap} of {@link EventStream}s keyed by the stream name.
     */
    private final ConcurrentMap<String,EventStream> streams;

    /**
     * A {@link ConcurrentMap} of {@link BlockingQueue}s keyed by the stream name
     * that are used for incoming events.
     */
    private final ConcurrentMap<String,BlockingQueue<byte[]>> eventQueuesIn;

    /**
     * A {@link ConcurrentMap} of {@link BlockingQueue}s keyed by the stream name
     * that are used for outgoing events.
     */
    private final ConcurrentMap<String,BlockingQueue<byte[]>> eventQueuesOut;

    /**
     * Create a {@link BlockingQueueRemoteChannel} using the specified
     * {@link ExecutorService} and {@link ConcurrentMap}s.
     *
     * @param executorService  the {@link ExecutorService} to use to run event
     *                         listener {@link Runnable}s.
     * @param eventQueuesIn    the {@link ConcurrentMap} of {@link BlockingQueue}s
     *                         to used for incoming events.
     * @param eventQueuesOut   the {@link ConcurrentMap} of {@link BlockingQueue}s
     *                         to used for outgoing events.
     */
    public BlockingQueueRemoteChannel(ExecutorService                             executorService,
                                      ConcurrentMap<String,BlockingQueue<byte[]>> eventQueuesIn,
                                      ConcurrentMap<String,BlockingQueue<byte[]>> eventQueuesOut)
    {
        this.executorService = executorService;
        this.eventQueuesIn   = eventQueuesIn;
        this.eventQueuesOut  = eventQueuesOut;
        this.streams         = new ConcurrentHashMap<>();
    }

    @Override
    public void close()
    {
        for (EventStream stream : streams.values())
        {
            try
            {
                stream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        streams.clear();
        eventQueuesIn.clear();
        eventQueuesOut.clear();
    }


    @Override
    public synchronized RemoteEventStream ensureEventStream(String name)
    {
        EventStream stream = streams.get(name);

        if (stream == null)
        {
            BlockingQueue<byte[]> queueIn = eventQueuesIn.get(name);

            if (queueIn == null)
            {
                eventQueuesIn.putIfAbsent(name, new LinkedBlockingQueue<>());

                queueIn = eventQueuesIn.get(name);
            }

            BlockingQueue<byte[]> queueOut = eventQueuesOut.get(name);

            if (queueOut == null)
            {
                eventQueuesOut.putIfAbsent(name, new LinkedBlockingQueue<>());

                queueOut = eventQueuesOut.get(name);
            }

            streams.putIfAbsent(name, new EventStream(name, queueIn, queueOut));

            stream = streams.get(name);

            executorService.submit(stream);
        }

        return stream;
    }


    @Override
    public <T> void submit(RemoteCallable<T> callable, CompletionListener<T> listener) throws IllegalStateException
    {
        try
        {
            // serialize the Callable so that we can deserialize it in the container
            // to use the correct ClassLoader
            final byte[] serializedCallable = Serialization.toByteArray(callable);

            Runnable     scopedRunnable     = new Runnable()
            {
                @Override
                public void run()
                {
                    // remember the current context ClassLoader of the thread
                    // (so that we can return it back to normal when we're finished executing)
                    ClassLoader    originalClassLoader = Thread.currentThread().getContextClassLoader();
                    ClassLoader    classLoader         = executorService.getClass().getClassLoader();
                    ContainerScope originalScope       = Container.getContainerScope();

                    try
                    {
                        // set the context ClassLoader of the Thread to be that of the
                        // ContainerClassLoader
                        Thread.currentThread().setContextClassLoader(classLoader);

                        // dissociate the Thread from the Scope in the Container
                        Container.dissociateThread();

                        // deserialize the callable (so that we can use the container-based class loader)
                        Callable<T> callable = Serialization.fromByteArray(serializedCallable,
                                                                           Callable.class,
                                                                           classLoader);

                        // then call the Callable as usual
                        T result = callable.call();

                        // serialize the result (so that we can use the application class loader)
                        byte[] serializedResult = Serialization.toByteArray(result);

                        // notify the listener (if there is one) of the result
                        if (listener != null)
                        {
                            listener.onCompletion((T) Serialization.fromByteArray(serializedResult,
                                                                                  Object.class,
                                                                                  originalClassLoader));
                        }
                    }
                    catch (Throwable throwable)
                    {
                        // TODO: write the exception to the platform (if diagnostics are on?)

                        // notify the listener (if there is one) of the exception
                        if (listener != null)
                        {
                            listener.onException(throwable);
                        }
                    }
                    finally
                    {
                        // afterwards  associate the Thread with the Scope in the Container
                        Container.associateThreadWith(originalScope);

                        // and return the current context ClassLoader back to normal
                        Thread.currentThread().setContextClassLoader(originalClassLoader);
                    }
                }
            };

            executorService.submit(scopedRunnable);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize the Callable: " + callable, e);
        }
    }


    @Override
    public void submit(RemoteRunnable runnable) throws IllegalStateException
    {
        try
        {
            // serialize the Runnable so that we can deserialize it in the container
            // to use the correct ClassLoader
            final byte[] serializedRunnable = Serialization.toByteArray(runnable);

            Runnable     scopedRunnable     = new Runnable()
            {
                @Override
                public void run()
                {
                    // remember the current context ClassLoader of the thread
                    // (so that we can return it back to normal when we're finished executing)
                    ClassLoader    originalClassLoader = Thread.currentThread().getContextClassLoader();
                    ClassLoader    classLoader         = executorService.getClass().getClassLoader();
                    ContainerScope originalScope       = Container.getContainerScope();

                    try
                    {
                        // set the context ClassLoader of the Thread to be that of the
                        // ContainerClassLoader
                        Thread.currentThread().setContextClassLoader(classLoader);

                        // and dissociate the Thread from the Scope in the Container
                        Container.dissociateThread();

                        // deserialize the runnable (so that we can use the container-based class loader)
                        Runnable runnable = Serialization.fromByteArray(serializedRunnable,
                                                                        Runnable.class,
                                                                        classLoader);

                        // then call the Callable as usual
                        runnable.run();
                    }
                    catch (IOException e)
                    {
                        // TODO: write the exception to the platform (if diagnostics are on?)
                    }
                    finally
                    {
                        // afterwards associate the Thread with the Scope in the Container
                        Container.associateThreadWith(originalScope);

                        // and return the current context ClassLoader back to normal
                        Thread.currentThread().setContextClassLoader(originalClassLoader);
                    }
                }
            };

            executorService.submit(scopedRunnable);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to submit runnable", e);
        }

    }


    /**
     * An implementation of a {@link RemoteEventStream} that uses
     * {@link BlockingQueue}s as a pipe between publishers and consumers
     * of {@link RemoteEvent}s.
     */
    public class EventStream implements RemoteEventStream, Runnable
    {
        /**
         * The unique name of this {@link EventStream}.
         */
        private final String name;

        /**
         * The {@link Set} of {@link RemoteEventListener}s that will receive {@link RemoteEvent}s.
         */
        private final Set<RemoteEventListener> listeners = new HashSet<>();

        private final BlockingQueue<byte[]> queueIn;

        private final BlockingQueue<byte[]> queueOut;

        private boolean running = true;

        /**
         * Create a new {@link EventStream} with the specified name.
         *
         * @param name      the unique name of this {@link EventStream}
         * @param queueIn   the {@link BlockingQueue} of incoming events
         * @param queueOut  the {@link BlockingQueue} of outgoing events
         */
        public EventStream(String name,
                           BlockingQueue<byte[]> queueIn,
                           BlockingQueue<byte[]> queueOut)
        {
            this.name     = name;
            this.queueIn  = queueIn;
            this.queueOut = queueOut;
        }


        @Override
        public String getName()
        {
            return name;
        }


        @Override
        public void close()
        {
            running = false;
        }


        @Override
        public void fireEvent(RemoteEvent event)
        {
            try
            {
                queueOut.offer(Serialization.toByteArray(event));

                onEvent(event);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error serializing event", e);
            }
        }


        @Override
        public void onEvent(RemoteEvent event)
        {
            for (RemoteEventListener listener : listeners)
            {
                try
                {
                    listener.onEvent(event);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }


        @Override
        public void addEventListener(RemoteEventListener listener)
        {
            if (listener != null)
            {
                listeners.add(listener);
            }
        }

        @Override
        public void removeEventListener(RemoteEventListener listener)
        {
            if (listener != null)
            {
                listeners.remove(listener);
            }
        }


        @Override
        public void run()
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // Continue to run until the running flag is false and the event queue is empty
            while(running || !queueIn.isEmpty())
            {
                try
                {
                    byte[]      bytes = queueIn.poll(1, TimeUnit.SECONDS);
                    RemoteEvent event = Serialization.fromByteArray(bytes, RemoteEvent.class, classLoader);

                    for (RemoteEventListener listener : listeners)
                    {
                        try
                        {
                            listener.onEvent(event);
                        }
                        catch (Throwable e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    break;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
