/*
 * File: DeployedArtifacts.java
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

import com.oracle.bedrock.runtime.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Defines a collection of {@link File}s deployed on a {@link Platform}, where by the {@link File}s
 * define both the {@link Platform} specific path and optionally the file name of each deployed resource.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeployedArtifacts implements Iterable<File>
{
    /**
     * The deployed {@link File}s.
     */
    private ArrayList<File> files;


    /**
     * Constructs an empty {@link DeployedArtifacts}.
     */
    public DeployedArtifacts()
    {
        this.files = new ArrayList<>();
    }


    /**
     * Determines if the {@link DeployedArtifacts} is empty (contains no files).
     *
     * @return  <code>true</code> if the {@link DeployedArtifacts} is empty,
     *          <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return files.isEmpty();
    }


    /**
     * Adds a {@link File} to the {@link DeployedArtifacts}.
     *
     * @param file  the {@link File} to add
     */
    public void add(File file)
    {
        files.add(file);
    }


    /**
     * Obtain the number of {@link File}s in the {@link DeployedArtifacts}.
     *
     * @return  the number of {@link File}s
     */
    public int size()
    {
        return files.size();
    }


    @Override
    public Iterator<File> iterator()
    {
        return files.iterator();
    }
}
