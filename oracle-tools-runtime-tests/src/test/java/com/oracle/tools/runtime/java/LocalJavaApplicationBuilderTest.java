/*
 * File: LocalJavaApplicationBuilderTest.java
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

package com.oracle.tools.runtime.java;

import classloader.applications.ParentApplication;
import classloader.applications.SleepingApplication;
import com.oracle.tools.deferred.Eventually;
import com.oracle.tools.deferred.listener.DeferredCompletionListener;
import com.oracle.tools.io.NetworkHelper;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;
import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteExecutorListener;
import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.runnable.SystemExit;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutorTests;
import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.util.Capture;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Functional Tests for {@link LocalJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class LocalJavaApplicationBuilderTest extends AbstractJavaApplicationBuilderTest
{
    @Override
    public JavaApplicationBuilder<JavaApplication> newJavaApplicationBuilder()
    {
        return new LocalJavaApplicationBuilder<JavaApplication>();
    }


    /**
     * Ensure that the {@link #newJavaApplicationBuilder()} method is producing
     * the expected type of builder.
     */
    @Test
    public void shouldBeCorrectJavaApplicationBuilder()
    {
        assertThat(newJavaApplicationBuilder(), is(instanceOf(LocalJavaApplicationBuilder.class)));
    }


    /**
     * Should run the application with remote debug enabled if set in schema
     * with start suspended set to false.
     */
    @Test
    public void shouldSetRemoteDebugEnabledSuspendedIsFalse() throws Exception
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                    .setRemoteDebuggingEnabled(true)
                                                    .setRemoteDebuggingStartSuspended(false)
                                                    .setRemoteDebugPorts(LocalPlatform.getInstance().getAvailablePorts());

        SimpleJavaApplication app = LocalPlatform.getInstance().realize(schema, "TestApp", new SystemApplicationConsole());

        List<String> args = app.submit(new GetProgramArgs());

        app.close();

        String debugArg = null;
        for (String arg : args)
        {
            if (arg.startsWith("-agentlib:jdwp="))
            {
                debugArg = arg.toLowerCase();
                break;
            }
        }

        assertThat(debugArg, is(notNullValue()));
        assertThat(debugArg, startsWith("-agentlib:jdwp=transport=dt_socket,"));
        assertThat(debugArg, containsString(String.format(",address=%s", LocalPlatform.getInstance().getHostName())));
        assertThat(debugArg, containsString(",suspend=n"));
        assertThat(debugArg, containsString(",server=y"));
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


        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                    .setRemoteDebuggingEnabled(true)
                                                    .setRemoteDebuggingStartSuspended(true)
                                                    .setRemoteDebugPorts(LocalPlatform.getInstance().getAvailablePorts());

        CapturingApplicationConsole console = new CapturingApplicationConsole();
        LinkedList<String>          lines   = console.getCapturedOutputLines();

        SimpleJavaApplication       app     = LocalPlatform.getInstance().realize(schema, "TestApp", console);


        assertCanConnectDebuggerToApplication(app);

        Eventually.assertThat(lines, hasItem(startsWith("Now sleeping")));

        List<String> args     = app.submit(new GetProgramArgs());
        String       debugArg = null;

        for (String arg : args)
        {
            if (arg.startsWith("-agentlib:jdwp="))
            {
                debugArg = arg.toLowerCase();
                break;
            }
        }

        app.close();

        assertThat(debugArg, is(notNullValue()));
        assertThat(debugArg, startsWith("-agentlib:jdwp=transport=dt_socket,"));
        assertThat(debugArg, containsString(String.format(",address=%s", LocalPlatform.getInstance().getHostName())));
        assertThat(debugArg, containsString(",suspend=y"));
        assertThat(debugArg, containsString(",server=y"));
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

        Capture<Integer>           debugPort    = new Capture<Integer>(LocalPlatform.getInstance().getAvailablePorts());

        SimpleApplicationSchema    jdbSchema    = new SimpleApplicationSchema("jdb")
                                                        .addArgument("-listen")
                                                        .addArgument(String.valueOf(debugPort.get()));

        CapturingApplicationConsole jdbConsole  = new CapturingApplicationConsole();
        LinkedList<String>          jdbOutput   = jdbConsole.getCapturedOutputLines();

        SimpleApplication           jdb         = LocalPlatform.getInstance().realize(jdbSchema, "JDB", jdbConsole);
        try
        {
            Eventually.assertThat("JDB did not start properly", jdbOutput, hasItem(startsWith("Listening at address:")));


            SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                            .setRemoteDebuggingEnabled(true)
                                                            .setRemoteDebuggingStartSuspended(false)
                                                            .setRemoteDebugPort(debugPort.get())
                                                            .setRemoteDebuggingMode(RemoteDebuggingMode.ATTACH_TO_DEBUGGER);

            CapturingApplicationConsole console = new CapturingApplicationConsole();
            LinkedList<String>          lines   = console.getCapturedOutputLines();

            SimpleJavaApplication       app     = LocalPlatform.getInstance().realize(schema, "TestApp", console);

            try
            {
                Eventually.assertThat(lines, hasItem(startsWith("Now sleeping")));
                Eventually.assertThat("Application did not connect back to JDB", jdbOutput, hasItem(containsString("VM Started:")));
            }
            finally
            {
                app.close();
            }
        }
        finally
        {
            jdb.close();
        }
    }


    protected void assertCanConnectDebuggerToApplication(JavaApplication application) throws Exception
    {
        InetSocketAddress socket = application.getRemoteDebugSocket();
        assertThat(socket, is(notNullValue()));

        SimpleApplicationSchema    schema   = new SimpleApplicationSchema("jdb")
                                                    .addArgument("-attach")
                                                    .addArgument(socket.getHostName() + ":" + socket.getPort());

        CapturingApplicationConsole console = new CapturingApplicationConsole();
        LinkedList<String>          lines   = console.getCapturedOutputLines();

        SimpleApplication           jdb     = LocalPlatform.getInstance().realize(schema, "JDB", console);

        try
        {
            Eventually.assertThat(lines, hasItem(startsWith("VM Started")));

            console.getInputWriter().println("run");
            console.getInputWriter().println("quit");
        }
        finally
        {
            jdb.close();
        }
    }

    protected boolean hasJDB() throws Exception
    {
        SimpleApplicationSchema    schema   = new SimpleApplicationSchema("jdb")
                                                    .addArgument("-version");

        CapturingApplicationConsole console = new CapturingApplicationConsole();
        LinkedList<String>          lines   = console.getCapturedOutputLines();
        SimpleApplication           jdb     = LocalPlatform.getInstance().realize(schema, "JDB", console);

        try
        {
            Eventually.assertThat(lines, hasItem(startsWith("This is jdb version")));
            return true;
        }
        catch (Throwable t)
        {
            // ignored
        }
        finally
        {
            jdb.close();
        }


        return true;
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
        Assume.assumeThat(JavaVirtualMachine.getInstance().shouldEnabledRemoteDebug(), is(false));

        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                    .setRemoteDebuggingEnabled(false);

        SimpleJavaApplication app = LocalPlatform.getInstance().realize(schema, "TestApp", new SystemApplicationConsole());

        List<String> args = app.submit(new GetProgramArgs());

        app.close();

        String debugArg = null;
        for (String arg : args)
        {
            if (arg.startsWith("-agentlib:jdwp="))
            {
                debugArg = arg.toLowerCase();
                break;
            }
        }

        assertThat(debugArg, is(nullValue()));
    }

    /**
     * Ensure that {@link LocalJavaApplicationBuilder}s in orphan mode
     * will leave orphan application processes running.
     */
    @Test
    public void shouldCreateOrphans()
    {
        RemoteExecutorServer  server            = new RemoteExecutorServer();
        SimpleJavaApplication parentApplication = null;

        try
        {
            // start a server that the child can connect too.
            server.open();

            // add a listener for the ClientApplication
            ClientApplicationListener listener = new ClientApplicationListener();

            server.addListener(listener);

            // define and start the ParentApplication (this will start a ChildApplication)
            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(ParentApplication.class.getName());

            schema.setSystemProperty("server.address",
                                     server.getInetAddress(NetworkHelper.LOOPBACK_ADDRESS).getHostAddress());
            schema.setSystemProperty("server.port", server.getPort());
            schema.setSystemProperty("orphan.children", true);

            LocalJavaApplicationBuilder<SimpleJavaApplication> builder =
                new LocalJavaApplicationBuilder<SimpleJavaApplication>();

            builder.setOrphansPermitted(false);

            ApplicationConsole console = new SystemApplicationConsole();

            parentApplication = builder.realize(schema, "parent", console);

            // wait for the ChildApplication to connect back to the ServerChannel
            Eventually.assertThat(invoking(listener).isOpened(), is(true));

            // close the ParentApplication
            parentApplication.close();

            // submit the child a request to prove that it's orphaned
            RemoteExecutor                     child    = listener.getExecutor();
            DeferredCompletionListener<String> response = new DeferredCompletionListener<String>(String.class);

            child.submit(new SocketBasedRemoteExecutorTests.PingPong(), response);

            Eventually.assertThat(response, is("PONG"));

            // shutdown the client (by invoking a System.exit internally)
            child.submit(new SystemExit());

            // wait for the ChildApplication to disconnect from the ServerChannel
            Eventually.assertThat(invoking(listener).isClosed(), is(true));
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (parentApplication != null)
            {
                parentApplication.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    /**
     * Ensure that {@link LocalJavaApplicationBuilder}s in orphan-less mode don't leave
     * orphan application processes running.
     */
    @Test
    public void shouldNotCreateOrphans()
    {
        RemoteExecutorServer  server            = new RemoteExecutorServer();
        SimpleJavaApplication parentApplication = null;

        try
        {
            // start a server that the child can connect too.
            server.open();

            // add a listener for the ClientApplication
            ClientApplicationListener listener = new ClientApplicationListener();

            server.addListener(listener);

            // define and start the ParentApplication (this will start a ChildApplication)
            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(ParentApplication.class.getName());

            schema.setSystemProperty("server.address",
                                     server.getInetAddress(NetworkHelper.LOOPBACK_ADDRESS).getHostAddress());
            schema.setSystemProperty("server.port", server.getPort());
            schema.setSystemProperty("orphan.children", false);

            LocalJavaApplicationBuilder<SimpleJavaApplication> builder =
                new LocalJavaApplicationBuilder<SimpleJavaApplication>();

            builder.setOrphansPermitted(false);

            ApplicationConsole console = new SystemApplicationConsole();

            parentApplication = builder.realize(schema, "parent", console);

            // wait for the ChildApplication to connect back to the ServerChannel
            Eventually.assertThat(invoking(listener).isOpened(), is(true));

            // close the ParentApplication
            parentApplication.close();

            // wait for the ChildApplication to disconnect from the ServerChannel
            Eventually.assertThat(invoking(listener).isClosed(), is(true));
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (parentApplication != null)
            {
                parentApplication.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    /**
     * Ensure that {@link LocalJavaApplicationBuilder}s create applications that
     * can have {@link java.util.concurrent.Callable}s submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable() throws InterruptedException
    {
        // define the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        schema.setSystemProperty("uuid", uuid);

        // build and start the SleepingApplication
        LocalJavaApplicationBuilder<JavaApplication> builder     = new LocalJavaApplicationBuilder<JavaApplication>();

        ApplicationConsole                           console     = new SystemApplicationConsole();

        SimpleJavaApplication                        application = builder.realize(schema, "sleeping", console);

        // request the system property from the SleepingApplication
        DeferredCompletionListener<String> deferred = new DeferredCompletionListener<String>(String.class);

        application.submit(new GetSystemProperty("uuid"), deferred);

        Eventually.assertThat(deferred, is(uuid));

        application.close();
    }


    /**
     * Ensure that {@link LocalJavaApplicationBuilder}s set the JAVA_HOME
     * environment variable.
     */
    @Test
    public void shouldSetJavaHome() throws InterruptedException
    {
        // define the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // set the JAVA_HOME environment variable to be the same as this application
        String javaHome = System.getProperty("java.home");

        schema.setJavaHome(javaHome);

        // build and start the SleepingApplication
        LocalJavaApplicationBuilder<JavaApplication> builder     = new LocalJavaApplicationBuilder<JavaApplication>();

        ApplicationConsole                           console     = new SystemApplicationConsole();

        SimpleJavaApplication                        application = builder.realize(schema, "sleeping", console);

        Eventually.assertThat(application, new GetSystemProperty("java.home"), is(javaHome));

        application.close();
    }


    /**
     * A {@link com.oracle.tools.runtime.concurrent.RemoteExecutorListener} to track when it's been opened and closed.
     */
    public static class ClientApplicationListener implements RemoteExecutorListener
    {
        /**
         * The {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} that was opened / closed.
         */
        private RemoteExecutor executor;

        /**
         * Was the {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} open?
         */
        private AtomicBoolean isOpened;

        /**
         * Was the {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} closed?
         */
        private AtomicBoolean isClosed;


        /**
         * Constructs a {@link ClientApplicationListener}.
         */
        public ClientApplicationListener()
        {
            executor = null;
            isOpened = new AtomicBoolean(false);
            isClosed = new AtomicBoolean(false);
        }


        @Override
        public void onOpened(RemoteExecutor executor)
        {
            isOpened.compareAndSet(false, true);
            this.executor = executor;
        }


        /**
         * Determine if the {@link ClientApplicationListener} has opened.
         *
         * @return if opened
         */
        public boolean isOpened()
        {
            return isOpened.get();
        }


        @Override
        public void onClosed(RemoteExecutor executor)
        {
            isClosed.compareAndSet(false, true);
        }


        /**
         * Determine if the {@link ClientApplicationListener} has closed.
         *
         * @return if closed
         */
        public boolean isClosed()
        {
            return isClosed.get();
        }


        /**
         * Obtains the {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} provided to the {@link com.oracle.tools.runtime.concurrent.RemoteExecutorListener}.
         *
         * @return a {@link com.oracle.tools.runtime.concurrent.RemoteExecutor}
         */
        public RemoteExecutor getExecutor()
        {
            return executor;
        }
    }
}
