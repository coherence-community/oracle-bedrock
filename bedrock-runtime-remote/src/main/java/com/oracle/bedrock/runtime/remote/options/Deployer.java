/*
 * File: Deployer.java
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

package com.oracle.bedrock.runtime.remote.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.DeployedArtifacts;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;

import java.io.File;
import java.util.List;

/**
 * A {@link Deployer} is able to deploy and undeploy {@link DeploymentArtifact}s to and from
 * a specified {@link Platform} using a specific type of file transfer.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public interface Deployer extends Option
{
    /**
     * Deploy the list of {@link DeploymentArtifact}s to the specified {@link Platform}, returning the
     * {@link DeployedArtifacts} representing the actual {@link File}s deployed and their location.
     *
     * @param artifactsToDeploy  the {@link DeploymentArtifact}s to deploy
     * @param remoteDirectory    the target directory to deploy the {@link DeploymentArtifact}s to if no
     *                           destination is specified for a {@link DeploymentArtifact}
     * @param platform           the target {@link Platform} to deploy the {@link DeploymentArtifact}s to
     * @param deploymentOptions  the {@link Option}s that can be applied to control the deployment
     *
     * @return the {@link DeployedArtifacts}
     */
    DeployedArtifacts deploy(List<DeploymentArtifact> artifactsToDeploy,
                             String                   remoteDirectory,
                             Platform                 platform,
                             Option...                deploymentOptions);


    /**
     * Undeploy the {@link DeployedArtifacts} on the specified {@link Platform}, returning the
     * {@link DeployedArtifacts} that were not undeployed successfully.
     *
     * @param deployedArtifacts  the {@link DeployedArtifacts} to undeploy
     * @param platform           the {@link Platform}
     * @param deploymentOptions  the {@link Option}s used for deployment and undeployment
     *
     * @return  the {@link DeployedArtifacts} that were failed to undeploy
     */
    DeployedArtifacts undeploy(DeployedArtifacts deployedArtifacts,
                               Platform          platform,
                               Option...         deploymentOptions);


    /**
     * A no-op implementation of a deployer.
     */
    Deployer NULL = new Deployer()
    {
        @Override
        public DeployedArtifacts deploy(List<DeploymentArtifact> artifactsToDeploy,
                                        String                   remoteDirectory,
                                        Platform                 platform,
                                        Option...                deploymentOptions)
        {
            return new DeployedArtifacts();
        }

        @Override
        public DeployedArtifacts undeploy(DeployedArtifacts deployedArtifacts,
                                          Platform          platform,
                                          Option...         deploymentOptions)
        {
            return new DeployedArtifacts();
        }
    };
}
