/*
 * File: VirtualPlatform.java
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

package com.oracle.bedrock.runtime.virtual;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.Authentication;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.Option;

import java.io.Closeable;
import java.io.IOException;

import java.net.InetAddress;

/**
 * A {@link Platform} implementation that represents
 * an O/S running in a virtual machine.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class VirtualPlatform extends RemotePlatform implements Closeable
{
    /**
     * Construct a new {@link VirtualPlatform}.
     *
     * @param name           the name of this {@link VirtualPlatform}
     * @param address         the {@link InetAddress} that will be used to SSH
     *                        into this {@link VirtualPlatform}
     * @param port            the remote port that will be used to SSH into
     *                        this {@link VirtualPlatform}
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     * @param options         the {@link Option}s for the {@link VirtualPlatform}
     */
    public VirtualPlatform(String         name,
                           InetAddress    address,
                           int            port,
                           String         userName,
                           Authentication authentication,
                           Option...      options)
    {
        super(name, address, port, userName, authentication, options);
    }


    /**
     * Closes the {@link VirtualPlatform} by performing the
     * any necessary {@link CloseAction} that has been specified
     * as a {@link VirtualPlatform} {@link Option}.
     *
     * @throws IOException  when closing failed
     *
     * @see java.io.Closeable
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Closes the {@link VirtualPlatform} by performing the
     * any necessary {@link CloseAction} that has been specified
     * as a {@link VirtualPlatform} {@link Option}.
     *
     * @param options  the {@link Option}s controlling the close operation
     *
     * @throws IOException  when closing failed
     *
     * @see java.io.Closeable
     */
    public abstract void close(Option... options) throws IOException;
}
