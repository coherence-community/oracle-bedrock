/*
 * File: RemoteEvents.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link Option.Collector} that collects together {@link RemoteEventListener}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemoteEvents
        implements Option.Collector<RemoteEvents.RemoteEventListenerOption, RemoteEvents>
{
    /**
     * The {@link List} of {@link RemoteEventListenerOption}s
     */
    private final List<RemoteEventListenerOption> listeners;

    /**
     * Create a {@link RemoteEvents} option with the specified
     * list of {@link RemoteEventListenerOption}s
     *
     * @param listeners  the list of {@link RemoteEventListenerOption}
     */
    public RemoteEvents(List<RemoteEventListenerOption> listeners)
    {
        this.listeners = listeners;
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(RemoteEventListenerOption.class))
        {
            return (Iterable<O>) Collections.unmodifiableCollection(listeners);
        }
        else
        {
            return Collections.emptyList();
        }
    }


    @Override
    public RemoteEvents with(RemoteEventListenerOption option)
    {
        if (option == null)
        {
            return this;
        }

        List<RemoteEventListenerOption> list = new ArrayList<>(this.listeners);

        list.add(option);

        return new RemoteEvents(list);
    }


    @Override
    public RemoteEvents without(RemoteEventListenerOption option)
    {
        if (option == null || this.listeners.isEmpty())
        {
            return this;
        }

        List<RemoteEventListenerOption> list = new ArrayList<>(this.listeners);

        list.remove(option);

        return new RemoteEvents(list);
    }


    @Override
    public Iterator<RemoteEventListenerOption> iterator()
    {
        return listeners.iterator();
    }


    /**
     * Create an {@link Option} that will add the specified
     * {@link RemoteEventListener} to the event stream with
     * the specified name.
     *
     * @param streamName  the name of the event stream to add the listener to
     * @param listener    the {@link RemoteEventListener} to be added to the stream
     *
     * @return  an {@link Option} that will add the specified
     *          {@link RemoteEventListener} to the event stream with
     *          the specified name
     */
    public static Option from(String streamName, RemoteEventListener listener)
    {
        return new RemoteEventListenerOption(streamName, listener);
    }


    /**
     * Create the default option of an empty set of {@link RemoteEventListener}s.
     *
     * @return  the default option of an empty set of {@link RemoteEventListener}s
     */
    @Options.Default
    public static RemoteEvents none()
    {
        return new RemoteEvents(Collections.emptyList());
    }


    /**
     * An {@link Option} implementation that holds a {@link RemoteEventListener}
     * and the name of the event stream that the listener should listen to.
     */
    public static class RemoteEventListenerOption implements Option.Collectable
    {
        /**
         * The name of the event stream.
         */
        private final String name;

        /**
         * The {@link RemoteEventListener} to add to the stream.
         */
        private final RemoteEventListener listener;


        /**
         * Create a {@link RemoteEventListenerOption} with the specified
         * listener and stream name.
         *
         * @param name      the name of the event stream to listen to
         * @param listener  the {@link RemoteEventListener} to add to the stream
         */
        RemoteEventListenerOption(String name, RemoteEventListener listener)
        {
            if (name == null)
            {
                throw new NullPointerException("The event stream name cannot be null");
            }

            if (listener == null)
            {
                throw new NullPointerException("The event listener cannot be null");
            }

            this.name     = name;
            this.listener = listener;
        }

        /**
         * Obtain the name of the event stream to listen to.
         *
         * @return  the name of the event stream to listen to
         */
        public String getName()
        {
            return name;
        }


        /**
         * Obtain the {@link RemoteEventListener} to add to the event stream.
         *
         * @return  the {@link RemoteEventListener} to add to the event stream
         */
        public RemoteEventListener getListener()
        {
            return listener;
        }


        @Override
        public Class<? extends Collector> getCollectorClass()
        {
            return RemoteEvents.class;
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

            RemoteEventListenerOption that = (RemoteEventListenerOption) o;

            return name.equals(that.name) && listener.equals(that.listener);

        }

        @Override
        public int hashCode()
        {
            int result = name.hashCode();
            result = 31 * result + listener.hashCode();
            return result;
        }
    }
}
