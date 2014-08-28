/*
 * File: RemotePlatform.java
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

package com.oracle.tools.runtime.remote;

import com.oracle.tools.runtime.AbstractPlatform;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationBuilder;

import com.oracle.tools.runtime.java.JavaApplication;

import com.oracle.tools.runtime.remote.java.RemoteJavaApplicationBuilder;

import java.net.InetAddress;

/**
 * A {@link com.oracle.tools.runtime.Platform} that is remote from the
 * current {@link com.oracle.tools.runtime.LocalPlatform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemotePlatform extends AbstractPlatform
{
    /**
     * The private {@link InetAddress}.
     */
    protected InetAddress privateAddress;

    /**
     * The public {@link InetAddress}.
     */
    protected InetAddress publicAddress;

    /**
     * The port of the remote host to connect for the SSH-based session.
     */
    protected int port;

    /**
     * The {@link Authentication} to use for the SSH-based session.
     */
    protected Authentication authentication;

    /**
     * The user name to use for the SSH-based session.
     */
    protected String userName;


    /**
     * Constructs a new {@link RemotePlatform}, using the default ssh port
     * for connections, named according to the address provided.
     *
     * @param address         the remote address
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public RemotePlatform(InetAddress    address,
                          String         userName,
                          Authentication authentication)
    {
        this(address.toString(), address, RemoteApplicationBuilder.DEFAULT_PORT, userName, authentication);
    }


    /**
     * Constructs a new {@link RemotePlatform}, using the default ssh port
     * for connections.
     *
     * @param name            the symbolic name of the {@link RemotePlatform}
     * @param address         the remote address
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public RemotePlatform(String         name,
                          InetAddress    address,
                          String         userName,
                          Authentication authentication)
    {
        this(name, address, RemoteApplicationBuilder.DEFAULT_PORT, userName, authentication);
    }


    /**
     * Constructs a new {@link RemotePlatform}.
     *
     * @param name            the symbolic name of the {@link RemotePlatform}
     * @param address         the remote address
     * @param port            the remote port (for ssh)
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public RemotePlatform(String         name,
                          InetAddress    address,
                          int            port,
                          String         userName,
                          Authentication authentication)
    {
        super(name);

        this.privateAddress = address;
        this.publicAddress  = address;
        this.port           = port;
        this.userName       = userName;
        this.authentication = authentication;
    }


    @Override
    public InetAddress getPrivateInetAddress()
    {
        return privateAddress;
    }


    @Override
    public InetAddress getPublicInetAddress()
    {
        return publicAddress;
    }


    /**
     * Obtain the port to use with the private {@link InetAddress}
     * to use to SSH to the remote host.
     *
     * @return the port to use with the private {@link InetAddress}
     *         to use to SSH to the remote host.
     */
    public int getPort()
    {
        return port;
    }


    /**
     * Obtain the username to use to SSH to the remote host.
     *
     * @return the username to use to SSH to the remote host.
     */
    public String getUserName()
    {
        return userName;
    }


    /**
     * Obtain the {@link Authentication} to use to SSH to the remote host.
     *
     * @return the {@link Authentication} to SSH to the remote host.
     */
    public Authentication getAuthentication()
    {
        return authentication;
    }


    public void setPublicAddress(InetAddress publicAddress)
    {
        this.publicAddress = publicAddress;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <A extends Application, B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass)
    {
        AbstractRemoteApplicationBuilder builder;

        if (JavaApplication.class.isAssignableFrom(applicationClass))
        {
            builder = new RemoteJavaApplicationBuilder(privateAddress.getHostName(), port, userName, authentication);
        }
        else
        {
            builder = new SimpleRemoteApplicationBuilder(privateAddress.getHostName(), port, userName, authentication);
        }

        return (B) builder;
    }
}
