package com.oracle.tools.runtime.remote;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.remote.winrm.WindowsRemoteShell;

/**
 * @author jk 2015.06.23
 */
public class WindowsShellTypes
{
    /**
     * Create a {@link RemoteShellType} that creates {@link WindowsRemoteShell} instances
     * running Windows cmd.exe.
     *
     * @return a {@link RemoteShellType} that creates {@link WindowsRemoteShell} instances
     *         running cmd.exe
     */
    public static RemoteShellType windowsCMD()
    {
        return new RemoteShellType()
        {
            @Override
            public <A extends Application, S extends ApplicationSchema<A>, E extends RemoteApplicationEnvironment>
            RemoteShell<A, S, E> createShell(String userName, Authentication authentication, String hostName, int port)
            {
                return new WindowsRemoteShell<>(userName, authentication, hostName, port);
            }
        };
    }

}
