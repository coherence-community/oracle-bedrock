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
import com.oracle.tools.runtime.java.options.RemoteDebugging;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author jk 2014.07.04
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
        RemoteDebugging                  remoteDebugging = RemoteDebugging.enabled();

        Options                          options         = new Options(remoteDebugging);

        RemotePlatform                   platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema, platform, options);

        String                           command         = env.getRemoteCommandToExecute(InetAddress.getLocalHost());

        String                           debugCommand    = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n";

        assertThat(command, containsString(debugCommand));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsAttachToDebugger() throws Exception
    {
        int                              debuggerPort    = 5005;
        RemoteDebugging                  remoteDebugging = RemoteDebugging.enabled().attachToDebugger(debuggerPort);
        Options                          options         = new Options(remoteDebugging);

        RemotePlatform                   platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema, platform, options);

        String                           command         = env.getRemoteCommandToExecute(InetAddress.getLocalHost());

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=%s:%d",
                                            LocalPlatform.getInstance().getHostName(),
                                            debuggerPort);

        assertThat(command, containsString(debugCommand));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsListenForDebugger() throws Exception
    {
        RemoteDebugging                  remoteDebugging = RemoteDebugging.enabled().listenForDebugger();
        Options                          options         = new Options(remoteDebugging);

        RemotePlatform                   platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema, platform, options);

        String                           command         = env.getRemoteCommandToExecute(InetAddress.getLocalHost());

        String                           debugCommand    = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n";

        assertThat(command, containsString(debugCommand));
    }


    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithSuspend() throws Exception
    {
        RemoteDebugging                  remoteDebugging = RemoteDebugging.enabled().startSuspended(true);
        Options                          options         = new Options(remoteDebugging);

        RemotePlatform                   platform        = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema      schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema, platform, options);

        String                           command         = env.getRemoteCommandToExecute(InetAddress.getLocalHost());

        String                           debugCommand    = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y";

        assertThat(command, containsString(debugCommand));
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

        String                           command         = env.getRemoteCommandToExecute(InetAddress.getLocalHost());

        assertThat(command, not(containsString("-agentlib:jdwp")));
    }
}
