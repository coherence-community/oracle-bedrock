/*
 * File: DockerfileDeployerTest.java
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

package com.oracle.bedrock.runtime.docker.options;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;

import static org.junit.Assert.assertThat;

/**
 * Tests for {@link DockerfileDeployer}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerfileDeployerTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void shouldCopyFilesToWorkingDirectory() throws Exception
    {
        Platform                 platform     = LocalPlatform.get();
        File                     folder       = temporaryFolder.newFolder();
        File                     source1      = temporaryFolder.newFile("file1.txt");
        File                     source2      = temporaryFolder.newFile("file2.txt");
        File                     source3      = temporaryFolder.newFile("file3.txt");
        String                   workingDir   = folder.getCanonicalPath();
        String                   remoteFolder = "/foo";
        DockerfileDeployer       deployer     = new DockerfileDeployer(workingDir);
        List<DeploymentArtifact> artifacts    = new ArrayList<>();

        artifacts.add(new DeploymentArtifact(source1));
        artifacts.add(new DeploymentArtifact(source2, new File("/bar/test.txt")));
        artifacts.add(new DeploymentArtifact(source3));

        deployer.deploy(artifacts, remoteFolder, platform);

        assertThat(new File(folder, "file1.txt").exists(), is(true));
        assertThat(new File(folder, "file2.txt").exists(), is(true));
        assertThat(new File(folder, "file3.txt").exists(), is(true));
    }


    @Test
    public void shouldWriteAddList() throws Exception
    {
        Platform                 platform     = LocalPlatform.get();
        File                     folder       = temporaryFolder.newFolder();
        File                     source1      = temporaryFolder.newFile("file1.txt");
        File                     source2      = temporaryFolder.newFile("file2.txt");
        File                     source3      = temporaryFolder.newFile("file3.txt");
        String                   workingDir   = folder.getCanonicalPath();
        String                   remoteFolder = "/foo";
        DockerfileDeployer       deployer     = new DockerfileDeployer(workingDir);
        List<DeploymentArtifact> artifacts    = new ArrayList<>();

        artifacts.add(new DeploymentArtifact(source1));
        artifacts.add(new DeploymentArtifact(source2, new File("/bar/test.txt")));
        artifacts.add(new DeploymentArtifact(source3));

        deployer.deploy(artifacts, remoteFolder, platform);

        StringWriter stringWriter = new StringWriter();

        try (PrintWriter writer = new PrintWriter(stringWriter))
        {
            deployer.write(writer);
        }

        List<String> addLines = Arrays.asList(stringWriter.toString().split("\n"));

        assertThat(addLines.size(), is(3));
        assertThat(addLines.get(0), is(equalToCompressingWhiteSpace("ADD file1.txt /foo/file1.txt")));
        assertThat(addLines.get(1), is(equalToCompressingWhiteSpace("ADD file2.txt /bar/test.txt")));
        assertThat(addLines.get(2), is(equalToCompressingWhiteSpace("ADD file3.txt /foo/file3.txt")));
    }


}
