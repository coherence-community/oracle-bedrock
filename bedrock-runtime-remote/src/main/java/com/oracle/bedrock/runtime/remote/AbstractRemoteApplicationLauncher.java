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

package com.oracle.bedrock.runtime.remote;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.diagnostics.DiagnosticsRecording;
import com.oracle.bedrock.lang.ExpressionEvaluator;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.options.LaunchLogging;
import com.oracle.bedrock.options.Variable;
import com.oracle.bedrock.options.Variables;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationLauncher;
import com.oracle.bedrock.runtime.ApplicationListener;
import com.oracle.bedrock.runtime.ApplicationProcess;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.Profiles;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.runtime.options.Shell;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.options.Deployment;
import com.oracle.bedrock.runtime.remote.ssh.SftpDeployer;
import com.oracle.bedrock.table.Table;
import com.oracle.bedrock.table.Tabularize;
import com.oracle.bedrock.util.ReflectionHelper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An abstract implementation of a {@link ApplicationLauncher}.
 *
 * @param <A> the type of the {@link Application}s the {@link ApplicationLauncher} will launch
 *            <p>
 *            Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 *            Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public abstract class AbstractRemoteApplicationLauncher<A extends Application> implements ApplicationLauncher<A>,
                                                                                          RemoteTerminal.Launchable
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(AbstractRemoteApplicationLauncher.class.getName());


    /**
     * Constructs an {@link AbstractRemoteApplicationLauncher}.
     *
     */
    public AbstractRemoteApplicationLauncher()
    {
    }


    @Override
    public A launch(Platform      platform,
                    MetaClass<A>  metaClass,
                    OptionsByType optionsByType)
    {
        // establish the diagnostics output table
        Table diagnosticsTable = new Table();

        diagnosticsTable.getOptions().add(Table.orderByColumn(0));

        if (platform != null)
        {
            diagnosticsTable.addRow("Target Platform", platform.getName());
        }

        // ----- establish the launch Options for the Application -----

        // add the platform options
        OptionsByType launchOptions = OptionsByType.of(platform.getOptions()).addAll(optionsByType);

        // add the meta-class options
        metaClass.onLaunching(platform, launchOptions);

        // ---- establish the default Options ----

        // define the PlatformSeparators as Unix if they are not already defined
        launchOptions.addIfAbsent(PlatformSeparators.forUnix());

        // define the default Platform Shell (assume BASH)
        launchOptions.addIfAbsent(Shell.is(Shell.Type.BASH));

        // define the "local.address" variable so that is can be used for resolving this platform address
        launchOptions.add(Variable.with("local.address", LocalPlatform.get().getAddress().getHostAddress()));

        // ----- establish an identity for the application -----

        // add a unique runtime id for expression support
        launchOptions.add(Variable.with("bedrock.runtime.id", UUID.randomUUID()));

        // ----- establish default Profiles for this Platform (and Builder) -----

        // auto-detect and add externally defined profiles
        launchOptions.addAll(Profiles.getProfiles());

        // ----- notify the Profiles that the application is about to be launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onLaunching(platform, metaClass, launchOptions);
        }

        // ----- add the diagnostic table to the options so it can be used by the terminal -----
        launchOptions.add(diagnosticsTable);

        // ----- prior to launching the application, let the implementation enhance the launch options -----

        onLaunching(launchOptions);

        // ----- give the MetaClass a last chance to manipulate any options -----

        metaClass.onLaunch(platform, launchOptions);

        // ----- determine the display name for the application -----

        DisplayName displayName = getDisplayName(launchOptions);

        // determine the Executable
        Executable executable = launchOptions.get(Executable.class);

        // ----- deploy remote application artifacts -----

        // determine the DeploymentArtifacts based on those specified by the Deployment option
        ArrayList<DeploymentArtifact> artifactsToDeploy = new ArrayList<>();
        Deployment                    deployment        = launchOptions.get(Deployment.class);

        if (deployment != null)
        {
            try
            {
                artifactsToDeploy.addAll(deployment.getDeploymentArtifacts(platform, launchOptions));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to determine artifacts to deploy", e);
            }
        }

        // determine the separators for the platform
        PlatformSeparators separators = launchOptions.get(PlatformSeparators.class);

        // assume the remote directory is the working directory
        WorkingDirectory workingDirectory = launchOptions.getOrSetDefault(WorkingDirectory.class,
                                                                          WorkingDirectory.temporaryDirectory());
        File remoteDirectoryFile = workingDirectory.resolve(platform, launchOptions);

        if (remoteDirectoryFile == null)
        {
            remoteDirectoryFile = WorkingDirectory.temporaryDirectory().resolve(platform, launchOptions);
        }

        String remoteDirectory = separators.asPlatformFileName(remoteDirectoryFile.toString());

        // Set the resolved working directory back into the options
        launchOptions.add(WorkingDirectory.at(remoteDirectoryFile));

        if (remoteDirectoryFile != null)
        {
            diagnosticsTable.addRow("Working Directory", remoteDirectoryFile.toString());
        }

        // Obtain the RemoteShell that will be used to launch the process
        RemoteTerminalBuilder terminalBuilder = launchOptions.getOrSetDefault(RemoteTerminalBuilder.class,
                                                                              RemoteTerminals.ssh());
        RemoteTerminal terminal = terminalBuilder.build(platform);

        // create the working directory
        terminal.makeDirectories(remoteDirectory, launchOptions);

        // deploy any artifacts required
        Deployer deployer = launchOptions.getOrSetDefault(Deployer.class, new SftpDeployer());

        DeployedArtifacts deployedArtifacts = deployer.deploy(artifactsToDeploy,
                                                              remoteDirectory,
                                                              platform,
                                                              launchOptions.asArray());

        // add the remote directory as something to clean up
        deployedArtifacts.add(remoteDirectoryFile);

        if (!deployedArtifacts.isEmpty())
        {
            // when we've deployed artifacts we need to add a listener to clean them up
            launchOptions.add(Decoration.of(new ApplicationListener<A>()
                                            {
                                                @Override
                                                public void onClosing(A             application,
                                                                      OptionsByType optionsByType)
                                                {
                                                    // nothing to do on closing
                                                }

                                                @Override
                                                public void onClosed(A             application,
                                                                     OptionsByType optionsByType)
                                                {
                                                    Level logLevel = optionsByType.get(LaunchLogging.class).isEnabled()
                                                                            ? Level.INFO : Level.OFF;

                                                    try (DiagnosticsRecording diagnostics =
                                                        DiagnosticsRecording.create("Undeploy Diagnostics for "
                                                                                    + application.getName()
                                                                                    + " on platform "
                                                                                    + platform.getName())
                                                                            .using(LOGGER, logLevel))
                                                    {
                                                        diagnostics.add("Platform", "Resource");

                                                        try (DiagnosticsRecording local =
                                                            DiagnosticsRecording.section("Local Platform"))
                                                        {
                                                            // clean up the locally created temporary artifacts
                                                            artifactsToDeploy.stream().filter(DeploymentArtifact::isTemporary)
                                                                .forEach(artifact -> {
                                                                    try
                                                                    {
                                                                        // attempt to remove the local file
                                                                        artifact.getSourceFile().delete();

                                                                        // include diagnostics
                                                                        local.add(artifact.getSourceFile().toString());
                                                                    }
                                                                    catch (Exception e)
                                                                    {
                                                                        // log exceptions when attempting to remove local sources
                                                                        LOGGER.log(Level.WARNING,
                                                                                   "Failed to remove temporary "
                                                                                   + artifact.toString()
                                                                                   + " for application "
                                                                                   + application.getName(),
                                                                                   e);

                                                                        // include diagnostics
                                                                        local.add(artifact.getSourceFile()
                                                                                  + " (failed to undeploy)");
                                                                    }
                                                                });
                                                        }

                                                        // undeploy the deployed artifacts
                                                        deployer.undeploy(deployedArtifacts,
                                                                          platform,
                                                                          launchOptions.asArray());
                                                    }
                                                }

                                                @Override
                                                public void onLaunched(A application)
                                                {
                                                    // nothing to do after launching
                                                }
                                            }));
        }

        // Realize the application arguments
        Arguments    arguments = launchOptions.get(Arguments.class);
        List<String> argList   = arguments.resolve(platform, launchOptions);

        // Set the actual arguments used back into the options
        launchOptions.add(Arguments.of(argList));

        // TODO: put a try/catch around the terminal.launch here so we can clean up the RemoteExecutor if
        // the application failed to launch

        // determine the application class that will represent the running application
        Class<? extends A> applicationClass = metaClass.getImplementationClass(platform, launchOptions);

        diagnosticsTable.addRow("Application", displayName.resolve(launchOptions));

        if (argList.size() > 0)
        {
            diagnosticsTable.addRow("Application Arguments ", argList.stream().collect(Collectors.joining(" ")));
        }

        diagnosticsTable.addRow("Application Launch Time",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // ----- start the process and establish the application -----

        // launch the remote process
        RemoteApplicationProcess remoteProcess = terminal.launch(this, applicationClass, launchOptions);

        // adapt the remote process into something that the application can use
        ApplicationProcess process = adapt(remoteProcess);

        // create the Application based on the RemoteApplicationProcess
        A application;

        try
        {
            // attempt to find a constructor(Platform, JavaApplicationProcess, Options)
            Constructor<? extends A> constructor = ReflectionHelper.getCompatibleConstructor(applicationClass,
                                                                                             platform.getClass(),
                                                                                             process.getClass(),
                                                                                             OptionsByType.class);

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

        onLaunched(application, launchOptions);

        // ----- notify the MetaClass that the application has been launched -----

        metaClass.onLaunched(platform, application, launchOptions);

        // ----- notify the Profiles that the application has been launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onLaunched(platform, application, launchOptions);
        }

        // ----- notify all of the application listeners -----

        // notify the ApplicationListener-based Options that the application has been launched
        for (ApplicationListener listener : launchOptions.getInstancesOf(ApplicationListener.class))
        {
            listener.onLaunched(application);
        }

        return application;
    }


    /**
     * Prepares the launch {@link OptionsByType} prior to being used to launch an {@link Application}.
     *
     * @param optionsByType  the launch {@link OptionsByType}
     */
    abstract protected void onLaunching(OptionsByType optionsByType);


    /**
     * Prepares the {@link Application} after it was launched for use.
     *
     * @param application    the launched {@link Application}
     * @param optionsByType  the launch {@link OptionsByType}
     */
    abstract protected void onLaunched(A             application,
                                       OptionsByType optionsByType);


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
    public String getCommandToExecute(Platform      platform,
                                      OptionsByType optionsByType)
    {
        return optionsByType.get(Executable.class).getName();
    }


    @Override
    public List<String> getCommandLineArguments(Platform      platform,
                                                OptionsByType optionsByType)
    {
        ArrayList<String> arguments = new ArrayList<>();

        // ----- add bedrock.runtime.inherit.xxx values to the arguments -----

        for (String propertyName : System.getProperties().stringPropertyNames())
        {
            if (propertyName.startsWith("bedrock.runtime.inherit."))
            {
                // resolve the property value
                String propertyValue = System.getProperty(propertyName);

                // evaluate any expressions in the property value
                ExpressionEvaluator evaluator = new ExpressionEvaluator(optionsByType.get(Variables.class));

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                arguments.add(propertyValue);
            }
        }

        List<String> argList = optionsByType.get(Arguments.class).resolve(platform, optionsByType);

        arguments.addAll(argList);

        return arguments;
    }


    @Override
    public Properties getEnvironmentVariables(Platform      platform,
                                              OptionsByType optionsByType)
    {
        Table diagnosticsTable = optionsByType.get(Table.class);
        EnvironmentVariables environmentVariables = optionsByType.getOrSetDefault(EnvironmentVariables.class,
                                                                                  EnvironmentVariables.of(EnvironmentVariables
                                                                                      .Source.TargetPlatform));

        Properties variables = new Properties();

        switch (environmentVariables.getSource())
        {
        case Custom :
            if (diagnosticsTable != null)
            {
                diagnosticsTable.addRow("Environment Variables", "(cleared)");
            }

            break;

        case ThisApplication :
            variables.putAll(System.getenv());

            if (diagnosticsTable != null)
            {
                diagnosticsTable.addRow("Environment Variables", "(based on parent process)");
            }

            break;

        case TargetPlatform :
            if (diagnosticsTable != null)
            {
                diagnosticsTable.addRow("Environment Variables", "(based on platform defaults)");
            }

            break;
        }

        // add the optionally defined environment variables
        variables.putAll(environmentVariables.realize(platform, optionsByType.asArray()));

        if (variables.size() > 0 && diagnosticsTable != null)
        {
            Table table = Tabularize.tabularize(variables);

            diagnosticsTable.addRow("", table.toString());
        }

        return variables;
    }
}
