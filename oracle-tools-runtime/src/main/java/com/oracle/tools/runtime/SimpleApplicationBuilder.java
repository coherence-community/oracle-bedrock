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
public class SimpleApplicationBuilder extends AbstractApplicationBuilder<SimpleApplication, SimpleApplicationSchema>
{
    /**
     * Constructs a {@link SimpleApplicationBuilder}.
     */
    public SimpleApplicationBuilder()
    {
        super();
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


    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleApplication realize(SimpleApplicationSchema schema,
                                     String                  name,
                                     ApplicationConsole      console) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder(schema.getExecutableName());
        File           directory      = schema.getWorkingDirectory();

        if (directory != null)
        {
            processBuilder.directory(directory);
        }

        // determine the environment variables for the process (based on the
        // Environment Variables Builder)
        Properties environmentVariables = schema.getEnvironmentVariablesBuilder().realize();

        // we always clear down the process environment variables as by default
        // they are inherited from the current process, which is not what we
        // want as it doesn't allow us to create a clean environment
        if (!schema.isEnvironmentInherited())
        {
            processBuilder.environment().clear();
        }

        // add the environment variables to the process
        for (String variableName : environmentVariables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, environmentVariables.getProperty(variableName));
        }

        List<String> command = processBuilder.command();

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            command.add(argument);
        }

        // should the standard error be redirected to the standard out?
        processBuilder.redirectErrorStream(schema.isErrorStreamRedirected());

        // start the process
        final SimpleApplication application = new SimpleApplication(new LocalApplicationProcess(processBuilder.start()),
                                                                    name,
                                                                    console,
                                                                    environmentVariables,
                                                                    schema.isDiagnosticsEnabled(),
                                                                    schema.getDefaultTimeout(),
                                                                    schema.getDefaultTimeoutUnits(),
                                                                    schema.getLifecycleInterceptors());

        // let interceptors know that the application has been realized
        raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

        return application;
    }
}
