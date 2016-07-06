/*
 * File: WindowsFileShareDeployer.java
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

package com.oracle.bedrock.runtime.remote;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.options.FileShareDeployer;
import com.oracle.bedrock.runtime.remote.winrm.WindowsRemoteTerminal;

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
    public WindowsFileShareDeployer(String    localShareName,
                                    String    remoteShareName,
                                    Option... options)
    {
        super(localShareName, remoteShareName, options);
    }


    @Override
    protected boolean performRemoteCopy(String        source,
                                        String        destination,
                                        Platform      platform,
                                        OptionsByType deploymentOptions) throws IOException
    {
        if (platform instanceof RemotePlatform)
        {
            WindowsRemoteTerminal terminal      = new WindowsRemoteTerminal((RemotePlatform) platform);
            OptionsByType         optionsByType = OptionsByType.of(platform.getOptions());

            optionsByType.addAll(deploymentOptions.asArray());

            terminal.moveFile(source, destination, optionsByType);

            return false;
        }
        else
        {
            throw new IllegalArgumentException("The platform argument must be an instance of a RemotePlatform");
        }
    }
}
