/*
 * File: JavaDeployment.java
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

package com.oracle.bedrock.runtime.remote.java.options;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

import com.oracle.bedrock.io.FileHelper;

import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.JavaApplication;

import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import com.oracle.bedrock.runtime.remote.options.Deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * An {@link Option} to define the remote {@link Deployment} for {@link JavaApplication}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JavaDeployment implements Deployment
{
    /**
     * Should the {@link Deployment} automatically detect the required {@link JavaApplication} artifacts?
     * Ultimately this means using the {@link ClassPath} of the {@link JavaApplication} to determine
     * the artifacts to deploy.
     */
    private boolean autoDeployEnabled;

    /**
     * The {@link HashSet} of paths, including folders or files that must be deployed
     * (in addition to those detected when auto-deploy is enabled).
     */
    private HashSet<String> includePaths;

    /**
     * The {@link HashSet} of file names that must not be deployed
     * (when auto-deploy is enabled).
     */
    private HashSet<String> excludeFileNames;
    private boolean         excludeJDK;


    /**
     * Privately constructs a {@link JavaDeployment}.
     *
     * @param autoDeployEnabled  should automatic deployment be enabled?
     */
    private JavaDeployment(boolean autoDeployEnabled)
    {
        this.autoDeployEnabled = autoDeployEnabled;
        this.includePaths      = new HashSet<>();
        this.excludeJDK        = true;

        // by default there are some files we never want to deploy
        // (as these are part of the Java platform)
        this.excludeFileNames = new HashSet<>();

        excludeFileNames.add("apple_provider.jar");
        excludeFileNames.add("classes.jar");
        excludeFileNames.add("charsets.jar");
        excludeFileNames.add("deploy.jar");
        excludeFileNames.add("dt.jar");
        excludeFileNames.add("dnsns.jar");
        excludeFileNames.add("idea_rt.jar");
        excludeFileNames.add("localedata.jar");
        excludeFileNames.add("jsse.jar");
        excludeFileNames.add("jce.jar");
        excludeFileNames.add("javaws.jar");
        excludeFileNames.add("jconsole.jar");
        excludeFileNames.add("management-agent.jar");
        excludeFileNames.add("plugin.jar");
        excludeFileNames.add("sa-jdi.jar");
        excludeFileNames.add("sunjce_provider.jar");
        excludeFileNames.add("sunpkcs11.jar");
        excludeFileNames.add("ui.jar");
    }


    /**
     * Determines if auto-deployment based on the {@link ClassPath} should be performed.
     *
     * @return  <code>true</code> if auto-deployment is configured, <code>false</code> otherwise
     */
    public boolean isAutoDeployEnabled()
    {
        return autoDeployEnabled;
    }


    /**
     * Include the specified path (folder or filename) as an artifact to be deployed.
     *
     * @param path  the path to a file or folder to be deployed
     *
     * @return  the {@link JavaDeployment} to permit fluent-style method calls
     */
    public JavaDeployment include(String path)
    {
        this.includePaths.add(path);

        return this;
    }


    /**
     * Exclude the specified filename from being deployed.
     *
     * @param fileName  the name of the file that should not be deployed
     *
     * @return  the {@link JavaDeployment} to permit fluent-style method calls
     */
    public JavaDeployment exclude(String fileName)
    {
        this.includePaths.remove(fileName);
        this.excludeFileNames.add(fileName);

        return this;
    }


    @Override
    public List<DeploymentArtifact> getDeploymentArtifacts(Platform platform,
                                                           Options  options) throws FileNotFoundException, IOException
    {
        ArrayList<DeploymentArtifact> deploymentArtifacts = new ArrayList<DeploymentArtifact>();
        File                          javaHomeFile        = new File(System.getProperty("java.home"));
        String                        javaHome            = javaHomeFile.getCanonicalPath();

        if (javaHomeFile.getName().equals("jre"))
        {
            javaHome = javaHomeFile.getParentFile().getCanonicalPath();
        }

        if (autoDeployEnabled)
        {
            // we'll use the class-path option to work out what to deploy
            ClassPath classPath = options.get(ClassPath.class);

            for (String path : classPath)
            {
                // we ignore leading and trailing spaces
                path = path.trim();

                if (this.excludeJDK && path.startsWith(javaHome))
                {
                    continue;
                }

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
                            if (!excludeFileNames.contains(fileName.toLowerCase()))
                            {
                                String             destinationFile = file.getName();
                                DeploymentArtifact artifact = new DeploymentArtifact(file, new File(destinationFile));

                                deploymentArtifacts.add(artifact);
                            }
                        }
                        else
                        {
                            // create a temporary file in which to zip the contents of the folder
                            File temporaryFile = File.createTempFile("bedrock-deployment-", ".jar");

                            FileHelper.zip(Collections.singletonList(file), "", temporaryFile.getAbsolutePath());

                            DeploymentArtifact artifact = new DeploymentArtifact(temporaryFile,
                                                                                 new File(temporaryFile.getName()));

                            deploymentArtifacts.add(artifact);
                        }
                    }
                }
            }
        }
        else
        {
            // TODO: non-automatic means we need to explicitly add the include list
        }

        return deploymentArtifacts;
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof JavaDeployment))
        {
            return false;
        }

        JavaDeployment that = (JavaDeployment) other;

        if (autoDeployEnabled != that.autoDeployEnabled)
        {
            return false;
        }

        if (!excludeFileNames.equals(that.excludeFileNames))
        {
            return false;
        }

        if (!includePaths.equals(that.includePaths))
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = (autoDeployEnabled ? 1 : 0);

        result = 31 * result + includePaths.hashCode();
        result = 31 * result + excludeFileNames.hashCode();

        return result;
    }


    /**
     * Constructs a {@link JavaDeployment} configured for auto-deployment.
     *
     * @return  a {@link JavaDeployment} configured for auto-deployment
     */
    public static JavaDeployment automatic()
    {
        return new JavaDeployment(true);
    }


    /**
     * Constructs a {@link JavaDeployment} without any files to deploy.
     *
     * @return  a {@link JavaDeployment} configured for no files to deploy
     */
    public static JavaDeployment empty()
    {
        return new JavaDeployment(false);
    }
}
