/*
 * File: SftpDeployer.java
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

package com.oracle.bedrock.runtime.remote.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.runtime.remote.Authentication;
import com.oracle.bedrock.runtime.remote.DeployedArtifacts;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.table.Table;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * An implementation of a {@link Deployer} that uses SFTP to
 * transfer {@link DeploymentArtifact}s to a platform.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SftpDeployer implements Deployer
{
    /**
     * The {@link JSchSessionFactory} to use to obtain JSch {@link Session}s.
     */
    private JSchSessionFactory sessionFactory;


    /**
     * Create a {@link SftpDeployer}.
     */
    public SftpDeployer()
    {
        this(new JSchSessionFactory());
    }


    /**
     * Create a {@link SftpDeployer} that will use the
     * specified {@link JSchSessionFactory}.
     *
     * @param sessionFactory  the {@link JSchSocketFactory}
     */
    public SftpDeployer(JSchSessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public DeployedArtifacts deploy(List<DeploymentArtifact> artifactsToDeploy,
                                    String                   remoteDirectory,
                                    Platform                 platform,
                                    Option...                deploymentOptions)
    {
        DeployedArtifacts deployedArtifacts = new DeployedArtifacts();

        if (artifactsToDeploy == null || artifactsToDeploy.isEmpty())
        {
            return deployedArtifacts;
        }

        if (!(platform instanceof RemotePlatform))
        {
            throw new IllegalArgumentException("The platform parameter must be an instance of RemotePlatform");
        }

        Table             deploymentTable = new Table();
        JSchSocketFactory socketFactory   = new JSchSocketFactory();
        RemotePlatform    remotePlatform  = (RemotePlatform) platform;
        String            userName        = remotePlatform.getUserName();
        Authentication    authentication  = remotePlatform.getAuthentication();
        String            hostName        = remotePlatform.getAddress().getHostName();
        int               port            = remotePlatform.getPort();

        // Create the deployment options
        OptionsByType optionsByType = OptionsByType.empty();

        // Add the Platform options
        optionsByType.addAll(platform.getOptions());

        // Override with specified Options
        optionsByType.addAll(deploymentOptions);

        // initially there's no session
        Session session = null;

        try
        {
            // Obtain the connected JSch Session
            session = sessionFactory.createSession(hostName,
                                                   port,
                                                   userName,
                                                   authentication,
                                                   socketFactory,
                                                   optionsByType);

            // ----- deploy remote application artifacts (using sftp) -----

            // determine the separators for the platform
            PlatformSeparators separators = optionsByType.get(PlatformSeparators.class);

            if (artifactsToDeploy.size() > 0)
            {
                ChannelSftp sftpChannel = null;

                try
                {
                    // open an sftp channel that we can use to copy over the artifacts
                    sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect(session.getTimeout());

                    try
                    {
                        // Obtain the status of the remote directory
                        sftpChannel.lstat(remoteDirectory);
                    }
                    catch (SftpException _ignored)
                    {
                        // the remote directory does not exist so attempt to create it
                        sftpChannel.mkdir(remoteDirectory);

                        // add the directory as something to clean up
                        deployedArtifacts.add(new File(remoteDirectory));
                    }

                    // copy deployment artifacts into the remote server
                    for (DeploymentArtifact artifactToDeploy : artifactsToDeploy)
                    {
                        // acquire the source file to deploy
                        File sourceFile = artifactToDeploy.getSourceFile();

                        // change to the desired remote directory
                        File   destinationFile = artifactToDeploy.getDestinationFile();

                        String destinationFileName;

                        if (destinationFile == null)
                        {
                            sftpChannel.cd(remoteDirectory);
                            destinationFileName = sourceFile.getName();

                            // add the file as a deployed artifact
                            deployedArtifacts.add(new File(remoteDirectory, destinationFileName));
                        }
                        else
                        {
                            String destinationFilePath = separators.asPlatformFileName(destinationFile.getParent());

                            String dirName;

                            if (destinationFilePath == null)
                            {
                                dirName = separators.asPlatformFileName(remoteDirectory);
                            }
                            else
                            {
                                dirName = separators.asPlatformFileName(destinationFilePath);
                            }

                            sftpChannel.cd(dirName);

                            destinationFileName = destinationFile.getName();

                            // add the file as a deployed artifact
                            deployedArtifacts.add(new File(dirName, destinationFileName));
                        }

                        // copy the source artifact to the destination file
                        double start = System.currentTimeMillis();

                        sftpChannel.put(new FileInputStream(sourceFile), destinationFileName);

                        double time = (System.currentTimeMillis() - start) / 1000.0d;

                        deploymentTable.addRow(sourceFile.toString(),
                                               String.valueOf(destinationFile),
                                               String.format("%.3f s", time));
                    }

                    Table diagnosticsTable = optionsByType.get(Table.class);

                    if (diagnosticsTable != null)
                    {
                        diagnosticsTable.addRow("Application Deployments ", deploymentTable.toString());
                    }
                }
                catch (IOException | SftpException e)
                {
                    throw new RuntimeException("Failed to deploy application", e);
                }
                finally
                {
                    if (sftpChannel != null)
                    {
                        sftpChannel.disconnect();
                    }
                }
            }

        }
        catch (JSchException e)
        {
            throw new RuntimeException("Failed to deploy application", e);
        }
        finally
        {
            if (session != null)
            {
                session.disconnect();
            }
        }

        return deployedArtifacts;
    }


    @Override
    public DeployedArtifacts undeploy(DeployedArtifacts deployedArtifacts,
                                      Platform          platform,
                                      Option...         deploymentOptions)
    {
        DeployedArtifacts failedArtifacts = new DeployedArtifacts();

        if (!(platform instanceof RemotePlatform))
        {
            throw new IllegalArgumentException("The platform parameter must be an instance of RemotePlatform");
        }

        JSchSocketFactory socketFactory  = new JSchSocketFactory();
        RemotePlatform    remotePlatform = (RemotePlatform) platform;
        String            userName       = remotePlatform.getUserName();
        Authentication    authentication = remotePlatform.getAuthentication();
        String            hostName       = remotePlatform.getAddress().getHostName();
        int               port           = remotePlatform.getPort();

        // create the deployment options
        OptionsByType optionsByType = OptionsByType.empty();

        // add the Platform options
        optionsByType.addAll(platform.getOptions());

        // override with specified Options
        optionsByType.addAll(deploymentOptions);

        // initially there's no session
        Session session = null;

        try
        {
            // obtain the connected JSch Session
            session = sessionFactory.createSession(hostName,
                                                   port,
                                                   userName,
                                                   authentication,
                                                   socketFactory,
                                                   optionsByType);

            // ----- undeploy remote application artifacts (using sftp) -----

            if (deployedArtifacts.size() > 0)
            {
                ChannelSftp sftpChannel = null;

                try
                {
                    // open an sftp channel that we can use to copy over the artifacts
                    sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect(session.getTimeout());

                    for (File file : deployedArtifacts)
                    {
                        try
                        {
                            System.out.println("Undeploying File: " + file.toString());

                            // attempt to delete the file
                            sftpChannel.rm(file.toString());
                        }
                        catch (SftpException exception)
                        {
                            try
                            {
                                System.out.println("Undeploying Directory: " + file.toString());

                                // attempt to remove the file as a directory
                                sftpChannel.rmdir(file.toString());
                            }
                            catch (SftpException e)
                            {
                                System.out.println("Failed to undeploying: " + file.toString() + " due to " + e.toString());
                                e.printStackTrace();

                                failedArtifacts.add(file);
                            }
                        }
                    }
                }
                finally
                {
                    if (sftpChannel != null)
                    {
                        sftpChannel.disconnect();
                    }
                }
            }

        }
        catch (JSchException e)
        {
            throw new RuntimeException("Failed to undeploy application", e);
        }
        finally
        {
            if (session != null)
            {
                session.disconnect();
            }
        }

        return failedArtifacts;
    }
}
