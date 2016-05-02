/*
 * File: RemoteJavaApplicationLauncherTest.java
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

package com.oracle.bedrock.runtime.remote.java;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.concurrent.runnable.RuntimeExit;
import com.oracle.bedrock.runtime.concurrent.runnable.RuntimeHalt;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.RemoteEvents;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.deferred.Eventually;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.Console;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.JavaHome;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.java.profiles.RemoteDebugging;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.runtime.remote.AbstractRemoteTest;
import com.oracle.bedrock.runtime.remote.java.applications.EventingApplication;
import com.oracle.bedrock.runtime.remote.java.applications.SleepingApplication;
import com.oracle.bedrock.util.Capture;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

/**
 * Functional tests for {@link RemoteJavaApplicationLauncher}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationLauncherTest extends AbstractRemoteTest
{
    /**
     * Field description
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    /**
     * Ensure that we can launch Java remotely.
     */
    @Test
    public void shouldLaunchJavaApplicationRemotely() throws Exception
    {
        RemotePlatform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           Argument.of("3"),
                                                           SystemApplicationConsole.builder()))
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
        RemotePlatform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           Argument.of("3"),
                                                           JavaHome.at(System.getProperty("java.home")),
                                                           SystemApplicationConsole.builder()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }


    /**
     * Ensure that {@link JavaApplication}s can is launched with a system-property
     * that contains spaces.
     */
    @Test
    public void shouldLaunchJavaApplicationUsingSystemPropertyWithSpaces() throws Exception
    {
        RemotePlatform platform = getRemotePlatform();

        // set a System-Property for the SleepingApplication (we'll request it back)
        String message = "Hello World";

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           SystemProperty.of("message", message),
                                                           IPv4Preferred.yes()))
        {
            // request the system property from the SleepingApplication
            String propertyValue = application.invoke(new GetSystemProperty("message"));

            Assert.assertThat(propertyValue, is(message));
        }
    }


    /**
     * Should run the application with remote debug enabled if set in schema
     * with start suspended set to false.
     */
    @Test
    public void shouldSetRemoteDebugEnabledSuspendedIsFalse() throws Exception
    {
        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        RemotePlatform              platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           Argument.of("300"),
                                                           RemoteDebugging.enabled().startSuspended(false).listen(),
                                                           Console.of(console)))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

            List<String> args     = application.invoke(new GetProgramArgs());

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

        CapturingApplicationConsole console  = new CapturingApplicationConsole();
        RemotePlatform              platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           RemoteDebugging.enabled().startSuspended(true)
                                                           .at(new RemoteDebugging
                                                               .TransportAddress(LocalPlatform.get()
                                                               .getAvailablePorts())),
                                                           JavaHome.at(System.getProperty("java.home")),
                                                           Console.of(console)))
        {
            assertCanConnectDebuggerToApplication(application);

            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

            List<String> args     = application.invoke(new GetProgramArgs());
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

        InetAddress                 debugAddress = LocalPlatform.get().getAddress();
        Capture<Integer>            debugPort    = new Capture<>(LocalPlatform.get().getAvailablePorts());

        CapturingApplicationConsole jdbConsole   = new CapturingApplicationConsole();

        try (Application jdb = LocalPlatform.get().launch(Application.class,
                                                          Executable.named(findJDB()),
                                                          Argument.of("-connect"),
                                                          Argument.of(String.format("com.sun.jdi.SocketListen:port=%d",
                                                                                    debugPort.get())),
                                                          Console.of(jdbConsole)))

        {
            Eventually.assertThat("JDB did not start properly",
                                  invoking(jdbConsole).getCapturedOutputLines(),
                                  hasItem(startsWith("Listening at address:")));

            CapturingApplicationConsole console  = new CapturingApplicationConsole();

            RemotePlatform              platform = getRemotePlatform();

            try (JavaApplication application = platform.launch(JavaApplication.class,
                                                               ClassName.of(SleepingApplication.class),
                                                               RemoteDebugging.enabled().attach()
                                                               .at(new RemoteDebugging.TransportAddress(debugAddress,
                                                                                                        debugPort)),
                                                               Console.of(console)))
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

        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application jdb = LocalPlatform.get().launch(Application.class,
                                                          Executable.named(findJDB()),
                                                          Argument.of("-connect"),
                                                          Argument.of(String.format("com.sun.jdi.SocketAttach:hostname=%s,port=%d",
                                                                                    socket.getAddress()
                                                                                    .getHostAddress(),
                                                                                    socket.getPort())),
                                                          Console.of(console)))
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
        CapturingApplicationConsole console         = new CapturingApplicationConsole();

        RemoteDebugging             remoteDebugging = RemoteDebugging.disabled();

        RemotePlatform              platform        = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           DisplayName.of("Java"),
                                                           Argument.of("30"),
                                                           Console.of(console),
                                                           remoteDebugging))
        {
            Eventually.assertThat(invoking(console).getCapturedOutputLines(), hasItem(startsWith("Now sleeping")));

            List<String> args     = application.invoke(new GetProgramArgs());

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
        // build and start the SleepingApplication
        Platform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           Argument.of("3"),
                                                           IPv4Preferred.yes()))
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
        // build and start the SleepingApplication
        Platform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           Argument.of("3"),
                                                           IPv4Preferred.yes()))
        {
            application.close(RuntimeHalt.withExitCode(42));

            int exitStatus = application.waitFor();

            assertThat(exitStatus, is(42));
        }
    }


    /**
     * Ensure that {@link JavaApplication}s correctly inherits arguments based on system properties
     * starting with bedrock.runtime.inherit.xxx=
     */
    @Test
    public void shouldInheritRuntimeProperties() throws Exception
    {
        // set a system property for this process
        System.getProperties().setProperty("bedrock.runtime.inherit.property", "-Dmessage=hello");

        // build and start the SleepingApplication
        Platform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           IPv4Preferred.yes()))
        {
            // ask the application for the current system property value
            String message = application.getSystemProperty("message");

            // ensure that the application has the system property
            Assert.assertThat(message, is("hello"));
        }
        finally
        {
            // remove the system property to clean up
            System.getProperties().remove("bedrock.runtime.inherit.property");
        }
    }


    @Test
    public void shouldSetWorkingDirectory() throws Exception
    {
        String appName           = "sleeping";
        File   folder            = temporaryFolder.newFolder();

        String appNameSanitized  = PlatformSeparators.autoDetect().asSanitizedFileName(appName);
        File   expectedDirectory = new File(folder, appNameSanitized);

        // build and start the SleepingApplication
        Platform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(SleepingApplication.class),
                                                           DisplayName.of(appName),
                                                           WorkingDirectory.subDirectoryOf(folder)))
        {
            String dir = application.invoke(new GetWorkingDirectory());

            Assert.assertThat(dir, is(expectedDirectory.getCanonicalPath()));

            WorkingDirectory workingDir = application.getOptions().get(WorkingDirectory.class);

            Assert.assertThat(workingDir, is(notNullValue()));
            Assert.assertThat(workingDir.getValue(), is((Object) expectedDirectory));
        }
    }


    @Test
    public void shouldReceiveEventFromApplication() throws Exception
    {
        Platform      platform  = getRemotePlatform();

        EventListener listener1 = new EventListener(1);
        EventListener listener2 = new EventListener(1);
        String        name      = "Foo";
        RemoteEvent   event     = new EventingApplication.Event(19);

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(EventingApplication.class),
                                                           IPv4Preferred.yes()))
        {
            application.addListener(listener1, StreamName.of(name));
            application.addListener(listener2, StreamName.of(name));

            EventingApplication.fireEvent(application, name, event);

            Assert.assertThat(listener1.await(1, TimeUnit.MINUTES), is(true));
            Assert.assertThat(listener2.await(1, TimeUnit.MINUTES), is(true));

            Assert.assertThat(listener1.getEvents().size(), is(1));
            Assert.assertThat(listener1.getEvents().get(0), is(event));

            Assert.assertThat(listener2.getEvents().size(), is(1));
            Assert.assertThat(listener2.getEvents().get(0), is(event));
        }
    }


    @Test
    public void shouldReceiveEventsFromApplicationUsingListenerAsOption() throws Exception
    {
        Platform      platform  = getRemotePlatform();

        String        name      = "Foo";
        int           count     = 10;
        EventListener listener1 = new EventListener(count);
        EventListener listener2 = new EventListener(count);

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(EventingApplication.class),
                                                           IPv4Preferred.yes(),
                                                           RemoteEvents.listener(listener1, StreamName.of(name)),
                                                           RemoteEvents.listener(listener2, StreamName.of(name)),
                                                           Argument.of(name),
                                                           Argument.of(count)))
        {
            Assert.assertThat(listener1.await(1, TimeUnit.MINUTES), is(true));
            Assert.assertThat(listener2.await(1, TimeUnit.MINUTES), is(true));

            application.close();

            Assert.assertThat(listener1.getEvents().size(), is(count));
            Assert.assertThat(listener2.getEvents().size(), is(count));
        }
    }


    @Test
    public void shouldSendEventsToApplication() throws Exception
    {
        Platform      platform = getRemotePlatform();

        EventListener listener = new EventListener(1);
        RemoteEvent   event    = new EventingApplication.Event(19);

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(EventingApplication.class),
                                                           IPv4Preferred.yes()))
        {
            application.addListener(listener, StreamName.of("Back"));

            EventingApplication.listen(application, "Out", "Back");

            application.raise(event, StreamName.of("Out"));

            Assert.assertThat(listener.await(1, TimeUnit.MINUTES), is(true));

            Assert.assertThat(listener.getEvents().size(), is(1));
            Assert.assertThat(listener.getEvents().get(0), is(event));

            application.close();
        }
    }


    @Test
    public void shouldSubmitRunnableBack() throws Exception
    {
        Platform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(EventingApplication.class),
                                                           IPv4Preferred.yes()))
        {
            EventingApplication.CountDownRunnable.latch = new CountDownLatch(1);

            application.submit(new EventingApplication.RoundTripRunnable());

            Assert.assertThat(EventingApplication.CountDownRunnable.latch.await(1, TimeUnit.MINUTES), is(true));
        }
    }


    @Test
    public void shouldSubmitCallableBack() throws Exception
    {
        Platform platform = getRemotePlatform();

        try (JavaApplication application = platform.launch(JavaApplication.class,
                                                           ClassName.of(EventingApplication.class),
                                                           IPv4Preferred.yes()))
        {
            EventingApplication.GetIntCallable.value = 1234;

            CompletableFuture<Integer> result = application.submit(new EventingApplication.RoundTripCallable());

            Assert.assertThat(result.get(), is(1234));
        }
    }


    /**
     * Detect whether the Java Debugger application is present.
     *
     * @return true if JDB is present
     */
    protected boolean hasJDB() throws Exception
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application jdb = LocalPlatform.get().launch(Application.class,
                                                          Executable.named(findJDB()),
                                                          Argument.of("-version"),
                                                          Console.of(console)))
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


    /**
     * An instance of a {@link RemoteEventListener} that captures events.
     */
    public static class EventListener implements RemoteEventListener
    {
        /**
         * The counter to count the number of events received.
         */
        private final CountDownLatch latch;

        /**
         * The list of events received.
         */
        private final List<RemoteEvent> events;


        /**
         * Create an {@link EventListener} to receieve the expected number of events.
         *
         * @param expected  the expected number of events
         */
        public EventListener(int expected)
        {
            latch  = new CountDownLatch(expected);
            events = new ArrayList<>();
        }


        /**
         * Causes the current thread to wait until the expected number of events
         * have been received, unless the thread is {@linkplain Thread#interrupt interrupted},
         * or the specified waiting time elapses.
         *
         * @param timeout  the maximum time to wait
         * @param unit     the time unit of the {@code timeout} argument
         *
         * @return {@code true} if the correct number of events is received and {@code false}
         *         if the waiting time elapsed before the events were received
         *
         * @throws InterruptedException if the current thread is interrupted
         *         while waiting
         */
        private boolean await(long     timeout,
                              TimeUnit unit) throws InterruptedException
        {
            return latch.await(timeout, unit);
        }


        public List<RemoteEvent> getEvents()
        {
            return events;
        }


        @Override
        public void onEvent(RemoteEvent event)
        {
            events.add(event);
            latch.countDown();
        }
    }
}
