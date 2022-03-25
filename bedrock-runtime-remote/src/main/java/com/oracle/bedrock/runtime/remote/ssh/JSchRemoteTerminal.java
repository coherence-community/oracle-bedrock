/*
 * File: JSchRemoteTerminal.java
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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.Bedrock;
import com.oracle.bedrock.lang.StringHelper;
import com.oracle.bedrock.options.LaunchLogging;
import com.oracle.bedrock.options.Variable;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.options.CommandInterceptor;
import com.oracle.bedrock.runtime.options.Shell;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.runtime.remote.AbstractRemoteTerminal;
import com.oracle.bedrock.runtime.remote.RemoteApplicationProcess;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.RemoteTerminal;
import com.oracle.bedrock.table.Table;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link RemoteTerminal} based on SSH (uses JSch) for a {@link RemotePlatform}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JSchRemoteTerminal extends AbstractRemoteTerminal
{
    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(JSchRemoteTerminal.class.getName());

    /**
     * The {@link JSch} framework.
     */
    protected JSchSessionFactory sessionFactory;


    /**
     * Create a {@link JSchRemoteTerminal} that will connect to a remote
     * platform with the specified connection details.
     *
     * @param platform  the {@link RemotePlatform}
     */
    public JSchRemoteTerminal(RemotePlatform platform)
    {
        this(platform, new JSchSessionFactory());
    }


    /**
     * Create a {@link JSchRemoteTerminal} that will connect to a remote
     * platform with the specified connection details.
     *
     * @param platform  the {@link RemotePlatform}
     * @param sessionFactory   the {@link JSchSessionFactory} to use to obtain a JSch {@link Session}
     */
    public JSchRemoteTerminal(RemotePlatform     platform,
                              JSchSessionFactory sessionFactory)
    {
        super(platform);

        this.sessionFactory = sessionFactory;
    }


    @Override
    public RemoteApplicationProcess launch(Launchable                   launchable,
                                           Class<? extends Application> applicationClass,
                                           OptionsByType                optionsByType)
    {
        // acquire the remote platform on which to launch the application
        RemotePlatform platform = getRemotePlatform();

        // establish a specialized SocketFactory for JSch
        JSchSocketFactory socketFactory = new JSchSocketFactory();

        // initially there's no session
        Session session = null;

        try
        {
            // create the remote session
            session = sessionFactory.createSession(platform.getAddress().getHostName(),
                                                   platform.getPort(),
                                                   platform.getUserName(),
                                                   platform.getAuthentication(),
                                                   socketFactory,
                                                   optionsByType);

            ChannelExec execChannel = (ChannelExec) session.openChannel("exec");

            // (re)define the "local.address" variable so that we can use for resolving the platform
            optionsByType.add(Variable.with("local.address", socketFactory.getLastLocalAddress().getHostAddress()));

            // ----- establish the remote environment variables -----

            String environmentVariables = "";

            // get the remote environment variables for the remote application
            Properties variables = launchable.getEnvironmentVariables(platform, optionsByType);

            // determine the format to use for setting variables
            String format;

            Shell  shell = optionsByType.getOrSetDefault(Shell.class, Shell.isUnknown());

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

            List<String>       arguments        = launchable.getCommandLineArguments(platform, optionsByType);
            CommandInterceptor interceptor      = optionsByType.get(CommandInterceptor.class);
            String             executableName   = launchable.getCommandToExecute(platform, optionsByType);
            File               workingDirectory = optionsByType.get(WorkingDirectory.class).resolve(platform, optionsByType);
            String             remoteCommand;

            if (interceptor == null)
            {
                for (String variableName : variables.stringPropertyNames())
                {
                    environmentVariables += String.format(format,
                                                          variableName,
                                                          StringHelper.doubleQuoteIfNecessary(variables.getProperty(variableName)));
                }

                // ----- establish the application command line to execute -----

                // determine the command to execute remotely
                StringBuilder command        = new StringBuilder(executableName);

                // add the arguments
                for (String arg : arguments)
                {
                    command.append(" ").append(arg);
                }

                // the actual remote command must include changing to the remote directory
                remoteCommand = environmentVariables + String.format("cd %s ; %s",
                                                                     workingDirectory,
                                                                     command);
            }
            else
            {
                remoteCommand = interceptor.onExecute(executableName, arguments, variables, workingDirectory);
            }

            execChannel.setCommand(remoteCommand);

            // ----- establish the remote application process to represent the remote application -----

            // establish a RemoteApplicationProcess representing the remote application
            RemoteApplicationProcess process = new JschRemoteApplicationProcess(session, execChannel);

            // ----- start the remote application -----

            if (optionsByType.get(LaunchLogging.class).isEnabled())
            {
                Table diagnosticsTable = optionsByType.get(Table.class);

                if (diagnosticsTable != null && LOGGER.isLoggable(Level.INFO))
                {
                    diagnosticsTable.addRow("Application Executable ", executableName);

                    LOGGER.log(Level.INFO,
                               "Oracle Bedrock " + Bedrock.getVersion() + ": Starting Application...\n"
                               + "------------------------------------------------------------------------\n"
                               + diagnosticsTable.toString() + "\n"
                               + "------------------------------------------------------------------------\n");
                }
            }

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

            throw new RuntimeException("Failed to create remote application", e);
        }
    }


    @Override
    public void makeDirectories(String        directoryName,
                                OptionsByType optionsByType)
    {
        Session session = null;

        try
        {
            // acquire the remote platform
            RemotePlatform platform = getRemotePlatform();

            // establish a specialized SocketFactory for JSch
            JSchSocketFactory socketFactory = new JSchSocketFactory();

            // create the remote session
            session = sessionFactory.createSession(platform.getAddress().getHostName(),
                                                   platform.getPort(),
                                                   platform.getUserName(),
                                                   platform.getAuthentication(),
                                                   socketFactory,
                                                   optionsByType);

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


    public void moveFile(String        source,
                         String        destination,
                         OptionsByType optionsByType)
    {
        Session session = null;

        try
        {
            // acquire the remote platform
            RemotePlatform platform = getRemotePlatform();

            // establish a specialized SocketFactory for JSch
            JSchSocketFactory socketFactory = new JSchSocketFactory();

            // create the remote session
            session = sessionFactory.createSession(platform.getAddress().getHostName(),
                                                   platform.getPort(),
                                                   platform.getUserName(),
                                                   platform.getAuthentication(),
                                                   socketFactory,
                                                   optionsByType);

            ChannelExec execChannel = (ChannelExec) session.openChannel("exec");
            String      moveCommand = String.format("mv %s %s", source, destination);

            execChannel.setCommand(moveCommand);

            RemoteApplicationProcess process = new JschRemoteApplicationProcess(session, execChannel);

            execChannel.connect(session.getTimeout());

            process.waitFor();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error moving file from " + source + " to " + destination, e);
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
