/*
 * File: AbstractWindowsHttpDeployerTest.java
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

package com.oracle.bedrock.runtime.remote.http;

import com.oracle.bedrock.runtime.remote.DeploymentArtifact;

import org.junit.ClassRule;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URLEncoder;

import java.security.DigestInputStream;
import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A base class of utility methods for HttpPullDeploymentMethod tests.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class AbstractWindowsHttpDeployerTest
{
    /**
     * JUnit {@link ClassRule} to create temporary folders
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    /**
     * Create a given number of random binary artifacts.
     *
     * @return a {@link List} containing the specified number
     *         of {@link DeploymentArtifact}s
     */
    public List<DeploymentArtifact> createArtifactList(int count) throws Exception
    {
        List<DeploymentArtifact> artifacts = new ArrayList<>();
        Random                   random    = new Random(System.currentTimeMillis());
        File                     root      = temporaryFolder.newFolder();

        for (int i = 0; i < count; i++)
        {
            File file = new File(root, "File-" + i + ".bin");
            int  size = random.nextInt(1000) * 100;

            try (FileOutputStream stream = new FileOutputStream(file))
            {
                for (int b = 0; b < size; b++)
                {
                    stream.write(random.nextInt());
                }
            }

            artifacts.add(new DeploymentArtifact(file));
        }

        return artifacts;
    }


    /**
     * Create a given number of random binary artifacts returned in a
     * {@link Map} keyed on the encoded URL path of the artifacts
     * source file name.
     *
     * @return a {@link Map} containing the specified number
     *         of {@link DeploymentArtifact}s keyed on the
     *         encoded URL path of the artifacts source file name
     */
    public Map<String, DeploymentArtifact> createArtifactMap(int count) throws Exception
    {
        Map<String, DeploymentArtifact> artifacts = new LinkedHashMap<>();

        for (DeploymentArtifact artifact : createArtifactList(count))
        {
            String urlPath = "/" + URLEncoder.encode(artifact.getSourceFile().getCanonicalPath(), "UTF-8");

            artifacts.put(urlPath, artifact);
        }

        return artifacts;
    }


    /**
     * Get the MD5 digest for the specified {@link File}.
     *
     * @param file the {@link File} to get the MD5 digest for
     *
     * @return the MD5 digest for the specified {@link File}
     *
     * @throws Exception if there is an error reading the {@link File}
     */
    public byte[] getMD5(File file) throws Exception
    {
        MessageDigest md     = MessageDigest.getInstance("MD5");
        byte[]        buffer = new byte[100000];

        try (InputStream is = new FileInputStream(file))
        {
            DigestInputStream dis = new DigestInputStream(is, md);

            while (true)
            {
                if (dis.read(buffer) < 0)
                {
                    break;
                }
            }
        }

        return md.digest();
    }
}
