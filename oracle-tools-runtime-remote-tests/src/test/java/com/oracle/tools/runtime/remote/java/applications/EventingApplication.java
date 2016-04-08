/*
 * File: EventingApplication.java
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

package com.oracle.tools.runtime.remote.java.applications;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteEvent;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.options.StreamName;

import com.oracle.tools.runtime.java.JavaApplication;

import java.util.Random;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * An application with access to an {@link RemoteChannel}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class EventingApplication
{
    /**
     * Field description
     */
    public static final Object WAITER = new Object();
    private static Random      random = new Random(System.currentTimeMillis());

    /**
     * Field description
     */
    @RemoteChannel.Inject
    public static RemoteChannel channel;


    public static void main(String[] args) throws Exception
    {
        if (args.length >= 2)
        {
            String streamName = args[0];
            int    count      = Integer.parseInt(args[1]);

            for (int i = 0; i < count; i++)
            {
                channel.raise(new Event(i), StreamName.of(streamName));
                Thread.sleep(random.nextInt(100) + 10);
            }
        }

        synchronized (WAITER)
        {
            WAITER.wait();
        }
    }


    /**
     * Fire an event from the specified application.
     *
     * @param application  the application to fire the event from
     * @param event        the event to fire
     */
    public static void fireEvent(JavaApplication application,
                                 String          streamName,
                                 RemoteEvent     event)
    {
        application.submit(new FireEvent(streamName, event));
    }


    /**
     * Make the application listen to events on the incoming stream
     * and return the same event on the outgoing stream
     *
     * @param application         the application to listen for events
     * @param incomingStreamName  the name of the stream to listen to
     * @param outgoingStreamName  the name of the stream to fire the event back on
     */
    public static void listen(JavaApplication application,
                              String          incomingStreamName,
                              String          outgoingStreamName)
    {
        application.submit(new Listen(incomingStreamName, outgoingStreamName)).join();
    }


    /**
     * A {@link RemoteRunnable} that counts down a {@link CountDownLatch}.
     */
    public static class CountDownRunnable implements RemoteRunnable
    {
        /**
         * Field description
         */
        public static CountDownLatch latch;


        /**
         * Constructs ...
         *
         */
        public CountDownRunnable()
        {
        }


        @Override
        public void run()
        {
            latch.countDown();
        }
    }


    /**
     * A simple implementation of a {@link RemoteEvent}.
     */
    public static class Event implements RemoteEvent
    {
        /**
         * The identifier of this event.
         */
        private int id;


        /**
         * Create an {@link Event} with the specified identifier.
         *
         * @param id  the identifier of this event
         */
        public Event(int id)
        {
            this.id = id;
        }


        /**
         * Obtain the identifier of this event.
         *
         * @return  the identifier of this event
         */
        public int getId()
        {
            return id;
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Event event = (Event) o;

            return id == event.id;

        }


        @Override
        public int hashCode()
        {
            return id;
        }


        @Override
        public String toString()
        {
            return "Event(" + "id=" + id + ')';
        }
    }


    /**
     * A {@link RemoteRunnable} that can be used to fire an
     * event from an application.
     */
    public static class FireEvent implements RemoteRunnable
    {
        private String      streamName;
        private RemoteEvent event;


        /**
         * Constructs ...
         *
         *
         * @param streamName
         * @param event
         */
        public FireEvent(String      streamName,
                         RemoteEvent event)
        {
            this.streamName = streamName;
            this.event      = event;
        }


        @Override
        public void run()
        {
            EventingApplication.channel.raise(event, StreamName.of(streamName));
        }
    }


    /**
     * A simple {@link RemoteCallable}.
     */
    public static class GetIntCallable implements RemoteCallable<Integer>
    {
        /**
         * Field description
         */
        public static int value = -1;


        @Override
        public Integer call() throws Exception
        {
            return value;
        }
    }


    /**
     * A {@link RemoteRunnable} that can be used to
     * register a {@link RemoteEventListener} on a
     * stream.
     */
    public static class Listen implements RemoteCallable<Object>
    {
        private String incomingStreamName;
        private String outgoingStreamName;


        /**
         * Constructs ...
         *
         *
         * @param incomingStreamName
         * @param outgoingStreamName
         */
        public Listen(String incomingStreamName,
                      String outgoingStreamName)
        {
            this.incomingStreamName = incomingStreamName;
            this.outgoingStreamName = outgoingStreamName;
        }


        @Override
        public Object call()
        {
            // Add a listener that listens to the
            // incoming event stream and fires the
            // event back on the outgoing stream

            System.err.println("*** Listening to " + incomingStreamName + " firing back on " + outgoingStreamName);

            RemoteChannel channel = EventingApplication.channel;

            System.err.println("Channel=" + channel);

            channel.addListener(new RemoteEventListener()
                                {
                                    @Override
                                    public void onEvent(RemoteEvent event)
                                    {
                                        System.err.println("*** Received event " + event + " sending back on "
                                                           + outgoingStreamName);
                                        EventingApplication.channel.raise(event, StreamName.of(outgoingStreamName));
                                    }
                                },StreamName.of(incomingStreamName));

            return null;
        }
    }


    /**
     * A {@link RemoteCallable} that submits another {@link RemoteChannel}
     */
    public static class RoundTripCallable implements RemoteCallable<Integer>
    {
        @Override
        public Integer call() throws Exception
        {
            GetIntCallable.value = -1;

            CompletableFuture<Integer> future = EventingApplication.channel.submit(new GetIntCallable());

            return future.get();
        }
    }


    /**
     * A {@link RemoteRunnable} that submits another {@link RemoteRunnable}.
     */
    public static class RoundTripRunnable implements RemoteRunnable
    {
        @Override
        public void run()
        {
            EventingApplication.channel.submit(new CountDownRunnable());
        }
    }
}
