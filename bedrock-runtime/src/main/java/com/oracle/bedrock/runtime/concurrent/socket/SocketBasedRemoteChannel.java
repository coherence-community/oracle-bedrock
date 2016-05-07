/*
 * File: SocketBasedRemoteChannel.java
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

import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.concurrent.AbstractRemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * A {@link Socket}-based implementation of a {@link RemoteChannel}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public class SocketBasedRemoteChannel extends AbstractRemoteChannel
{
    /**
     * The {@link Socket} over which {@link Callable}s will be sent and accepted.
     */
    private Socket socket;


    /**
     * Constructs a {@link SocketBasedRemoteChannel} to submit and accept {@link Callable}s.
     *
     * @param socket  the {@link Socket} over which {@link Callable}s
     *                will be submit and accepted
     *
     * @throws IOException when the {@link SocketBasedRemoteChannel} can't connect
     *                     using the {@link Socket}
     */
    public SocketBasedRemoteChannel(Socket socket) throws IOException
    {
        super(socket.getOutputStream(), socket.getInputStream());

        // remember the socket so we can close it
        this.socket = socket;

        // we'll always attempt to reuse addresses
        this.socket.setReuseAddress(true);
    }


    /**
     * Obtains the {@link InetAddress} of the {@link Socket}.
     *
     * @return the {@link InetAddress} of the {@link Socket}
     */
    public InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }


    /**
     * Obtains the {@link InetAddress} port number for the {@link Socket}.
     *
     * @return the {@link InetAddress} port number
     */
    public int getPort()
    {
        return socket.getPort();
    }


    @Override
    protected void onClose()
    {
        super.onClose();

        // close the socket
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            // don't care
        }
        finally
        {
            socket = null;
        }
    }
}
