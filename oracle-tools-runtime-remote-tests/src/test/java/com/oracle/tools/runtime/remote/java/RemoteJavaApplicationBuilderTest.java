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

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.runnable.RuntimeExit;
import com.oracle.tools.runtime.concurrent.runnable.RuntimeHalt;

import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.LocalJavaApplicationBuilder;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.profiles.RemoteDebugging;

import com.oracle.tools.runtime.options.PlatformSeparators;
import com.oracle.tools.runtime.options.WorkingDirectory;
import com.oracle.tools.runtime.remote.AbstractRemoteTest;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;

import com.oracle.tools.util.Capture;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.util.List;

/**
 * Functional tests for {@link RemoteJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationBuilderTest extends AbstractRemoteTest
{
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


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

        try (SimpleJavaApplication application = platform.realize("Java", schema, new SystemApplicationConsole()))
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

        try (SimpleJavaApplication application = platform.realize("Java", schema, new SystemApplicationConsole()))
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

        CapturingApplicationConsole console         = new CapturingApplicationConsole();

        RemoteDebugging             remoteDebugging = RemoteDebugging.enabled().startSuspended(false).listen();

        RemotePlatform              platform        = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java", schema, console, remoteDebugging))
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

        SimpleJavaApplicationSchema schema   = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        CapturingApplicationConsole console  = new CapturingApplicationConsole();
        RemotePlatform              platform = getRemotePlatform();
        RemoteDebugging remoteDebugging =
            RemoteDebugging.enabled().startSuspended(true)
            .at(new RemoteDebugging.TransportAddress(LocalPlatform.getInstance().getAvailablePorts()));
        JavaHome javaHome = JavaHome.at(System.getProperty("java.home"));

        try (SimpleJavaApplication application = platform.realize("Java", schema, console, remoteDebugging, javaHome))
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

        InetAddress      debugAddress = LocalPlatform.getInstance().getAddress();
        Capture<Integer> debugPort    = new Capture<>(LocalPlatform.getInstance().getAvailablePorts());

        SimpleApplicationSchema jdbSchema =
            new SimpleApplicationSchema(findJDB()).addArgument("-connect")
            .addArgument(String.format("com.sun.jdi.SocketListen:port=%d",
                                       debugPort.get()));

        CapturingApplicationConsole jdbConsole = new CapturingApplicationConsole();

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize("JDB", jdbSchema, jdbConsole))
        {
            Eventually.assertThat("JDB did not start properly",
                                  invoking(jdbConsole).getCapturedOutputLines(),
                                  hasItem(startsWith("Listening at address:")));

            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

            RemoteDebugging remoteDebugging =
                RemoteDebugging.enabled().attach().at(new RemoteDebugging.TransportAddress(debugAddress,
                                                                                           debugPort));

            CapturingApplicationConsole console  = new CapturingApplicationConsole();

            RemotePlatform              platform = getRemotePlatform();

            try (SimpleJavaApplication application = platform.realize("Java", schema, console, remoteDebugging))
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
        RemoteDebugging.TransportAddress transportAddress =
            application.getOptions().get(RemoteDebugging.TransportAddress.class);

        InetSocketAddress socket = transportAddress.getSocketAddress();

        Assert.assertThat(socket, is(notNullValue()));

        Eventually.assertThat(invoking(this).isListening(socket), is(true));

        SimpleApplicationSchema schema =
            new SimpleApplicationSchema(findJDB()).addArgument("-connect")
            .addArgument(String.format("com.sun.jdi.SocketAttach:hostname=%s,port=%d",
                                       socket.getAddress().getHostAddress(),
                                       socket.getPort()));

        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (SimpleApplication jdb = LocalPlatform.getInstance().realize("JDB", schema, console))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("VM Started")));

            console.getInputWriter().println("run");
            console.getInputWriter().println("quit");
        }
    }


    public boolean isListening(InetSocketAddress socket)
    {
        try (Socket s = new Socket())
        {
            s.connect(socket, 100);

            return true;
        }
        catch (IOException e)
        {
            return false;
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

        CapturingApplicationConsole console         = new CapturingApplicationConsole();

        RemoteDebugging             remoteDebugging = RemoteDebugging.disabled();

        RemotePlatform              platform        = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java", schema, console, remoteDebugging))
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
     * Ensure that local {@link JavaApplication}s can be terminated using {@link RuntimeExit}.
     */
    @Test
    public void shouldTerminateUsingRuntimeExit() throws Exception
    {
        // define the SleepingApplication
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).setPreferIPv4(true);

        // sleep only for 3 seconds
        schema.addArgument("3");

        // build and start the SleepingApplication
        Platform           platform = getRemotePlatform();

        ApplicationConsole console  = new SystemApplicationConsole();

        try (SimpleJavaApplication application = platform.realize("sleeping", schema, console))
        {
            application.close(RuntimeExit.withExitCode(42));

            int exitStatus = application.waitFor();

            assertThat(exitStatus, is(42));
        }
    }


    /**
     * Ensure that local {@link JavaApplication}s can be terminated using {@link RuntimeHalt}.
     */
    @Test
    public void shouldTerminateUsingRuntimeHalt() throws Exception
    {
        // define the SleepingApplication
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).setPreferIPv4(true);

        // sleep only for 3 seconds
        schema.addArgument("3");

        // build and start the SleepingApplication
        Platform           platform = getRemotePlatform();

        ApplicationConsole console  = new SystemApplicationConsole();

        try (SimpleJavaApplication application = platform.realize("sleeping", schema, console))
        {
            application.close(RuntimeHalt.withExitCode(42));

            int exitStatus = application.waitFor();

            assertThat(exitStatus, is(42));
        }
    }


    /**
     * Ensure that {@link LocalJavaApplicationBuilder}s correctly inherits arguments based on system properties
     * starting with oracletools.runtime.inherit.xxx=
     */
    @Test
    public void shouldInheritOracleToolsRuntimeProperties() throws Exception
    {
        // define the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        schema.setPreferIPv4(true);

        System.getProperties().setProperty("oracletools.runtime.inherit.property", "-Dmessage=hello");

        // build and start the SleepingApplication
        Platform           platform = getRemotePlatform();

        ApplicationConsole console  = new SystemApplicationConsole();

        try (SimpleJavaApplication application = platform.realize("sleeping", schema, console))
        {
            String message = application.submit(new GetSystemProperty("message"));

            Assert.assertThat(message, is("hello"));
        }
        finally
        {
            System.getProperties().remove("oracletools.runtime.inherit.property");
        }
    }


    @Test
    public void shouldSetWorkingDirectory() throws Exception
    {
        String                      appName = "sleeping";
        File                        folder  = temporaryFolder.newFolder();
        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                .setWorkingDirectory(WorkingDirectory.subDirectoryOf(folder));

        String appNameSanitized  = PlatformSeparators.autoDetect().asSanitizedFileName(appName);
        File   expectedDirectory = new File(folder, appNameSanitized);

        // build and start the SleepingApplication
        Platform           platform = getRemotePlatform();

        ApplicationConsole console  = new SystemApplicationConsole();

        try (SimpleJavaApplication application = platform.realize(appName, schema, console))
        {
            String dir = application.submit(new GetWorkingDirectory());

            Assert.assertThat(dir, is(expectedDirectory.getCanonicalPath()));

            WorkingDirectory workingDir = application.getOptions().get(WorkingDirectory.class);

            Assert.assertThat(workingDir, is(notNullValue()));
            Assert.assertThat(workingDir.getValue(), is((Object) expectedDirectory));
        }
    }


    /**
     * Detect whether the Java Debugger application is present.
     *
     * @return true if JDB is present
     */
    protected boolean hasJDB() throws Exception
    {
        String                      command = findJDB();
        SimpleApplicationSchema     schema  = new SimpleApplicationSchema(command).addArgument("-version");

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


    protected String findJDB()
    {
        String javaHome = System.getProperty("java.home");

        if (javaHome.endsWith("jre"))
        {
            javaHome = javaHome + File.separator + "..";
        }

        String jdbFileName = javaHome + File.separator + "bin" + File.separator + "jdb";

        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {
            jdbFileName = jdbFileName + ".exe";
        }

        return jdbFileName;
    }
}
