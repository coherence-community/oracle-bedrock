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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.deferred.AbstractDeferred;
import com.oracle.tools.deferred.PermanentlyUnavailableException;
import com.oracle.tools.deferred.TemporarilyUnavailableException;

import com.oracle.tools.io.NetworkHelper;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.predicate.Predicate;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.LocalApplicationProcess;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;

import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.options.JvmOption;
import com.oracle.tools.runtime.java.options.RemoteDebugging;

import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.Orphanable;

import com.oracle.tools.util.CompletionListener;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.within;

import static com.oracle.tools.predicate.Predicates.allOf;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;

import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link JavaApplicationBuilder} that realizes {@link JavaApplication}s as
 * external, non-child local operating system processes, by default using the
 * environment variables of the current system process.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalJavaApplicationBuilder<A extends JavaApplication> extends AbstractJavaApplicationBuilder<A>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(LocalJavaApplicationBuilder.class.getName());


    /**
     * Constructs a {@link LocalJavaApplicationBuilder}.
     */
    public LocalJavaApplicationBuilder()
    {
        super();
    }


    @Override
    public <T extends A, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console,
                                                                   Platform           platform,
                                                                   Option...          applicationOptions)
    {
        // add all of the default application schema options
        Options options = new Options(applicationSchema.getOptions().asArray());

        // add all of the custom application options
        options.addAll(applicationOptions);

        // TODO: this should be a safe cast but we should also check to make sure
        JavaApplicationSchema<T> schema = (JavaApplicationSchema) applicationSchema;

        // ---- establish the underlying ProcessBuilder -----

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder = new ProcessBuilder(schema.getExecutableName());

        // ----- establish the working directory -----

        // set the working directory for the Process
        File directory = schema.getWorkingDirectory();

        if (directory != null)
        {
            processBuilder.directory(schema.getWorkingDirectory());
        }

        // ----- establish environment variables -----
        EnvironmentVariables environmentVariables = options.get(EnvironmentVariables.class,
                                                                EnvironmentVariables.inherited());

        // when not inheriting we need to clear the defined environment
        if (!environmentVariables.areInherited() &&!schema.isEnvironmentInherited())
        {
            processBuilder.environment().clear();
        }

        // add the environment variables defined by the schema
        Properties variables = schema.getEnvironmentVariables(platform);

        for (String variableName : variables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, variables.getProperty(variableName));
        }

        // realize and add the optionally defined environment variables
        variables = environmentVariables.getBuilder().realize();

        for (String variableName : variables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, variables.getProperty(variableName));
        }

        // ----- establish java specific environment variables -----

        // by default we use the java home defined by the schema.  if that's not
        // defined we'll attempt to use the java home defined by this builder.
        JavaHome javaHome = options.get(JavaHome.class);

        // when we still don't have a java home we use what this process defines
        // (using the system property)
        if (javaHome == null)
        {
            javaHome = JavaHome.at(System.getProperty("java.home", null));
        }

        if (javaHome != null)
        {
            processBuilder.environment().put("JAVA_HOME", javaHome.get());
        }

        // set the class path (it's an environment variable)
        ClassPath classPath;

        try
        {
            classPath = new ClassPath(schema.getClassPath(), ClassPath.ofClass(JavaApplicationLauncher.class));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to locate required classes for the class path", e);
        }

        processBuilder.environment().put("CLASSPATH", classPath.toString());

        // ----- establish the command to start java -----

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            processBuilder.command(schema.getExecutableName());
        }
        else
        {
            // when we have a java home, we prefix the executable name with the java.home/bin/
            String javaHomePath = javaHome.get();

            javaHomePath = javaHomePath.trim();

            if (!javaHomePath.endsWith(File.separator))
            {
                javaHomePath = javaHomePath + File.separator;
            }

            processBuilder.command(javaHomePath + "bin" + File.separator + schema.getExecutableName());
        }

        // ----- establish the system properties for the java application -----

        // define the system properties based on those defined by the schema
        Properties systemProperties = schema.getSystemProperties(platform);

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            String propertyValue = systemProperties.getProperty(propertyName);

            // filter out (don't set) system properties that start with "oracletools"
            // (we don't want to have "parents" applications effect child applications
            if (!propertyName.startsWith("oracletools"))
            {
                processBuilder.command().add("-D" + propertyName
                                             + (propertyValue.isEmpty()
                                                ? "" : "=" + StringHelper.doubleQuoteIfNecessary(propertyValue)));
            }
        }

        // ----- establish Oracle Tools specific system properties -----

        // configure a server channel to communicate with the native process
        final RemoteExecutorServer server = new RemoteExecutorServer();

        try
        {
            server.open();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create remote execution server for the application", e);
        }

        // add Oracle Tools specific System Properties
        Predicate<InetAddress> preferred = schema.isIPv4Preferred() ? allOf(NetworkHelper.IPv4_ADDRESS,
                                                                            NetworkHelper
                                                                                .NON_LOOPBACK_ADDRESS) : NetworkHelper
                                                                                    .DEFAULT_ADDRESS;

        processBuilder.command().add("-D" + Settings.PARENT_ADDRESS + "="
                                     + server.getInetAddress(preferred).getHostAddress());
        processBuilder.command().add("-D" + Settings.PARENT_PORT + "=" + server.getPort());

        // add Orphanable configuration
        Orphanable orphanable = options.get(Orphanable.class, Orphanable.disabled());

        processBuilder.command().add("-D" + Settings.ORPHANABLE + "=" + orphanable.isOrphanable());

        // ----- establish Java Virtual Machine options -----

        for (JvmOption option : options.getAll(JvmOption.class))
        {
            processBuilder.command().add(option.get());
        }

        // ----- establish remote debugging JVM options -----

        // TODO: also consider getting this from the schema (if it only had "default" options ;)
        RemoteDebugging remoteDebugging = options.get(RemoteDebugging.class, RemoteDebugging.autoDetect());

        int             debugPort       = -1;

        if (remoteDebugging.isEnabled())
        {
            debugPort = remoteDebugging.getBehavior() == RemoteDebugging.Behavior.LISTEN_FOR_DEBUGGER
                        ? remoteDebugging.getListenPort() : remoteDebugging.getAttachPort();

            if (debugPort <= 0)
            {
                debugPort = LocalPlatform.getInstance().getAvailablePorts().next();
            }

            boolean isDebugServer = remoteDebugging.getBehavior() == RemoteDebugging.Behavior.LISTEN_FOR_DEBUGGER;

            // construct the Java option
            String option = String.format("-agentlib:jdwp=transport=dt_socket,server=%s,suspend=%s,address=%s:%d",
                                          (isDebugServer ? "y" : "n"),
                                          (remoteDebugging.isStartSuspended() ? "y" : "n"),
                                          LocalPlatform.getInstance().getHostName(),
                                          debugPort);

            processBuilder.command().add(option);
        }

        // ----- establish the application command line to execute -----

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        processBuilder.command().add("com.oracle.tools.runtime.java.JavaApplicationLauncher");

        // set the Java application class name we need to launch
        processBuilder.command().add(schema.getApplicationClassName());

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            processBuilder.command().add(argument);
        }

        // should the standard error be redirected to the standard out?
        processBuilder.redirectErrorStream(schema.isErrorStreamRedirected());

        // ----- start the local process -----

        if (LOGGER.isLoggable(Level.INFO))
        {
            StringBuilder commandBuilder = new StringBuilder();

            for (String command : processBuilder.command())
            {
                commandBuilder.append(command);
                commandBuilder.append(" ");
            }

            LOGGER.log(Level.INFO, "Starting Local Process: " + commandBuilder.toString());
        }

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

        // ----- create the local process and application -----

        // establish a LocalJavaProcess to represent the underlying Process
        LocalJavaApplicationProcess localJavaProcess = new LocalJavaApplicationProcess(process, server);

        // delegate Application creation to the Schema
        final T application = schema.createJavaApplication(localJavaProcess,
                                                           applicationName,
                                                           platform,
                                                           options,
                                                           console,
                                                           variables,
                                                           systemProperties,
                                                           debugPort);

        // ensure that the launcher process connects back to the server to
        // know that the application has started
        if (!(remoteDebugging.isEnabled() && remoteDebugging.isStartSuspended()))
        {
            Timeout timeout = options.get(Timeout.class, Timeout.autoDetect());

            ensure(new AbstractDeferred<Boolean>()
            {
                @Override
                public Boolean get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
                {
                    if (!server.getRemoteExecutors().iterator().hasNext())
                    {
                        throw new TemporarilyUnavailableException(this);
                    }
                    else
                    {
                        return true;
                    }
                }
            }, within(timeout));
        }

        // ----- notify all of the lifecycle listeners -----

        // let interceptors know that the application has been realized
        raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

        return application;
    }


    /**
     * A {@link LocalApplicationProcess} specifically for Java-based applications.
     */
    public static class LocalJavaApplicationProcess extends LocalApplicationProcess implements JavaApplicationProcess
    {
        /**
         * The {@link RemoteExecutor} for the {@link LocalJavaApplicationProcess}.
         */
        private ControllableRemoteExecutor remoteExecutor;


        /**
         * Constructs a {@link LocalJavaApplicationProcess}.
         *
         * @param process         the underlying operating system {@link Process}
         * @param remoteExecutor  the {@link ControllableRemoteExecutor} that may be used
         *                        to submit and control the process remotely
         */
        public LocalJavaApplicationProcess(Process                    process,
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
