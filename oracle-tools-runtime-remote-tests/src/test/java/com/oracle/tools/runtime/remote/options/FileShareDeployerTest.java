package com.oracle.tools.runtime.remote.options;

import com.oracle.tools.Option;

import com.oracle.tools.Options;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.remote.DeploymentArtifact;

import org.junit.ClassRule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.file.Files;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link FileShareDeployer} class.
 *
 * @author jk 2015.07.13
 */
public class FileShareDeployerTest
{
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void shouldDeployArtifactWithDestination() throws Exception
    {
        Platform platform          = mock(Platform.class);
        Option   option            = mock(Option.class);
        Options  platformOptions   = new Options();
        File     localShareFolder  = temporaryFolder.newFolder();
        File     remoteShareFolder = temporaryFolder.newFolder();
        File     workingFolder     = temporaryFolder.newFolder();
        String   content           = "File 1";
        File     sourceFile        = createSourceFile(content);
        File     destinationFolder = temporaryFolder.newFolder();
        File     destinationFile   = new File(destinationFolder, "foo.txt");

        when(platform.getOptions()).thenReturn(platformOptions);

        DeploymentArtifact    artifact = new DeploymentArtifact(sourceFile, destinationFile);

        FileShareDeployerStub deployer = new FileShareDeployerStub(localShareFolder.getAbsolutePath(),
                                                                remoteShareFolder.getAbsolutePath());

        deployer.deploy(Collections.singletonList(artifact), workingFolder.getCanonicalPath(), platform, option);

        assertThat(destinationFile.exists(), is(true));

        try (BufferedReader reader = new BufferedReader(new FileReader(destinationFile)))
        {
            String line = reader.readLine();
            assertThat(line, is(content));
        }

        assertThat(deployer.platform, is(sameInstance(platform)));
        assertThat(deployer.options.asArray(), hasItemInArray(sameInstance(option)));
    }

    @Test
    public void shouldDeployArtifactWithoutDestination() throws Exception
    {
        Platform platform          = mock(Platform.class);
        Option   option            = mock(Option.class);
        Options  platformOptions   = new Options();
        File     localShareFolder  = temporaryFolder.newFolder();
        File     remoteShareFolder = temporaryFolder.newFolder();
        File     workingFolder     = temporaryFolder.newFolder();
        String   content           = "File 2";
        File     sourceFile        = createSourceFile(content);

        when(platform.getOptions()).thenReturn(platformOptions);

        DeploymentArtifact    artifact = new DeploymentArtifact(sourceFile);

        FileShareDeployerStub deployer = new FileShareDeployerStub(localShareFolder.getAbsolutePath(),
                                                                remoteShareFolder.getAbsolutePath());

        deployer.deploy(Collections.singletonList(artifact), workingFolder.getCanonicalPath(), platform, option);

        File destinationFile = new File(workingFolder, sourceFile.getName());

        assertThat(destinationFile.exists(), is(true));

        try (BufferedReader reader = new BufferedReader(new FileReader(destinationFile)))
        {
            String line = reader.readLine();
            assertThat(line, is(content));
        }

        assertThat(deployer.platform, is(sameInstance(platform)));
        assertThat(deployer.options.asArray(), hasItemInArray(sameInstance(option)));
    }

    @Test
    public void shouldDeployArtifactWhenFinalDestinationIsRemoteShare() throws Exception
    {
        Platform platform          = mock(Platform.class);
        Option   option            = mock(Option.class);
        Options  platformOptions   = new Options();
        File     localShareFolder  = temporaryFolder.newFolder();
        File     remoteShareFolder = temporaryFolder.newFolder();
        File     workingFolder     = temporaryFolder.newFolder();
        String   content           = "File 3";
        File     sourceFile        = createSourceFile(content);
        File     destinationFile   = new File(remoteShareFolder, sourceFile.getName());

        when(platform.getOptions()).thenReturn(platformOptions);

        DeploymentArtifact    artifact = new DeploymentArtifact(sourceFile, destinationFile);

        FileShareDeployer     deployer = new FileShareDeployerStub(localShareFolder.getAbsolutePath(),
                                                                remoteShareFolder.getAbsolutePath());

        FileShareDeployer     spy      = spy(deployer);

        spy.deploy(Collections.singletonList(artifact), workingFolder.getCanonicalPath(), platform, option);

        verify(spy, never()).performRemoteCopy(anyString(), anyString(), any(Platform.class), any(Options.class));
    }


    private File createSourceFile(String content) throws Exception
    {
        File     sourceFile        = temporaryFolder.newFile();

        try (PrintWriter writer = new PrintWriter(sourceFile))
        {
            writer.println(content);
        }

        return sourceFile;
    }


    public static class FileShareDeployerStub extends FileShareDeployer
    {
        protected Platform platform;

        protected Options  options;

        public FileShareDeployerStub(String localShareName, String remoteShareName)
        {
            super(localShareName, remoteShareName);
        }

        @Override
        protected boolean performRemoteCopy(String source,
                                            String destination,
                                            Platform platform,
                                            Options deploymentOptions) throws IOException
        {
            this.platform = platform;
            this.options  = deploymentOptions;

            File sourceFile      = new File(source);
            File destinationFile = new File(destination);
            File localFile       = new File(getLocalShareName(), sourceFile.getName());

            Files.copy(localFile.toPath(), sourceFile.toPath());
            Files.copy(sourceFile.toPath(), destinationFile.toPath());

            return true;
        }
    }
}
