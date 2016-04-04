/*
 * File: LocalJavaApplicationLauncher.java
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

import com.oracle.tools.lang.ExpressionEvaluator;
import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.options.Timeout;
import com.oracle.tools.options.Variable;
import com.oracle.tools.options.Variables;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationListener;
import com.oracle.tools.runtime.LocalApplicationProcess;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;
import com.oracle.tools.runtime.Profiles;
import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.ControllableRemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteEvent;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteChannelServer;

import com.oracle.tools.runtime.java.features.JmxFeature;
import com.oracle.tools.runtime.java.options.ClassName;
import com.oracle.tools.runtime.java.options.IPv4Preferred;
import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.options.JvmOption;
import com.oracle.tools.runtime.java.options.RemoteEvents;
import com.oracle.tools.runtime.java.options.SystemProperties;
import com.oracle.tools.runtime.java.options.WaitToStart;
import com.oracle.tools.runtime.java.profiles.CommercialFeatures;
import com.oracle.tools.runtime.java.profiles.RemoteDebugging;

import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.ErrorStreamRedirection;
import com.oracle.tools.runtime.options.Executable;
import com.oracle.tools.runtime.options.MetaClass;
import com.oracle.tools.runtime.options.Orphanable;
import com.oracle.tools.runtime.options.WorkingDirectory;

import com.oracle.tools.table.Cell;
import com.oracle.tools.table.Table;
import com.oracle.tools.table.Tabularize;

import com.oracle.tools.util.CompletionListener;
import com.oracle.tools.util.ReflectionHelper;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.within;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Constructor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link JavaApplicationLauncher} that launches a {@link JavaApplication}s as
 * external, non-child local operating system processes, by default using the
 * environment variables of the current system process.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalJavaApplicationLauncher<A extends JavaApplication>
    extends AbstractJavaApplicationLauncher<A, LocalPlatform>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(LocalJavaApplicationLauncher.class.getName());


    /**
     * Constructs a {@link LocalJavaApplicationLauncher}.
     *
     * @param platform the {@link Platform} on which an {@link Application} will be launched
     */
    public LocalJavaApplicationLauncher(LocalPlatform platform)
    {
        super(platform);
    }


    @Override
    public A launch(Options options)
    {
        // establish the diagnostics output table
        Table diagnosticsTable = new Table();

        diagnosticsTable.getOptions().add(Table.orderByColumn(0));

        if (platform != null)
        {
            diagnosticsTable.addRow("Target Platform", platform.getName());
        }

        // ----- determine the meta-class for our application -----

        // establish the options for resolving the meta-class
        Options metaOptions = new Options(platform.getOptions()).addAll(options);

        // determine the meta-class
        MetaClass metaClass = metaOptions.getOrDefault(MetaClass.class, new JavaApplication.MetaClass());

        // ----- establish the launch Options for the Application -----

        // add the platform options
        Options launchOptions = new Options(platform.getOptions().asArray());

        // add the meta-class options
        metaClass.onBeforeLaunch(platform, launchOptions);

        // add the launch specific options
        launchOptions.addAll(options);

        // ----- establish an identity for the application -----

        // add a unique runtime id for expression support
        launchOptions.add(Variable.with("oracletools.runtime.id", UUID.randomUUID()));

        // ----- establish default Profiles for this Platform (and Builder) -----

        // java applications can automatically detect the following profiles
        launchOptions.get(RemoteDebugging.class);
        launchOptions.get(CommercialFeatures.class);

        // auto-detect and add externally defined profiles
        launchOptions.addAll(Profiles.getProfiles());

        // ----- notify the Profiles that the application is about to be launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onBeforeLaunch(platform, launchOptions);
        }

        // ----- determine the display name for the application -----

        DisplayName displayName = getDisplayName(launchOptions);

        // ----- establish the underlying ProcessBuilder -----

        // determine the Executable, defaulting to "java" if not defined
        Executable executable = launchOptions.getOrDefault(Executable.class, Executable.named("java"));

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder = new ProcessBuilder(executable.getName());

        // ----- establish the working directory -----

        // set the working directory for the Process
        WorkingDirectory workingDirectory = launchOptions.getOrDefault(WorkingDirectory.class,
                                                                       WorkingDirectory.currentDirectory());

        File directory = workingDirectory.resolve(platform, launchOptions);

        // set the resolved working directory back into the options
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

        // ----- establish java specific environment variables -----

        // by default we use the java home defined by the schema.  if that's not
        // defined we'll attempt to use the java home defined by this builder.
        JavaHome javaHome = launchOptions.get(JavaHome.class);

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

        String javaExecutable;

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            javaExecutable = StringHelper.doubleQuoteIfNecessary(executable.getName());
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

            javaExecutable = StringHelper.doubleQuoteIfNecessary(javaHomePath + "bin" + File.separator
                                                                 + executable.getName());

        }

        processBuilder.command(javaExecutable);

        diagnosticsTable.addRow("Java Executable", javaExecutable);

        // ----- establish the class path -----

        // determine the predefined class path based on the launch options
        ClassPath classPath = launchOptions.get(ClassPath.class);

        try
        {
            // include the JavaApplicationLauncher at the end of the class path
            classPath = new ClassPath(classPath, ClassPath.ofClass(JavaApplicationRunner.class));

            // add the updated ClassPath back into the launch options
            launchOptions.add(classPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to locate required classes for the class path", e);
        }

        processBuilder.command().add("-cp");
        processBuilder.command().add(classPath.toString(launchOptions.asArray()));

        Table classPathTable = classPath.getTable();

        classPathTable.getOptions().add(Cell.Separator.of(""));
        diagnosticsTable.addRow("Class Path", classPathTable.toString());

        // ----- establish Oracle Tools specific system properties -----

        // configure a server channel to communicate with the native process
        final SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer();

        // register the defined RemoteEventListeners with the server so that when the application starts
        // the listeners can immediately start receiving RemoteEvents
        RemoteEvents remoteEvents = launchOptions.get(RemoteEvents.class);

        remoteEvents.forEach((remoteEventListener, listenerOptions) -> server.addListener(remoteEventListener,
                listenerOptions));

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
        InetAddress   parentAddress;

        IPv4Preferred iPv4Preferred = launchOptions.get(IPv4Preferred.class);

        if (iPv4Preferred.isPreferred())
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
        Orphanable orphanable = launchOptions.get(Orphanable.class);

        processBuilder.command().add("-D" + Settings.ORPHANABLE + "=" + orphanable.isOrphanable());

        systemPropertiesTable.addRow(Settings.ORPHANABLE, Boolean.toString(orphanable.isOrphanable()));

        // ----- establish the system properties for the java application -----

        // define the system properties based on those defined by the launch options
        Properties systemProperties = launchOptions.get(SystemProperties.class).resolve(platform, launchOptions);

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            String propertyValue = systemProperties.getProperty(propertyName);

            // filter out (don't set) system properties that start with "oracletools", unless it's a profile
            // (we don't want to have "parents" applications effect child applications
            if (propertyName.startsWith("oracletools.profile.") ||!propertyName.startsWith("oracletools"))
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

        for (JvmOption jvmOption : launchOptions.getInstancesOf(JvmOption.class))
        {
            for (String value : jvmOption.resolve(launchOptions))
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

        // ----- add oracletools.runtime.inherit.xxx values to the command -----

        for (String propertyName : System.getProperties().stringPropertyNames())
        {
            if (propertyName.startsWith("oracletools.runtime.inherit."))
            {
                // resolve the property value
                String propertyValue = System.getProperty(propertyName);

                // evaluate any expressions in the property value
                ExpressionEvaluator evaluator = new ExpressionEvaluator(launchOptions.get(Variables.class));

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                processBuilder.command().add(propertyValue);
            }
        }

        // ----- establish the application command line to execute -----

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        String applicationLauncherClassName = JavaApplicationRunner.class.getName();

        processBuilder.command().add(applicationLauncherClassName);

        // set the Java application class name we need to launch
        ClassName className = launchOptions.get(ClassName.class);

        if (className == null)
        {
            throw new IllegalArgumentException("Java Application ClassName not specified");
        }

        String applicationClassName = className.getName();

        processBuilder.command().add(applicationClassName);

        diagnosticsTable.addRow("Application Launcher", applicationLauncherClassName);
        diagnosticsTable.addRow("Application Class", applicationClassName);
        diagnosticsTable.addRow("Application", displayName.resolve(launchOptions));

        // ----- included the java arguments to the command -----

        List<String> argList = launchOptions.get(Arguments.class).resolve(platform, launchOptions);

        // Set the actual arguments used back into the options
        launchOptions.add(Arguments.of(argList));

        String arguments = "";

        for (String argument : argList)
        {
            processBuilder.command().add(argument);

            arguments += argument + " ";
        }

        if (arguments.length() > 0)
        {
            diagnosticsTable.addRow("Application Arguments", arguments);
        }

        // should the standard error be redirected to the standard out?
        ErrorStreamRedirection redirection = launchOptions.get(ErrorStreamRedirection.class);

        processBuilder.redirectErrorStream(redirection.isEnabled());

        diagnosticsTable.addRow("Standard Error Device",
                                redirection.isEnabled() ? "stdout" : "stderr");

        diagnosticsTable.addRow("Application Launch Time",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // TODO: add all of the Profile options to the table

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
        LocalJavaApplicationProcess localJavaProcess = new LocalJavaApplicationProcess(process,
                                                                                       server,
                                                                                       systemProperties);

        // determine the application class that will represent the running application
        Class<? extends A> applicationClass = metaClass.getImplementationClass(platform, launchOptions);

        A                  application;

        try
        {
            // attempt to find a constructor(Platform, JavaApplicationProcess, Options)
            Constructor<? extends A> constructor = ReflectionHelper.getCompatibleConstructor(applicationClass,
                                                                                             platform.getClass(),
                                                                                             localJavaProcess.getClass(),
                                                                                             launchOptions.getClass());

            // create the application
            application = constructor.newInstance(platform, localJavaProcess, launchOptions);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to instantiate the Application class specified by the MetaClass:"
                                       + metaClass,
                                       e);
        }

        // ----- enhance the application with java-specific features -----

        if (JmxFeature.isSupportedBy(application))
        {
            application.add(new JmxFeature());
        }

        // ----- wait for the application to start -----

        // ensure that the launcher process connects back to the server to
        // know that the application has started
        WaitToStart waitToStart = launchOptions.get(WaitToStart.class);

        if (waitToStart.isEnabled())
        {
            Timeout timeout = launchOptions.get(Timeout.class);

            ensure(new AbstractDeferred<Boolean>()
                   {
                       @Override
                       public Boolean get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
                       {
                           if (!server.getRemoteChannels().iterator().hasNext())
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

        // ----- notify the MetaClass that the application has been launched -----

        metaClass.onAfterLaunch(platform, application, launchOptions);

        // ----- notify the Profiles that the application has been launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onAfterLaunch(platform, application, launchOptions);
        }

        // ----- notify all of the application listeners -----

        // notify the ApplicationListener-based Options that the application has been launched
        for (ApplicationListener listener : launchOptions.getInstancesOf(ApplicationListener.class))
        {
            listener.onRealized(application);
        }

        return application;
    }


    /**
     * A {@link LocalApplicationProcess} specifically for Java-based applications.
     */
    public static class LocalJavaApplicationProcess extends LocalApplicationProcess implements JavaApplicationProcess
    {
        /**
         * The {@link RemoteChannel} for the {@link LocalJavaApplicationProcess}.
         */
        private ControllableRemoteChannel remoteExecutor;

        /**
         * The resolved System {@link Properties} provided to the {@link JavaApplicationProcess} when it was launched.
         */
        private Properties systemProperties;


        /**
         * Constructs a {@link LocalJavaApplicationProcess}.
         *
         * @param process          the underlying operating system {@link Process}
         * @param remoteExecutor   the {@link ControllableRemoteChannel} that may be used
         *                         to submit and control the process remotely
         * @param systemProperties the resolved System {@link Properties} provided to the {}
         */
        public LocalJavaApplicationProcess(Process                   process,
                                           ControllableRemoteChannel remoteExecutor,
                                           Properties                systemProperties)
        {
            super(process);

            this.remoteExecutor   = remoteExecutor;
            this.systemProperties = systemProperties;
        }


        @Override
        public Properties getSystemProperties()
        {
            return systemProperties;
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
        public void addListener(RemoteEventListener listener,
                                Option...           options)
        {
            remoteExecutor.addListener(listener, options);
        }


        @Override
        public void removeListener(RemoteEventListener listener,
                                   Option...           options)
        {
            remoteExecutor.removeListener(listener, options);
        }


        @Override
        public void raise(RemoteEvent event,
                          Option...   options)
        {
            remoteExecutor.raise(event, options);
        }


        @Override
        public void close()
        {
            super.close();

            remoteExecutor.close();
        }
    }
}
