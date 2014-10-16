/*
 * File: AbstractRemoteApplicationBuilder.java
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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.runtime.AbstractApplicationBuilder;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.PlatformSeparators;
import com.oracle.tools.runtime.options.TemporaryDirectory;

import com.oracle.tools.runtime.remote.options.Deployment;
import com.oracle.tools.runtime.remote.options.StrictHostChecking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;

import java.util.concurrent.TimeUnit;

/**
 * An {@link AbstractRemoteApplicationBuilder} is a base implementation of an {@link RemoteApplicationBuilder}
 * (over SSH).
 *
 * @param <A>  the type of the {@link Application}s the {@link RemoteApplicationBuilder} will realize
 * @param <E>  the type of the {@link RemoteApplicationEnvironment} used by the {@link RemoteApplicationBuilder}
 * @param <B>  the type of the {@link RemoteApplicationBuilder}
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractRemoteApplicationBuilder<A extends Application, E extends RemoteApplicationEnvironment,
                                                       B extends AbstractRemoteApplicationBuilder<A, E, B>>
    extends AbstractApplicationBuilder<A> implements RemoteApplicationBuilder<A>
{
    /**
     * The {@link JSch} framework.
     */
    protected JSch jsch;

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
     * The {@link PropertiesBuilder} defining custom environment variables to
     * establish when realizing a remote {@link Application}.
     */
    private PropertiesBuilder remoteEnvironmentVariablesBuilder;


    /**
     * Constructs an {@link AbstractRemoteApplicationBuilder} (using the default port).
     *
     * @param hostName        the remote host name
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public AbstractRemoteApplicationBuilder(String         hostName,
                                            String         userName,
                                            Authentication authentication)
    {
        this(hostName, DEFAULT_PORT, userName, authentication);
    }


    /**
     * Constructs an {@link AbstractRemoteApplicationBuilder}.
     *
     * @param hostName        the remote host name
     * @param port            the remote port
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public AbstractRemoteApplicationBuilder(String         hostName,
                                            int            port,
                                            String         userName,
                                            Authentication authentication)
    {
        super();

        this.hostName       = hostName;
        this.port           = port;
        this.userName       = userName;
        this.authentication = authentication;

        // by default there are no custom remote environment variables
        remoteEnvironmentVariablesBuilder = new PropertiesBuilder();

        // establish the JSch framework for the builder
        this.jsch = new JSch();

        // allow the authentication to configure the framework
        if (authentication instanceof JSchBasedAuthentication)
        {
            ((JSchBasedAuthentication) authentication).configureFramework(jsch);
        }
    }


    /**
     * Defines a custom environment variable for remote {@link Application}s
     * realized by this {@link RemoteApplicationBuilder} based on values
     * returned by the {@link Iterator}.
     *
     * @param name      the name of the environment variable
     * @param iterator  an {@link Iterator} providing values for the environment
     *                  variable
     *
     * @return this {@link RemoteApplicationBuilder} to permit fluent method calls
     */
    public B setEnvironmentVariable(String      name,
                                    Iterator<?> iterator)
    {
        remoteEnvironmentVariablesBuilder.setProperty(name, iterator);

        return (B) this;
    }


    /**
     * Defines a custom environment variable for remote {@link Application}s
     * realized by this {@link RemoteApplicationBuilder}.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return this {@link RemoteApplicationBuilder} to permit fluent method calls
     */
    public B setEnvironmentVariable(String name,
                                    Object value)
    {
        remoteEnvironmentVariablesBuilder.setProperty(name, value);

        return (B) this;
    }


    /**
     * Creates a remote-platform specific filename, given a fileName
     * represented in a format for this platform.
     *
     * @param fileName  the file name to convert
     *
     * @return the file as it would be represented by the remote platform
     */
    protected String asRemotePlatformFileName(String             fileName,
                                              PlatformSeparators separators)
    {
        return fileName == null ? null : fileName.replace(File.separator, separators.getFileSeparator());
    }


    /**
     * Creates a sanitized and lower-case version of a file name by replacing
     * consecutive non-numerical, non-alphabetical, non ".", non "-" and non "~"
     * characters of a string with "-".
     *
     * @param fileName  the un-sanitized file name
     *
     * @return  the sanitized file name
     */
    protected String asSanitizedFileName(String             fileName,
                                         PlatformSeparators separators)
    {
        if (fileName == null)
        {
            return null;
        }
        else
        {
            StringBuilder builder = new StringBuilder(fileName.length());
            String valid = "1234567890abcdefghijklmnopqrstuvwxyz.~" + File.separatorChar
                           + separators.getFileSeparator();
            char last = '\0';

            fileName = fileName.toLowerCase();

            for (char c : fileName.toCharArray())
            {
                if (valid.indexOf(c) < 0)
                {
                    last = '-';
                }
                else
                {
                    if (last == '-' && builder.length() > 0)
                    {
                        builder.append(last);
                    }

                    last = c;
                    builder.append(c);
                }
            }

            return builder.toString().toLowerCase();
        }
    }


    /**
     * Obtains the {@link RemoteApplicationBuilder} specific {@link RemoteApplicationEnvironment}
     * to be used for configuring and realizing a remote {@link Application}.
     *
     * @param schema           the {@link com.oracle.tools.runtime.ApplicationSchema} defining the application
     * @param platform         the {@link Platform} representing the remote O/S
     * @param options  the {@link Options} for the {@link Platform}
     *
     * @return the {@link RemoteApplicationEnvironment}
     */
    abstract protected <T extends A, S extends ApplicationSchema<T>> E getRemoteApplicationEnvironment(S        schema,
                                                                                                       Platform platform,
                                                                                                       Options  options);


    /**
     * Creates the {@link Application} representing the underlying
     * {@link RemoteApplicationProcess}.
     *
     * @param platform         the {@link Platform} that this {@link Application} is running on
     * @param schema           the {@link ApplicationSchema} used to define the application
     * @param environment      the {@link RemoteApplicationEnvironment} for the application
     * @param applicationName  the name of the application
     * @param process          the {@link RemoteApplicationProcess}
     * @param console          the {@link ApplicationConsole} to use to capture the
     *                         {@link RemoteApplicationProcess} input and output
     *
     * @return the {@link Application}
     */
    protected abstract <T extends A, S extends ApplicationSchema<T>> T createApplication(Platform                 platform,
                                                                                         Options                  options,
                                                                                         S                        schema,
                                                                                         E                        environment,
                                                                                         String                   applicationName,
                                                                                         RemoteApplicationProcess process,
                                                                                         ApplicationConsole       console);


    @Override
    public <T extends A, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console,
                                                                   Platform           platform,
                                                                   Option...          applicationOptions)
    {
        Session session = null;

        // obtain the platform specific options from the schema
        Options options = applicationSchema.getPlatformSpecificOptions(platform);

        // add the custom application options
        options.addAll(applicationOptions);

        // define the PlatformSeparators as Unix if they are not already defined
        options.addIfAbsent(PlatformSeparators.forUnix());

        // obtain the builder-specific remote application environment based on the schema
        E environment = getRemoteApplicationEnvironment(applicationSchema, platform, options);

        try
        {
            // create the remote session
            session = jsch.getSession(userName, hostName, port);

            // the session should not cause the JVM not to exit
            session.setDaemonThread(true);

            // determine the timeout
            Timeout timeout   = options.get(Timeout.class, Timeout.autoDetect());
            int     timeoutMS = (int) timeout.getUnits().convert(timeout.getDuration(), TimeUnit.MILLISECONDS);

            // set the default session timeouts (in milliseconds)
            session.setTimeout(timeoutMS);

            // allow the authentication to configure the session
            if (authentication instanceof JSchBasedAuthentication)
            {
                ((JSchBasedAuthentication) authentication).configureSession(session);
            }

            // ----- configure the session channel properties -----
            Properties config = new Properties();

            // are we to use strict-host-checking? (when it's not defined we enabled it by default)
            StrictHostChecking strictHostChecking = options.get(StrictHostChecking.class, StrictHostChecking.enabled());

            config.put("StrictHostKeyChecking", strictHostChecking.isEnabled() ? "yes" : "no");
            session.setConfig(config);

            // connect the session
            session.connect();

            // ----- deploy remote application artifacts (using sftp) -----

            // determine the separators for the platform
            PlatformSeparators separators = options.get(PlatformSeparators.class);

            // assume the remote directory is the working directory
            File remoteDirectoryFile = applicationSchema.getWorkingDirectory();
            String remoteDirectory = remoteDirectoryFile == null
                                     ? null : asRemotePlatformFileName(remoteDirectoryFile.toString(),
                                                                       separators);

            // determine the DeploymentArtifacts based on those specified by the Deployment option
            ArrayList<DeploymentArtifact> artifactsToDeploy = new ArrayList<DeploymentArtifact>();

            Deployment<T, S>              deployment        = options.get(Deployment.class);

            if (deployment != null)
            {
                try
                {
                    for (DeploymentArtifact deploymentArtifact :
                        deployment.getDeploymentArtifacts(applicationSchema, platform, options))
                    {
                        artifactsToDeploy.add(deploymentArtifact);
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Failed to determine artifacts to deploy", e);
                }

            }

            if (artifactsToDeploy.size() > 0)
            {
                ChannelSftp sftpChannel = null;

                try
                {
                    // open an sftp channel that we can use to copy over the artifacts
                    sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect(timeoutMS);

                    // create a temporary working folder (if there's no working folder set)
                    if (remoteDirectoryFile == null)
                    {
                        // create deployment directory as the working directory
                        // (as applicationName-YYYYmmdd-HHMMSS-LLL)

                        String   sanitizedApplicationName = asSanitizedFileName(applicationName, separators);
                        Calendar now                      = Calendar.getInstance();
                        String temporaryDirectoryName = String.format("%1$s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-%2$tL",
                                                                      sanitizedApplicationName,
                                                                      now);

                        // determine the remote TemporaryDirectory
                        TemporaryDirectory temporaryDirectory = options.get(TemporaryDirectory.class,
                                                                            TemporaryDirectory
                                                                                .at(separators.getFileSeparator()
                                                                                    + "tmp"));

                        remoteDirectoryFile = new File(temporaryDirectory.get().toFile(), temporaryDirectoryName);
                        remoteDirectory     = asRemotePlatformFileName(remoteDirectoryFile.toString(), separators);

                        // create the remote directory
                        sftpChannel.mkdir(remoteDirectory);
                    }

                    // copy deployment artifacts into the remote server
                    for (DeploymentArtifact artifactToDeploy : artifactsToDeploy)
                    {
                        // acquire the source file to deploy
                        File sourceFile = artifactToDeploy.getSourceFile();

                        // change to the desired remote directory
                        File   destinationFile = artifactToDeploy.getDestinationFile();

                        String destinationFileName;

                        if (destinationFile == null)
                        {
                            sftpChannel.cd(remoteDirectory);
                            destinationFileName = sourceFile.getName();
                        }
                        else
                        {
                            String destinationFilePath = asRemotePlatformFileName(destinationFile.getParent(),
                                                                                  separators);

                            if (destinationFilePath == null)
                            {
                                sftpChannel.cd(remoteDirectory);
                            }
                            else
                            {
                                sftpChannel.cd(asRemotePlatformFileName(destinationFile.getPath(), separators));
                            }

                            destinationFileName = destinationFile.getName();
                        }

                        // copy the source artifact to the destination file
                        sftpChannel.put(new FileInputStream(sourceFile), destinationFileName);
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to deploy application", e);
                }
                catch (SftpException e)
                {
                    throw new RuntimeException("Failed to deploy application", e);
                }
                finally
                {
                    if (sftpChannel != null)
                    {
                        sftpChannel.disconnect();
                    }
                }
            }

            // ----- establish the remote channel (using ssh) -----

            // open the channel (for a remote execution)
            ChannelExec execChannel = (ChannelExec) session.openChannel("exec");

            // ----- establish the remote environment variables -----

            // define the remote environment variables in the remote channel
            Properties variables = environment.getRemoteEnvironmentVariables();

            for (String variableName : variables.stringPropertyNames())
            {
                execChannel.setEnv(variableName, variables.getProperty(variableName));
            }

            // ----- establish the application command line to execute -----

            // determine the command to execute remotely
            String command = environment.getRemoteCommandToExecute();

            // the actual remote command must include changing to the remote directory
            String remoteCommand = String.format("cd %s ; %s", remoteDirectory, command);

            execChannel.setCommand(remoteCommand);

            // ----- establish the remote application process to represent the remote application -----

            // establish a RemoteApplicationProcess representing the remote application
            RemoteApplicationProcess process;

            try
            {
                process = new RemoteApplicationProcess(session, execChannel);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to connect to the underlying remote process for the application", e);
            }

            // ----- start the remote application -----

            // connect the channel
            execChannel.connect(timeoutMS);

            // create the Application based on the RemoteApplicationProcess
            T application = createApplication(platform,
                                              options,
                                              applicationSchema,
                                              environment,
                                              applicationName,
                                              process,
                                              console);

            // ----- notify all of the lifecycle listeners -----

            raiseOnRealizedFor(application);

            return application;
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
}
