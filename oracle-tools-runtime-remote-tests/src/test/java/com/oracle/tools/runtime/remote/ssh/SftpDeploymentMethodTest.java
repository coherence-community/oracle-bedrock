/*
 * File: SftpDeploymentMethodTest.java
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

package com.oracle.tools.runtime.remote.ssh;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.remote.AbstractRemoteTest;
import com.oracle.tools.runtime.remote.DeploymentArtifact;
import com.oracle.tools.runtime.remote.options.DeploymentMethod;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link SftpDeploymentMethod} class.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SftpDeploymentMethodTest extends AbstractRemoteTest
{
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldFindAsDeploymentMethodInOptions() throws Exception
    {
        DeploymentMethod sftp    = new SftpDeploymentMethod();
        Options          options = new Options(sftp);
        DeploymentMethod result  = options.get(DeploymentMethod.class);

        assertThat(result, is(sameInstance(sftp)));
    }

    @Test
    public void shouldDeployFiles() throws Exception
    {
        File                     source     = temporaryFolder.newFile();
        File                     root       = temporaryFolder.newFolder();
        File                     defaultDir = new File(root, "default");
        Platform                 platform   = getRemotePlatform();
        List<DeploymentArtifact> artifacts  = new ArrayList<>();

        artifacts.add(new DeploymentArtifact(source));
        artifacts.add(new DeploymentArtifact(source, new File(root, "temp.txt")));

        SftpDeploymentMethod sftp = new SftpDeploymentMethod();

        sftp.deploy(artifacts, defaultDir.getCanonicalPath(), platform);

        assertThat(new File(defaultDir, source.getName()).exists(), is(true));
        assertThat(new File(root, "temp.txt").exists(), is(true));
    }

}
