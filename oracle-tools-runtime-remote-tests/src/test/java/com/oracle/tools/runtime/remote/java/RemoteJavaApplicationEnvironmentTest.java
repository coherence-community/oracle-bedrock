/*
 * File: RemoteJavaApplicationEnvironmentTest.java
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

package com.oracle.tools.runtime.remote.java;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.LocalPlatform;

import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import com.oracle.tools.runtime.java.profiles.RemoteDebugging;

import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;

import java.util.List;

/**
 * @author Jonathan Knight
 */
public class RemoteJavaApplicationEnvironmentTest
{
    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithDefaultModeAsServer() throws Exception
    {
        RemoteDebugging             remoteDebugging = RemoteDebugging.enabled();

        Options                     options         = new Options(remoteDebugging);

        RemotePlatform              platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());
        InetAddress                 address         = InetAddress.getLoopbackAddress();

        when(platform.getAddress()).thenReturn(address);

        RemoteJavaApplicationEnvironment env          = new RemoteJavaApplicationEnvironment(schema, platform, options);

        List<String>                     arguments    = env.getRemoteCommandArguments(InetAddress.getLocalHost());

        String                           debugCommand = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n";

        assertThat(arguments, hasItem(containsString(debugCommand)));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsAttachToDebugger() throws Exception
    {
        int debuggerPort = 5005;
        RemoteDebugging remoteDebugging =
            RemoteDebugging.enabled().attach()
            .at(new RemoteDebugging.TransportAddress(LocalPlatform.getInstance().getAddress(),
                                                     debuggerPort));
        Options                          options   = new Options(remoteDebugging);

        RemotePlatform                   platform  = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env       = new RemoteJavaApplicationEnvironment(schema, platform, options);

        List<String>                     arguments = env.getRemoteCommandArguments(InetAddress.getLocalHost());

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=%s:%d",
                                            LocalPlatform.getInstance().getAddress().getHostName(),
                                            debuggerPort);

        assertThat(arguments, hasItem(containsString(debugCommand)));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsAttachToDebuggerWithSpecifiedAddress() throws Exception
    {
        InetAddress address      = InetAddress.getLocalHost();
        int         debuggerPort = 5005;
        RemoteDebugging remoteDebugging =
            RemoteDebugging.enabled().attach().at(new RemoteDebugging.TransportAddress(address,
                                                                                       debuggerPort));
        Options                          options   = new Options(remoteDebugging);

        RemotePlatform                   platform  = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env       = new RemoteJavaApplicationEnvironment(schema, platform, options);

        List<String>                     arguments = env.getRemoteCommandArguments(InetAddress.getLocalHost());

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=%s:%d",
                                            address.getHostName(),
                                            debuggerPort);

        assertThat(arguments, hasItem(containsString(debugCommand)));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsListenForDebugger() throws Exception
    {
        RemoteDebugging             remoteDebugging = RemoteDebugging.enabled().listen();
        Options                     options         = new Options(remoteDebugging);

        RemotePlatform              platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());
        InetAddress                 address         = InetAddress.getLoopbackAddress();

        when(platform.getAddress()).thenReturn(address);

        RemoteJavaApplicationEnvironment env          = new RemoteJavaApplicationEnvironment(schema, platform, options);

        List<String>                     arguments    = env.getRemoteCommandArguments(InetAddress.getLocalHost());

        String                           debugCommand = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n";

        assertThat(arguments, hasItem(containsString(debugCommand)));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithSuspend() throws Exception
    {
        RemoteDebugging             remoteDebugging = RemoteDebugging.enabled().startSuspended(true);
        Options                     options         = new Options(remoteDebugging);

        RemotePlatform              platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());
        InetAddress                 address         = InetAddress.getLoopbackAddress();

        when(platform.getAddress()).thenReturn(address);

        RemoteJavaApplicationEnvironment env          = new RemoteJavaApplicationEnvironment(schema, platform, options);

        List<String>                     arguments    = env.getRemoteCommandArguments(InetAddress.getLocalHost());

        String                           debugCommand = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y";

        assertThat(arguments, hasItem(containsString(debugCommand)));
    }


    /**
     * Should add the remote debug options if not enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotAddDebugOption() throws Exception
    {
        RemoteDebugging                  remoteDebugging = RemoteDebugging.disabled();
        Options                          options         = new Options(remoteDebugging);

        RemotePlatform                   platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema, platform, options);

        List<String>                     arguments       = env.getRemoteCommandArguments(InetAddress.getLocalHost());

        assertThat(arguments, not(hasItem(containsString("-agentlib:jdwp"))));
    }
}
