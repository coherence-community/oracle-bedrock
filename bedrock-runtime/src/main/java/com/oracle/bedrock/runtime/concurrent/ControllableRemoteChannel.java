/*
 * File: ControllableRemoteChannel.java
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

import com.oracle.bedrock.annotations.Internal;

import java.io.Closeable;

/**
 * A {@link RemoteChannel} that may have its lifecycle controlled and observed.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see RemoteChannel
 */
@Internal
public interface ControllableRemoteChannel extends RemoteChannel, Closeable
{
    /**
     * Closes the {@link ControllableRemoteChannel}.
     * <p>
     * After being closed attempts to make submissions to the {@link ControllableRemoteChannel}
     * will throw an {@link IllegalStateException}.  Should the {@link ControllableRemoteChannel}
     * already be closed, nothing happens.
     */
    public void close();


    /**
     * Registers the specified {@link RemoteChannelListener} on the {@link ControllableRemoteChannel}.
     *
     * @param listener  the {@link RemoteChannelListener}
     */
    public void addListener(RemoteChannelListener listener);
}
