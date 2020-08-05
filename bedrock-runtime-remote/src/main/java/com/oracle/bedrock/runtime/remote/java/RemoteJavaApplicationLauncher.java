/*
 * File: RemoteJavaApplicationLauncher.java
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

package com.oracle.bedrock.runtime.remote.java;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.deferred.AbstractDeferred;
import com.oracle.bedrock.deferred.PermanentlyUnavailableException;
import com.oracle.bedrock.deferred.TemporarilyUnavailableException;
import com.oracle.bedrock.lang.ExpressionEvaluator;
import com.oracle.bedrock.lang.StringHelper;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.ApplicationProcess;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Settings;
import com.oracle.bedrock.runtime.concurrent.ControllableRemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;
import com.oracle.bedrock.runtime.concurrent.socket.SocketBasedRemoteChannelServer;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.ClassPathModifier;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.JavaApplicationLauncher;
import com.oracle.bedrock.runtime.java.JavaApplicationProcess;
import com.oracle.bedrock.runtime.java.JavaApplicationRunner;
import com.oracle.bedrock.runtime.java.features.JmxFeature;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.JavaHome;
import com.oracle.bedrock.runtime.java.options.JvmOption;
import com.oracle.bedrock.runtime.java.options.RemoteEvents;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.WaitToStart;
import com.oracle.bedrock.runtime.java.profiles.CommercialFeatures;
import com.oracle.bedrock.runtime.java.profiles.RemoteDebugging;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.runtime.options.Orphanable;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.runtime.remote.AbstractRemoteApplicationLauncher;
import com.oracle.bedrock.runtime.remote.RemoteApplicationProcess;
import com.oracle.bedrock.runtime.remote.java.options.JavaDeployment;
import com.oracle.bedrock.runtime.remote.options.Deployment;
import com.oracle.bedrock.table.Cell;
import com.oracle.bedrock.table.Table;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static com.oracle.bedrock.deferred.DeferredHelper.ensure;
import static com.oracle.bedrock.deferred.DeferredHelper.within;

/**
 * A {@link JavaApplicationLauncher} that launches a {@link JavaApplication} on a {@link Platform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public class RemoteJavaApplicationLauncher extends AbstractRemoteApplicationLauncher<JavaApplication>
    implements JavaApplicationLauncher<JavaApplication>
{
    /**
     * The {@link ControllableRemoteChannel} that can be used to communicate with
     * the remote {@link JavaApplication}.
     */
    private SocketBasedRemoteChannelServer remoteChannel;

    /**
     * The {@link ClassPath} for the remote {@link JavaApplication}.
     */
    private ClassPath remoteClassPath;

    /**
     * The System {@link Properties} that were resolved for launching the {@link JavaApplication}.
     */
    private Properties systemProperties;


    /**
     * Constructs a {@link RemoteJavaApplicationLauncher}.
     *
     */
    public RemoteJavaApplicationLauncher() throws UnsupportedOperationException
    {
        // configure a server that the remote process can communicate with
        remoteChannel = new SocketBasedRemoteChannelServer("RemoteLauncher");

        // assume no resolved system properties at first
        systemProperties = new Properties();

        // open the server
        try
        {
            remoteChannel.open();
        }
        catch (IOException e)
        {
            throw new UnsupportedOperationException("Failed to create a " + this.getClass().getName()
                + " to launch the application remotely due to a communication problem",
                                                    e);
        }
    }


    @Override
    protected void onLaunching(OptionsByType optionsByType)
    {
        // ----- establish default Profiles for this Platform (and Builder) -----

        // java applications can automatically detect the following profiles
        optionsByType.get(RemoteDebugging.class);
        optionsByType.get(CommercialFeatures.class);

        // ----- determine the remote classpath based on the deployment option -----

        // when no deployment is specified we assume automatic
        JavaDeployment deployment = (JavaDeployment) optionsByType.getOrSetDefault(Deployment.class,
                                                                                   JavaDeployment.automatic());

        if (deployment.isAutoDeployEnabled())
        {
            // determine the PlatformSeparators (assume unix if not defined)
            PlatformSeparators separators = optionsByType.getOrSetDefault(PlatformSeparators.class,
                                                                          PlatformSeparators.forUnix());

            // when an automatic deployment is specified,
            // we use our modified class-path
            // (which is where all of the deployed jars will be located)
            String thisDir        = ".";
            String thisDirAllJars = thisDir + separators.getFileSeparator() + "*";

            remoteClassPath = new ClassPath(thisDir, thisDirAllJars);
        }
        else
        {
            // when a non-automatic deployment is specified we use the specified class path
            remoteClassPath = optionsByType.get(ClassPath.class);
        }

        // register the defined RemoteEventListeners before the application starts so they can
        // immediately start receiving RemoteEvents
        RemoteEvents remoteEvents = optionsByType.get(RemoteEvents.class);

        remoteEvents.forEach((remoteEventListener, listenerOptions) -> remoteChannel.addListener(remoteEventListener,
                                                                                                 listenerOptions));
    }


    @Override
    protected void onLaunched(JavaApplication application,
                              OptionsByType   optionsByType)
    {
        // ----- enhance the application with java-specific features -----

        if (JmxFeature.isSupportedBy(application))
        {
            application.add(new JmxFeature());
        }

        // ----- wait for the application to start -----

        // ensure that the application connects back to the server to
        // know that the application has started
        WaitToStart waitToStart = optionsByType.get(WaitToStart.class);

        if (waitToStart.isEnabled())
        {
            Timeout                              timeout = optionsByType.get(Timeout.class);

            final SocketBasedRemoteChannelServer server  = remoteChannel;

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
    }


    @Override
    protected <P extends ApplicationProcess> P adapt(RemoteApplicationProcess process)
    {
        return (P) new RemoteJavaApplicationProcess(process, remoteChannel, systemProperties);
    }


    @Override
    public Properties getEnvironmentVariables(Platform      platform,
                                              OptionsByType optionsByType)
    {
        Properties properties = super.getEnvironmentVariables(platform, optionsByType);

        // ----- establish the java home -----

        JavaHome javaHome = optionsByType.get(JavaHome.class);

        if (javaHome != null)
        {
            properties.put("JAVA_HOME", javaHome.get());
        }

        return properties;
    }


    @Override
    public String getCommandToExecute(Platform      platform,
                                      OptionsByType optionsByType)
    {
        StringBuilder commandBuilder = new StringBuilder();

        // ----- establish the command to start java -----

        JavaHome javaHome = optionsByType.get(JavaHome.class);

        // determine the Executable, defaulting to "java" if not defined
        Executable executable = optionsByType.getOrSetDefault(Executable.class, Executable.named("java"));

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            commandBuilder.append(StringHelper.doubleQuoteIfNecessary(executable.getName()));
        }
        else
        {
            // determine the PlatformSeparators (assume unix if not defined)
            PlatformSeparators separators = optionsByType.getOrSetDefault(PlatformSeparators.class,
                                                                          PlatformSeparators.forUnix());

            // when we have a java home, we prefix the executable name with the java.home/bin/
            String javaHomePath   = javaHome.get().trim();

            String javaExecutable = javaHomePath;

            if (!javaHomePath.endsWith(separators.getFileSeparator()))
            {
                javaExecutable += separators.getFileSeparator();
            }

            javaExecutable += "bin";
            javaExecutable += separators.getFileSeparator();
            javaExecutable += executable.getName();

            commandBuilder.append(StringHelper.doubleQuoteIfNecessary(javaExecutable));

            Table diagnosticsTable = optionsByType.get(Table.class);

            if (diagnosticsTable != null)
            {
                diagnosticsTable.addRow("Java Home", javaHomePath);
                diagnosticsTable.addRow("Java Executable", javaExecutable);
            }
        }

        return commandBuilder.toString();
    }


    @Override
    public List<String> getCommandLineArguments(Platform      platform,
                                                OptionsByType optionsByType)
    {
        ArrayList<String> arguments = new ArrayList<>();

        // ----- establish Bedrock specific system properties -----

        Table systemPropertiesTable = new Table();

        systemPropertiesTable.getOptions().add(Table.orderByColumn(0));
        systemPropertiesTable.getOptions().add(Cell.Separator.of(""));

        // establish the URI for this (the parent) process
        String              parentURI       = "//${local.address}:" + remoteChannel.getPort();

        ExpressionEvaluator evaluator       = new ExpressionEvaluator(optionsByType);
        String              parentUriString = evaluator.evaluate(parentURI, String.class);

        arguments.add("-D" + Settings.PARENT_URI + "=" + parentUriString);

        systemPropertiesTable.addRow(Settings.PARENT_URI, parentUriString);

        // add Orphanable configuration
        Orphanable orphanable = optionsByType.get(Orphanable.class);

        arguments.add("-D" + Settings.ORPHANABLE + "=" + orphanable.isOrphanable());
        systemPropertiesTable.addRow(Settings.ORPHANABLE, Boolean.toString(orphanable.isOrphanable()));

        // ----- establish the remote application class path -----

        // set the remote classpath (it must be quoted to prevent wildcard expansion)
        arguments.add("-cp");

        ClassPathModifier modifier  = optionsByType.getOrSetDefault(ClassPathModifier.class, ClassPathModifier.none());
        String            classPath = modifier.applyQuotes(remoteClassPath.toString(optionsByType.asArray()));

        arguments.add(classPath);

        Table diagnosticsTable = optionsByType.get(Table.class);

        if (diagnosticsTable != null)
        {
            Table classPathTable = remoteClassPath.getTable();

            classPathTable.getOptions().add(Cell.Separator.of(""));

            diagnosticsTable.addRow("Class Path", classPathTable.toString());
        }

        // ----- establish Java Virtual Machine options -----

        for (JvmOption jvmOption : optionsByType.getInstancesOf(JvmOption.class))
        {
            for (String value : jvmOption.resolve(optionsByType))
            {
                arguments.add(value);
            }
        }

        // ----- establish the system properties for the java application -----

        systemProperties = optionsByType.get(SystemProperties.class).resolve(platform, optionsByType);

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            // filter out (don't set) system properties that start with "bedrock", unless it's a profile
            // (we don't want to have "parents" applications effect child applications
            if (propertyName.startsWith("bedrock.profile.") ||!propertyName.startsWith("bedrock"))
            {
                // evaluate the property value
                String propertyValue = systemProperties.getProperty(propertyName);

                // build the actual system property command line argument
                StringBuilder propertyBuilder = new StringBuilder();

                propertyBuilder.append("-D");
                propertyBuilder.append(propertyName);

                if (!propertyValue.isEmpty())
                {
                    propertyValue = StringHelper.doubleQuoteIfNecessary(propertyValue);

                    propertyBuilder.append("=");
                    propertyBuilder.append(propertyValue);

                    systemPropertiesTable.addRow(propertyName, propertyValue);
                }

                arguments.add(propertyBuilder.toString());

            }
        }

        // ----- add bedrock.runtime.inherit.xxx values to the command -----

        for (String propertyName : System.getProperties().stringPropertyNames())
        {
            if (propertyName.startsWith("bedrock.runtime.inherit."))
            {
                // resolve the property value
                String propertyValue = System.getProperty(propertyName);

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                arguments.add(propertyValue);
            }
        }

        if (diagnosticsTable != null)
        {
            diagnosticsTable.addRow("System Properties", systemPropertiesTable.toString());
        }

        // ----- establish the application command line to execute -----

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        String applicationLauncherClassName = JavaApplicationRunner.class.getName();

        arguments.add(applicationLauncherClassName);

        // set the Java application class name we need to launch
        ClassName className = optionsByType.get(ClassName.class);

        if (className == null)
        {
            throw new IllegalArgumentException("Java Application ClassName not specified");
        }

        String applicationClassName = className.getName();

        arguments.add(applicationClassName);

        if (diagnosticsTable != null)
        {
            diagnosticsTable.addRow("Application Launcher", applicationLauncherClassName);
            diagnosticsTable.addRow("Application Class", applicationClassName);
        }

        // ----- included the java arguments to the command -----

        List<String> argList = optionsByType.get(Arguments.class).resolve(platform, optionsByType);

        // Set the actual arguments used back into the options
        optionsByType.add(Arguments.of(argList));

        for (String argument : argList)
        {
            arguments.add(argument);
        }

        return arguments;
    }


    /**
     * A {@link RemoteJavaApplicationProcess} is an adapter for a {@link RemoteApplicationProcess},
     * specifically for Java-based applications.
     */
    public static class RemoteJavaApplicationProcess implements JavaApplicationProcess
    {
        /**
         * The {@link RemoteApplicationProcess} being adapted.
         */
        private RemoteApplicationProcess process;

        /**
         * The {@link RemoteChannel} for the {@link RemoteJavaApplicationProcess}.
         */
        private ControllableRemoteChannel remoteChannel;

        /**
         * The resolved System {@link Properties} provided to the {@link JavaApplicationProcess} when it was launched.
         */
        private Properties systemProperties;


        /**
         * Constructs a {@link RemoteJavaApplicationProcess}.
         *
         * @param process           the underlying {@link RemoteApplicationProcess}
         * @param remoteChannel     the {@link RemoteChannel} for executing remote requests
         * @param systemProperties  the resolved System {@link Properties} provided to the {}
         */
        public RemoteJavaApplicationProcess(RemoteApplicationProcess  process,
                                            ControllableRemoteChannel remoteChannel,
                                            Properties                systemProperties)
        {
            this.process          = process;
            this.remoteChannel    = remoteChannel;
            this.systemProperties = systemProperties;
        }


        @Override
        public long getId()
        {
            return process.getId();
        }


        @Override
        public Properties getSystemProperties()
        {
            return systemProperties;
        }


        @Override
        public void close()
        {
            process.close();
            remoteChannel.close();
        }


        @Override
        public int exitValue()
        {
            return process.exitValue();
        }


        @Override
        public InputStream getErrorStream()
        {
            return process.getErrorStream();
        }


        @Override
        public InputStream getInputStream()
        {
            return process.getInputStream();
        }


        @Override
        public OutputStream getOutputStream()
        {
            return process.getOutputStream();
        }


        @Override
        public int waitFor(Option... options)
        {
            return process.waitFor(options);
        }


        @Override
        public <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
                                               Option...         options) throws IllegalStateException
        {
            return remoteChannel.submit(callable, options);
        }


        @Override
        public CompletableFuture<Void> submit(RemoteRunnable runnable,
                                              Option...      options) throws IllegalStateException
        {
            return remoteChannel.submit(runnable, options);
        }


        @Override
        public void addListener(RemoteEventListener listener,
                                Option...           options)
        {
            remoteChannel.addListener(listener, options);
        }


        @Override
        public void removeListener(RemoteEventListener listener,
                                   Option...           options)
        {
            remoteChannel.removeListener(listener, options);
        }


        @Override
        public CompletableFuture<Void> raise(RemoteEvent event,
                                             Option...   options)
        {
            return remoteChannel.raise(event, options);
        }
    }
}
