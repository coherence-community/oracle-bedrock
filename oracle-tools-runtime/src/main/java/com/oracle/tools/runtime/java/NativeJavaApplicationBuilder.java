/*
 * File: NativeJavaApplicationBuilder.java
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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.NativeApplicationProcess;

import com.oracle.tools.runtime.java.container.Container;

import java.io.IOException;

import java.util.Properties;

/**
 * A {@link JavaApplicationBuilder} that realizes {@link JavaApplication}s as
 * external, non-child native operating system processes.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NativeJavaApplicationBuilder<A extends JavaApplication<A>, S extends JavaApplicationSchema<A, S>>
    extends AbstractJavaApplicationBuilder<A, S> implements JavaApplicationBuilder<A, S>
{
    /**
     * Should processes be started in remote debug mode?
     */
    private boolean m_isRemoteDebuggingEnabled;

    /**
     * Should remote debugging processes be started in suspended mode?
     */
    private boolean m_isRemoteStartSuspended;


    /**
     * Constructs a {@link NativeJavaApplicationBuilder}.
     */
    public NativeJavaApplicationBuilder()
    {
        super();

        // don't start in remote debug mode
        m_isRemoteDebuggingEnabled = false;

        // don't suspend when in remote debug mode
        m_isRemoteStartSuspended = false;
    }


    /**
     * Sets if diagnostic information should be logged/output for {@link Application}s
     * produced by this builder.
     *
     * @param isDiagnosticsEnabled  should diagnostics be output
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public NativeJavaApplicationBuilder setDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        m_isDiagnosticsEnabled = isDiagnosticsEnabled;

        return this;
    }


    /**
     * Sets if remote Java debugging is enabled for the next {@link JavaApplication}
     * produced by this builder.
     *
     * @param isRemoteDebuggingEnabled <code>true</code> to enable remote debugging
     *
     * @return  the {@link NativeJavaApplicationBuilder} to allow fluent-method calls
     */
    public NativeJavaApplicationBuilder setRemoteDebuggingEnabled(boolean isRemoteDebuggingEnabled)
    {
        m_isRemoteDebuggingEnabled = isRemoteDebuggingEnabled;

        return this;
    }


    /**
     * Determines if the next {@link JavaApplication} should be started in
     * remote debugging mode.
     *
     * @return <code>true</code> if remote debugging is enabled
     */
    public boolean isRemoteDebuggingEnabled()
    {
        return m_isRemoteDebuggingEnabled;
    }


    /**
     * Sets if a remote debugging process should be started in a suspended mode.
     *
     * @param isRemoteStartSuspended  <code>true</code> to start in a suspended mode
     *
     * @return  the {@link NativeJavaApplicationBuilder} to allow fluent-method calls
     */
    public NativeJavaApplicationBuilder setRemoteDebuggingStartSuspended(boolean isRemoteStartSuspended)
    {
        m_isRemoteStartSuspended = isRemoteStartSuspended;

        return this;
    }


    /**
     * Determines if a remotely debugged application will be started in suspend
     * mode.
     *
     * @return <code>true</code> if started in suspend mode
     */
    public boolean isRemoteDebuggingStartSuspended()
    {
        return m_isRemoteStartSuspended;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public A realize(S                  schema,
                     String             applicationName,
                     ApplicationConsole console) throws IOException
    {
        // establish the ProcessBuilder that we'll use to construct the
        // underlying operating system Process
        ProcessBuilder builder = new ProcessBuilder(schema.getExecutableName());

        // set the working directory for the Process
        builder.directory(schema.getWorkingDirectory());

        // set the environment variables for the Process
        if (!schema.isEnvironmentInherited())
        {
            // when we're not inheriting from the current environment we must
            // start with a clean environment
            builder.environment().clear();
        }

        // realize the environment variables from the provided Schema
        Properties environmentVariables = schema.getEnvironmentVariablesBuilder().realize();

        for (String variableName : environmentVariables.stringPropertyNames())
        {
            builder.environment().put(variableName, environmentVariables.getProperty(variableName));
        }

        // set the class path (it's an environment variable)
        String classPath = schema.getClassPath().toString();

        builder.environment().put("CLASSPATH", classPath);

        // set the System Properties for the Process
        Properties systemProperties = schema.getSystemPropertiesBuilder().realize();

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            String propertyValue = systemProperties.getProperty(propertyName);

            builder.command().add("-D" + propertyName + (propertyValue.isEmpty() ? "" : "=" + propertyValue));
        }

        // set the JVM options for the Process
        for (String option : schema.getJVMOptions())
        {
            builder.command().add("-" + option);
        }

        // add debug option
        if (m_isRemoteDebuggingEnabled)
        {
            // determine a free debug port
            int debugPort = Container.getAvailablePorts().next();

            // construct the Java option
            String option = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=%s,address=%d",
                                          (m_isRemoteStartSuspended ? "y" : "n"),
                                          debugPort);

            builder.command().add(option);
        }

        // set the Java application class name we'll be running
        builder.command().add(schema.getApplicationClassName());

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            builder.command().add(argument);
        }

        // should the standard error be redirected to the standard out?
        builder.redirectErrorStream(schema.isErrorStreamRedirected());

        // create and start the process
        Process process = builder.start();

        // establish a NativeJavaProcess to represent the underlying Process
        NativeJavaProcess nativeJavaProcess = new NativeJavaProcess(process);

        // delegate Application creation to the Schema
        final A application = schema.createJavaApplication(nativeJavaProcess,
                                                           applicationName,
                                                           console,
                                                           environmentVariables,
                                                           systemProperties);

        // let interceptors know that the application has been realized
        raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

        return application;
    }


    /**
     * A {@link com.oracle.tools.runtime.NativeApplicationProcess} specifically
     * for Java-based applications.
     *
     * @author Brian Oliver
     */
    public static class NativeJavaProcess extends NativeApplicationProcess implements JavaProcess
    {
        /**
         * Constructs a NativeJavaProcess.
         *
         * @param process  the underlying operating system {@link Process}
         */
        public NativeJavaProcess(Process process)
        {
            super(process);
        }
    }
}
