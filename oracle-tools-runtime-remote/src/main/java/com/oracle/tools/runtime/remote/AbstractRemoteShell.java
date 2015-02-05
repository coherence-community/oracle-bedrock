/*
 * File: AbstractRemoteShell.java
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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;

/**
 * A base class for implementations of {@link RemoteShell}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class AbstractRemoteShell<A extends Application,S extends ApplicationSchema<A>,
        E extends RemoteApplicationEnvironment> implements RemoteShell<A,S,E>
{
    /**
     * The name of the remote host for the SSH-based session.
     */
    private final String hostName;

    /**
     * The port of the remote host to connect for the SSH-based session.
     */
    private final int port;

    /**
     * The {@link Authentication} to use for the SSH-based session.
     */
    private final Authentication authentication;

    /**
     * The user name to use for the SSH-based session.
     */
    private final String userName;

    public AbstractRemoteShell(String userName, Authentication authentication, String hostName, int port)
    {
        this.userName       = userName;
        this.authentication = authentication;
        this.hostName       = hostName;
        this.port           = port;
    }

    /**
     * Obtain the user name to use to connect to the remote platform.
     *
     * @return the user name to use to connect to the remote platform
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Obtain the {@link Authentication} to use to connect to the remote platform.
     *
     * @return the {@link Authentication} to use to connect to the remote platform
     */
    public Authentication getAuthentication()
    {
        return authentication;
    }

    /**
     * Obtain the host name of the remote platform.
     *
     * @return the host name of the remote platform
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * Obtain the port to use to connect to the remote platform.
     *
     * @return the port to use to connect to the remote platform
     */
    public int getPort()
    {
        return port;
    }
}
