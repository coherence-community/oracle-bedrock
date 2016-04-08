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

package com.oracle.tools.runtime.remote.java;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.deferred.AbstractDeferred;
import com.oracle.tools.deferred.PermanentlyUnavailableException;
import com.oracle.tools.deferred.TemporarilyUnavailableException;

import com.oracle.tools.lang.ExpressionEvaluator;
import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationProcess;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.ControllableRemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteEvent;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteChannelServer;

import com.oracle.tools.runtime.java.ClassPath;
import com.oracle.tools.runtime.java.ClassPathModifier;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.JavaApplicationLauncher;
import com.oracle.tools.runtime.java.JavaApplicationProcess;
import com.oracle.tools.runtime.java.JavaApplicationRunner;
import com.oracle.tools.runtime.java.features.JmxFeature;
import com.oracle.tools.runtime.java.options.ClassName;
import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.options.JvmOption;
import com.oracle.tools.runtime.java.options.RemoteEvents;
import com.oracle.tools.runtime.java.options.SystemProperties;
import com.oracle.tools.runtime.java.options.WaitToStart;
import com.oracle.tools.runtime.java.profiles.CommercialFeatures;
import com.oracle.tools.runtime.java.profiles.RemoteDebugging;

import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.Executable;
import com.oracle.tools.runtime.options.Orphanable;
import com.oracle.tools.runtime.options.PlatformSeparators;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationLauncher;
import com.oracle.tools.runtime.remote.RemoteApplicationProcess;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.options.JavaDeployment;
import com.oracle.tools.runtime.remote.options.Deployment;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.within;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link JavaApplicationLauncher} that launches a {@link JavaApplication} on a {@link RemotePlatform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationLauncher extends AbstractRemoteApplicationLauncher<JavaApplication>
    implements JavaApplicationLauncher<JavaApplication, RemotePlatform>
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
     * @param platform  the {@link Platform} on which an {@link Application} will be launched
     */
    public RemoteJavaApplicationLauncher(RemotePlatform platform) throws UnsupportedOperationException
    {
        super(platform);

        // configure a server that the remote process can communicate with
        remoteChannel = new SocketBasedRemoteChannelServer();

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
    protected DisplayName getDisplayName(Options options)
    {
        ClassName className = options.get(ClassName.class);

        if (className == null)
        {
            return options.get(DisplayName.class);
        }
        else
        {
            // determine the short class name of the class we're launching (as a possible default)
            String shortClassName = className.getName();
            int    lastDot        = shortClassName.lastIndexOf(".");

            shortClassName = lastDot <= 0 ? shortClassName : shortClassName.substring(lastDot + 1);

            if (shortClassName.isEmpty())
            {
                return options.get(DisplayName.class);
            }
            else
            {
                return options.getOrDefault(DisplayName.class, DisplayName.of(shortClassName));
            }
        }
    }


    @Override
    protected void onLaunching(Options options)
    {
        // ----- establish default Profiles for this Platform (and Builder) -----

        // java applications can automatically detect the following profiles
        options.get(RemoteDebugging.class);
        options.get(CommercialFeatures.class);

        // ----- determine the remote classpath based on the deployment option -----

        JavaDeployment deployment = (JavaDeployment) options.get(Deployment.class);

        if (deployment == null)
        {
            // when no deployment is specified we assume automatic
            deployment = JavaDeployment.automatic();

            options.addIfAbsent(deployment);
        }

        if (deployment.isAutoDeployEnabled())
        {
            // determine the PlatformSeparators (assume unix if not defined)
            PlatformSeparators separators = options.getOrDefault(PlatformSeparators.class,
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
            remoteClassPath = options.get(ClassPath.class);
        }

        // register the defined RemoteEventListeners before the application starts so they can
        // immediately start receiving RemoteEvents
        RemoteEvents remoteEvents = options.get(RemoteEvents.class);

        remoteEvents.forEach((remoteEventListener, listenerOptions) -> remoteChannel.addListener(remoteEventListener,
                                                                                                 listenerOptions));
    }


    @Override
    protected void onLaunched(JavaApplication application,
                              Options         options)
    {
        // ----- enhance the application with java-specific features -----

        if (JmxFeature.isSupportedBy(application))
        {
            application.add(new JmxFeature());
        }

        // ----- wait for the application to start -----

        // ensure that the application connects back to the server to
        // know that the application has started
        WaitToStart waitToStart = options.get(WaitToStart.class);

        if (waitToStart.isEnabled())
        {
            Timeout                              timeout = options.get(Timeout.class);

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
    public Properties getEnvironmentVariables(Platform platform,
                                              Options  options)
    {
        Properties properties = super.getEnvironmentVariables(platform, options);

        // ----- establish the java home -----

        JavaHome javaHome = options.get(JavaHome.class);

        if (javaHome != null)
        {
            properties.put("JAVA_HOME", javaHome.get());
        }

        return properties;
    }


    @Override
    public String getCommandToExecute(Platform platform,
                                      Options  options)
    {
        StringBuilder commandBuilder = new StringBuilder();

        // ----- establish the command to start java -----

        JavaHome javaHome = options.get(JavaHome.class);

        // determine the Executable, defaulting to "java" if not defined
        Executable executable = options.getOrDefault(Executable.class, Executable.named("java"));

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            commandBuilder.append(StringHelper.doubleQuoteIfNecessary(executable.getName()));
        }
        else
        {
            // determine the PlatformSeparators (assume unix if not defined)
            PlatformSeparators separators = options.getOrDefault(PlatformSeparators.class,
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
        }

        return commandBuilder.toString();
    }


    @Override
    public List<String> getCommandLineArguments(Platform platform,
                                                Options  options)
    {
        ArrayList<String> arguments = new ArrayList<>();

        // ----- establish Oracle Tools specific system properties -----

        // establish the URI for this (the parent) process
        String              parentURI = "//${local.address}:" + remoteChannel.getPort();

        ExpressionEvaluator evaluator = new ExpressionEvaluator(options);

        arguments.add("-D" + Settings.PARENT_URI + "=" + evaluator.evaluate(parentURI, String.class));

        // add Orphanable configuration
        Orphanable orphanable = options.get(Orphanable.class);

        arguments.add("-D" + Settings.ORPHANABLE + "=" + orphanable.isOrphanable());

        // ----- establish the remote application class path -----

        // set the remote classpath (it must be quoted to prevent wildcard expansion)
        arguments.add("-cp");

        ClassPathModifier modifier  = options.getOrDefault(ClassPathModifier.class, ClassPathModifier.none());
        String            classPath = modifier.applyQuotes(remoteClassPath.toString(options.asArray()));

        arguments.add(classPath);

        // ----- establish Java Virtual Machine options -----

        for (JvmOption jvmOption : options.getInstancesOf(JvmOption.class))
        {
            for (String value : jvmOption.resolve(options))
            {
                arguments.add(value);
            }
        }

        // ----- establish the system properties for the java application -----

        systemProperties = options.get(SystemProperties.class).resolve(platform, options);

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            // filter out (don't set) system properties that start with "oracletools", unless it's a profile
            // (we don't want to have "parents" applications effect child applications
            if (propertyName.startsWith("oracletools.profile.") ||!propertyName.startsWith("oracletools"))
            {
                // evaluate the property value
                String propertyValue = systemProperties.getProperty(propertyName);

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                // build the actual system property command line argument
                StringBuilder propertyBuilder = new StringBuilder();

                propertyBuilder.append("-D");
                propertyBuilder.append(propertyName);

                if (!propertyValue.isEmpty())
                {
                    propertyBuilder.append("=");
                    propertyBuilder.append(StringHelper.doubleQuoteIfNecessary(propertyValue));
                }

                arguments.add(propertyBuilder.toString());
            }
        }

        // ----- add oracletools.runtime.inherit.xxx values to the command -----

        for (String propertyName : System.getProperties().stringPropertyNames())
        {
            if (propertyName.startsWith("oracletools.runtime.inherit."))
            {
                // resolve the property value
                String propertyValue = System.getProperty(propertyName);

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                arguments.add(propertyValue);
            }
        }

        // ----- establish the application command line to execute -----

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        String applicationLauncherClassName = JavaApplicationRunner.class.getName();

        arguments.add(applicationLauncherClassName);

        // set the Java application class name we need to launch
        ClassName className = options.get(ClassName.class);

        if (className == null)
        {
            throw new IllegalArgumentException("Java Application ClassName not specified");
        }

        String applicationClassName = className.getName();

        arguments.add(applicationClassName);

        // ----- included the java arguments to the command -----

        List<String> argList = options.get(Arguments.class).resolve(platform, options);

        // Set the actual arguments used back into the options
        options.add(Arguments.of(argList));

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
        public void destroy()
        {
            process.destroy();
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
        public <T> CompletableFuture<T> submit(RemoteCallable<T>     callable,
                                               Option...             options) throws IllegalStateException
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
