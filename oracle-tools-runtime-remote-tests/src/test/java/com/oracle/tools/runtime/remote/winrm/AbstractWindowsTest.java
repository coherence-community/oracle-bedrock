/*
 * File: AbstractWindowsTest.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.oracle.tools.Option;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.console.CapturingApplicationConsole;

import com.oracle.tools.runtime.remote.Password;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.http.HttpBasedAuthentication;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.greaterThan;

import java.io.File;

import java.net.InetAddress;

import java.util.Queue;

/**
 * A base class for tests requiring a windows host.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class AbstractWindowsTest
{
    /**
     * Obtain a {@link RemotePlatform} connecting to the
     * current local platform.
     *
     * @return a {@link RemotePlatform} connecting to the
     *         current local platform
     *
     * @throws Exception if there is an error creating the {@link RemotePlatform}
     */
    public RemotePlatform getRemotePlatform(Option... options) throws Exception
    {
        return new RemotePlatform("Remote",
                                  InetAddress.getByName(getRemoteHostName()),
                                  getRemotePort(),
                                  getRemoteUserName(),
                                  getRemoteAuthentication(),
                                  options);
    }


    /**
     * Obtains the remote server host name that the tests will run against.
     * <p>
     * By default this will use the "oracletools.remote.windows.hostname" system property
     * to determine the host name and if this is not defined, it will default to the
     * host address of the {@link LocalPlatform}.
     *
     * @return  the remote server host
     */
    public String getRemoteHostName()
    {
        String defaultHost = LocalPlatform.getInstance().getAddress().getHostAddress();

        return System.getProperty("oracletools.remote.windows.hostname", defaultHost);
    }


    /**
     * Obtains the remote server port name that the tests will run against.
     * <p>
     * By default this will use the "oracletools.remote.windows.port" system property
     * to determine the host name and if this is not defined, it will default to
     * the value returned by the {@link #getDefaultPort()} method.
     *
     * @return  the remote server host
     */
    public int getRemotePort()
    {
        String portValue = System.getProperty("oracletools.remote.windows.port", String.valueOf(getDefaultPort()));

        return Integer.parseInt(portValue);
    }


    /**
     * Obtain the default port to use to connect to the remote host
     * if one is not specified by the "oracletools.remote.windows.port" system
     * property.
     * </p>
     * This implementation returns the default {@link WindowsSession#DEFAULT_WINRM_PORT}.
     *
     * @return the default port to use to connect to the remote host
     *         if one is not specified by the "oracletools.remote.windows.port"
     *         system property
     */
    public int getDefaultPort()
    {
        return WindowsSession.DEFAULT_WINRM_PORT;
    }


    /**
     * Obtains the user name that will be used for connecting and running tests
     * on the remote server.
     * <p>
     * By default this will use the "oracletools.remote.windows.username" system property
     * to determine the user name and if this is not defined, it will default to the "user.name"
     * system property.
     *
     * @return  the remote server user name
     */
    public String getRemoteUserName()
    {
        return System.getProperty("oracletools.remote.windows.username", System.getProperty("user.name"));
    }


    /**
     * Obtains the remote {@link HttpBasedAuthentication} to use for connection
     * to the Windows host.
     * </p>
     * The oracletools.remote.windows.password System property will be used to obtain
     * the password. If this property is not set this method will return null.
     *
     * @return  the {@link HttpBasedAuthentication} to authenticate connections to the
     *          Windows host
     */
    public HttpBasedAuthentication getRemoteAuthentication()
    {
        String password = System.getProperty("oracletools.remote.windows.password");

        if (password == null || password.isEmpty())
        {
            return null;
        }

        return new Password(password);
    }


    /**
     * Determine whether the current O/S is Windows and that
     * powershell.exe exists on the system.
     *
     * @return true if powershell.exe exists otherwise false
     */
    public static boolean powershellPresent()
    {
        return isWindows() && new File("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe").exists();
    }


    /**
     * If powershell.exe exists then return the version
     * of PowerShell installed.
     *
     * @return the version of PowerShell installed
     */
    public static double getPowershellVersion(Platform platform)
    {
        if (!powershellPresent())
        {
            return -1.0;
        }

        double                      version = -1.0;
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        SimpleApplicationSchema schema =
            new SimpleApplicationSchema("powershell").addArgument("-Command")
                .addArgument("&{$PSVersionTable.PSVersion}");

        try (SimpleApplication ignored = platform.realize("PSVersionCheck", schema, console))
        {
            try
            {
                Eventually.assertThat(invoking(console).getCapturedOutputLines().size(), is(greaterThan(3)));
            }
            catch (AssertionError assertionError)
            {
                // ignored
            }
        }

        Queue<String> lines = console.getCapturedOutputLines();

        if (lines.size() >= 4)
        {
            lines.poll();
            lines.poll();
            lines.poll();

            String[] versions = lines.poll().split("(\\s)+");

            try
            {
                version = Double.parseDouble(versions[0] + '.' + versions[1]);
            }
            catch (NumberFormatException e)
            {
                // ignored
            }
        }

        return version;
    }


    /**
     * Determine whether the local O/S is Windows.
     *
     * @return true if the local O/S is Windows, otherwise false
     */
    public static boolean isWindows()
    {
        return System.getProperty("os.name", "").startsWith("Win");
    }


    /**
     * Determine whether WinRM is running on the local platform.
     *
     * @return true if WinRM is running, otherwise false.
     */
    public static boolean hasWinRM()
    {
        if (!isWindows())
        {
            return false;
        }

        int                         exitCode = -1;
        Platform                    platform = LocalPlatform.getInstance();
        CapturingApplicationConsole console  = new CapturingApplicationConsole();
        SimpleApplicationSchema schema =
                new SimpleApplicationSchema("cmd.exe").addArgument("/C").addArgument("winrm").addArgument("enumerate")
                .addArgument("winrm/config/listener");

        try (SimpleApplication application = platform.realize("WinRMCheck", schema, console))
        {
            try
            {
                exitCode = application.waitFor();
            }
            catch (AssertionError assertionError)
            {
                // ignored
            }
        }

        Queue<String> lines = console.getCapturedOutputLines();

        return exitCode == 0 && lines.size() > 0 && lines.poll().startsWith("Listener");
    }
}
