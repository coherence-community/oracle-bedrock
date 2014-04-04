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

import com.oracle.tools.io.FileHelper;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;

import com.oracle.tools.runtime.java.ClassPath;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.JavaApplicationSchema;
import com.oracle.tools.runtime.java.container.Container;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationEnvironment;
import com.oracle.tools.runtime.remote.DeploymentArtifact;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * A Java-based implementation of a {@link RemoteJavaApplicationEnvironment}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationEnvironment<A extends JavaApplication<A>, S extends JavaApplicationSchema<A, S>>
    extends AbstractRemoteApplicationEnvironment<A, S>
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
     * The {@link DeploymentArtifact}s for the remote {@link JavaApplication}.
     */
    private ArrayList<DeploymentArtifact> deploymentArtifacts;

    /**
     * The remote JAVA_HOME to use or <code>null</code> to use the
     * schema java home (which if null means the platform defined JAVA HOME).
     */
    private String remoteJavaHome;


    /**
     * Constructs a {@link RemoteJavaApplicationEnvironment}.
     *
     * @param schema                the {@link JavaApplicationSchema}
     * @param remoteFileSeparator   the {@link File#separator} for the remote server
     * @param remotePathSeparator   the {@link File#pathSeparator} for the remote server
     * @param areOrphansPermitted   are orphaned remote {@link JavaApplication}s permitted
     * @param isAutoDeployEnabled   automatically deploy {@link JavaApplication}s
     * @param doNotDeployFileNames  the names of files not to deploy (when deployment enabled)
     * @param remoteJavaHome        the remote JAVA HOME (may be null for a default)
     */
    public RemoteJavaApplicationEnvironment(S           schema,
                                            char        remoteFileSeparator,
                                            char        remotePathSeparator,
                                            boolean     areOrphansPermitted,
                                            boolean     isAutoDeployEnabled,
                                            Set<String> doNotDeployFileNames,
                                            String      remoteJavaHome) throws IOException
    {
        super(schema);

        // TODO: the following port must be on a public address, not loopback
        // TODO: it should also be an ephemeral port

        // configure a server that the remote process can communicate with
        remoteExecutor = new RemoteExecutorServer(Container.getAvailablePorts().next());

        remoteExecutor.open();

        // ----- determine the remote system properties -----
        Properties properties = schema.getSystemPropertiesBuilder().realize();

        remoteSystemProperties = new Properties();

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
        remoteSystemProperties.setProperty(Settings.PARENT_ADDRESS, remoteExecutor.getInetAddress().getHostAddress());
        remoteSystemProperties.setProperty(Settings.PARENT_PORT, Integer.toString(remoteExecutor.getPort()));
        remoteSystemProperties.setProperty(Settings.ORPHANABLE, Boolean.toString(areOrphansPermitted));

        // ----- determine the remote classpath and deployment artifacts -----
        ClassPath classPath = schema.getClassPath();

        deploymentArtifacts = new ArrayList<DeploymentArtifact>();

        if (isAutoDeployEnabled)
        {
            ArrayList<ClassPath> remoteClassPaths = new ArrayList<ClassPath>();

            for (String path : classPath)
            {
                // we ignore leading and trailing spaces
                path = path.trim();

                if (path.endsWith("*"))
                {
                    // TODO: deal with wild-card based class paths
                    // (we need to copy all of the jars in the directory)
                }
                else if (path.endsWith("."))
                {
                    // TODO: deal with current directory based class paths
                    // (we need to copy all of the current directory, including sub-folders)
                }
                else if (path.endsWith(".."))
                {
                    // TODO: deal with parent directory based class paths
                    // (is this even possible?)
                }
                else
                {
                    // create a file based on the path
                    File file = new File(path);

                    if (file.exists())
                    {
                        if (file.isFile())
                        {
                            String fileName = file.getName();

                            // ensure that certain jars are not deployed
                            if (!doNotDeployFileNames.contains(fileName.toLowerCase()))
                            {
                                String             destinationFile = file.getName();
                                DeploymentArtifact artifact = new DeploymentArtifact(file, new File(destinationFile));

                                deploymentArtifacts.add(artifact);
                                remoteClassPaths.add(new ClassPath(destinationFile));
                            }
                        }
                        else
                        {
                            // create a temporary file in which to zip the contents of the folder
                            File temporaryFile = File.createTempFile("oracle-tools-deployment-", ".jar");

                            FileHelper.zip(Collections.singletonList(file), "", temporaryFile.getAbsolutePath());

                            DeploymentArtifact artifact = new DeploymentArtifact(temporaryFile,
                                                                                 new File(temporaryFile.getName()));

                            deploymentArtifacts.add(artifact);
                            remoteClassPaths.add(new ClassPath(temporaryFile.getName()));
                        }
                    }
                }
            }

            // the remote class-path includes all of the deployed jars
            remoteClassPath = new ClassPath(".", "./*");
        }
        else
        {
            // no deployment means no changes in class path are required
            remoteClassPath = classPath;
        }

        this.remoteJavaHome = remoteJavaHome;
    }


    /**
     * Obtains the {@link ControllableRemoteExecutor} that can be used to communicate
     * with the remote {@link JavaApplication}.
     *
     * @return  the {@link ControllableRemoteExecutor}
     */
    public ControllableRemoteExecutor getRemoteExecutor()
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
    public String getRemoteCommandToExecute()
    {
        StringBuilder builder = new StringBuilder();

        // we use java to execute the application
        builder.append("java");

        // set the remote classpath
        builder.append(" -cp " + remoteClassPath.toString());

        // add the java runtime options to the command
        // set the JVM options for the Process
        for (String option : schema.getJVMOptions())
        {
            builder.append(" ");
            builder.append("-" + option);
        }

        // add the system properties to the command
        Properties properties = getRemoteSystemProperties();

        for (String propertyName : properties.stringPropertyNames())
        {
            String propertyValue = properties.getProperty(propertyName);

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

        // set the JAVA_HOME
        String javaHome = remoteJavaHome == null ? schema.getJavaHome() : remoteJavaHome;

        if (javaHome != null)
        {
            properties.put("JAVA_HOME", javaHome);
        }

        return properties;
    }


    @Override
    public Iterable<DeploymentArtifact> getRemoteDeploymentArtifacts()
    {
        return deploymentArtifacts;
    }


    @Override
    public void close() throws IOException
    {
        remoteExecutor.close();
        super.close();
    }
}
