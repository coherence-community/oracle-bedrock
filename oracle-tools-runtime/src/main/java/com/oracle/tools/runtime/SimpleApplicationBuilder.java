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

import java.io.File;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * A {@link SimpleApplicationBuilder} is an {@link ApplicationBuilder} for
 * {@link SimpleApplication}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleApplicationBuilder extends AbstractApplicationBuilder<SimpleApplication>
{
    /**
     * Should environment variables be inherited from the current executing process
     * as the basis for {@link SimpleApplication}s produced by this {@link SimpleApplicationBuilder}.
     */
    private boolean isEnvironmentInherited;

    /**
     * The {@link PropertiesBuilder} defining custom environment variables to
     * establish when realizing a {@link SimpleApplication}.
     */
    private PropertiesBuilder environmentVariablesBuilder;


    /**
     * Constructs a {@link SimpleApplicationBuilder}.
     */
    public SimpleApplicationBuilder()
    {
        super();

        // by default there are no custom environment variables
        environmentVariablesBuilder = new PropertiesBuilder();

        // by default we always inherit local environment variables
        isEnvironmentInherited = true;
    }


    /**
     * Obtains the {@link PropertiesBuilder} defining custom
     * {@link SimpleApplication}-specific operating system environment
     * variables to be established when realizing an {@link SimpleApplication}.
     *
     * @return {@link PropertiesBuilder}
     */
    public PropertiesBuilder getEnvironmentVariablesBuilder()
    {
        return environmentVariablesBuilder;
    }


    /**
     * Sets whether the environment variables from the currently executing
     * process should be inherited and used as the basis for environment variables
     * when realizing an {@link SimpleApplication}.
     *
     * @param isEnvironmentInherited  <code>true</code> if the {@link SimpleApplicationBuilder}
     *                                should inherit the environment variables from the
     *                                currently executing process or <code>false</code>
     *                                if a clean/empty environment should be used
     *                                (containing only those custom variables defined by this
     *                                {@link SimpleApplicationBuilder} and an
     *                                {@link SimpleApplication})
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public SimpleApplicationBuilder setEnvironmentInherited(boolean isEnvironmentInherited)
    {
        this.isEnvironmentInherited = isEnvironmentInherited;

        return this;
    }


    /**
     * Determines if the environment variables of the currently executing
     * process will be inherited and used as a basis for environment variables
     * when realizing a {@link SimpleApplication}.
     *
     * @return  <code>true</code> if environment variables are inherited from
     *          the current process when realizing a {@link SimpleApplication} or
     *          <code>false</code> if a clean environment is used instead
     */
    public boolean isEnvironmentInherited()
    {
        return isEnvironmentInherited;
    }


    /**
     * Defines a custom environment variable for {@link SimpleApplication}s
     * realized by this {@link SimpleApplicationBuilder} based on values
     * returned by the {@link Iterator}.
     *
     * @param name      the name of the environment variable
     * @param iterator  an {@link Iterator} providing values for the environment
     *                  variable
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public SimpleApplicationBuilder setEnvironmentVariable(String      name,
                                                           Iterator<?> iterator)
    {
        environmentVariablesBuilder.setProperty(name, iterator);

        return this;
    }


    /**
     * Defines a custom environment variable for {@link SimpleApplication}s
     * realized by this {@link SimpleApplicationBuilder}.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public SimpleApplicationBuilder setEnvironmentVariable(String name,
                                                           Object value)
    {
        environmentVariablesBuilder.setProperty(name, value);

        return this;
    }


    /**
     * Sets if diagnostic information should be logged/output for {@link Application}s
     * produced by this builder.
     *
     * @param isDiagnosticsEnabled  should diagnostics be output
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public SimpleApplicationBuilder setDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        m_isDiagnosticsEnabled = isDiagnosticsEnabled;

        return this;
    }


    @Override
    public <T extends SimpleApplication, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                                   String             applicationName,
                                                                                   ApplicationConsole console)
    {
        ApplicationSchema<T> schema = applicationSchema;

        // ---- establish the underlying ProcessBuilder -----

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder = new ProcessBuilder(schema.getExecutableName());

        // ----- establish the working directory -----

        // set the working directory for the Process
        File directory = schema.getWorkingDirectory();

        if (directory != null)
        {
            processBuilder.directory(directory);
        }

        // ----- establish environment variables -----

        // when not inheriting we need to clear the defined environment
        if (!isEnvironmentInherited() &&!schema.isEnvironmentInherited())
        {
            processBuilder.environment().clear();
        }

        // add the environment variables defined by the schema
        Properties environmentVariables = schema.getEnvironmentVariablesBuilder().realize();

        for (String variableName : environmentVariables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, environmentVariables.getProperty(variableName));
        }

        // add the environment variables defined by the builder
        environmentVariables = getEnvironmentVariablesBuilder().realize();

        for (String variableName : environmentVariables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, environmentVariables.getProperty(variableName));
        }

        // ----- establish the application command line to execute -----

        List<String> command = processBuilder.command();

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            command.add(argument);
        }

        // should the standard error be redirected to the standard out?
        processBuilder.redirectErrorStream(schema.isErrorStreamRedirected());

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

        final T application = (T) new SimpleApplication(new LocalApplicationProcess(process),
                                                        applicationName,
                                                        console,
                                                        environmentVariables,
                                                        schema.isDiagnosticsEnabled(),
                                                        schema.getDefaultTimeout(),
                                                        schema.getDefaultTimeoutUnits(),
                                                        schema instanceof FluentApplicationSchema
                                                        ? ((FluentApplicationSchema<SimpleApplication, ?>) schema)
                                                            .getLifecycleInterceptors() : null);

        // ----- notify all of the lifecycle listeners -----

        // let interceptors know that the application has been realized
        raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

        return application;
    }
}
