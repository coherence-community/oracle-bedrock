/*
 * File: AbstractJavaApplicationTest.java
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

package com.oracle.bedrock.runtime.java;

import classloader.applications.EventingApplication;
import classloader.applications.SleepingApplication;
import classloader.applications.Tester;
import classloader.applications.TesterApplication;
import classloader.applications.TesterProducer;
import com.oracle.bedrock.deferred.Eventually;
import com.oracle.bedrock.junit.AbstractTest;
import com.oracle.bedrock.lang.StringHelper;
import com.oracle.bedrock.options.Diagnostics;
import com.oracle.bedrock.runtime.DummyApp;
import com.oracle.bedrock.runtime.DummyClassPathApp;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;
import com.oracle.bedrock.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteCallableStaticMethod;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.console.PipedApplicationConsole;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.RemoteEvents;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Integration Tests for {@link JavaApplication} launched across various {@link Platform}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public abstract class AbstractJavaApplicationTest<P extends Platform> extends AbstractTest
{
    /**
     * Obtains the {@link Platform} {@link JavaApplication}s will be launched.
     *
     * @return  the {@link Platform}
     */
    public abstract P getPlatform();


    /**
     * Ensure that we can start and terminate a {@link JavaApplication}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                DisplayName.of("java-app"),
                                                                ClassName.of(DummyApp.class),
                                                                Arguments.of("arg1", "arg2"),
                                                                SystemProperty.of("test.prop.1", "value.1"),
                                                                SystemProperty.of("test.prop.2", "value.2"),
                                                                IPv4Preferred.yes(),
                                                                Diagnostics.enabled(),
                                                                Console.of(console)))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout.startsWith("[java-app:"), is(true));
            assertThat(stdout, containsString("arg1,arg2"));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("test.prop.1=value.1"));
        }
    }


    /**
     * Ensure that we can start and terminate a {@link JavaApplication} with a customized {@link ClassPath}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplicationWithRestrictedClasspath() throws Exception
    {
        ClassPath               knownJarClassPath = ClassPath.ofResource("LICENSE");
        Class<Mock>             knownClass        = Mock.class;

        ClassPath               path1             = ClassPath.ofClass(DummyClassPathApp.class);
        ClassPath               path2             = ClassPath.ofClass(StringHelper.class);
        ClassPath               path3             = ClassPath.ofClass(getPlatform().getClass());
        ClassPath               classPath         = new ClassPath(knownJarClassPath, path1, path2, path3);

        PipedApplicationConsole console           = new PipedApplicationConsole();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                DisplayName.of("java-app"),
                                                                ClassName.of(DummyClassPathApp.class),
                                                                Arguments.of(knownClass.getCanonicalName()),
                                                                classPath,
                                                                IPv4Preferred.yes(),
                                                                Diagnostics.enabled(),
                                                                Console.of(console)))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString(knownJarClassPath.iterator().next()));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString(path1.iterator().next()));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString(path2.iterator().next()));
        }
    }


    /**
     * Ensure that we can create {@link JavaApplication}s that can have {@link Callable}s
     * submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable()
    {
        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                SystemProperty.of("uuid", uuid)))
        {
            Eventually.assertThat(application, new GetSystemProperty("uuid"), is(uuid));
        }
    }


    /**
     * Ensure that a {@link Callable} that raises an exception is re-raised as an {@link ExecutionException}.
     */
    @Test
    public void shouldRaiseExecutionExceptionFromCallable()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class)))
        {
            CompletableFuture<Void> future = application.submit(new ErrorThrowingCallable("Submission Failed"));

            future.join();

            fail();
        }
        catch (CompletionException e)
        {
            Assert.assertThat(e.getCause().getMessage(), is("Submission Failed"));
        }
    }


    /**
     * Ensure that we can use a {@link RemoteCallableStaticMethod} with a {@link JavaApplication}.
     */
    @Test
    public void shouldCallRemoteStaticMethodsInAnApplication() throws InterruptedException, ExecutionException
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(TesterApplication.class),
                                                                IPv4Preferred.yes()))
        {
            RemoteCallable<Integer> callable = new RemoteCallableStaticMethod<>(TesterApplication.class.getName(),
                                                                                "getMeaningOfLife");

            CompletableFuture<Integer> future = application.submit(callable);

            assertThat(future.get(), is(42));

            application.waitFor();
        }
    }


    /**
     * Ensure that we can create a local proxy of a {@link Tester} in an {@link JavaApplication}.
     */
    @Test
    public void shouldProxyTesterInApplication()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(TesterApplication.class),
                                                                IPv4Preferred.yes()))
        {
            Tester tester = application.getProxyFor(Tester.class, new TesterProducer(), null);

            tester.doNothing();

            assertThat(tester.getMeaningOfLife(), is("42"));
            assertThat(tester.identity(42), is(42));

            application.waitFor();
        }
    }


    /**
     * Ensure that we can wait for {@link JavaApplication}s to terminate.
     */
    @Test(timeout = 10000)
    public void shouldWaitFor()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                Arguments.of("5"),
                                                                IPv4Preferred.yes()))
        {
            application.waitFor();
        }
    }


    /**
     * Ensure that we can define a {@link SystemProperty} in the {@link JavaApplication}
     * (and it won't be defined in this process).
     */
    @Test
    public void shouldDefineSystemPropertyInApplication()
    {
        // the custom property name and value
        String propertyName  = "custom.property";
        String propertyValue = "gudday";

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                SystemProperty.of(propertyName, propertyValue),
                                                                IPv4Preferred.yes()))
        {
            // assert that the custom property is defined in the application
            Eventually.assertThat(application, new GetSystemProperty(propertyName), is(propertyValue));

            // assert that the custom property is not defined in the test
            assertThat(System.getProperty(propertyName), is(nullValue()));
        }
    }


    /**
     * Ensure that we can create applications that can have {@link RemoteCallable}s
     * represented as lambda submitted to them and executed.
     */
    @Test
    public void shouldExecuteLambda() throws InterruptedException
    {
        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                SystemProperty.of("uuid", uuid),
                                                                IPv4Preferred.yes()))
        {
            Eventually.assertThat(application, () -> System.getProperty("uuid"), is(uuid));
        }
    }


    @Test
    public void shouldReceiveEventFromApplication() throws Exception
    {
        EventListener listener1 = new EventListener(1);
        EventListener listener2 = new EventListener(1);
        String        name      = "Foo";
        RemoteEvent   event     = new EventingApplication.Event(19);

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(EventingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            application.addListener(listener1, StreamName.of(name));
            application.addListener(listener2, StreamName.of(name));

            EventingApplication.fireEvent(application, name, event);

            assertThat(listener1.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listener2.await(1, TimeUnit.MINUTES), is(true));

            assertThat(listener1.getEvents().size(), is(1));
            assertThat(listener1.getEvents().get(0), is(event));

            assertThat(listener2.getEvents().size(), is(1));
            assertThat(listener2.getEvents().get(0), is(event));
        }
    }


    @Test
    public void shouldReceiveEventsFromApplicationUsingListenerAsOption() throws Exception
    {
        String        name      = "Foo";
        int           count     = 10;
        EventListener listener1 = new EventListener(count);
        EventListener listener2 = new EventListener(count);

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(EventingApplication.class),
                                                                IPv4Preferred.yes(),
                                                                RemoteEvents.listener(listener1, StreamName.of(name)),
                                                                RemoteEvents.listener(listener2, StreamName.of(name)),
                                                                Argument.of(name),
                                                                Argument.of(count)))
        {
            assertThat(listener1.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listener2.await(1, TimeUnit.MINUTES), is(true));

            application.close();

            assertThat(listener1.getEvents().size(), is(count));
            assertThat(listener2.getEvents().size(), is(count));
        }
    }


    @Test
    public void shouldSendEventsToApplication() throws Exception
    {
        EventListener listener = new EventListener(1);
        RemoteEvent   event    = new EventingApplication.Event(19);

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
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


    /**
     * Ensure that we don't get silent failure of a {@link RemoteCallable} that
     * is a non static inner class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNonStaticInnerClassRemoteCallable()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            RemoteCallable<String> callable = new NonStaticInnerClasses().new InvalidCallable();

            // ensure attempting to use a non-static inner-class will raise an IllegalArgumentException
            application.submit(callable);
        }
    }


    /**
     * Ensure that we don't get silent failure of a {@link RemoteRunnable} that
     * is a non static inner class.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNonStaticInnerClassRemoteRunnable()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            RemoteRunnable runnable = new NonStaticInnerClasses().new InvalidRunnable();

            // ensure attempting to use a non-static inner-class will raise an IllegalArgumentException
            application.submit(runnable);
        }
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
