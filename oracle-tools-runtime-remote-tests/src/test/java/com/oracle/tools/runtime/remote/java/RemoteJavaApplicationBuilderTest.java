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
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.options.RemoteDebugging;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationBuilderTest;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;
import com.oracle.tools.runtime.remote.options.StrictHostChecking;

import com.oracle.tools.util.Capture;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

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
    public void shouldLaunchJavaApplicationRemotely() throws Exception
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // sleep only for 3 seconds
        schema.addArgument("3");

        RemotePlatform platform = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java",
                schema,
                new SystemApplicationConsole(),
                StrictHostChecking.disabled()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }

    /**
     * Ensure that we can launch Java remotely using a Java Home.
     */
    @Test
    public void shouldLaunchJavaApplicationUsingJavaHome() throws Exception
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // sleep only for 3 seconds
        schema.addArgument("3");

        // use the JavaHome of this process
        schema.setOptions(JavaHome.at(System.getProperty("java.home")));

        RemotePlatform platform = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java",
                                                                  schema,
                                                                  new SystemApplicationConsole(),
                                                                  StrictHostChecking.disabled()))
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
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).addArgument("300");

        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        RemoteDebugging remoteDebugging      = RemoteDebugging.enabled().startSuspended(false).listenForDebugger();

        RemotePlatform              platform = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java",
                                                                  schema,
                                                                  console,
                                                                  remoteDebugging,
                                                                  StrictHostChecking.disabled()))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

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

        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        CapturingApplicationConsole console         = new CapturingApplicationConsole();
        RemotePlatform              platform        = getRemotePlatform();
        RemoteDebugging             remoteDebugging = RemoteDebugging.enabled().startSuspended(true);

        try (SimpleJavaApplication application = platform.realize("Java",
                                                                  schema,
                                                                  console,
                                                                  remoteDebugging,
                                                                  StrictHostChecking.disabled()))
        {
            assertCanConnectDebuggerToApplication(application);

            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

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
            Assert.assertThat(debugArg, containsString(",suspend=y"));
            Assert.assertThat(debugArg, containsString(",server=y"));
        }
    }


    /**
     * Start the application with remote debugging enabled using {@link RemoteDebugging.Behavior#ATTACH_TO_DEBUGGER}
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

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize("JDB", jdbSchema, jdbConsole))
        {
            Eventually.assertThat("JDB did not start properly",
                                  invoking(jdbConsole).getCapturedOutputLines(),
                                  hasItem(startsWith("Listening at address:")));

            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

            RemoteDebugging remoteDebugging =
                RemoteDebugging.enabled().startSuspended(false).attachToDebugger(debugPort.get());

            CapturingApplicationConsole console  = new CapturingApplicationConsole();

            RemotePlatform              platform = getRemotePlatform();

            try (SimpleJavaApplication application = platform.realize("Java",
                                                                      schema,
                                                                      console,
                                                                      remoteDebugging,
                                                                      StrictHostChecking.disabled()))
            {
                Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));
                Eventually.assertThat("Application did not connect back to JDB",
                                      invoking(jdbConsole).getCapturedOutputLines(),
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

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize("JDB", schema, console))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("VM Started")));

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
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).addArgument("30");

        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        RemoteDebugging remoteDebugging      = RemoteDebugging.disabled();

        RemotePlatform              platform = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java",
                                                                  schema,
                                                                  console,
                                                                  remoteDebugging,
                                                                  StrictHostChecking.disabled()))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

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

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize("JDB", schema, console))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(),
                                  hasItem(startsWith("This is jdb version")));

            return true;
        }
        catch (Throwable t)
        {
            // ignored
        }

        return true;
    }


    protected RemotePlatform getRemotePlatform() throws Exception
    {
        return new RemotePlatform("Remote",
                                  InetAddress.getByName(getRemoteHostName()),
                                  getRemoteUserName(),
                                  getRemoteAuthentication());
    }
}
