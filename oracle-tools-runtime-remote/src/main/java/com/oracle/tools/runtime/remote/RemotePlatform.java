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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.remote.java.RemoteJavaApplicationBuilder;

/**
 * A {@link com.oracle.tools.runtime.Platform} that is remote from the
 * current {@link com.oracle.tools.runtime.LocalPlatform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemotePlatform implements Platform
{
    /**
     * The name of the remote host for the SSH-based session.
     */
    protected String hostName;

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
     * Construct a new {@link RemotePlatform}.
     *
     * @param hostName        the remote host name
     * @param port            the remote port
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public RemotePlatform(String hostName, int port, String userName, Authentication authentication)
    {
        this.hostName       = hostName;
        this.port           = port;
        this.userName       = userName;
        this.authentication = authentication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHostname()
    {
        return hostName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Application, B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass)
    {
        if (JavaApplication.class.isAssignableFrom(applicationClass))
        {
            return (B) new RemoteJavaApplicationBuilder(hostName, port, userName, authentication);
        }
        else
        {
            return (B) new SimpleRemoteApplicationBuilder(hostName, port, userName, authentication);
        }
    }
}
