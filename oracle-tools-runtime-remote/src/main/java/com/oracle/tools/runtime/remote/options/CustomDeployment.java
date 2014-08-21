/*
 * File: CustomDeployment.java
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
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.remote.DeploymentArtifact;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Option} to define a custom collection of artifacts to deploy.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CustomDeployment implements Deployment
{
    /**
     * The list of {@link DeploymentArtifact}s.
     */
    private ArrayList<DeploymentArtifact> artifactsToDeploy;


    /**
     * Privately constructs an empty {@link CustomDeployment}.
     */
    private CustomDeployment()
    {
        this.artifactsToDeploy = new ArrayList<>();
    }


    @Override
    public List<DeploymentArtifact> getDeploymentArtifacts(ApplicationSchema schema,
                                                           Platform          platform,
                                                           Options           options) throws FileNotFoundException, IOException
    {
        return artifactsToDeploy;
    }


    /**
     * Creates an empty {@link CustomDeployment}.
     *
     * @return  an empty {@link CustomDeployment}
     */
    public static CustomDeployment empty()
    {
        return new CustomDeployment();
    }


    /**
     * Creates a {@link CustomDeployment} consisting of the specified {@link DeploymentArtifact}s.
     *
     * @param artifacts  the {@link DeploymentArtifact}s
     *
     * @return the {@link CustomDeployment}
     */
    public static CustomDeployment including(DeploymentArtifact... artifacts)
    {
        CustomDeployment deployment = new CustomDeployment();

        if (artifacts != null)
        {
            for (DeploymentArtifact artifact : artifacts)
            {
                deployment.artifactsToDeploy.add(artifact);
            }
        }

        return deployment;
    }
}
