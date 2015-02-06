/*
 * File: JSchRemoteShell.java
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

package com.oracle.tools.runtime.remote.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import com.oracle.tools.Options;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.options.Shell;

import com.oracle.tools.runtime.remote.AbstractRemoteShell;
import com.oracle.tools.runtime.remote.Authentication;
import com.oracle.tools.runtime.remote.RemoteApplicationEnvironment;
import com.oracle.tools.runtime.remote.RemoteApplicationProcess;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * An implementation of a {@link com.oracle.tools.runtime.remote.RemoteShell} that wraps
 * an SSH session on a remote platform.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JSchRemoteShell<A extends Application, S extends ApplicationSchema<A>,
                             E extends RemoteApplicationEnvironment> extends AbstractRemoteShell<A, S, E>
{
    /**
     * The {@link JSch} framework.
     */
    protected JSchSessionFactory sessionFactory;


    /**
     * Create a {@link JSchRemoteShell} that will connect to a remote
     * platform with the specified connection details.
     *
     * @param userName       the user name to use to create the SSH connection
     * @param authentication the authentication type to use
     * @param hostName       the host name of the remote platform
     * @param port           the port the remote platform is listening on for SSH connections
     */
    public JSchRemoteShell(String         userName,
                           Authentication authentication,
                           String         hostName,
                           int            port)
    {
        this(userName, authentication, hostName, port, new JSchSessionFactory());
    }


    /**
     * Create a {@link JSchRemoteShell} that will connect to a remote
     * platform with the specified connection details.
     *
     * @param userName         the user name to use to create the SSH connection
     * @param authentication   the authentication type to use
     * @param hostName         the host name of the remote platform
     * @param port             the port the remote platform is listening on for SSH connections
     * @param sessionFactory   the {@link JSchSessionFactory} to use to obtain a JSch {@link Session}
     */
    public JSchRemoteShell(String             userName,
                           Authentication     authentication,
                           String             hostName,
                           int                port,
                           JSchSessionFactory sessionFactory)
    {
        super(userName, authentication, hostName, port);

        this.sessionFactory = sessionFactory;
    }


    @Override
    @SuppressWarnings("unchecked")
    public RemoteApplicationProcess realize(S        applicationSchema,
                                            String   applicationName,
                                            Platform platform,
                                            E        environment,
                                            String   workingDirectory,
                                            Options  options)
    {
        // establish a specialized SocketFactory for JSch
        JSchSocketFactory socketFactory = new JSchSocketFactory();

        // initially there's no session
        Session session = null;

        try
        {
            // create the remote session
            session = sessionFactory.createSession(getHostName(), getPort(), getUserName(), getAuthentication(),
                                                   socketFactory, options);


            ChannelExec execChannel = (ChannelExec) session.openChannel("exec");

            // ----- establish the remote environment variables -----

            String environmentVariables = "";

            // get the remote environment variables for the remote application
            Properties variables = environment.getRemoteEnvironmentVariables();

            // determine the format to use for setting variables
            String format;

            Shell  shell = options.get(Shell.class, Shell.isUnknown());

            switch (shell.getType())
            {
            case SH :
            case BASH :
                format = "export %s=%s ; ";
                break;

            case CSH :
            case TSCH :
                format = "setenv %s %s ; ";
                break;

            default :

                // when we don't know, assume something bash-like
                format = "export %s=%s ; ";
                break;
            }

            for (String variableName : variables.stringPropertyNames())
            {
                environmentVariables +=
                    String.format(format, variableName,
                                  StringHelper.doubleQuoteIfNecessary(variables.getProperty(variableName)));
            }

            // ----- establish the application command line to execute -----

            // determine the command to execute remotely
            StringBuilder command   = new StringBuilder(environment.getRemoteCommandToExecute());
            List<String>  arguments = environment.getRemoteCommandArguments(socketFactory.getLastLocalAddress());

            for (String arg : arguments)
            {
                command.append(" ").append(arg);
            }

            // the actual remote command must include changing to the remote directory
            String remoteCommand = environmentVariables + String.format("cd %s ; %s",
                                                                        workingDirectory,
                                                                        command);

            execChannel.setCommand(remoteCommand);

            // ----- establish the remote application process to represent the remote application -----

            // establish a RemoteApplicationProcess representing the remote application
            RemoteApplicationProcess process = new JschRemoteApplicationProcess(session, execChannel);

            // ----- start the remote application -----

            // connect the channel
            execChannel.connect(session.getTimeout());

            return process;
        }
        catch (JSchException e)
        {
            if (session != null)
            {
                session.disconnect();
            }

            environment.close();

            throw new RuntimeException("Failed to create remote application", e);
        }
    }

    @Override
    public void makeDirectories(String directoryName, Options options)
    {
        Session session = null;

        try
        {
            // establish a specialized SocketFactory for JSch
            JSchSocketFactory socketFactory = new JSchSocketFactory();

            // create the remote session
            session = sessionFactory.createSession(getHostName(), getPort(), getUserName(),
                                                           getAuthentication(), socketFactory, options);

            ChannelExec execChannel = (ChannelExec) session.openChannel("exec");

            execChannel.setCommand("mkdir -p " + directoryName);

            RemoteApplicationProcess process = new JschRemoteApplicationProcess(session, execChannel);

            execChannel.connect(session.getTimeout());

            process.waitFor();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error creating remote directories " + directoryName, e);
        }
        finally
        {
            if (session != null)
            {
                session.disconnect();
            }
        }
    }
}
