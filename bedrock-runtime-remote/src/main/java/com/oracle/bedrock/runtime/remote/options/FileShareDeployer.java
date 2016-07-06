/*
 * File: FileShareDeployer.java
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

package com.oracle.bedrock.runtime.remote.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import com.oracle.bedrock.table.Table;

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
     * The {@link OptionsByType} to use to control the deployer.
     */
    private OptionsByType optionsByType;


    /**
     * Create a {@link FileShareDeployer} that uses the
     * specified local and remote file share to deploy artifacts.
     *
     * @param localShareName   the name of the file share on the local platform
     * @param remoteShareName  the name of the file share eon the remote platform
     * @param options          the {@link Option}s to control the deployer
     */
    protected FileShareDeployer(String    localShareName,
                                String    remoteShareName,
                                Option... options)
    {
        this.localShareName  = localShareName;
        this.remoteShareName = remoteShareName;
        this.optionsByType   = OptionsByType.of(options);
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
    public void deploy(List<DeploymentArtifact> artifactsToDeploy,
                       String                   remoteDirectory,
                       Platform                 platform,
                       Option...                deploymentOptions)
    {
        OptionsByType combinedOptions = platform == null
                                        ? OptionsByType.empty() : OptionsByType.of(platform.getOptions());
        Table deploymentTable = new Table();

        combinedOptions.addAll(optionsByType);
        combinedOptions.addAll(deploymentOptions);

        PlatformSeparators separators      = combinedOptions.get(PlatformSeparators.class);
        File               remoteShareFile = new File(remoteShareName);

        for (DeploymentArtifact artifact : artifactsToDeploy)
        {
            double start = System.currentTimeMillis();

            try
            {
                File sourceFile = artifact.getSourceFile();
                Path localCopy  = new File(localShareName, sourceFile.getName()).toPath();

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
                    String destinationFilePath = separators.asPlatformFileName(destinationFile.getParent());

                    String dirName;

                    if (destinationFilePath == null)
                    {
                        dirName     = separators.asPlatformFileName(remoteDirectory);
                        destination = dirName + separators.getFileSeparator() + destinationFile.getPath();
                    }
                    else
                    {
                        destination = separators.asPlatformFileName(destinationFile.getCanonicalPath());
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

                double time = (System.currentTimeMillis() - start) / 1000.0d;

                deploymentTable.addRow(sourceFile.toString(),
                                       String.valueOf(destination),
                                       String.format("%.3f s", time));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to deploy " + artifact, e);
            }
        }

        Table diagnosticsTable = optionsByType.get(Table.class);

        if (diagnosticsTable != null)
        {
            diagnosticsTable.addRow("Application Deployments ", deploymentTable.toString());
        }
    }


    /**
     * Perform the copy of the {@link DeploymentArtifact} from the remote share location
     * to the final target location.
     *
     * @param source             the file to copy in the remote share folder
     * @param destination        the remote location to copy the artifact to
     * @param platform           the {@link Platform} to perform the remote copy on
     * @param deploymentOptions  the {@link OptionsByType}s to control the deployment
     *
     * @return true it the file on the remote share was copied and needs to be cleaned up
     *         or false if it was moved and no clean-up is required.
     *
     * @throws IOException  when the remote copy fails
     */
    protected abstract boolean performRemoteCopy(String        source,
                                                 String        destination,
                                                 Platform      platform,
                                                 OptionsByType deploymentOptions) throws IOException;
}
