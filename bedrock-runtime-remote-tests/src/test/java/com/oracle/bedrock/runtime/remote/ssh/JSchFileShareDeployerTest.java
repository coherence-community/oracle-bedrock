/*
 * File: JSchFileShareDeployerTest.java
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

package com.oracle.bedrock.runtime.remote.ssh;

import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.Application;

import com.oracle.bedrock.runtime.console.SystemApplicationConsole;

import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;

import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.Executable;

import com.oracle.bedrock.runtime.remote.AbstractRemoteTest;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import com.oracle.bedrock.runtime.remote.java.applications.SleepingApplication;
import com.oracle.bedrock.runtime.remote.options.Deployer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.List;

/**
 * Functional tests for {@link JSchFileShareDeployer}.
 *
 * @author Jonathan Knight
 */
public class JSchFileShareDeployerTest extends AbstractRemoteTest
{
    /**
     * Field description
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private String         localShare;
    private String         remoteShare;
    private String         workingDirectory;


    @Before
    public void setup() throws Exception
    {
        RemotePlatform platform = getRemotePlatform();

        localShare       = temporaryFolder.newFolder().getAbsolutePath();
        workingDirectory = temporaryFolder.newFolder().getAbsolutePath();

        File remoteParent = temporaryFolder.newFolder();

        remoteShare = new File(remoteParent, "share").getAbsolutePath();

        try (Application app = platform.launch(Application.class,
                                               Executable.named("ln"),
                                               Arguments.of("-s", localShare, remoteShare),
                                               DisplayName.of("Link"),
                                               SystemApplicationConsole.builder()))
        {
            app.waitFor();
        }
    }


    @Test
    public void shouldDeployArtifactWithNoDestination() throws Exception
    {
        String                   content  = "Jack and Jill went up the hill";
        File                     file     = createArtifact(content);
        DeploymentArtifact       artifact = new DeploymentArtifact(file);
        List<DeploymentArtifact> list     = Collections.singletonList(artifact);
        Deployer                 deployer = JSchFileShareDeployer.sshFileShareDeployer(localShare, remoteShare);

        deployer.deploy(list, workingDirectory, getRemotePlatform());

        File expected = new File(workingDirectory, file.getName());

        assertThat(expected.exists(), is(true));

        try (BufferedReader reader = new BufferedReader(new FileReader(expected)))
        {
            String line = reader.readLine();

            assertThat(line, is(content));
        }
    }


    @Test
    public void shouldDeployArtifactWithDestination() throws Exception
    {
        String                   content  = "To fetch a pail of water";
        File                     file     = createArtifact(content);
        File                     dest     = temporaryFolder.newFolder();
        DeploymentArtifact       artifact = new DeploymentArtifact(file, dest);
        List<DeploymentArtifact> list     = Collections.singletonList(artifact);
        Deployer                 deployer = JSchFileShareDeployer.sshFileShareDeployer(localShare, remoteShare);

        deployer.deploy(list, workingDirectory, getRemotePlatform());

        File expected = new File(dest, file.getName());

        assertThat(expected.exists(), is(true));

        try (BufferedReader reader = new BufferedReader(new FileReader(expected)))
        {
            String line = reader.readLine();

            assertThat(line, is(content));
        }
    }


    private File createArtifact(String content) throws Exception
    {
        File file = temporaryFolder.newFile();

        try (PrintWriter writer = new PrintWriter(file))
        {
            writer.println(content);
        }

        return file;
    }


    @Test
    public void shouldRunApplicationUsingDeployer() throws Exception
    {
        RemotePlatform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           Argument.of("3"),
                                                           JSchFileShareDeployer.sshFileShareDeployer(localShare,
                                                                                                      remoteShare),
                                                           DisplayName.of("Java"),
                                                           SystemApplicationConsole.builder()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }
}
