/*
 * File: RemoteShellType.java
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

import com.oracle.tools.Option;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;

import com.oracle.tools.runtime.remote.ssh.JSchRemoteShell;
import com.oracle.tools.runtime.remote.winrm.WindowsRemoteShell;

/**
 * An {@link Option} that can be used to specify the type of
 * {@link RemoteShell} that should be used to control processes
 * on a {@link RemotePlatform}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemoteShellType implements Option
{

    protected RemoteShellType()
    {
    }

    /**
     * Create a {@link RemoteShell} that will connect to a remote
     * platform with the specified connection details.
     *
     * @param userName       the user name to use to create the SSH connection
     * @param authentication the authentication type to use
     * @param hostName       the host name of the remote platform
     * @param port           the port the remote platform is listening on for SSH connections
     */
    public <A extends Application,S extends ApplicationSchema<A>, E extends RemoteApplicationEnvironment>
    RemoteShell<A,S,E> createShell(String userName, Authentication authentication, String hostName, int port)
    {
        throw new UnsupportedOperationException("This method should only be called on sub-classes");
    }

    /**
     * Create a {@link RemoteShellType} that creates {@link JSchRemoteShell} instances.
     *
     * @return a {@link RemoteShellType} that creates {@link JSchRemoteShell} instances
     */
    public static RemoteShellType sshShell()
    {
        return new RemoteShellType()
        {
            @Override
            public <A extends Application, S extends ApplicationSchema<A>, E extends RemoteApplicationEnvironment>
            RemoteShell<A, S, E> createShell(String userName, Authentication authentication, String hostName, int port)
            {
                return new JSchRemoteShell<>(userName, authentication, hostName, port);
            }
        };
    }

    /**
     * Create a {@link RemoteShellType} that creates {@link WindowsRemoteShell} instances
     * running Windows cmd.exe.
     *
     * @return a {@link RemoteShellType} that creates {@link WindowsRemoteShell} instances
     *         running cmd.exe
     */
    public static RemoteShellType windowsCMD()
    {
        return new RemoteShellType()
        {
            @Override
            public <A extends Application, S extends ApplicationSchema<A>, E extends RemoteApplicationEnvironment>
            RemoteShell<A, S, E> createShell(String userName, Authentication authentication, String hostName, int port)
            {
                return new WindowsRemoteShell<>(userName, authentication, hostName, port);
            }
        };
    }
}
