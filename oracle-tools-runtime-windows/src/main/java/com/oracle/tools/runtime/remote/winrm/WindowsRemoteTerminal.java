/*
 * File: WindowsRemoteTerminal.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.oracle.tools.Options;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.remote.AbstractRemoteTerminal;
import com.oracle.tools.runtime.remote.RemoteApplicationEnvironment;
import com.oracle.tools.runtime.remote.RemoteApplicationProcess;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.RemoteTerminal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A Windows implementation of a {@link RemoteTerminal}
 * that uses the WinRM SOAP service to execute commands.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsRemoteTerminal<A extends Application, S extends ApplicationSchema<A>,
                                   E extends RemoteApplicationEnvironment> extends AbstractRemoteTerminal<A, S, E>
{
    /**
     * Create a Windows remote shell that will connect to the WinRM
     * service running on the {@link RemotePlatform}.
     *
     * @param platform  the {@link RemotePlatform}
     */
    public WindowsRemoteTerminal(RemotePlatform platform)
    {
        super(platform);
    }


    @SuppressWarnings("unchecked")
    @Override
    public RemoteApplicationProcess realize(S       applicationSchema,
                                            String  applicationName,
                                            E       environment,
                                            String  workingDirectory,
                                            Options options)
    {
        try
        {
            // ----- establish the application command line to execute -----

            // determine the command to execute remotely
            String       command = environment.getRemoteCommandToExecute();
            List<String> args    = environment.getRemoteCommandArguments(LocalPlatform.getInstance().getAddress());

            // ----- establish the remote application process to represent the remote application -----

            WindowsSession session = createSession();

            session.connect(workingDirectory, environment.getRemoteEnvironmentVariables());

            // establish a RemoteApplicationProcess representing the remote application
            WindowsRemoteApplicationProcess process = new WindowsRemoteApplicationProcess(session);

            // ----- start the remote application -----

            process.execute(command, args);

            return process;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create remote application", e);
        }
    }


    @Override
    public void makeDirectories(String  directoryName,
                                Options options)
    {
        try (WindowsSession session = createSession())
        {
            session.connect();

            try (WindowsRemoteApplicationProcess process = new WindowsRemoteApplicationProcess(session))
            {
                process.execute("mkdir", Collections.singletonList(directoryName));

                int rc = process.waitFor();

                if (rc != 0 && rc != 1)
                {
                    throw new RuntimeException("Error creating directory " + directoryName + " - mkdir returned " + rc);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error creating directory " + directoryName, e);
        }
    }


    public void moveFile(String source, String destination, Options options)
    {
        try (WindowsSession session = createSession())
        {
            session.connect();

            try (WindowsRemoteApplicationProcess process = new WindowsRemoteApplicationProcess(session))
            {
                process.execute("move", Arrays.asList(source, destination));

                int rc = process.waitFor();

                if (rc != 0 && rc != 1)
                {
                    throw new RuntimeException("Error moving file from " + source + " to " + destination
                                               + " - move return code = " + rc);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error moving file from " + source + " to " + destination, e);
        }
    }


    /**
     * Create a {@link WindowsSession} that can be used to communicate
     * with the WinRM server on the remote host.
     *
     * @return a {@link WindowsSession} that can be used to communicate
     *         with the WinRM server on the remote host
     */
    protected WindowsSession createSession()
    {
        RemotePlatform platform = getRemotePlatform();

        return new WindowsSession(platform.getAddress().getHostName(),
                                  platform.getPort(),
                                  platform.getUserName(),
                                  platform.getAuthentication());
    }
}
