/*
 * File: LocalPlatformJavaApplicationTest.java
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

import classloader.applications.EventingApplication;
import classloader.applications.ParentApplication;
import classloader.applications.SleepingApplication;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.deferred.listener.DeferredCompletionListener;

import com.oracle.tools.io.NetworkHelper;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;

import com.oracle.tools.runtime.concurrent.RemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteChannelListener;
import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.runnable.RuntimeExit;
import com.oracle.tools.runtime.concurrent.runnable.RuntimeHalt;
import com.oracle.tools.runtime.concurrent.runnable.SystemExit;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteChannelServer;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteChannelTests;

import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.Console;

import com.oracle.tools.runtime.java.options.ClassName;
import com.oracle.tools.runtime.java.options.HeapSize;
import com.oracle.tools.runtime.java.options.HotSpot;
import com.oracle.tools.runtime.java.options.IPv4Preferred;
import com.oracle.tools.runtime.java.options.JavaHome;
import com.oracle.tools.runtime.java.options.SystemProperty;
import com.oracle.tools.runtime.java.profiles.CommercialFeatures;
import com.oracle.tools.runtime.java.profiles.RemoteDebugging;

import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.Executable;
import com.oracle.tools.runtime.options.Orphanable;
import com.oracle.tools.runtime.options.PlatformSeparators;
import com.oracle.tools.runtime.options.WorkingDirectory;

import com.oracle.tools.util.Capture;

import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import static com.oracle.tools.deferred.DeferredHelper.delayedBy;
import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.deferred.DeferredHelper.valueOf;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import java.net.InetSocketAddress;

import java.util.List;
import java.util.UUID;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Functional Tests for {@link LocalJavaApplicationLauncher}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class LocalPlatformJavaApplicationTest extends AbstractJavaApplicationTest<LocalPlatform>
{
    /**
     * Field description
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Override
    public LocalPlatform getPlatform()
    {
        return LocalPlatform.get();
    }


    /**
     * Should run a {@link JavaApplication} with remote debug enabled with start suspended set to false.
     */
    @Test
    public void shouldSetRemoteDebugEnabledSuspendedIsFalse() throws Exception
    {
        try (JavaApplication app = getPlatform().launch(JavaApplication.class,
                                                        ClassName.of(SleepingApplication.class),
                                                        IPv4Preferred.yes(),
                                                        RemoteDebugging.enabled().startSuspended(false)))
        {
            List<String> args     = app.submitAndGet(new GetProgramArgs());
            String       debugArg = null;

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
            assertThat(debugArg, containsString(",suspend=n"));
            assertThat(debugArg, containsString(",server=y"));
        }
    }


    /**
     * Should run a {@link JavaApplication} with remote debug enabled with start suspended set to false.
     */
    @Test
    public void shouldSetRemoteDebugEnabledSuspendedIsTrue() throws Exception
    {
        // make sure we can run the JDB debugger otherwise we cannot run this test
        Assume.assumeThat(hasJDB(), is(true));

        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                DisplayName.of("TestApp"),
                                                                IPv4Preferred.yes(),
                                                                Console.of(console),
                                                                RemoteDebugging.enabled().startSuspended(true)))
        {
            assertCanConnectDebuggerToApplication(application);

            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

            List<String> args     = application.submitAndGet(new GetProgramArgs());
            String       debugArg = null;

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
            assertThat(debugArg, containsString(",suspend=y"));
            assertThat(debugArg, containsString(",server=y"));
        }
    }


    /**
     * Start the {@link JavaApplication} with remote debugging enabled and mode set to
     * {@link com.oracle.tools.runtime.java.profiles.RemoteDebugging.Behavior#ATTACH_TO_DEBUGGER}
     * and assert the process connects back to the debugger
     */
    @Ignore("This test can cause JVMs to crash on various platforms.  Disabled for now until there is a fix.")
    @Test
    public void shouldEnableRemoteDebugAndConnectBackToDebugger() throws Exception
    {
        // make sure we can run the JDB debugger otherwise we cannot run this test
        Assume.assumeThat(hasJDB(), is(true));

        Capture<Integer>            debuggerPort = new Capture<>(getPlatform().getAvailablePorts());

        CapturingApplicationConsole jdbConsole   = new CapturingApplicationConsole();

        try (Application jdb = getPlatform().launch(Application.class,
                                                    Executable.named("jdb"),
                                                    Argument.of("-connect"),
                                                    Argument.of("com.sun.jdi.SocketListen:localAddress="
                                                                + getPlatform().getAddress().getHostAddress()
                                                                + ",port=" + debuggerPort.get()),
                                                    Console.of(jdbConsole)))
        {
            Eventually.assertThat("JDB did not start properly",
                                  invoking(jdbConsole).getCapturedOutputLines(),
                                  hasItem(startsWith("Listening at address:")));

            CapturingApplicationConsole console = new CapturingApplicationConsole();

            try (JavaApplication app = getPlatform().launch(JavaApplication.class,
                                                            ClassName.of(SleepingApplication.class),
                                                            DisplayName.of("TestApp"),
                                                            Argument.of("60"),    // sleeping for 60 seconds to allow connection
                                                            RemoteDebugging.enabled().attach()
                                                            .at(new RemoteDebugging.TransportAddress(debuggerPort)),
                                                            Console.of(console)))
            {
                Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

                // assert that the application connects back to the debugger
                // (this can take sometime as JDB initializes itself)
                Eventually.assertThat("Application did not connect back to JDB",
                                      invoking(jdbConsole).getCapturedOutputLines(),
                                      hasItem(containsString("VM Started:")),
                                      delayedBy(10, TimeUnit.SECONDS));
            }
        }
    }


    /**
     * Ensure that we can connect a debugger to the specified {@link JavaApplication}.
     *
     * @param application  the {@link JavaApplication}
     *
     * @throws Exception
     */
    protected void assertCanConnectDebuggerToApplication(JavaApplication application) throws Exception
    {
        RemoteDebugging.TransportAddress transportAddress =
            application.getOptions().get(RemoteDebugging.TransportAddress.class);

        InetSocketAddress socket = transportAddress.getSocketAddress();

        assertThat(socket, is(notNullValue()));

        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application jdb = LocalPlatform.get().launch(Application.class,
                                                          Executable.named("jdb"),
                                                          Argument.of("-connect"),
                                                          Argument.of("com.sun.jdi.SocketAttach:hostname="
                                                                      + socket.getAddress().getHostAddress() + ",port="
                                                                      + socket.getPort()),
                                                          Console.of(console)))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("VM Started")));

            console.getInputWriter().println("run");
            console.getInputWriter().println("quit");

            jdb.waitFor();
        }
    }


    /**
     * Determine if Java Debugging is available by attempting to launch the Java Debugger.
     *
     * @return <code>true</code> if "jdb" is available, <code>false</code> otherwise
     *
     * @throws Exception
     */
    protected boolean hasJDB() throws Exception
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application jdb = LocalPlatform.get().launch(Application.class,
                                                          Executable.named("jdb"),
                                                          Argument.of("-version"),
                                                          DisplayName.of("JDB"),
                                                          Console.of(console)))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(),
                                  hasItem(startsWith("This is jdb version")));

            console.getInputWriter().println("quit");

            jdb.waitFor();

            return true;
        }
        catch (Throwable t)
        {
            return false;
        }
    }


    /**
     * Should run the {@link JavaApplication} with remote debug disabled.
     *
     * NOTE: This test is ignored when running in an IDE in debug mode
     * as the {@link JavaVirtualMachine} will pick up the debug settings
     * and pass them on to the process causing the test to fail
     */
    @Test
    public void shouldSetRemoteDebugDisabled() throws Exception
    {
        Assume.assumeThat(JavaVirtualMachine.get().shouldEnableRemoteDebugging(), is(false));

        try (JavaApplication app = getPlatform().launch(JavaApplication.class,
                                                        ClassName.of(SleepingApplication.class),
                                                        IPv4Preferred.yes(),
                                                        DisplayName.of("TestApp"),
                                                        RemoteDebugging.disabled()))
        {
            List<String> args     = app.submitAndGet(new GetProgramArgs());

            String       debugArg = null;

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
    }


    /**
     * Ensure that {@link JavaApplication}s started in orphan-mode will be left running.
     */
    @Test
    public void shouldCreateOrphans()
    {
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer())
        {
            // start a server that the child can connect too.
            server.open();

            // add a listener for the ClientApplication
            ClientApplicationListener listener = new ClientApplicationListener();

            server.addListener(listener);

            try (JavaApplication parentApplication = getPlatform().launch(JavaApplication.class,
                                                                          ClassName.of(ParentApplication.class),
                                                                          Orphanable.disabled(),    // the parent can't be orphaned
                                                                          SystemProperty.of("server.address",
                                                                                            server.getInetAddress(NetworkHelper
                                                                                                .LOOPBACK_ADDRESS)
                                                                                                .getHostAddress()),
                                                                          SystemProperty.of("server.port",
                                                                                            server.getPort()),
                                                                          SystemProperty.of("orphan.children", true),    // the child can be orphaned
                                                                          IPv4Preferred.yes(),
                                                                          DisplayName.of("parent")))
            {
                // wait for the ChildApplication to connect back to our server
                Eventually.assertThat(invoking(listener).isOpened(), is(true));
            }

            // submit the child a request to prove that it's orphaned
            RemoteChannel                      child            = listener.getExecutor();
            DeferredCompletionListener<String> deferredResponse = new DeferredCompletionListener<>(String.class);

            child.submit(new SocketBasedRemoteChannelTests.PingPong(), deferredResponse);

            Eventually.assertThat(valueOf(deferredResponse), is("PONG"));

            // shutdown the client (by invoking a System.exit internally)
            child.submit(new SystemExit());

            // wait for the ChildApplication to disconnect from the ServerChannel
            Eventually.assertThat(invoking(listener).isClosed(), is(true));
        }
        catch (IOException e)
        {
        }
    }


    /**
     * Ensure that {@link JavaApplication}s started in orphan-less mode don't leave
     * orphan {@link JavaApplication}s running.
     */
    @Test
    public void shouldNotCreateOrphans()
    {
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer())
        {
            // start a server that the child can connect too.
            server.open();

            // add a listener for the ClientApplication
            ClientApplicationListener listener = new ClientApplicationListener();

            server.addListener(listener);

            try (JavaApplication parentApplication = getPlatform().launch(JavaApplication.class,
                                                                          ClassName.of(ParentApplication.class),
                                                                          Orphanable.disabled(),
                                                                          SystemProperty.of("server.address",
                                                                                            server.getInetAddress(NetworkHelper
                                                                                                .LOOPBACK_ADDRESS)
                                                                                                .getHostAddress()),
                                                                          SystemProperty.of("server.port",
                                                                                            server.getPort()),
                                                                          SystemProperty.of("orphan.children", false),
                                                                          IPv4Preferred.yes(),
                                                                          DisplayName.of("parent")))
            {
                // wait for the ChildApplication to connect back to the ServerChannel
                Eventually.assertThat(invoking(listener).isOpened(), is(true));
            }

            // wait for the ChildApplication to disconnect from the ServerChannel
            Eventually.assertThat(invoking(listener).isClosed(), is(true));
        }
        catch (IOException e)
        {
        }
    }


    /**
     * Ensure that {@link JavaApplication}s can have
     * {@link java.util.concurrent.Callable}s submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable()
    {
        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                SystemProperty.of("uuid", uuid),
                                                                IPv4Preferred.yes()))
        {
            // request the system property from the SleepingApplication
            DeferredCompletionListener<String> deferredResponse = new DeferredCompletionListener<String>(String.class);

            application.submit(new GetSystemProperty("uuid"), deferredResponse);

            Eventually.assertThat(valueOf(deferredResponse), is(uuid));
        }
    }


    /**
     * Ensure that {@link JavaApplication}s use the set the JAVA_HOME environment variable.
     */
    @Test
    public void shouldSetJavaHome() throws InterruptedException
    {
        // determine the JAVA_HOME based on this process
        String javaHomePath = System.getProperty("java.home");

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes(),
                                                                JavaHome.at(javaHomePath)))
        {
            Eventually.assertThat(application, new GetSystemProperty("java.home"), is(javaHomePath));
        }
    }


    /**
     * Ensure that {@link JavaApplication}s use the set {@link HeapSize}.
     */
    @Test
    public void shouldSetHeapSize()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes(),
                                                                DisplayName.of("sleeping"),
                                                                HotSpot.Mode.SERVER,
                                                                HeapSize.initial(256, HeapSize.Units.MB),
                                                                HeapSize.maximum(1, HeapSize.Units.GB)))
        {
        }
    }


    /**
     * Ensure that {@link JavaApplication}s can be launched with SystemProperties that use Expressions.
     */
    @Test
    public void shouldResolveExpressionsInASystemProperty()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes(),
                                                                SystemProperty.of("id", "${oracletools.runtime.id}"),
                                                                DisplayName.of("sleeping")))
        {
            String id = application.getSystemProperty("id");

            assertThat(id, is(not(nullValue())));
            assertThat(id, is(not("${oracletools.runtime.id}")));
        }
    }


    /**
     * Ensure that {@link JavaApplication}s correctly inherit arguments based on system properties
     * starting with oracletools.runtime.inherit.xxx=
     */
    @Test
    public void shouldInheritOracleToolsRuntimeProperties()
    {
        System.getProperties().setProperty("oracletools.runtime.inherit.property", "-Dmessage=hello");

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            String message = application.submitAndGet(new GetSystemProperty("message"));

            assertThat(message, is("hello"));
        }
        finally
        {
            System.getProperties().remove("oracletools.runtime.inherit.property");
        }
    }


    /**
     * Ensure that local {@link JavaApplication}s can be terminated using {@link RuntimeExit}.
     */
    @Test
    public void shouldTerminateUsingRuntimeExit()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            application.close(RuntimeExit.withExitCode(42));

            Eventually.assertThat(invoking(application).exitValue(), is(42));
        }
    }


    /**
     * Ensure that local {@link JavaApplication}s can be terminated using {@link RuntimeHalt}.
     */
    @Test
    public void shouldTerminateUsingRuntimeHalt()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            application.close(RuntimeHalt.withExitCode(42));

            Eventually.assertThat(invoking(application).exitValue(), is(42));
        }
    }


    /**
     * Ensure that local {@link JavaApplication}s with hanging shutdown hooks
     * can be terminated.
     */
    @Test
    public void shouldTerminateHangingApplication()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(ApplicationWithHangingShutdownHook.class),
                                                                IPv4Preferred.yes()))
        {
            application.close(SystemExit.withExitCode(42), Timeout.after(5, TimeUnit.SECONDS));

            Eventually.assertThat(invoking(application).exitValue(), is(2));
        }
    }


    /**
     * Should run a {@link JavaApplication} with commercial features.
     *
     * NOTE: This test is ignored when running in an IDE with commercial features
     */
    @Test
    public void shouldUnlockCommercialFeatures() throws Exception
    {
        Assume.assumeThat(CommercialFeatures.autoDetect().isEnabled(), is(false));

        try (JavaApplication app = getPlatform().launch(JavaApplication.class,
                                                        ClassName.of(SleepingApplication.class),
                                                        IPv4Preferred.yes(),
                                                        CommercialFeatures.enabled()))
        {
            List<String> args                       = app.submitAndGet(new GetProgramArgs());

            String       commercialFeaturesArgument = null;

            for (String arg : args)
            {
                if (arg.startsWith("-XX:+UnlockCommercialFeatures"))
                {
                    commercialFeaturesArgument = arg;
                    break;
                }
            }

            assertThat(commercialFeaturesArgument, is(not(nullValue())));
        }
    }


    @Test
    public void shouldSetWorkingDirectory() throws Exception
    {
        String appName           = "TestApp";
        File   folder            = temporaryFolder.newFolder();

        String appNameSanitized  = PlatformSeparators.autoDetect().asSanitizedFileName(appName);
        File   expectedDirectory = new File(folder, appNameSanitized);

        expectedDirectory.mkdirs();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                DisplayName.of(appName),
                                                                IPv4Preferred.yes(),
                                                                WorkingDirectory.subDirectoryOf(folder)))
        {
            String dir = application.submitAndGet(new GetWorkingDirectory());

            assertThat(dir, is(expectedDirectory.getCanonicalPath()));

            WorkingDirectory workingDir = application.getOptions().get(WorkingDirectory.class);

            assertThat(workingDir, is(notNullValue()));
            assertThat(workingDir.getValue(), is((Object) expectedDirectory));
        }
    }


    @Test
    public void shouldSubmitRunnableBack() throws Exception
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(EventingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            EventingApplication.CountDownRunnable.latch = new CountDownLatch(1);

            application.submit(new EventingApplication.RoundTripRunnable());

            assertThat(EventingApplication.CountDownRunnable.latch.await(1, TimeUnit.MINUTES), is(true));
        }
    }


    @Test
    public void shouldSubmitCallableBack() throws Exception
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(EventingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            EventingApplication.GetIntCallable.value = 1234;

            int result = application.submitAndGet(new EventingApplication.RoundTripCallable());

            assertThat(result, is(1234));
        }
    }


    /**
     * A {@link RemoteChannelListener} to track when it's been opened and closed.
     */
    public static class ClientApplicationListener implements RemoteChannelListener
    {
        /**
         * The {@link RemoteChannel} that was opened / closed.
         */
        private RemoteChannel executor;

        /**
         * Was the {@link RemoteChannel} open?
         */
        private AtomicBoolean isOpened;

        /**
         * Was the {@link RemoteChannel} closed?
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
        public void onOpened(RemoteChannel channel)
        {
            isOpened.compareAndSet(false, true);
            this.executor = channel;
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
        public void onClosed(RemoteChannel channel)
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
         * Obtains the {@link RemoteChannel} provided to the {@link RemoteChannelListener}.
         *
         * @return a {@link RemoteChannel}
         */
        public RemoteChannel getExecutor()
        {
            return executor;
        }
    }
}
