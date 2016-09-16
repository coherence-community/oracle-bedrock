/*
 * File: DeploymentArtifact.java
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

package com.oracle.bedrock.runtime.remote;

import java.io.File;

/**
 * Defines an {@link File} artifact to be deployed to a remote server,
 * optionally at a specific {@link File} or destination.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeploymentArtifact
{
    /**
     * The source {@link File} to be deployed.
     */
    private File sourceFile;

    /**
     * The optional {@link File} destination.
     */
    private File destinationFile;


    /**
     * Constructs a {@link DeploymentArtifact} (without a specific destination).
     *
     * @param sourceFile  the source {@link File}
     */
    public DeploymentArtifact(File sourceFile)
    {
        this.sourceFile      = sourceFile;
        this.destinationFile = null;
    }


    /**
     * Constructs a {@link DeploymentArtifact}.
     *
     * @param sourceFile       the source {@link File}
     * @param destinationFile  the destination {@link File}
     */
    public DeploymentArtifact(File sourceFile,
                              File destinationFile)
    {
        this.sourceFile      = sourceFile;
        this.destinationFile = destinationFile;
    }


    /**
     * Obtains the source {@link File} to be deployed.
     *
     * @return  the source {@link File}
     */
    public File getSourceFile()
    {
        return sourceFile;
    }


    /**
     * Obtains the desired location to which the
     * source {@link File} should be deployed remotely.
     *
     * @return  the destination {@link File} or <code>null</code> if the destination
     *          location has not been specified (which means a default will be used)
     */
    public File getDestinationFile()
    {
        return destinationFile;
    }


    @Override
    public String toString()
    {
        return "DeploymentArtifact [" + destinationFile + " -> " + sourceFile + ']';
    }
}
