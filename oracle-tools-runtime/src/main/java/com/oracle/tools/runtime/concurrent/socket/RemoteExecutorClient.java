/*
 * File: RemoteExecutorClient.java
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

package com.oracle.tools.runtime.concurrent.socket;

import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: brianoliver
 * Date: 9/3/13
 * Time: 2:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteExecutorClient extends SocketBasedRemoteExecutor
{
    /**
     * Constructs a {@link RemoteExecutorClient}.
     *
     * @param address
     * @param port
     *
     * @throws IOException
     */
    public RemoteExecutorClient(InetAddress address,
                                int         port) throws IOException
    {
        super(-1, new Socket(address, port));
    }
}
