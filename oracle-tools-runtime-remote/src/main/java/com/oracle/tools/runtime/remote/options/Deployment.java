/*
 * File: Deployment.java
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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.remote.DeploymentArtifact;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;

/**
 * An {@link Option} to define the files an {@link Application} requires for
 * remote deployment.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Deployment extends Option
{
    /**
     * Obtain the {@link DeploymentArtifact}s to deploy.
     *
     * @param platform  the {@link Platform} on which the {@link Application} will be deployed
     * @param options   the {@link Options} for realizing the {@link Application}
     *
     * @return  a {@link List} of {@link DeploymentArtifact}s
     *
     * @throws FileNotFoundException  when a {@link DeploymentArtifact} cannot be found
     * @throws IOException            when a {@link DeploymentArtifact} fail to be created/accessed
     */
    List<DeploymentArtifact> getDeploymentArtifacts(Platform platform,
                                                    Options  options) throws IOException;
}
