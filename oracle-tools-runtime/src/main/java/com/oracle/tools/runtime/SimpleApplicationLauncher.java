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

package com.oracle.tools.runtime;

import com.oracle.tools.Options;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.ErrorStreamRedirection;
import com.oracle.tools.runtime.options.Executable;
import com.oracle.tools.runtime.options.MetaClass;
import com.oracle.tools.runtime.options.WorkingDirectory;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Properties;

/**
 * An {@link ApplicationLauncher} for {@link SimpleApplication}s on a
 * {@link LocalPlatform}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleApplicationLauncher extends AbstractApplicationLauncher<SimpleApplication, LocalPlatform>
{
    /**
     * Constructs a {@link SimpleApplicationLauncher}.
     *
     * @param platform  the {@link Platform} on which an {@link Application} will be launched
     */
    public SimpleApplicationLauncher(LocalPlatform platform)
    {
        super(platform);
    }


    @Override
    public SimpleApplication launch(Options options)
    {
        // ----- determine the meta-class for our application -----

        // establish the options for resolving the meta-class
        Options metaOptions = new Options(platform.getOptions()).addAll(options);

        // determine the meta-class
        MetaClass metaClass = metaOptions.getOrDefault(MetaClass.class, new Application.MetaClass());

        // ----- establish the launch Options for the Application -----

        // add the platform options
        Options launchOptions = new Options(platform.getOptions().asArray());

        // add the meta-class options
        metaClass.onLaunching(platform, launchOptions);

        // add the launch specific options
        launchOptions.addAll(options);

        // ----- establish default Profiles for this Platform (and Builder) -----

        // auto-detect and add externally defined profiles
        launchOptions.addAll(Profiles.getProfiles());

        // ----- notify the Profiles that the application is about to be realized -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onLaunching(platform, launchOptions);
        }

        // ----- determine the display name for the application -----

        // ensure there's a display name
        DisplayName displayName = launchOptions.getOrDefault(DisplayName.class,
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
        WorkingDirectory workingDirectory = launchOptions.getOrDefault(WorkingDirectory.class,
                                                                       WorkingDirectory.currentDirectory());
        File directory = workingDirectory.resolve(platform, launchOptions);

        // Set the resolved working directory back into the options
        launchOptions.add(WorkingDirectory.at(directory));

        if (directory != null)
        {
            processBuilder.directory(directory);
        }

        // ----- establish environment variables -----

        EnvironmentVariables environmentVariables = launchOptions.get(EnvironmentVariables.class);

        switch (environmentVariables.getSource())
        {
        case Custom :
            processBuilder.environment().clear();
            break;

        case ThisApplication :
            processBuilder.environment().clear();
            processBuilder.environment().putAll(System.getenv());
            break;

        case TargetPlatform :
            break;
        }

        // add the optionally defined environment variables
        Properties variables = environmentVariables.realize(platform, launchOptions.asArray());

        for (String variableName : variables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, variables.getProperty(variableName));
        }

        // ----- establish the application command line to execute -----

        List<String> command = processBuilder.command();

        // add the arguments to the command for the process
        List<String> arguments = launchOptions.get(Arguments.class).resolve(platform, launchOptions);

        command.addAll(arguments);

        // set the actual arguments used back into the options
        launchOptions.add(Arguments.of(arguments));

        // should the standard error be redirected to the standard out?
        ErrorStreamRedirection redirection = launchOptions.get(ErrorStreamRedirection.class);

        processBuilder.redirectErrorStream(redirection.isEnabled());

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

        // create the application based on the process
        SimpleApplication application = new SimpleApplication(platform,
                                                              new LocalApplicationProcess(process),
                                                              launchOptions);

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
