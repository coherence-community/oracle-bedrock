/*
 * File: CustomServer.java
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

package com.oracle.bedrock.runtime.coherence;

import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.tangosol.net.DefaultCacheServer;

/**
 * A custom server class with a main method that
 * calls {@link DefaultCacheServer#main(String[])}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CustomServer
{
    public static void main(String[] args)
    {
        DefaultCacheServer.main(args);
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
        /**
         * The {@link RemoteChannel} for asynchronously sending events / responses back
         * (in addition to return values).
         */
        @RemoteChannel.Inject
        private RemoteChannel remoteChannel;
        private String        streamName;
        private RemoteEvent   event;


        /**
         * Constructs a {@link FireEvent}.
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
            remoteChannel.raise(event, StreamName.of(streamName));
        }
    }
}
