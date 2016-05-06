/*
 * File: SocketBasedRemoteChannelClient.java
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

package com.oracle.bedrock.runtime.concurrent.socket;

import com.oracle.bedrock.runtime.concurrent.RemoteChannel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A {@link RemoteChannel} that sends requests to {@link SocketBasedRemoteChannelServer}s for processing.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SocketBasedRemoteChannelClient extends SocketBasedRemoteChannel
{
    /**
     * Constructs a {@link SocketBasedRemoteChannelClient}.
     *
     * @param address  the {@link InetAddress} of the {@link SocketBasedRemoteChannelServer}
     * @param port     the port of the {@link SocketBasedRemoteChannelServer}
     *
     * @throws IOException  should the {@link SocketBasedRemoteChannelClient} fail to connect
     */
    public SocketBasedRemoteChannelClient(InetAddress address,
                                          int         port) throws IOException
    {
        super(new Socket(address, port));
    }
}
