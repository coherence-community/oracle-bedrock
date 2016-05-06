/*
 * File: JSchSocketFactory.java
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

package com.oracle.bedrock.runtime.remote.ssh;

import com.jcraft.jsch.SocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A {@link SocketFactory} that keeps track of the local {@link InetAddress} of the
 * last created {@link Socket}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JSchSocketFactory implements SocketFactory
{
    /**
     * The local {@link InetAddress} of the last created {@link Socket}.
     */
    private InetAddress lastLocalAddress = null;


    @Override
    public Socket createSocket(String host,
                               int    port) throws IOException
    {
        Socket socket = new Socket(host, port);

        this.lastLocalAddress = socket.getLocalAddress();

        return socket;
    }


    @Override
    public InputStream getInputStream(Socket socket) throws IOException
    {
        return socket.getInputStream();
    }


    @Override
    public OutputStream getOutputStream(Socket socket) throws IOException
    {
        return socket.getOutputStream();
    }


    /**
     * Obtains the local {@link InetAddress} of the last {@link Socket} created.
     *
     * @return a local {@link InetAddress}
     */
    public InetAddress getLastLocalAddress()
    {
        return lastLocalAddress;
    }
}
