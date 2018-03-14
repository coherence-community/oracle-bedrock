/*
 * File: SimpleApplicationLauncher.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.Bedrock;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.lang.StringHelper;
import com.oracle.bedrock.options.LaunchLogging;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;
import com.oracle.bedrock.runtime.options.ErrorStreamRedirection;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.table.Table;
import com.oracle.bedrock.table.Tabularize;
import com.oracle.bedrock.util.ReflectionHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An {@link ApplicationLauncher} for {@link SimpleApplication}s on a
 * {@link LocalPlatform}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
@Internal
public class SimpleApplicationLauncher implements ApplicationLauncher<Application>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(SimpleApplicationLauncher.class.getName());


    /**
     * Constructs a {@link SimpleApplicationLauncher}.
     *
     */
    public SimpleApplicationLauncher()
    {
    }


    @Override
    public Application launch(Platform               platform,
                              MetaClass<Application> metaClass,
                              OptionsByType          optionsByType)
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

        // ----- establish default Profiles for this Platform (and Builder) -----

        // auto-detect and add externally defined profiles
        launchOptions.addAll(Profiles.getProfiles());

        // ----- notify the Profiles that the application is about to be realized -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onLaunching(platform, metaClass, launchOptions);
        }

        // ----- give the MetaClass a last chance to manipulate any options -----

        metaClass.onLaunch(platform, launchOptions);

        // ----- determine the display name for the application -----

        // ensure there's a display name
        DisplayName displayName = launchOptions.getOrSetDefault(DisplayName.class,
                                                                DisplayName.of(launchOptions.get(Executable.class)
                                                                .getName()));

        // ---- establish the underlying ProcessBuilder -----

        // determine the Executable
        Executable executable = launchOptions.get(Executable.class);

        if (executable == null)
        {
            throw new IllegalArgumentException("Failed to define an Executable option");
        }

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder = new ProcessBuilder(StringHelper.doubleQuoteIfNecessary(executable.getName()));

        // ----- establish the working directory -----

        // set the working directory for the Process
        WorkingDirectory workingDirectory = launchOptions.getOrSetDefault(WorkingDirectory.class,
                                                                          WorkingDirectory.currentDirectory());
        File directory = workingDirectory.resolve(platform, launchOptions);

        // Set the resolved working directory back into the options
        launchOptions.add(WorkingDirectory.at(directory));

        if (directory != null)
        {
            processBuilder.directory(directory);

            diagnosticsTable.addRow("Working Directory", directory.toString());
        }

        // ----- establish environment variables -----

        EnvironmentVariables environmentVariables = launchOptions.get(EnvironmentVariables.class);

        switch (environmentVariables.getSource())
        {
        case Custom :
            processBuilder.environment().clear();

            diagnosticsTable.addRow("Environment Variables", "(cleared)");
            break;

        case ThisApplication :
            processBuilder.environment().clear();
            processBuilder.environment().putAll(System.getenv());

            diagnosticsTable.addRow("Environment Variables", "(based on parent process)");
            break;

        case TargetPlatform :

            diagnosticsTable.addRow("Environment Variables", "(based on platform defaults)");
            break;
        }

        // add the optionally defined environment variables
        Properties variables = environmentVariables.realize(platform, launchOptions.asArray());

        for (String variableName : variables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, variables.getProperty(variableName));
        }

        if (variables.size() > 0)
        {
            Table table = Tabularize.tabularize(variables);

            diagnosticsTable.addRow("", table.toString());
        }

        // ----- establish the application command line to execute -----

        List<String> command = processBuilder.command();

        // add the arguments to the command for the process
        List<String> arguments = launchOptions.get(Arguments.class).resolve(platform, launchOptions);

        command.addAll(arguments);

        diagnosticsTable.addRow("Application", displayName.resolve(launchOptions));
        diagnosticsTable.addRow("Application Executable ", executable.getName());

        if (arguments.size() > 0)
        {
            diagnosticsTable.addRow("Application Arguments ", arguments.stream().collect(Collectors.joining(" ")));
        }

        diagnosticsTable.addRow("Application Launch Time",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // set the actual arguments used back into the options
        launchOptions.add(Arguments.of(arguments));

        // should the standard error be redirected to the standard out?
        ErrorStreamRedirection redirection = launchOptions.get(ErrorStreamRedirection.class);

        processBuilder.redirectErrorStream(redirection.isEnabled());

        boolean launchLogging = optionsByType.get(LaunchLogging.class).isEnabled();

        if (launchLogging && LOGGER.isLoggable(Level.INFO))
        {
            LOGGER.log(Level.INFO,
                       "Oracle Bedrock " + Bedrock.getVersion() + ": Starting Application...\n"
                       + "------------------------------------------------------------------------\n"
                       + diagnosticsTable.toString() + "\n"
                       + "------------------------------------------------------------------------\n");
        }

        // ----- start the process and establish the application -----

        // create and start the native process
        Process process;

        try
        {
            process = processBuilder.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to build the underlying native process for the application", e);
        }

        // determine the application class that will represent the running application
        Class<? extends Application> applicationClass = metaClass.getImplementationClass(platform, launchOptions);

        Application                  application;

        try
        {
            // attempt to find a constructor(Platform, LocalApplicationProcess, Options)

            Constructor<? extends Application> constructor = ReflectionHelper.getCompatibleConstructor(applicationClass,
                                                                                                       platform.getClass(),
                                                                                                       LocalApplicationProcess.class,
                                                                                                       OptionsByType.class);

            // create the application
            application = constructor.newInstance(platform, new LocalApplicationProcess(process), launchOptions);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to instantiate the Application class specified by the MetaClass:"
                                       + metaClass,
                                       e);
        }

        // ----- notify the MetaClass that the application has been launched -----

        metaClass.onLaunched(platform, application, launchOptions);

        // ----- notify the Profiles that the application has been realized -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onLaunched(platform, application, launchOptions);
        }

        // ----- notify all of the application listeners -----

        // notify the ApplicationListener-based Options that the application has been realized
        for (ApplicationListener listener : launchOptions.getInstancesOf(ApplicationListener.class))
        {
            listener.onLaunched(application);
        }

        return application;
    }
}
