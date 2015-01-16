/*
 * File: RemoteJavaApplicationEnvironment.java
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

import com.oracle.tools.Options;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;

import com.oracle.tools.runtime.java.ClassPath;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.JavaApplicationSchema;
import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.options.JvmOption;
import com.oracle.tools.runtime.java.options.RemoteDebugging;

import com.oracle.tools.runtime.options.Orphanable;
import com.oracle.tools.runtime.options.PlatformSeparators;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationEnvironment;
import com.oracle.tools.runtime.remote.java.options.JavaDeployment;

import java.io.IOException;

import java.net.InetAddress;

import java.util.Properties;

/**
 * A Java-based implementation of a {@link RemoteJavaApplicationEnvironment}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationEnvironment<A extends JavaApplication>
    extends AbstractRemoteApplicationEnvironment<A, JavaApplicationSchema<A>>
{
    /**
     * The {@link ControllableRemoteExecutor} that can be used to communicate with
     * the remote {@link JavaApplication}.
     */
    private RemoteExecutorServer remoteExecutor;

    /**
     * The System {@link Properties} for the remote {@link JavaApplication}.
     */
    private Properties remoteSystemProperties;

    /**
     * The {@link ClassPath} for the remote {@link JavaApplication}.
     */
    private ClassPath remoteClassPath;

    /**
     * The remote debugging port assigned to the remote {@link JavaApplication}.
     */
    private int remoteDebugPort;


    /**
     * Constructs a {@link RemoteJavaApplicationEnvironment}.
     *
     * @param schema                the {@link com.oracle.tools.runtime.java.JavaApplicationSchema}
     * @param platform              the {@link Platform} representing the remote O/S
     * @param options       the {@link Options} for the remote O/S
     */
    public RemoteJavaApplicationEnvironment(JavaApplicationSchema<A> schema,
                                            Platform                 platform,
                                            Options                  options) throws IOException
    {
        super(schema, platform, options);

        // assume no system properties to start with
        remoteSystemProperties = new Properties();

        // configure a server that the remote process can communicate with
        remoteExecutor = new RemoteExecutorServer();

        // open the server
        remoteExecutor.open();

        // ----- determine the remote classpath based on the deployment option -----

        JavaDeployment deployment = options.get(JavaDeployment.class);

        if (deployment == null)
        {
            // when no deployment is specified we assume automatic
            deployment = JavaDeployment.automatic();

            options.addIfAbsent(deployment);
        }

        if (deployment.isAutoDeployEnabled())
        {
            // when an automatic deployment is specified,
            // we use our modified class-path
            // (which is where all of the deployed jars will be located)
            remoteClassPath = new ClassPath(".", "./*");
        }
        else
        {
            // when a non-automatic deployment is specified
            // we'll use what the schema defines
            remoteClassPath = schema.getClassPath();
        }
    }


    /**
     * Obtains the {@link RemoteExecutorServer} that can be used to communicate
     * with the remote {@link JavaApplication}.
     *
     * @return  the {@link ControllableRemoteExecutor}
     */
    public RemoteExecutorServer getRemoteExecutor()
    {
        return remoteExecutor;
    }


    /**
     * Obtains the system {@link Properties} to use for the remote {@link JavaApplication}.
     *
     * @return  the system {@link Properties}
     */
    public Properties getRemoteSystemProperties()
    {
        return remoteSystemProperties;
    }


    @Override
    public String getRemoteCommandToExecute(InetAddress localInetAddress)
    {
        StringBuilder builder = new StringBuilder();

        // ----- establish the command to start java -----

        JavaHome javaHome = options.get(JavaHome.class);

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            builder.append(StringHelper.doubleQuoteIfNecessary(schema.getExecutableName()));
        }
        else
        {
            // determine the PlatformSeparators (assume unix if not defined)
            PlatformSeparators separators = options.get(PlatformSeparators.class, PlatformSeparators.forUnix());

            // when we have a java home, we prefix the executable name with the java.home/bin/
            String javaHomePath   = javaHome.get().trim();

            String javaExecutable = javaHomePath;

            if (!javaHomePath.endsWith(separators.getFileSeparator()))
            {
                javaExecutable += separators.getFileSeparator();
            }

            javaExecutable += "bin";
            javaExecutable += separators.getFileSeparator();
            javaExecutable += schema.getExecutableName();

            builder.append(StringHelper.doubleQuoteIfNecessary(javaExecutable));
        }

        // ----- establish the remote application class path -----

        // set the remote classpath (it must be quoted to prevent wildcard expansion)
        builder.append(" -cp \"" + remoteClassPath.toString(options.asArray()) + "\"");

        // ----- establish Java Virtual Machine options -----

        for (JvmOption jvmOption : options.getAll(JvmOption.class))
        {
            for (String option : jvmOption.getOptions())
            {
                builder.append(" ");
                builder.append(option);
            }
        }

        // ----- establish remote debugging JVM options -----

        RemoteDebugging remoteDebugging = options.get(RemoteDebugging.class, RemoteDebugging.autoDetect());

        if (remoteDebugging.isEnabled())
        {
            boolean isDebugServer = remoteDebugging.getBehavior() == RemoteDebugging.Behavior.LISTEN_FOR_DEBUGGER;
            boolean suspend       = remoteDebugging.isStartSuspended();

            remoteDebugPort = isDebugServer ? remoteDebugging.getListenPort() : remoteDebugging.getAttachPort();

            if (remoteDebugPort <= 0)
            {
                remoteDebugPort = LocalPlatform.getInstance().getAvailablePorts().next();
            }

            String debugAddress = isDebugServer
                                  ? String.valueOf(remoteDebugPort)
                                  : LocalPlatform.getInstance().getAddress().getHostAddress() + ":" + remoteDebugPort;

            String debugOption = String.format(" -agentlib:jdwp=transport=dt_socket,server=%s,suspend=%s,address=%s",
                                               (isDebugServer ? "y" : "n"),
                                               (suspend ? "y" : "n"),
                                               debugAddress);

            builder.append(debugOption);
        }

        // ----- establish the system properties for the java application -----

        Properties properties = schema.getSystemProperties(platform);

        for (String propertyName : properties.stringPropertyNames())
        {
            String propertyValue = properties.getProperty(propertyName);

            // filter out (don't set) system properties that start with "oracletools"
            // (we don't want to have "parents" applications effect child applications
            if (!propertyName.startsWith("oracletools"))
            {
                remoteSystemProperties.setProperty(propertyName, propertyValue);
            }
        }

        // add oracle tools specific system properties
        InetAddress remoteExecutorAddress = localInetAddress;

        remoteSystemProperties.setProperty(Settings.PARENT_ADDRESS, remoteExecutorAddress.getHostAddress());
        remoteSystemProperties.setProperty(Settings.PARENT_PORT, Integer.toString(remoteExecutor.getPort()));

        Orphanable orphanable = options.get(Orphanable.class, Orphanable.disabled());

        remoteSystemProperties.setProperty(Settings.ORPHANABLE, Boolean.toString(orphanable.isOrphanable()));

        // add the system properties to the command
        for (String propertyName : remoteSystemProperties.stringPropertyNames())
        {
            String propertyValue = remoteSystemProperties.getProperty(propertyName);

            builder.append(" ");
            builder.append("-D");
            builder.append(propertyName);

            if (!propertyValue.isEmpty())
            {
                builder.append("=");
                builder.append(StringHelper.doubleQuoteIfNecessary(propertyValue));
            }
        }

        // we use the launcher to launch the application
        // (we don't start the application directly itself)
        builder.append(" ");
        builder.append("com.oracle.tools.runtime.java.JavaApplicationLauncher");

        // set the java application class name we need to launch
        builder.append(" ");
        builder.append(schema.getApplicationClassName());

        // add the arguments to the command
        for (String argument : schema.getArguments())
        {
            builder.append(" ");
            builder.append(argument);
        }

        return builder.toString();
    }


    @Override
    public Properties getRemoteEnvironmentVariables()
    {
        Properties properties = super.getRemoteEnvironmentVariables();

        // ----- establish the java home -----

        JavaHome javaHome = options.get(JavaHome.class);

        if (javaHome != null)
        {
            properties.put("JAVA_HOME", javaHome.get());
        }

        return properties;
    }


    public int getRemoteDebugPort()
    {
        return remoteDebugPort;
    }


    @Override
    public void close()
    {
        remoteExecutor.close();
        super.close();
    }
}
