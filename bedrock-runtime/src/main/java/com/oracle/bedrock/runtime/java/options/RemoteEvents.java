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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;

import java.util.EventListener;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * An {@link ComposableOption} for managing zero or more {@link RemoteEventListener} registrations.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class RemoteEvents implements ComposableOption<RemoteEvents>
{
    /**
     * The {@link HashMap} of {@link EventListener}s and {@link OptionsByType}s, organized by {@link StreamName}.
     */
    private HashMap<StreamName, HashMap<RemoteEventListener, OptionsByType>> eventListeners;


    /**
     * Constructs an empty {@link RemoteEvents}
     */
    private RemoteEvents()
    {
        eventListeners = new HashMap<>();
    }


    /**
     * Internally adds a {@link RemoteEventListener} to this {@link RemoteEvents} option
     *
     * @param listener       the {@link RemoteEventListener} to add
     * @param optionsByType  the {@link OptionsByType} for the {@link RemoteEventListener}
     */
    private void add(RemoteEventListener listener,
                     OptionsByType       optionsByType)
    {
        StreamName streamName = optionsByType.get(StreamName.class);

        HashMap<RemoteEventListener, OptionsByType> streamEventListeners = eventListeners.computeIfAbsent(streamName,
                                                                                                          name -> new HashMap<>());

        streamEventListeners.put(listener, optionsByType);
    }


    /**
     * Iterate over the registered {@link RemoteEventListener}s together with the registration {@link Option}s.
     *
     * @param consumer  the {@link BiConsumer}
     */
    public void forEach(BiConsumer<RemoteEventListener, Option[]> consumer)
    {
        eventListeners.forEach(
            (streamName, remoteEventListenerMap) -> {
                remoteEventListenerMap.forEach(
                    (remoteEventListener, optionsByType) -> {
                        consumer.accept(remoteEventListener, optionsByType.asArray());
                    });
            });
    }


    @Override
    public RemoteEvents compose(RemoteEvents other)
    {
        // construct a new RemoteEvents
        RemoteEvents remoteEvents = new RemoteEvents();

        // include all of this RemoteEvents listeners
        remoteEvents.eventListeners.putAll(eventListeners);

        // add all of the other RemoteEvent listeners (these will overrider individual event listeners)
        other.eventListeners.forEach(((streamName, remoteEventListenerMap) -> {
                                          remoteEventListenerMap.forEach(
                                              (remoteEventListener, optionsByType) -> {
                                                  remoteEvents.add(remoteEventListener, optionsByType);
                                              });
                                      }));

        return remoteEvents;
    }


    /**
     * Defines a {@link RemoteEventListener} with the specified {@link Option}s in the {@link RemoteEvents}.
     *
     * @param remoteEventListener  the {@link RemoteEventListener}
     * @param options              the {@link Option}s
     *
     * @return  a new {@link RemoteEvents}
     */
    public static RemoteEvents listener(RemoteEventListener remoteEventListener,
                                        Option...           options)
    {
        RemoteEvents remoteEvents = new RemoteEvents();

        remoteEvents.add(remoteEventListener, OptionsByType.of(options));

        return remoteEvents;
    }


    /**
     * Defines an {@link RemoteEvents} that is empty.
     *
     * @return an empty {@link RemoteEvents}
     */
    @OptionsByType.Default
    public static RemoteEvents none()
    {
        return new RemoteEvents();
    }
}
