package com.oracle.tools.runtime.remote.ssh;

import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.remote.AbstractRemoteTest;
import com.oracle.tools.runtime.remote.DeploymentArtifact;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.options.Deployer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;


/**
 * Functional tests for {@link JSchFileShareDeployer}.
 *
 * @author jk 2015.07.13
 */
public class JSchFileShareDeployerTest extends AbstractRemoteTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String localShare;

    private String remoteShare;

    private String workingDirectory;

    @Before
    public void setup() throws Exception
    {
        RemotePlatform platform = getRemotePlatform();

        localShare        = temporaryFolder.newFolder().getAbsolutePath();
        workingDirectory  = temporaryFolder.newFolder().getAbsolutePath();

        File remoteParent = temporaryFolder.newFolder();

        remoteShare = new File(remoteParent, "share").getAbsolutePath();

        String                  command = String.format("ln -s %s %s", localShare, remoteShare);
        SimpleApplicationSchema schema = new SimpleApplicationSchema(command);

        try (SimpleApplication app = platform.realize("Link", schema, new SystemApplicationConsole()))
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
}
