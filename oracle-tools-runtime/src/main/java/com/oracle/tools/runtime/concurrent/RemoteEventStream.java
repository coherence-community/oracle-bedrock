/*
 * File: RemoteEventStream.java
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

import java.io.Closeable;

/**
 * Represents a channel to send and receive {@link RemoteEvent}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface RemoteEventStream extends Closeable
{
    /**
     * Obtain the unique name of this {@link RemoteEventStream}.
     *
     * @return  the unique name of this {@link RemoteEventStream}
     */
    String getName();


    /**
     * Register a {@link RemoteEventListener} to
     * receive any {@link RemoteEvent}s that are fired by producers
     * on the other end of this {@link RemoteEventStream}.
     *
     * @param listener  the {@link RemoteEventListener} to register
     */
    void addEventListener(RemoteEventListener listener);


    /**
     * De-register the specified {@link RemoteEventListener} so
     * that is no longer receives any {@link RemoteEvent}s.
     *
     * @param listener  the {@link RemoteEventListener} to de-register
     */
    void removeEventListener(RemoteEventListener listener);


    /**
     * Forward the specified {@link RemoteEvent} to all registered
     * listeners.
     *
     * @param event  the event to forward
     */
    void onEvent(RemoteEvent event);


    /**
     * Publish the specified {@link RemoteEvent} to registered
     * listeners on the other end of this {@link RemoteEventStream}.
     *
     * @param event  the {@link RemoteEvent} to publish
     */
    void fireEvent(RemoteEvent event);


    @Override
    void close();
}
