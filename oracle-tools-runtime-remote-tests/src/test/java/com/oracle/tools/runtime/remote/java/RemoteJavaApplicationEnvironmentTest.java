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

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.java.RemoteDebuggingMode;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;
import org.junit.Test;

import java.util.Collections;

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
        int                         port     = 5005;
        RemotePlatform              platform = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                 .setRemoteDebuggingEnabled(true)
                                                 .setRemoteDebugPort(port);

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema,
                                                                                    '/',
                                                                                    ':',
                                                                                    false,
                                                                                    true,
                                                                                    Collections.emptySet(),
                                                                                    "/java_home",
                                                                                    platform);

        String command = env.getRemoteCommandToExecute();

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%d", port);

        assertThat(command, containsString(debugCommand));
    }

    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsAttachToDebugger() throws Exception
    {
        int                         port     = 5005;
        RemotePlatform              platform = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                 .setRemoteDebuggingEnabled(true)
                                                 .setRemoteDebugPort(port)
                                                 .setRemoteDebuggingMode(RemoteDebuggingMode.ATTACH_TO_DEBUGGER);

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema,
                                                                                    '/',
                                                                                    ':',
                                                                                    false,
                                                                                    true,
                                                                                    Collections.emptySet(),
                                                                                    "/java_home",
                                                                                    platform);

        String command = env.getRemoteCommandToExecute();

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=%s:%d",
                                            LocalPlatform.getInstance().getHostName(),
                                            port);

        assertThat(command, containsString(debugCommand));
    }

    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithModeAsListenForDebugger() throws Exception
    {
        int                         port     = 5005;
        RemotePlatform              platform = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                 .setRemoteDebuggingEnabled(true)
                                                 .setRemoteDebugPort(port)
                                                 .setRemoteDebuggingMode(RemoteDebuggingMode.LISTEN_FOR_DEBUGGER);

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema,
                                                                                    '/',
                                                                                    ':',
                                                                                    false,
                                                                                    true,
                                                                                    Collections.emptySet(),
                                                                                    "/java_home",
                                                                                    platform);

        String command = env.getRemoteCommandToExecute();

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%d", port);

        assertThat(command, containsString(debugCommand));
    }

    /**
     * Should add the remote debug options if enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOptionWithSuspend() throws Exception
    {
        int                         port     = 5005;
        RemotePlatform              platform = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema   = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                 .setRemoteDebuggingEnabled(true)
                                                 .setRemoteDebuggingStartSuspended(true)
                                                 .setRemoteDebugPort(port);

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema,
                                                                                    '/',
                                                                                    ':',
                                                                                    false,
                                                                                    true,
                                                                                    Collections.emptySet(),
                                                                                    "/java_home",
                                                                                    platform);

        String command = env.getRemoteCommandToExecute();

        String debugCommand = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d", port);

        assertThat(command, containsString(debugCommand));
    }

    /**
     * Should add the remote debug options if not enabled on the schema
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotAddDebugOption() throws Exception
    {
        int                         port     = 5005;
        RemotePlatform              platform = mock(RemotePlatform.class);
        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                 .setRemoteDebuggingEnabled(false)
                                                 .setRemoteDebugPort(port);

        RemoteJavaApplicationEnvironment env = new RemoteJavaApplicationEnvironment(schema,
                                                                                    '/',
                                                                                    ':',
                                                                                    false,
                                                                                    true,
                                                                                    Collections.emptySet(),
                                                                                    "/java_home",
                                                                                    platform);

        String command = env.getRemoteCommandToExecute();

        assertThat(command, not(containsString("-agentlib:jdwp")));
    }
}
