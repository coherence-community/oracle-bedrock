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
     * Indicates that the source file was created for the purposes of deployment
     * as a temporary file and should be removed (if possible automatically)
     */
    private boolean temporary;


    /**
     * Constructs a {@link DeploymentArtifact} (without a specific destination),
     * for a non-temporary source file.
     *
     * @param sourceFile  the source {@link File} (non-temporary)
     */
    public DeploymentArtifact(File sourceFile)
    {
        this(sourceFile, null, false);
    }


    /**
     * Constructs a {@link DeploymentArtifact} (without a specific destination),
     * for a possibly temporary source file.
     *
     * @param sourceFile  the source {@link File}
     * @param temporary   indicates if the source file is considered temporary
     */
    public DeploymentArtifact(File    sourceFile,
                              boolean temporary)
    {
        this(sourceFile, null, temporary);
    }


    /**
     * Constructs a {@link DeploymentArtifact} for a non-temporary source file.
     *
     * @param sourceFile       the source {@link File} (non-temporary)
     * @param destinationFile  the destination {@link File}
     */
    public DeploymentArtifact(File sourceFile,
                              File destinationFile)
    {
        this(sourceFile, destinationFile, false);
    }


    /**
     * Constructs a {@link DeploymentArtifact} for a possibly temporary source file.
     *
     * @param sourceFile       the source {@link File}
     * @param destinationFile  the destination {@link File}
     * @param temporary        indicates if the source file is considered temporary
     */
    public DeploymentArtifact(File    sourceFile,
                              File    destinationFile,
                              boolean temporary)
    {
        this.sourceFile      = sourceFile;
        this.destinationFile = destinationFile;
        this.temporary       = temporary;
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


    /**
     * Determines if the {@link #getSourceFile()} was created for the purposes of
     * deployment and thus considered temporary, in which case it should be
     * automatically removed once deployment has occurred.
     *
     * @return <code>true</code> when the {@link #getSourceFile()} is temporary,
     *         <code>false</code> otherwise
     */
    public boolean isTemporary()
    {
        return temporary;
    }


    @Override
    public String toString()
    {
        return "DeploymentArtifact{" + "sourceFile=" + sourceFile + ", destinationFile=" + destinationFile
               + ", temporary=" + temporary + '}';
    }
}
