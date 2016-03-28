/*
 * File: ContainerBasedJavaApplicationTest.java
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
import com.oracle.tools.options.Decoration;
import com.oracle.tools.options.Diagnostics;

import com.oracle.tools.runtime.concurrent.RemoteEvent;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutorTests;
import com.oracle.tools.runtime.console.AbstractPipedApplicationConsole;
import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.console.PipedApplicationConsole;

import com.oracle.tools.runtime.java.options.ClassName;

import com.oracle.tools.runtime.java.options.IPv4Preferred;
import com.oracle.tools.runtime.options.Argument;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Functional Tests for {@link ContainerBasedJavaApplicationLauncher}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class ContainerBasedJavaApplicationTest extends AbstractJavaApplicationTest<JavaVirtualMachine>
{
    @Override
    public JavaVirtualMachine getPlatform()
    {
        return JavaVirtualMachine.get();
    }


    /**
     * Ensure that the system properties, including their mutation, is isolated
     * between applications and from the test itself.
     */
    @Test
    public void shouldIsolateApplicationSystemProperties() throws IOException
    {
        String propertyName = "message";
        String firstValue   = "gudday";
        String secondValue  = "bonjour";

        try (PipedApplicationConsole firstConsole =
            new PipedApplicationConsole(AbstractPipedApplicationConsole.DEFAULT_PIPE_SIZE,
                                        false);
            JavaApplication firstApplication = getPlatform().launch(JavaApplication.class,
                                                                    ClassName.of(SystemPropertyMutatingApplication.class),
                                                                    Argument.of(propertyName, firstValue),
                                                                    Diagnostics.enabled(),
                                                                    Console.of(firstConsole)))
        {
            BufferedReader firstOutput = firstConsole.getOutputReader();

            // assert that the property is not set in the first application
            assertThat(firstOutput.readLine(), is("Existing: null"));

            // assert that the property is now set in the first application
            assertThat(firstOutput.readLine(), is("Now: " + firstValue));

            try (PipedApplicationConsole secondConsole =
                new PipedApplicationConsole(AbstractPipedApplicationConsole.DEFAULT_PIPE_SIZE,
                                            false);
                JavaApplication secondApplication = getPlatform().launch(JavaApplication.class,
                                                                         ClassName.of(SystemPropertyMutatingApplication.class),
                                                                         Argument.of(propertyName, secondValue),
                                                                         Diagnostics.enabled(),
                                                                         Console.of(secondConsole)))
            {
                BufferedReader secondOutput = secondConsole.getOutputReader();

                // assert that the property is not set in the second application,
                // even though it is now set in the first application
                assertThat(secondOutput.readLine(), is("Existing: null"));

                // assert that the property is now set in the second application
                assertThat(secondOutput.readLine(), is("Now: " + secondValue));

                // now send some input to terminate the second application
                secondConsole.getInputWriter().println();

                secondApplication.waitFor();
            }

            // now send some input to terminate the first application
            firstConsole.getInputWriter().println();

            firstApplication.waitFor();
        }

        // assert that the property remains unset in this test
        assertThat(System.getProperty(propertyName), is(nullValue()));
    }


    /**
     * Ensure that a {@link RuntimeException} caused by a {@link ClassNotFoundException} is
     * thrown when a class doesn't exist.
     */
    @Test
    public void shouldThrowClassNotFoundExceptionWhenApplicationDoesntExist()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of("ThisClassDoesntExist"),
                                                                Diagnostics.enabled()))
        {
            // nothing to do here
        }
        catch (RuntimeException e)
        {
            assertThat(e.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }


    /**
     * Ensure that a {@link RuntimeException} thrown by a {@link JavaApplication} is re-thrown.
     */
    @Test
    public void shouldThrowExceptionWhenApplicationFailsToStart()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(FaultyApplication.class),
                                                                Diagnostics.enabled()))
        {
            // wait for the application to terminate
            application.waitFor();

            fail("We should have seen a RuntimeException");
        }
        catch (RuntimeException e)
        {
            // we're all good!
        }
    }


    @Test
    public void shouldReceiveEventFromApplication() throws Exception
    {
        EventListener listener1 = new EventListener(1);
        EventListener listener2 = new EventListener(1);
        RemoteEvent   event     = new EventingApplication.Event(19);

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(EventingApplication.class),
                                                                IPv4Preferred.yes()))
        {
            application.addEventListener(listener1);
            application.addEventListener(listener2);

            EventingApplication.fireEvent(application, event);

            assertThat(listener1.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listener2.await(1, TimeUnit.MINUTES), is(true));

            assertThat(listener1.getEvents().size(), is(1));
            assertThat(listener2.getEvents().size(), is(1));

            assertThat(listener1.getEvents().get(0), is(event));
            assertThat(listener2.getEvents().get(0), is(event));

            application.close();
        }
    }

    @Test
    public void shouldReceiveEventsFromApplicationUsingListenerAsOption() throws Exception
    {
        int           count     = 10;
        EventListener listener1 = new EventListener(count);
        EventListener listener2 = new EventListener(count);

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(EventingApplication.class),
                                                                IPv4Preferred.yes(),
                                                                Decoration.of(listener1),
                                                                Decoration.of(listener2),
                                                                Argument.of(count)))
        {
            assertThat(listener1.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listener2.await(1, TimeUnit.MINUTES), is(true));

            application.close();

            List<RemoteEvent> events1 = listener1.getEvents();
            List<RemoteEvent> events2 = listener1.getEvents();

            assertThat(events1.size(), is(count));
            assertThat(events2.size(), is(count));

            for (int i=0; i<count; i++)
            {
                RemoteEvent event = new EventingApplication.Event(i);

                assertThat(events1.get(i), is(event));
                assertThat(events2.get(i), is(event));
            }
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
        private boolean await(long timeout, TimeUnit unit) throws InterruptedException
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
