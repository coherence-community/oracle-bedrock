/*
 * File: AbstractControllableRemoteChannel.java
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * An abstract implementation of a {@link ControllableRemoteChannel}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractControllableRemoteChannel implements ControllableRemoteChannel
{
    /**
     * A flag indicating the open state of the {@link ControllableRemoteChannel}.
     */
    private volatile boolean isOpen;

    /**
     * The {@link RemoteChannelListener}s for the {@link ControllableRemoteChannel}.
     */
    protected CopyOnWriteArraySet<RemoteChannelListener> channelListeners;

    /**
     * The {@link RemoteEventListener}s for the {@link RemoteChannel} according to their {@link StreamName}.
     */
    protected ConcurrentHashMap<StreamName, CopyOnWriteArraySet<RemoteEventListener>> eventListenersByStreamName;


    /**
     * Constructs an {@link AbstractControllableRemoteChannel}.
     */
    public AbstractControllableRemoteChannel()
    {
        isOpen                     = false;
        channelListeners           = new CopyOnWriteArraySet<>();
        eventListenersByStreamName = new ConcurrentHashMap<>();
    }


    /**
     * Sets if the {@link ControllableRemoteChannel} is open.
     *
     * @param isOpen  a flag indicating if the {@link ControllableRemoteChannel}
     *                is open
     */
    protected synchronized void setOpen(boolean isOpen)
    {
        this.isOpen = true;
    }


    @Override
    public final synchronized void close()
    {
        if (isOpen)
        {
            isOpen = false;

            onClose();

            for (RemoteChannelListener listener : channelListeners)
            {
                try
                {
                    listener.onClosed(this);
                }
                catch (Throwable throwable)
                {
                    // we ignore exceptions that occur while notifying the listeners
                }
            }
        }
    }


    /**
     * Determines if the {@link ControllableRemoteChannel} is open.
     *
     * @return <code>true</code> if the {@link RemoteChannel} is open
     */
    public synchronized boolean isOpen()
    {
        return isOpen;
    }


    @Override
    public synchronized void addListener(RemoteChannelListener listener)
    {
        channelListeners.add(listener);
    }


    @Override
    public void addListener(RemoteEventListener listener,
                            Option...           options)
    {
        Options    listenerOptions = Options.from(options);

        StreamName streamName      = listenerOptions.get(StreamName.class);

        eventListenersByStreamName.compute(streamName,
            (name, eventListeners) -> {
                if (eventListeners == null)
                {
                    eventListeners = new CopyOnWriteArraySet<>();
                }

                eventListeners.add(listener);

                return eventListeners;
            });
    }


    @Override
    public void removeListener(RemoteEventListener listener,
                               Option...           options)
    {
        Options    listenerOptions = Options.from(options);

        StreamName streamName      = listenerOptions.get(StreamName.class);

        eventListenersByStreamName.computeIfPresent(streamName,
            (name, eventListeners) -> {
                eventListeners.remove(listener);

                return eventListeners.size() == 0 ? null : eventListeners;
            });
    }


    /**
     * Handle when the {@link ControllableRemoteChannel} is closed.
     */
    protected abstract void onClose();
}
