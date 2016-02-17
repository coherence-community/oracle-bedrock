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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.options.Variable;

import com.oracle.tools.runtime.AbstractApplicationBuilder;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;
import com.oracle.tools.runtime.Profiles;
import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.options.PlatformSeparators;
import com.oracle.tools.runtime.options.Shell;
import com.oracle.tools.runtime.options.TemporaryDirectory;

import com.oracle.tools.runtime.remote.options.Deployer;
import com.oracle.tools.runtime.remote.options.Deployment;
import com.oracle.tools.runtime.remote.ssh.SftpDeployer;

import java.io.File;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;

/**
 * An abstract implementation of a {@link RemoteApplicationBuilder}.
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
    extends AbstractApplicationBuilder<A, RemotePlatform> implements RemoteApplicationBuilder<A>
{
    /**
     * The {@link PropertiesBuilder} defining custom environment variables to
     * establish when realizing a remote {@link Application}.
     */
    private PropertiesBuilder remoteEnvironmentVariablesBuilder;


    /**
     * Constructs an {@link AbstractRemoteApplicationBuilder} for a specified
     * {@link RemotePlatform}.
     *
     * @param platform  the {@link RemotePlatform}
     */
    public AbstractRemoteApplicationBuilder(RemotePlatform platform)
    {
        super(platform);

        // by default there are no custom remote environment variables
        remoteEnvironmentVariablesBuilder = new PropertiesBuilder();
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
     * Obtains the {@link RemoteApplicationBuilder} specific {@link RemoteApplicationEnvironment}
     * to be used for configuring and realizing a remote {@link Application}.
     *
     * @param schema           the {@link com.oracle.tools.runtime.ApplicationSchema} defining the application
     * @param options  the {@link Options} for the {@link Platform}
     *
     * @return the {@link RemoteApplicationEnvironment}
     */
    abstract protected <T extends A, S extends ApplicationSchema<T>> E getRemoteApplicationEnvironment(S       schema,
                                                                                                       Options options);


    /**
     * Creates the {@link Application} representing the underlying
     * {@link RemoteApplicationProcess}.
     *
     * @param schema           the {@link ApplicationSchema} used to define the application
     * @param environment      the {@link RemoteApplicationEnvironment} for the application
     * @param applicationName  the name of the application
     * @param process          the {@link RemoteApplicationProcess}
     * @param console          the {@link ApplicationConsole} to use to capture the
     *                         {@link RemoteApplicationProcess} input and output
     *
     * @return the {@link Application}
     */
    protected abstract <T extends A, S extends ApplicationSchema<T>> T createApplication(Options                  options,
                                                                                         S                        schema,
                                                                                         E                        environment,
                                                                                         String                   applicationName,
                                                                                         RemoteApplicationProcess process,
                                                                                         ApplicationConsole       console);


    @Override
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console,
                                                                   Option...          applicationOptions)
    {
        // ---- establish the Options for the Application -----

        // add the platform options
        Options options = new Options(platform == null ? null : platform.getOptions().asArray());

        // add the schema options
        options.addAll(applicationSchema.getOptions().asArray());

        // add the schema options (based on the platform)
        options.addAll(applicationSchema.getPlatformSpecificOptions(platform).asArray());

        // add the custom application options
        options.addAll(applicationOptions);

        // ---- establish the default Options ----

        // define the PlatformSeparators as Unix if they are not already defined
        options.addIfAbsent(PlatformSeparators.forUnix());

        // define the default Platform Shell (assume BASH)
        options.addIfAbsent(Shell.is(Shell.Type.BASH));

        // ----- establish an identity for the application -----

        // add a unique runtime id for expression support
        options.add(Variable.with("oracletools.runtime.id", UUID.randomUUID()));

        // ----- establish default Profiles for this Platform (and Builder) -----

        // auto-detect and add externally defined profiles
        options.addAll(Profiles.getProfiles());

        // ---- establish the environment for the application ----

        // obtain the builder-specific remote application environment based on the schema
        // NOTE: This call will modify the Options so should be done before anything else!!
        E environment = getRemoteApplicationEnvironment(applicationSchema, options);

        // ----- deploy remote application artifacts -----

        // determine the DeploymentArtifacts based on those specified by the Deployment option
        ArrayList<DeploymentArtifact> artifactsToDeploy = new ArrayList<>();
        Deployment<T, S>              deployment        = options.get(Deployment.class);

        if (deployment != null)
        {
            try
            {
                for (DeploymentArtifact deploymentArtifact : deployment.getDeploymentArtifacts(applicationSchema,
                                                                                               platform,
                                                                                               options))
                {
                    artifactsToDeploy.add(deploymentArtifact);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to determine artifacts to deploy", e);
            }
        }

        // determine the separators for the platform
        PlatformSeparators separators = options.get(PlatformSeparators.class);

        // assume the remote directory is the working directory
        File   remoteDirectoryFile = applicationSchema.getWorkingDirectory();

        String remoteDirectory;

        if (remoteDirectoryFile == null)
        {
            String   sanitizedApplicationName = separators.asSanitizedFileName(applicationName);
            Calendar now                      = Calendar.getInstance();
            String temporaryDirectoryName = String.format("%1$s-%2$tY%2$tm%2$td-%2$tH%2$tM%2$tS-%2$tL",
                                                          sanitizedApplicationName,
                                                          now);

            // determine the remote TemporaryDirectory
            TemporaryDirectory temporaryDirectory = options.get(TemporaryDirectory.class,
                                                                TemporaryDirectory.at(separators.getFileSeparator()
                                                                                      + "tmp"));

            remoteDirectoryFile = new File(temporaryDirectory.get().toFile(), temporaryDirectoryName);
            remoteDirectory     = separators.asRemotePlatformFileName(remoteDirectoryFile.toString());
        }
        else
        {
            remoteDirectory = separators.asRemotePlatformFileName(remoteDirectoryFile.toString());
        }

        // Obtain the RemoteShell that will be used to realize the process
        RemoteTerminalBuilder   terminalBuilder = options.get(RemoteTerminalBuilder.class, RemoteTerminals.ssh());
        RemoteTerminal<T, S, E> terminal        = terminalBuilder.realize(platform);

        // create the working directory
        terminal.makeDirectories(remoteDirectory, options);

        // Deploy any artifacts required
        Deployer deployer = options.get(Deployer.class, new SftpDeployer());

        deployer.deploy(artifactsToDeploy, remoteDirectory, platform, options.asArray());

        // Realize the remote process
        RemoteApplicationProcess process = terminal.realize(applicationSchema,
                                                            applicationName,
                                                            environment,
                                                            remoteDirectory,
                                                            options);

        // create the Application based on the RemoteApplicationProcess
        T application = createApplication(options, applicationSchema, environment, applicationName, process, console);

        // ----- notify the Profiles that the application has been realized -----

        for (Profile profile : options.getInstancesOf(Profile.class))
        {
            profile.onAfterRealize(platform, application, options);
        }

        // ----- notify all of the lifecycle listeners -----

        raiseOnRealizedFor(application);

        return application;
    }
}
