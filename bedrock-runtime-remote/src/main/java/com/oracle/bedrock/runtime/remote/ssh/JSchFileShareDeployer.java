package com.oracle.bedrock.runtime.remote.ssh;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.options.FileShareDeployer;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

/**
 * An implementation of a {@link FileShareDeployer} that uses the
 * unix cp command via ssh to copy the remote file from the share
 * to its final destination.
 *
 * @author jk 2015.07.13
 */
public class JSchFileShareDeployer extends FileShareDeployer
{
    /**
     * Create a {@link JSchFileShareDeployer} that uses the
     * specified local and remote file share to deploy artifacts.
     *
     * @param localShareName  the name of the file share on the local platform
     * @param remoteShareName the name of the file share eon the remote platform
     * @param options         the {@link Option}s to control the deployer
     */
    private JSchFileShareDeployer(String localShareName, String remoteShareName, Option... options)
    {
        super(localShareName, remoteShareName, options);
    }

    @Override
    protected boolean performRemoteCopy(String source,
                                        String destination,
                                        Platform platform,
                                        Options deploymentOptions)
    {
        if (platform instanceof RemotePlatform)
        {
            JSchRemoteTerminal terminal = new JSchRemoteTerminal((RemotePlatform) platform);
            Options            options  = new Options(platform.getOptions().asArray());

            options.addAll(deploymentOptions.asArray());

            terminal.moveFile(source, destination, options);

            return false;
        }
        else
        {
            throw new IllegalArgumentException("The platform argument must be an instance of a RemotePlatform");
        }
    }


    /**
     * Create a new {@link Deployer} that deploys artifacts on a *nix remote
     * platform by using an intermediary file share location to copy the artifacts.
     *
     * @param localShareName  the name of the file share on the local platform
     * @param remoteShareName the name of the file share eon the remote platform
     * @param options         the {@link Option}s to control the deployer
     *
     * @return the new instance of the {@link Deployer}
     */
    public static Deployer sshFileShareDeployer(String localShareName, String remoteShareName, Option... options)
    {
        return new JSchFileShareDeployer(localShareName, remoteShareName, options);
    }
}
