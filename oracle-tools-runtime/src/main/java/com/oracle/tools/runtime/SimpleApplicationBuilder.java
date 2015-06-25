/*
 * File: SimpleApplicationBuilder.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.ErrorStreamRedirection;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Properties;

/**
 * An {@link ApplicationBuilder} for {@link SimpleApplication}s on a
 * {@link LocalPlatform}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleApplicationBuilder extends AbstractApplicationBuilder<SimpleApplication, LocalPlatform>
{
    /**
     * Constructs a {@link SimpleApplicationBuilder} for the specified
     * {@link LocalPlatform}.
     *
     * @param platform  the {@link LocalPlatform}
     */
    public SimpleApplicationBuilder(LocalPlatform platform)
    {
        super(platform);
    }


    @Override
    public <T extends SimpleApplication, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                                   String             applicationName,
                                                                                   ApplicationConsole console,
                                                                                   Option...          applicationOptions)
    {
        ApplicationSchema<T> schema = applicationSchema;

        // ---- establish the Options for the Application -----

        // add the platform options
        Options options = new Options(platform == null ? null : platform.getOptions().asArray());

        // add the schema options
        options.addAll(applicationSchema.getOptions().asArray());

        // add the schema options (based on the platform)
        options.addAll(applicationSchema.getPlatformSpecificOptions(platform).asArray());

        // add the custom application options
        options.addAll(applicationOptions);

        // ---- establish the underlying ProcessBuilder -----

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder =
            new ProcessBuilder(StringHelper.doubleQuoteIfNecessary(schema.getExecutableName()));

        // ----- establish the working directory -----

        // set the working directory for the Process
        File directory = schema.getWorkingDirectory();

        if (directory != null)
        {
            processBuilder.directory(directory);
        }

        // ----- establish environment variables -----

        EnvironmentVariables environmentVariables = options.get(EnvironmentVariables.class,
                                                                EnvironmentVariables.inherited());

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
        Properties variables = environmentVariables.getBuilder().realize();

        for (String variableName : variables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, variables.getProperty(variableName));
        }

        // ----- establish the application command line to execute -----

        List<String> command = processBuilder.command();

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            command.add(argument);
        }

        // should the standard error be redirected to the standard out?
        ErrorStreamRedirection redirection = options.get(ErrorStreamRedirection.class,
                                                         ErrorStreamRedirection.disabled());

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

        // construct an ApplicationEnvironment for the SimpleApplication
        SimpleApplicationRuntime environment = new SimpleApplicationRuntime(applicationName,
                                                                            platform,
                                                                            options,
                                                                            new LocalApplicationProcess(process),
                                                                            console,
                                                                            variables);

        // create the SimpleApplication
        final T application = (T) new SimpleApplication(environment,
                                                        schema instanceof FluentApplicationSchema
                                                        ? ((FluentApplicationSchema<SimpleApplication, ?>) schema)
                                                            .getApplicationListeners() : null);

        // ----- notify all of the application listeners -----

        // raise life-cycle events for the application
        raiseOnRealizedFor(application);

        return application;
    }
}
