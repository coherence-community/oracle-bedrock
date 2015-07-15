package com.oracle.tools.runtime.remote;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.remote.options.FileShareDeployer;

import com.oracle.tools.runtime.remote.winrm.WindowsRemoteTerminal;

import java.io.IOException;

/**
 * An implementation of a {@link FileShareDeployer} that uses the
 * Windows move command via WinRM to copy the remote file from the
 * share to its final destination.
 *
 * @author jk 2015.07.13
 */
public class WindowsFileShareDeployer extends FileShareDeployer
{
    /**
     * Create a {@link WindowsFileShareDeployer} that uses the
     * specified local and remote file share to deploy artifacts.
     *
     * @param localShareName  the name of the file share on the local platform
     * @param remoteShareName the name of the file share eon the remote platform
     * @param options         the {@link Option}s to control the deployer
     */
    public WindowsFileShareDeployer(String localShareName, String remoteShareName, Option... options)
    {
        super(localShareName, remoteShareName, options);
    }

    @Override
    protected boolean performRemoteCopy(String source, String destination, Platform platform,
                                        Options deploymentOptions) throws IOException
    {
        if (platform instanceof RemotePlatform)
        {
            WindowsRemoteTerminal terminal = new WindowsRemoteTerminal((RemotePlatform) platform);
            Options               options  = new Options(platform.getOptions().asArray());

            options.addAll(deploymentOptions.asArray());

            terminal.moveFile(source, destination, options);

            return false;
        }
        else
        {
            throw new IllegalArgumentException("The platform argument must be an instance of a RemotePlatform");
        }
    }
}
