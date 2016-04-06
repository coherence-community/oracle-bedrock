/*
 * File: AbstractRemoteApplicationLauncher.java
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

import com.oracle.tools.Options;

import com.oracle.tools.lang.ExpressionEvaluator;

import com.oracle.tools.options.Variable;
import com.oracle.tools.options.Variables;

import com.oracle.tools.runtime.AbstractApplicationLauncher;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationListener;
import com.oracle.tools.runtime.ApplicationProcess;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;
import com.oracle.tools.runtime.Profiles;

import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.Executable;
import com.oracle.tools.runtime.options.MetaClass;
import com.oracle.tools.runtime.options.PlatformSeparators;
import com.oracle.tools.runtime.options.Shell;
import com.oracle.tools.runtime.options.WorkingDirectory;

import com.oracle.tools.runtime.remote.options.Deployer;
import com.oracle.tools.runtime.remote.options.Deployment;
import com.oracle.tools.runtime.remote.ssh.SftpDeployer;

import com.oracle.tools.util.ReflectionHelper;

import java.io.File;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * An abstract implementation of a {@link RemoteApplicationLauncher}.
 *
 * @param <A> the type of the {@link Application}s the {@link RemoteApplicationLauncher} will launch
 *            <p>
 *            Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 *            Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractRemoteApplicationLauncher<A extends Application>
    extends AbstractApplicationLauncher<A, RemotePlatform> implements RemoteApplicationLauncher<A>,
                                                                      RemoteTerminal.Launchable
{
    /**
     * Constructs an {@link AbstractRemoteApplicationLauncher}.
     *
     * @param platform the {@link Platform} on which an {@link Application} will be launched
     */
    public AbstractRemoteApplicationLauncher(RemotePlatform platform)
    {
        super(platform);
    }


    @Override
    public A launch(Options options)
    {
        // ----- determine the meta-class for our application -----

        // establish the options for resolving the meta-class
        Options metaOptions = new Options(platform.getOptions()).addAll(options);

        // determine the meta-class
        MetaClass metaClass = metaOptions.getOrDefault(MetaClass.class, new Application.MetaClass());

        // ---- establish the Options for the Application -----

        // add the platform options
        Options launchOptions = new Options(platform == null ? null : platform.getOptions().asArray());

        // add the schema options
        metaClass.onBeforeLaunch(platform, launchOptions);

        // add the custom application options
        launchOptions.addAll(options);

        // ---- establish the default Options ----

        // define the PlatformSeparators as Unix if they are not already defined
        launchOptions.addIfAbsent(PlatformSeparators.forUnix());

        // define the default Platform Shell (assume BASH)
        launchOptions.addIfAbsent(Shell.is(Shell.Type.BASH));

        // define the "local.address" variable so that is can be used for resolving this platform address
        launchOptions.add(Variable.with("local.address", LocalPlatform.get().getAddress().getHostAddress()));

        // ----- establish an identity for the application -----

        // add a unique runtime id for expression support
        launchOptions.add(Variable.with("oracletools.runtime.id", UUID.randomUUID()));

        // ----- establish default Profiles for this Platform (and Builder) -----

        // auto-detect and add externally defined profiles
        launchOptions.addAll(Profiles.getProfiles());

        // ----- notify the Profiles that the application is about to be launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onBeforeLaunch(platform, launchOptions);
        }

        // ----- prior to launching the application, let the implementation enhance the launch options -----

        onBeforeLaunch(launchOptions);

        // ----- establish the display name for the application -----

        DisplayName displayName = getDisplayName(launchOptions);

        // ----- deploy remote application artifacts -----

        // determine the DeploymentArtifacts based on those specified by the Deployment option
        ArrayList<DeploymentArtifact> artifactsToDeploy = new ArrayList<>();
        Deployment                    deployment        = launchOptions.get(Deployment.class);

        if (deployment != null)
        {
            try
            {
                for (DeploymentArtifact deploymentArtifact : deployment.getDeploymentArtifacts(platform, launchOptions))
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
        PlatformSeparators separators = launchOptions.get(PlatformSeparators.class);

        // assume the remote directory is the working directory
        WorkingDirectory workingDirectory = launchOptions.getOrDefault(WorkingDirectory.class,
                                                                       WorkingDirectory.temporaryDirectory());
        File remoteDirectoryFile = workingDirectory.resolve(platform, launchOptions);

        if (remoteDirectoryFile == null)
        {
            remoteDirectoryFile = WorkingDirectory.temporaryDirectory().resolve(platform, launchOptions);
        }

        String remoteDirectory = separators.asPlatformFileName(remoteDirectoryFile.toString());

        // Set the resolved working directory back into the options
        launchOptions.add(WorkingDirectory.at(remoteDirectoryFile));

        // Obtain the RemoteShell that will be used to launch the process
        RemoteTerminalBuilder terminalBuilder = launchOptions.getOrDefault(RemoteTerminalBuilder.class,
                                                                           RemoteTerminals.ssh());
        RemoteTerminal terminal = terminalBuilder.realize(platform);

        // create the working directory
        terminal.makeDirectories(remoteDirectory, launchOptions);

        // Deploy any artifacts required
        Deployer deployer = launchOptions.getOrDefault(Deployer.class, new SftpDeployer());

        deployer.deploy(artifactsToDeploy, remoteDirectory, platform, launchOptions.asArray());

        // Realize the application arguments
        Arguments    arguments = launchOptions.get(Arguments.class);
        List<String> argList   = arguments.resolve(platform, launchOptions);

        // Set the actual arguments used back into the options
        launchOptions.add(Arguments.of(argList));

        // TODO: put a try/catch around the terminal.launch here so we can clean up the RemoteExecutor if
        // the application failed to launch

        // launch the remote process
        RemoteApplicationProcess remoteProcess = terminal.launch(this, launchOptions);

        // adapt the remote process into something that the application can use
        ApplicationProcess process = adapt(remoteProcess);

        // create the Application based on the RemoteApplicationProcess
        A application = null;

        // determine the application class that will represent the running application
        Class<? extends A> applicationClass = metaClass.getImplementationClass(platform, launchOptions);

        try
        {
            // attempt to find a constructor(Platform, JavaApplicationProcess, Options)
            Constructor<? extends A> constructor = ReflectionHelper.getCompatibleConstructor(applicationClass,
                                                                                             platform.getClass(),
                                                                                             process.getClass(),
                                                                                             Options.class);

            // create the application
            application = constructor.newInstance(platform, process, launchOptions);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to instantiate the Application class specified by the MetaClass:"
                                       + metaClass,
                                       e);
        }

        // ----- after launching the application, let the implementation interact with the application -----

        onAfterLaunch(application, launchOptions);

        // ----- notify the MetaClass that the application has been launched -----

        metaClass.onAfterLaunch(platform, application, launchOptions);

        // ----- notify the Profiles that the application has been launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onAfterLaunch(platform, application, launchOptions);
        }

        // ----- notify all of the lifecycle listeners -----

        // notify the ApplicationListener-based Options that the application has been launched
        for (ApplicationListener listener : launchOptions.getInstancesOf(ApplicationListener.class))
        {
            listener.onLaunched(application);
        }

        return application;
    }


    /**
     * Prepares the launch {@link Options} prior to being used to launch an {@link Application}.
     *
     * @param options  the launch {@link Options}
     */
    protected void onBeforeLaunch(Options options)
    {
        // be default we don't change the launch options
    }


    /**
     * Prepares the {@link Application} after it was launched for use.
     *
     * @param application  the launched {@link Application}
     * @param options      the launch {@link Options}
     */
    protected void onAfterLaunch(A       application,
                                 Options options)
    {
        // be default we don't change the application after it was created
    }


    /**
     * Adapt the {@link RemoteApplicationProcess} as something specific for the {@link Application} being built.
     *
     * @param process  the {@link RemoteApplicationProcess}
     * @param <P>      the desired type of the {@link ApplicationProcess}
     *
     * @return  the adapted {@link ApplicationProcess}
     */
    protected <P extends ApplicationProcess> P adapt(RemoteApplicationProcess process)
    {
        return (P) process;
    }


    @Override
    public String getCommandToExecute(Platform platform,
                                      Options  options)
    {
        return options.get(Executable.class).getName();
    }


    @Override
    public List<String> getCommandLineArguments(Platform platform,
                                                Options  options)
    {
        ArrayList<String> arguments = new ArrayList<>();

        // ----- add oracletools.runtime.inherit.xxx values to the arguments -----

        for (String propertyName : System.getProperties().stringPropertyNames())
        {
            if (propertyName.startsWith("oracletools.runtime.inherit."))
            {
                // resolve the property value
                String propertyValue = System.getProperty(propertyName);

                // evaluate any expressions in the property value
                ExpressionEvaluator evaluator = new ExpressionEvaluator(options.get(Variables.class));

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                arguments.add(propertyValue);
            }
        }

        List<String> argList = options.get(Arguments.class).resolve(platform, options);

        arguments.addAll(argList);

        return arguments;
    }


    @Override
    public Properties getEnvironmentVariables(Platform platform,
                                              Options  options)
    {
        EnvironmentVariables environmentVariables = options.getOrDefault(EnvironmentVariables.class,
                                                                         EnvironmentVariables.of(EnvironmentVariables
                                                                             .Source.TargetPlatform));

        Properties variables = new Properties();

        switch (environmentVariables.getSource())
        {
        case Custom :
            break;

        case ThisApplication :
            variables.putAll(System.getenv());
            break;

        case TargetPlatform :
            break;
        }

        // add the optionally defined environment variables
        variables.putAll(environmentVariables.realize(platform, options.asArray()));

        return variables;
    }
}
