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

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.options.Timeout;
import com.oracle.tools.options.Variable;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.LocalApplicationProcess;
import com.oracle.tools.runtime.LocalPlatform;
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
import com.oracle.tools.runtime.options.ErrorStreamRedirection;
import com.oracle.tools.runtime.options.Orphanable;

import com.oracle.tools.table.Cell;
import com.oracle.tools.table.Table;
import com.oracle.tools.table.Tabularize;

import com.oracle.tools.util.CompletionListener;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.within;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

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
public class LocalJavaApplicationBuilder<A extends JavaApplication>
    extends AbstractJavaApplicationBuilder<A, LocalPlatform>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(LocalJavaApplicationBuilder.class.getName());


    /**
     * Constructs a {@link LocalJavaApplicationBuilder} for the specified
     * {@link LocalPlatform}.
     *
     * @param platform  the {@link LocalPlatform}
     */
    public LocalJavaApplicationBuilder(LocalPlatform platform)
    {
        super(platform);
    }


    @Override
    public <T extends A, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console,
                                                                   Option...          applicationOptions)
    {
        // TODO: this should be a safe cast but we should also check to make sure
        JavaApplicationSchema<T> schema = (JavaApplicationSchema) applicationSchema;

        // establish the diagnostics output table
        Table diagnosticsTable = new Table();

        diagnosticsTable.getOptions().add(Table.orderByColumn(0));

        if (platform != null)
        {
            diagnosticsTable.addRow("Target Platform", platform.getName());
        }

        // ----- establish the Options for the Application -----

        // add the platform options
        Options options = new Options(platform == null ? null : platform.getOptions().asArray());

        // add the schema options
        options.addAll(applicationSchema.getOptions().asArray());

        // add the schema options (based on the platform)
        options.addAll(applicationSchema.getPlatformSpecificOptions(platform).asArray());

        // add the custom application options
        options.addAll(applicationOptions);

        // ----- establish an identity for the application -----

        // add a unique runtime id for expression support
        options.add(Variable.with("oracletools.runtime.id", UUID.randomUUID()));

        // ----- establish the underlying ProcessBuilder -----

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder = new ProcessBuilder(schema.getExecutableName());

        // ----- establish the working directory -----

        // set the working directory for the Process
        File directory = schema.getWorkingDirectory();

        if (directory != null)
        {
            processBuilder.directory(directory);

            diagnosticsTable.addRow("Working Directory", directory.toString());
        }

        // ----- establish environment variables -----

        EnvironmentVariables environmentVariables = options.get(EnvironmentVariables.class);

        switch (environmentVariables.getSource())
        {
        case Custom :
            processBuilder.environment().clear();

            diagnosticsTable.addRow("Environment Variables", "(cleared)");
            break;

        case ThisApplication :

            Map<String, String> map = System.getenv();

            processBuilder.environment().clear();
            processBuilder.environment().putAll(map);

            diagnosticsTable.addRow("Environment Variables", "(based on parent process)");
            break;

        case TargetPlatform :

            diagnosticsTable.addRow("Environment Variables", "(based on platform defaults)");
            break;
        }

        // add the optionally defined environment variables
        Properties variables = environmentVariables.realize(platform, schema, options.asArray());

        for (String variableName : variables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, variables.getProperty(variableName));
        }

        if (variables.size() > 0)
        {
            Table table = Tabularize.tabularize(variables);

            diagnosticsTable.addRow("", table.toString());
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
            processBuilder.environment().put("JAVA_HOME", StringHelper.doubleQuoteIfNecessary(javaHome.get()));
        }

        // ----- establish the command to start java -----

        String executable;

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            executable = StringHelper.doubleQuoteIfNecessary(schema.getExecutableName());
        }
        else
        {
            // when we have a java home, we prefix the executable name with the java.home/bin/
            String javaHomePath = javaHome.get();

            javaHomePath = javaHomePath.trim();

            diagnosticsTable.addRow("Java Home", javaHomePath);

            if (!javaHomePath.endsWith(File.separator))
            {
                javaHomePath = javaHomePath + File.separator;
            }

            executable = StringHelper.doubleQuoteIfNecessary(javaHomePath + "bin" + File.separator
                                                             + schema.getExecutableName());

        }

        processBuilder.command(executable);

        diagnosticsTable.addRow("Java Executable", executable);

        // ----- establish the class path -----

        // set the class path
        ClassPath classPath;

        try
        {
            classPath = new ClassPath(schema.getClassPath(), ClassPath.ofClass(JavaApplicationLauncher.class));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to locate required classes for the class path", e);
        }

        processBuilder.command().add("-cp");
        processBuilder.command().add(classPath.toString(options.asArray()));

        Table classPathTable = classPath.getTable();

        classPathTable.getOptions().add(Cell.Separator.of(""));
        diagnosticsTable.addRow("Class Path", classPathTable.toString());

        // ----- establish Oracle Tools specific system properties -----

        // configure a server channel to communicate with the native process
        final RemoteExecutorServer server = new RemoteExecutorServer();

        try
        {
            // NOTE: this listens on the wildcard address on an ephemeral port
            server.open();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create remote execution server for the application", e);
        }

        // add Oracle Tools specific System Properties

        // NOTE: the Oracle Tools parent address for locally created applications is always "loopback" as
        // i). they are always running locally,
        // ii). they only need to connect locally, and
        // iii). the "loopback" interface should work regardless of the network we're on.
        InetAddress parentAddress;

        if (schema.isIPv4Preferred())
        {
            // we have to provide the schema with an IPv4 address!
            try
            {
                parentAddress = InetAddress.getByName("127.0.0.1");
            }
            catch (UnknownHostException e)
            {
                // TODO: log that we couldn't determine the loopback address!
                parentAddress = InetAddress.getLoopbackAddress();
            }
        }
        else
        {
            // when the schema doesn't care, we can default to what this platform chooses
            parentAddress = InetAddress.getLoopbackAddress();
        }

        Table systemPropertiesTable = new Table();

        systemPropertiesTable.getOptions().add(Table.orderByColumn(0));
        systemPropertiesTable.getOptions().add(Cell.Separator.of(""));

        // establish the URI for this (parent) process
        String parentURI = "//" + parentAddress.getHostAddress() + ":" + server.getPort();

        systemPropertiesTable.addRow(Settings.PARENT_URI, parentURI.toString());

        processBuilder.command().add("-D" + Settings.PARENT_URI + "=" + parentURI);

        // add Orphanable configuration
        Orphanable orphanable = options.get(Orphanable.class);

        processBuilder.command().add("-D" + Settings.ORPHANABLE + "=" + orphanable.isOrphanable());

        systemPropertiesTable.addRow(Settings.ORPHANABLE, Boolean.toString(orphanable.isOrphanable()));

        // ----- establish the system properties for the java application -----

        // define the system properties based on those defined by the schema
        Properties systemProperties = schema.getSystemProperties().realize(platform, schema, options.asArray());

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

                systemPropertiesTable.addRow(propertyName, propertyValue);
            }
        }

        diagnosticsTable.addRow("System Properties", systemPropertiesTable.toString());

        // ----- establish Java Virtual Machine options -----

        StringBuilder jvmOptions = new StringBuilder();

        for (JvmOption jvmOption : options.getInstancesOf(JvmOption.class))
        {
            for (String value : jvmOption.getValues(options.asArray()))
            {
                processBuilder.command().add(value);

                if (jvmOptions.length() > 0)
                {
                    jvmOptions.append(" ");
                }

                jvmOptions.append(value);
            }
        }

        if (jvmOptions.length() > 0)
        {
            diagnosticsTable.addRow("Java Options", jvmOptions.toString());
        }

        // ----- establish remote debugging JVM options -----

        RemoteDebugging remoteDebugging = options.get(RemoteDebugging.class);

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
            String option = String.format("-agentlib:jdwp=transport=dt_socket,server=%s,suspend=%s,address=%d",
                                          (isDebugServer ? "y" : "n"),
                                          (remoteDebugging.isStartSuspended() ? "y" : "n"),
                                          debugPort);

            processBuilder.command().add(option);

            diagnosticsTable.addRow("Remote Debugging Options", option);
        }

        // ----- establish the application command line to execute -----

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        String applicationLauncherClassName = "com.oracle.tools.runtime.java.JavaApplicationLauncher";

        processBuilder.command().add(applicationLauncherClassName);

        // set the Java application class name we need to launch
        String applicationClassName = schema.getApplicationClassName();

        processBuilder.command().add(applicationClassName);

        diagnosticsTable.addRow("Application Launcher", applicationLauncherClassName);
        diagnosticsTable.addRow("Application Class", applicationClassName);
        diagnosticsTable.addRow("Application", applicationName);

        // add the arguments to the command for the process
        String arguments = "";

        for (String argument : schema.getArguments())
        {
            processBuilder.command().add(argument);

            arguments += argument + " ";
        }

        if (arguments.length() > 0)
        {
            diagnosticsTable.addRow("Application Arguments", arguments);
        }

        // should the standard error be redirected to the standard out?
        ErrorStreamRedirection redirection = options.get(ErrorStreamRedirection.class);

        processBuilder.redirectErrorStream(redirection.isEnabled());

        diagnosticsTable.addRow("Standard Error Device",
                                redirection.isEnabled() ? "stdout" : "stderr");

        diagnosticsTable.addRow("Application Launch Time",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // ----- start the local process -----

        if (LOGGER.isLoggable(Level.INFO))
        {
            LOGGER.log(Level.INFO,
                       "Oracle Tools Diagnostics: Starting Application...\n"
                       + "------------------------------------------------------------------------\n"
                       + diagnosticsTable.toString() + "\n"
                       + "------------------------------------------------------------------------\n");
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
            Timeout timeout = options.get(Timeout.class);

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
                   },within(timeout));
        }

        // ----- notify all of the application listeners -----

        raiseOnRealizedFor(application);

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
