/*
 * File: LocalJavaApplicationBuilder.java
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

import com.oracle.tools.deferred.AbstractDeferred;
import com.oracle.tools.deferred.DeferredHelper;
import com.oracle.tools.deferred.InstanceUnavailableException;
import com.oracle.tools.deferred.UnresolvableInstanceException;

import com.oracle.tools.io.NetworkHelper;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LocalApplicationProcess;
import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;

import com.oracle.tools.runtime.java.container.Container;

import com.oracle.tools.util.CompletionListener;
import com.oracle.tools.util.Predicate;

import java.io.IOException;

import java.net.InetAddress;

import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link JavaApplicationBuilder} that realizes {@link JavaApplication}s as
 * external, non-child local operating system processes.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalJavaApplicationBuilder<A extends JavaApplication<A>, S extends JavaApplicationSchema<A, S>>
    extends AbstractJavaApplicationBuilder<A, S>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(LocalJavaApplicationBuilder.class.getName());

    /**
     * The path for the JAVA_HOME.
     * <p>
     * This is <code>null</code> if the JAVA_HOME of the {@link JavaApplicationSchema}
     * should be used.
     */
    private String javaHome;

    /**
     * Should processes be started in remote debug mode?
     * <p/>
     * The default is <code>false</code>.
     */
    private boolean isRemoteDebuggingEnabled;

    /**
     * Should remote debugging processes be started in suspended mode?
     * <p/>
     * The default is <code>false</code>.
     */
    private boolean isRemoteStartSuspended;

    /**
     * Should {@link JavaApplication}s produced by this builder allowed
     * to become orphans (when their parent application process is destroyed/killed)?
     * <p/>
     * The default is <code>false</code>.
     */
    private boolean areOrphansPermitted;


    /**
     * Constructs a {@link LocalJavaApplicationBuilder}.
     */
    public LocalJavaApplicationBuilder()
    {
        super();

        // use the JAVA HOME of the {@link JavaApplicationSchema}
        javaHome = null;

        // don't start in remote debug mode
        isRemoteDebuggingEnabled = false;

        // don't suspend when in remote debug mode
        isRemoteStartSuspended = false;

        // don't permit orphaned applications
        areOrphansPermitted = false;
    }


    /**
     * Sets if diagnostic information should be logged/output for {@link Application}s
     * produced by this builder.
     *
     * @param isDiagnosticsEnabled  should diagnostics be output
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public LocalJavaApplicationBuilder setDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        this.m_isDiagnosticsEnabled = isDiagnosticsEnabled;

        return this;
    }


    /**
     * Sets the JAVA_HOME to be used to realize the {@link JavaApplication}.
     *
     * @param javaHome  the value for the JAVA_HOME environment variable
     *                  or <code>null</code> if {@link JavaApplicationSchema}
     *                  value should be used instead
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public LocalJavaApplicationBuilder setJavaHome(String javaHome)
    {
        this.javaHome = javaHome;

        return this;
    }


    /**
     * Sets if remote Java debugging is enabled for the next {@link JavaApplication}
     * produced by this builder.
     *
     * @param isRemoteDebuggingEnabled <code>true</code> to enable remote debugging
     *
     * @return  the {@link LocalJavaApplicationBuilder} to allow fluent-method calls
     */
    public LocalJavaApplicationBuilder setRemoteDebuggingEnabled(boolean isRemoteDebuggingEnabled)
    {
        this.isRemoteDebuggingEnabled = isRemoteDebuggingEnabled;

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
        return isRemoteDebuggingEnabled;
    }


    /**
     * Sets if a remote debugging process should be started in a suspended mode.
     *
     * @param isRemoteStartSuspended  <code>true</code> to start in a suspended mode
     *
     * @return  the {@link LocalJavaApplicationBuilder} to allow fluent-method calls
     */
    public LocalJavaApplicationBuilder setRemoteDebuggingStartSuspended(boolean isRemoteStartSuspended)
    {
        this.isRemoteStartSuspended = isRemoteStartSuspended;

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
        return isRemoteStartSuspended;
    }


    /**
     * Sets if {@link JavaApplication}s produced by this {@link JavaApplicationBuilder}
     * can be orphaned (left running without their parent running).  The default
     * is <code>false</code>.
     *
     * @param areOrphansPermitted  <code>true</code> to allow for orphaned applications
     *
     * @return  the {@link LocalJavaApplicationBuilder} to allow fluent-method calls
     */
    public LocalJavaApplicationBuilder setOrphansPermitted(boolean areOrphansPermitted)
    {
        this.areOrphansPermitted = areOrphansPermitted;

        return this;
    }


    /**
     * Determines if {@link JavaApplication}s produced by this {@link JavaApplicationBuilder}
     * are allowed to be orphaned (to keep running if their parent is not running).
     *
     * @return  <code>true</code> if applications can be orphaned, <code>false</code> otherwise
     */
    public boolean areOrphansPermitted()
    {
        return areOrphansPermitted;
    }


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

        // set the JAVA_HOME
        String javaHome = this.javaHome == null ? schema.getJavaHome() : this.javaHome;

        if (javaHome != null)
        {
            builder.environment().put("JAVA_HOME", javaHome);
        }

        // set the class path (it's an environment variable)
        String classPath = schema.getClassPath().toString();

        builder.environment().put("CLASSPATH", classPath);

        // set the System Properties for the Process
        Properties systemProperties = schema.getSystemPropertiesBuilder().realize();

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            String propertyValue = systemProperties.getProperty(propertyName);

            // filter out (don't set) system properties that start with "oracletools"
            // (we don't want to have "parents" applications effect child applications
            if (!propertyName.startsWith("oracletools"))
            {
                builder.command().add("-D" + propertyName
                                      + (propertyValue.isEmpty()
                                         ? "" : "=" + StringHelper.doubleQuoteIfNecessary(propertyValue)));
            }
        }

        // configure a server channel to communicate with the native process
        final RemoteExecutorServer server = new RemoteExecutorServer();

        server.open();

        // add Oracle Tools specific System Properties
        Predicate<InetAddress> preferred = schema.isIPv4Preferred()
                                           ? new Predicate.All<InetAddress>(NetworkHelper.IPv4_ADDRESS,
                                                                            NetworkHelper
                                                                                .NON_LOOPBACK_ADDRESS) : NetworkHelper
                                                                                    .DEFAULT_ADDRESS;

        builder.command().add("-D" + Settings.PARENT_ADDRESS + "=" + server.getInetAddress(preferred).getHostAddress());
        builder.command().add("-D" + Settings.PARENT_PORT + "=" + server.getPort());
        builder.command().add("-D" + Settings.ORPHANABLE + "=" + areOrphansPermitted());

        // set the JVM options for the Process
        for (String option : schema.getJVMOptions())
        {
            builder.command().add("-" + option);
        }

        // add debug option
        if (this.isRemoteDebuggingEnabled)
        {
            // determine a free debug port
            int debugPort = Container.getAvailablePorts().next();

            // construct the Java option
            String option = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=%s,address=%d",
                                          (isRemoteStartSuspended ? "y" : "n"),
                                          debugPort);

            builder.command().add(option);
        }

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        builder.command().add("com.oracle.tools.runtime.java.JavaApplicationLauncher");

        // set the Java application class name we need to launch
        builder.command().add(schema.getApplicationClassName());

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            builder.command().add(argument);
        }

        // should the standard error be redirected to the standard out?
        builder.redirectErrorStream(schema.isErrorStreamRedirected());

        if (LOGGER.isLoggable(Level.INFO))
        {
            StringBuilder commandBuilder = new StringBuilder();

            for (String command : builder.command())
            {
                commandBuilder.append(command);
                commandBuilder.append(" ");
            }

            LOGGER.log(Level.INFO, "Starting Local Process: " + commandBuilder.toString());
        }

        // create and start the process
        Process process = builder.start();

        // establish a LocalJavaProcess to represent the underlying Process
        LocalJavaProcess localJavaProcess = new LocalJavaProcess(process, server);

        // delegate Application creation to the Schema
        final A application = schema.createJavaApplication(localJavaProcess,
                                                           applicationName,
                                                           console,
                                                           environmentVariables,
                                                           systemProperties);

        // ensure that the process connects back
        DeferredHelper.ensure(new AbstractDeferred<Boolean>()
        {
            @Override
            public Boolean get() throws UnresolvableInstanceException, InstanceUnavailableException
            {
                if (!server.getRemoteExecutors().iterator().hasNext())
                {
                    throw new InstanceUnavailableException(this);
                }
                else
                {
                    return true;
                }
            }
        });

        // let interceptors know that the application has been realized
        raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

        return application;
    }


    /**
     * A {@link LocalApplicationProcess} specifically for Java-based applications.
     */
    public static class LocalJavaProcess extends LocalApplicationProcess implements JavaProcess
    {
        /**
         * The {@link RemoteExecutor} for the {@link LocalJavaProcess}.
         */
        private ControllableRemoteExecutor remoteExecutor;


        /**
         * Constructs a {@link LocalJavaProcess}.
         *
         * @param process         the underlying operating system {@link Process}
         * @param remoteExecutor  the {@link ControllableRemoteExecutor} that may be used
         *                        to submit and control the process remotely
         */
        public LocalJavaProcess(Process                    process,
                                ControllableRemoteExecutor remoteExecutor)
        {
            super(process);

            this.remoteExecutor = remoteExecutor;
        }


        @Override
        public <T> void submit(RemoteCallable<T>     callable,
                               CompletionListener<T> listener)
        {
            remoteExecutor.submit(callable, listener);
        }


        @Override
        public void submit(RemoteRunnable runnable) throws IllegalStateException
        {
            remoteExecutor.submit(runnable);
        }


        @Override
        public void close()
        {
            super.close();

            remoteExecutor.close();
        }
    }
}
