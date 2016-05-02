package com.oracle.bedrock.runtime.remote;

import com.oracle.bedrock.runtime.remote.http.HttpDeployer;
import com.oracle.bedrock.runtime.remote.http.PowerShellHttpDeployer;
import com.oracle.bedrock.runtime.remote.options.FileShareDeployer;
import com.oracle.bedrock.Option;

/**
 * @author jk 2015.06.23
 */
public class WindowsDeployers
{
    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use PowerShell Invoke-WebRequest to retrieve
     * artifacts.
     *
     * @param options the {@link Option}s controlling the deployer
     */
    public static HttpDeployer powerShellHttp(Option... options)
    {
        return new PowerShellHttpDeployer(options);
    }

    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use PowerShell Invoke-WebRequest to retrieve
     * artifacts.
     *
     * @param options the {@link Option}s controlling the deployer
     */
    public static FileShareDeployer fileShare(String localShareName, String remoteShareName, Option... options)
    {
        return new WindowsFileShareDeployer(localShareName, remoteShareName, options);
    }
}
