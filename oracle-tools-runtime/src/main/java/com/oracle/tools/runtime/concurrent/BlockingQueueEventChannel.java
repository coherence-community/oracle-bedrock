/*
 * File: QueueEventChannel.java
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

import com.oracle.tools.runtime.java.io.Serialization;

import java.io.IOException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of a {@link RemoteEventChannel.Publisher} and  {@link RemoteEventChannel.Consumer}
 * pair that use a {@link BlockingQueue} as the communication pipe for {@link RemoteEvent}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class BlockingQueueEventChannel
{
    /**
     * An implementation of a {@link RemoteEventChannel.Publisher} that
     * sends {@link RemoteEvent}s to a {@link Queue} in their serialized
     * form as byte arrays.
     */
    public static class Publisher implements RemoteEventChannel.Publisher
    {
        /**
         * The {@link Queue} of serialized {@link RemoteEvent}s.
         */
        private final Queue<byte[]> eventQueue;

        /**
         * Create a {@link Publisher} that will use the specified
         * {@link Queue} to store serialized {@link RemoteEvent}s.
         *
         * @param eventQueue  the {@link Queue} to use to store
         *                    serialized {@link RemoteEvent}s.
         */
        public Publisher(Queue<byte[]> eventQueue)
        {
            this.eventQueue = eventQueue;
        }

        @Override
        public void fireEvent(RemoteEvent event)
        {
            try
            {
                eventQueue.offer(Serialization.toByteArray(event));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error serializing event", e);
            }
        }
    }

    /**
     * An implementation of a {@link RemoteEventChannel.Publisher} that
     * reads {@link RemoteEvent}s from a {@link BlockingQueue} in their serialized
     * form as byte arrays.
     */
    public static class Consumer implements RemoteEventChannel.Consumer, Runnable
    {
        /**
         * The {@link BlockingQueue} to read serialized {@link RemoteEvent}s from.
         */
        private final BlockingQueue<byte[]> eventQueue;

        /**
         * The {@link Set} of {@link RemoteEventListener}s to forward events to.
         */
        private final Set<RemoteEventListener> listeners;

        /**
         * True while this consumer should continue to poll for events
         */
        private boolean running = true;

        /**
         * Create a {@link Consumer} that will read serialized {@link RemoteEvent}s from
         * the specified {@link BlockingQueue}.
         *
         * @param eventQueue  the {@link BlockingQueue} to read serialized
         *                    {@link RemoteEvent}s from
         */
        public Consumer(BlockingQueue<byte[]> eventQueue)
        {
            this.eventQueue = eventQueue;
            this.listeners  = new HashSet<>();
        }

        /**
         * Obtain the {@link BlockingQueue} used to read
         * serialized {@link RemoteEvent}s from.
         *
         * @return  the {@link BlockingQueue} used to read
         *          serialized {@link RemoteEvent}s from
         */
        public BlockingQueue<byte[]> getEventQueue()
        {
            return eventQueue;
        }


        @Override
        public void run()
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // Continue to run until the running flag is false and the event queue is empty
            while(running || !eventQueue.isEmpty())
            {
                try
                {
                    byte[]      bytes = eventQueue.poll(1, TimeUnit.SECONDS);
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

        /**
         * Set the running flag for this {@link Consumer} to false.
         */
        public void stop()
        {
            running = false;
        }

        @Override
        public void addEventListener(RemoteEventListener listener)
        {
            listeners.add(listener);
        }

        @Override
        public void removeEventListener(RemoteEventListener listener)
        {
            listeners.remove(listener);
        }
    }
}
