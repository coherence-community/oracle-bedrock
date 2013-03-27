/*
 * File: AbstractJavaApplicationBuilder.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.AbstractApplicationBuilder;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Application.EventKind;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LifecycleEvent;
import com.oracle.tools.runtime.LifecycleEventInterceptor;
import com.oracle.tools.runtime.java.process.AbstractJavaProcessBuilder;
import com.oracle.tools.runtime.java.process.JavaProcessBuilder;

import java.io.IOException;
import java.util.Properties;

/**
 * An {@link AbstractJavaApplicationBuilder} is the base implementation for {@link JavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplicationBuilder<A extends JavaApplication<A>,
                                                     S extends JavaApplicationSchema<A, S>>
    extends AbstractApplicationBuilder<A, S>
{
    /**
     * Constructs a {@link AbstractJavaApplicationBuilder}.
     */
    public AbstractJavaApplicationBuilder()
    {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public A realize(S                  schema,
                     String             applicationName,
                     ApplicationConsole console) throws IOException
    {
        // construct an appropriate JavaProcessBuilder for the specified schema
        JavaProcessBuilder processBuilder = createJavaProcessBuilder(schema, applicationName, console);

        // add the jvm options to the operating system command
        for (String option : schema.getJVMOptions())
        {
            processBuilder.getCommands().add("-" + option);
        }

        String classPath = schema.getClassPath().toString();

        // determine the environment variables for the process (based on the Environment Variables Builder)
        Properties environmentVariables = schema.getEnvironmentVariablesBuilder().realize();

        // we always clear down the process environment variables as by default they are inherited from
        // the current process, which is not what we want as it doesn't allow us to create a clean environment
        if (!schema.isInherited())
        {
            processBuilder.getEnvironment().clear();
        }

        // add the environment variables to the process
        for (String variableName : environmentVariables.stringPropertyNames())
        {
            processBuilder.getEnvironment().put(variableName, environmentVariables.getProperty(variableName));
        }

        // add the class path (it's an environment variable)
        processBuilder.getEnvironment().put("CLASSPATH", classPath);

        // realize the system properties for the process
        Properties systemProperties = schema.getSystemPropertiesBuilder().realize();

        processBuilder.getSystemProperties().putAll(systemProperties);

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            processBuilder.addArgument(argument);
        }

        // start the process and capture the application as a JavaApplication
        final A application = schema.createJavaApplication(processBuilder.realize(),
                                                           applicationName,
                                                           console,
                                                           environmentVariables,
                                                           systemProperties);

        // raise the starting / realized event for the application
        @SuppressWarnings("rawtypes") LifecycleEvent event = new LifecycleEvent<A>()
        {
            @Override
            public EventKind getType()
            {
                return Application.EventKind.REALIZED;
            }

            @Override
            public A getObject()
            {
                return application;
            }
        };

        for (LifecycleEventInterceptor<A> interceptor : application.getLifecycleInterceptors())
        {
            interceptor.onEvent(event);
        }

        return application;
    }


    /**
     * Creates a {@link AbstractJavaProcessBuilder}, identifiable by the proposed name
     * using the specified {@link JavaApplicationSchema} for configuration,
     * using the provided {@link ApplicationConsole} for output.
     *
     * @param schema           the {@link JavaApplicationSchema} containing configuration
     * @param applicationName  the proposed name of the process to be built
     * @param console          the {@link ApplicationConsole} for output
     * @return
     */
    protected abstract JavaProcessBuilder createJavaProcessBuilder(S                  schema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console);
}
