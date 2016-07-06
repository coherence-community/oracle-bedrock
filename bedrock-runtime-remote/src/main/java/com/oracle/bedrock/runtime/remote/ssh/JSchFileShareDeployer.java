/*
 * File: JSchFileShareDeployer.java
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

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.options.FileShareDeployer;

/**
 * An implementation of a {@link FileShareDeployer} that uses the
 * unix cp command via ssh to copy the remote file from the share
 * to its final destination.
 *
 * @author jk 2015.07.13
 */
public class JSchFileShareDeployer extends FileShareDeployer
{
    /**
     * Create a {@link JSchFileShareDeployer} that uses the
     * specified local and remote file share to deploy artifacts.
     *
     * @param localShareName   the name of the file share on the local platform
     * @param remoteShareName  the name of the file share eon the remote platform
     * @param options          the {@link Option}s to control the deployer
     */
    private JSchFileShareDeployer(String    localShareName,
                                  String    remoteShareName,
                                  Option... options)
    {
        super(localShareName, remoteShareName, options);
    }


    @Override
    protected boolean performRemoteCopy(String        source,
                                        String        destination,
                                        Platform      platform,
                                        OptionsByType deploymentOptions)
    {
        if (platform instanceof RemotePlatform)
        {
            JSchRemoteTerminal terminal      = new JSchRemoteTerminal((RemotePlatform) platform);
            OptionsByType      optionsByType = OptionsByType.of(platform.getOptions());

            optionsByType.addAll(deploymentOptions.asArray());

            terminal.moveFile(source, destination, optionsByType);

            return false;
        }
        else
        {
            throw new IllegalArgumentException("The platform argument must be an instance of a RemotePlatform");
        }
    }


    /**
     * Create a new {@link Deployer} that deploys artifacts on a *nix remote
     * platform by using an intermediary file share location to copy the artifacts.
     *
     * @param localShareName   the name of the file share on the local platform
     * @param remoteShareName  the name of the file share eon the remote platform
     * @param options          the {@link Option}s to control the deployer
     *
     * @return the new instance of the {@link Deployer}
     */
    public static Deployer sshFileShareDeployer(String    localShareName,
                                                String    remoteShareName,
                                                Option... options)
    {
        return new JSchFileShareDeployer(localShareName, remoteShareName, options);
    }
}
