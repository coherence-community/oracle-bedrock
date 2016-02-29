package com.oracle.tools.runtime.remote.options;

import com.oracle.tools.Option;

import com.oracle.tools.Options;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.options.PlatformSeparators;
import com.oracle.tools.runtime.remote.DeploymentArtifact;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.List;

/**
 * A FileShareDeployer is a {@link Deployer} deploys to a
 * remote platform by copying files via a shared file system
 * location.
 * <p>
 * Sub-classes of this class will implement the actual
 * remote part of the copy operation, which is specific to
 * the remote platform O/S.
 *
 * @author jk 2015.07.10
 */
public abstract class FileShareDeployer implements Deployer
{
    /**
     * The name of the shared file location on the local platform.
     */
    private String localShareName;

    /**
     * The name of the shared file location on the remote platform.
     */
    private String remoteShareName;

    /**
     * The {@link Options} to use to control the deployer.
     */
    private Options options;


    /**
     * Create a {@link FileShareDeployer} that uses the
     * specified local and remote file share to deploy artifacts.
     *
     * @param localShareName  the name of the file share on the local platform
     * @param remoteShareName the name of the file share eon the remote platform
     * @param options         the {@link Option}s to control the deployer
     */
    protected FileShareDeployer(String localShareName, String remoteShareName, Option... options)
    {
        this.localShareName  = localShareName;
        this.remoteShareName = remoteShareName;
        this.options         = new Options(options);
    }


    public String getLocalShareName()
    {
        return localShareName;
    }


    public String getRemoteShareName()
    {
        return remoteShareName;
    }


    @Override
    public void deploy(List<DeploymentArtifact> artifactsToDeploy, String remoteDirectory, Platform platform,
                       Option... deploymentOptions)
    {
        Options combinedOptions = new Options(platform == null ? null : platform.getOptions().asArray());

        combinedOptions.addAll(options.asArray());
        combinedOptions.addAll(deploymentOptions);

        PlatformSeparators separators      = combinedOptions.get(PlatformSeparators.class);
        File               remoteShareFile = new File(remoteShareName);

        for (DeploymentArtifact artifact : artifactsToDeploy)
        {
            try
            {
                Path localCopy = new File(localShareName, artifact.getSourceFile().getName()).toPath();
                Files.copy(artifact.getSourceFile().toPath(), localCopy, StandardCopyOption.REPLACE_EXISTING);

                String destination;
                String sourceName      = artifact.getSourceFile().getName();
                File   destinationFile = artifact.getDestinationFile();

                if (destinationFile == null)
                {
                    destination = remoteDirectory + separators.getFileSeparator() + sourceName;
                }
                else
                {
                    String destinationFilePath = separators.asRemotePlatformFileName(destinationFile.getParent());

                    String dirName;
                    if (destinationFilePath == null)
                    {
                        dirName     = separators.asRemotePlatformFileName(remoteDirectory);
                        destination = dirName + separators.getFileSeparator() + destinationFile.getPath();
                    }
                    else
                    {
                        destination = separators.asRemotePlatformFileName(destinationFile.getCanonicalPath());
                    }
                }

                String source = new File(remoteShareFile, sourceName).getCanonicalPath();

                if (!source.equals(destination))
                {
                    boolean cleanup = performRemoteCopy(source, destination, platform, combinedOptions);

                    if (cleanup)
                    {
                        Files.delete(localCopy);
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to deploy " + artifact, e);
            }
        }

    }

    /**
     * Perform the copy of the {@link DeploymentArtifact} from the remote share location
     * to the final target location.
     *
     * @param source            the file to copy in the remote share folder
     * @param destination       the remote location to copy the artifact to
     * @param platform          the {@link Platform} to perform the remote copy on
     * @param deploymentOptions the {@link Option}s to control the deployment
     *
     * @return true it the file on the remote share was copied and needs to be cleaned up
     *         or false if it was moved and no clean-up is required.
     */
    protected abstract boolean performRemoteCopy(String source, String destination, Platform platform,
                                                 Options deploymentOptions) throws IOException;
}
