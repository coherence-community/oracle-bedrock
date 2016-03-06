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

package com.oracle.tools.runtime.remote.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.options.PlatformSeparators;

import com.oracle.tools.runtime.remote.Authentication;
import com.oracle.tools.runtime.remote.DeploymentArtifact;
import com.oracle.tools.runtime.remote.RemotePlatform;

import com.oracle.tools.runtime.remote.options.Deployer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;

/**
 * An implementation of a {@link com.oracle.tools.runtime.remote.options.Deployer} that uses SFTP to
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
     */
    public SftpDeployer(JSchSessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void deploy(List<DeploymentArtifact> artifactsToDeploy, String remoteDirectory, Platform platform,
                       Option... deploymentOptions)
    {
        if (artifactsToDeploy == null || artifactsToDeploy.isEmpty())
        {
            return;
        }

        if (!(platform instanceof RemotePlatform))
        {
            throw new IllegalArgumentException("The platform parameter must be an instance of RemotePlatform");
        }

        JSchSocketFactory socketFactory   = new JSchSocketFactory();
        RemotePlatform    remotePlatform  = (RemotePlatform) platform;
        String            userName        = remotePlatform.getUserName();
        Authentication    authentication  = remotePlatform.getAuthentication();
        String            hostName        = remotePlatform.getAddress().getHostName();
        int               port            = remotePlatform.getPort();

        // Create the deplpyment options
        Options options = new Options();
        // Add the Platform options
        options.addAll(platform.getOptions().asArray());
        // Override with specified Options
        options.addAll(deploymentOptions);

        // initially there's no session
        Session session = null;

        try
        {
            // Obtain the connected JSch Session
            session = sessionFactory.createSession(hostName, port, userName, authentication, socketFactory, options);

            // ----- deploy remote application artifacts (using sftp) -----

            // determine the separators for the platform
            PlatformSeparators separators = options.get(PlatformSeparators.class);

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
                        }

                        // copy the source artifact to the destination file
                        sftpChannel.put(new FileInputStream(sourceFile), destinationFileName);
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
    }
}
