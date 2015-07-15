package com.oracle.tools.runtime.remote;

import com.oracle.tools.Option;
import com.oracle.tools.runtime.remote.http.HttpDeployer;
import com.oracle.tools.runtime.remote.http.PowerShellHttpDeployer;
import com.oracle.tools.runtime.remote.options.FileShareDeployer;

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
