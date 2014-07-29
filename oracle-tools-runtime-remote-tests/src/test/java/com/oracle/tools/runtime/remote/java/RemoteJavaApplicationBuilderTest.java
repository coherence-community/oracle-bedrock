/*
 * File: RemoteJavaApplicationBuilderTest.java
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

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.RemoteDebuggingMode;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationBuilderTest;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;

import com.oracle.tools.util.Capture;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.util.LinkedList;
import java.util.List;

/**
 * Functional tests for {@link RemoteJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationBuilderTest extends AbstractRemoteApplicationBuilderTest
{
    /**
     * Ensure that we can launch Java remotely.
     */
    @Test
    public void shouldLaunchJavaApplicationRemotely() throws IOException, InterruptedException
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // sleep only for 3 seconds
        schema.addArgument("3");

        RemoteJavaApplicationBuilder<SimpleJavaApplication> builder = createBuilder();

        try (SimpleJavaApplication application = builder.realize(schema, "Java", new SystemApplicationConsole()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }


    /**
     * Should run the application with remote debug enabled if set in schema
     * with start suspended set to false.
     */
    @Test
    public void shouldSetRemoteDebugEnabledSuspendedIsFalse() throws Exception
    {
        Capture<Integer> port = new Capture<>(LocalPlatform.getInstance().getAvailablePorts());
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).addArgument("300")
                .setRemoteDebuggingEnabled(true).setRemoteDebuggingMode(RemoteDebuggingMode.LISTEN_FOR_DEBUGGER)
                .setRemoteDebuggingStartSuspended(false).setRemoteDebugPorts(port);

        CapturingApplicationConsole                         console = new CapturingApplicationConsole();
        LinkedList<String>                                  lines   = console.getCapturedOutputLines();

        RemoteJavaApplicationBuilder<SimpleJavaApplication> builder = createBuilder();

        try (SimpleJavaApplication application = builder.realize(schema, "Java", console))
        {
            Eventually.assertThat(lines, hasItem(startsWith("Now sleeping")));

            List<String> args = application.submit(new GetProgramArgs());

            String debugArg = null;

            for (String arg : args)
            {
                if (arg.startsWith("-agentlib:jdwp="))
                {
                    debugArg = arg.toLowerCase();
                    break;
                }
            }

            Assert.assertThat(debugArg, is(notNullValue()));
            Assert.assertThat(debugArg, startsWith("-agentlib:jdwp=transport=dt_socket,"));
            Assert.assertThat(debugArg, containsString(String.format(",address=%d", port.get())));
            Assert.assertThat(debugArg, containsString(",suspend=n"));
            Assert.assertThat(debugArg, containsString(",server=y"));
        }
    }


    /**
     * Should run the application with remote debug enabled if set in schema
     * with start suspended set to false.
     */
    @Test
    public void shouldSetRemoteDebugEnabledSuspendedIsTrue() throws Exception
    {
        // Make sure we can run the JDB debugger otherwise we cannot run this test
        Assume.assumeThat(hasJDB(), is(true));

        Capture<Integer> port = new Capture<>(LocalPlatform.getInstance().getAvailablePorts());
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).setRemoteDebuggingEnabled(true)
                .setRemoteDebuggingStartSuspended(true).setRemoteDebugPorts(port);

        CapturingApplicationConsole console  = new CapturingApplicationConsole();
        LinkedList<String>          lines    = console.getCapturedOutputLines();

        RemotePlatform              platform = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize(schema, "Java", console))
        {
            assertCanConnectDebuggerToApplication(application);

            Eventually.assertThat(lines, hasItem(startsWith("Now sleeping")));

            List<String> args     = application.submit(new GetProgramArgs());
            String       debugArg = null;

            for (String arg : args)
            {
                if (arg.startsWith("-agentlib:jdwp="))
                {
                    debugArg = arg.toLowerCase();
                    break;
                }
            }

            Assert.assertThat(debugArg, is(notNullValue()));
            Assert.assertThat(debugArg, startsWith("-agentlib:jdwp=transport=dt_socket,"));
            Assert.assertThat(debugArg, containsString(String.format(",address=%d", port.get())));
            Assert.assertThat(debugArg, containsString(",suspend=y"));
            Assert.assertThat(debugArg, containsString(",server=y"));
        }
    }


    /**
     * Start the application with remote debugging enabled and mode set to
     * {@link com.oracle.tools.runtime.java.RemoteDebuggingMode#ATTACH_TO_DEBUGGER}
     * and assert the process connects back to the debugger
     */
    @Test
    public void shouldEnableRemoteDebugAndConnectBackToDebugger() throws Exception
    {
        // Make sure we can run the JDB debugger otherwise we cannot run this test
        Assume.assumeThat(hasJDB(), is(true));

        Capture<Integer> debugPort = new Capture<Integer>(LocalPlatform.getInstance().getAvailablePorts());

        SimpleApplicationSchema jdbSchema =
            new SimpleApplicationSchema("jdb").addArgument("-listen").addArgument(String.valueOf(debugPort.get()));

        CapturingApplicationConsole jdbConsole = new CapturingApplicationConsole();
        LinkedList<String>          jdbOutput  = jdbConsole.getCapturedOutputLines();

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize(jdbSchema, "JDB", jdbConsole))
        {
            Eventually.assertThat("JDB did not start properly",
                                  jdbOutput,
                                  hasItem(startsWith("Listening at address:")));

            SimpleJavaApplicationSchema schema =
                new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).setRemoteDebuggingEnabled(true)
                    .setRemoteDebuggingStartSuspended(false).setRemoteDebugPort(debugPort.get())
                    .setRemoteDebuggingMode(RemoteDebuggingMode.ATTACH_TO_DEBUGGER);

            CapturingApplicationConsole console  = new CapturingApplicationConsole();
            LinkedList<String>          lines    = console.getCapturedOutputLines();

            RemotePlatform              platform = getRemotePlatform();

            try (SimpleJavaApplication application = platform.realize(schema, "Java", console))
            {
                Eventually.assertThat(lines, hasItem(startsWith("Now sleeping")));
                Eventually.assertThat("Application did not connect back to JDB",
                                      jdbOutput,
                                      hasItem(containsString("VM Started:")));
            }
        }
    }


    protected void assertCanConnectDebuggerToApplication(JavaApplication application) throws Exception
    {
        InetSocketAddress socket = application.getRemoteDebugSocket();

        Assert.assertThat(socket, is(notNullValue()));

        SimpleApplicationSchema schema =
            new SimpleApplicationSchema("jdb").addArgument("-attach").addArgument(socket.getHostName() + ":"
                                        + socket.getPort());

        CapturingApplicationConsole console = new CapturingApplicationConsole();
        LinkedList<String>          lines   = console.getCapturedOutputLines();

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize(schema, "JDB", console))
        {
            Eventually.assertThat(lines, hasItem(startsWith("VM Started")));

            console.getInputWriter().println("run");
            console.getInputWriter().println("quit");
        }
    }


    /**
     * Should run the application with remote debug disabled.
     *
     * NOTE: This test is ignored when running in an IDE in debug mode
     * as the JavaVirtualMachine class will pick up the debug settings
     * and pass them on to the process causing the test to fail
     */
    @Test
    public void shouldSetRemoteDebugDisabled() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).addArgument("30")
                .setRemoteDebuggingEnabled(false);

        try (SimpleJavaApplication app = LocalPlatform.getInstance().realize(schema,
                                                                             "TestApp",
                                                                             new SystemApplicationConsole()))
        {
            List<String> args = app.submit(new GetProgramArgs());

            String debugArg = null;

            for (String arg : args)
            {
                if (arg.startsWith("-agentlib:jdwp="))
                {
                    debugArg = arg.toLowerCase();
                    break;
                }
            }

            Assert.assertThat(debugArg, is(nullValue()));
        }
    }


    /**
     * Detect whether the Java Debugger application is present.
     *
     * @return true if JDB is present
     */
    protected boolean hasJDB() throws Exception
    {
        SimpleApplicationSchema     schema  = new SimpleApplicationSchema("jdb").addArgument("-version");

        CapturingApplicationConsole console = new CapturingApplicationConsole();
        LinkedList<String>          lines   = console.getCapturedOutputLines();

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize(schema, "JDB", console))
        {
            Eventually.assertThat(lines, hasItem(startsWith("This is jdb version")));

            return true;
        }
        catch (Throwable t)
        {
            // ignored
        }

        return true;
    }


    protected RemoteJavaApplicationBuilder<SimpleJavaApplication> createBuilder()
    {
        RemoteJavaApplicationBuilder<SimpleJavaApplication> builder =
            new RemoteJavaApplicationBuilder<SimpleJavaApplication>(getRemoteHostName(),
                                                                    getRemoteUserName(),
                                                                    getRemoteAuthentication());

        builder.setStrictHostChecking(false);
        builder.setAutoDeployEnabled(true);
        builder.setJavaHome(System.getProperty("java.home"));

        return builder;
    }


    protected RemotePlatform getRemotePlatform() throws Exception
    {
        return new RemotePlatform("Remote",
                                  InetAddress.getByName(getRemoteHostName()),
                                  22,
                                  getRemoteUserName(),
                                  getRemoteAuthentication());
    }
}
