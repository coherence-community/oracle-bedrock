/*
 * File: DeploymentMethod.java
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

package com.oracle.tools.runtime.remote.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.remote.DeploymentArtifact;

import java.util.List;

/**
 * A {@link DeploymentMethod} is able to deploy
 * a collection of {@link DeploymentArtifact}s using
 * a specific file transfer method.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface DeploymentMethod extends Option
{
    /**
     * Deploy the list of {@link DeploymentArtifact}s to the specified {@link Platform}.
     *
     * @param artifactsToDeploy  the {@link DeploymentArtifact}s to deploy
     * @param remoteDirectory    the target directory to deploy the {@link DeploymentArtifact}s to if no
     *                           destination is specified for a {@link DeploymentArtifact}
     * @param platform           the target {@link Platform} to deploy the {@link DeploymentArtifact}s to
     * @param deploymentOptions  the {@link Options} that can be applied to control the deployment
     */
    void deploy(List<DeploymentArtifact> artifactsToDeploy,
                String                   remoteDirectory,
                Platform                 platform,
                Option...                deploymentOptions);
}
