/*
 * File: JUnitTestRunnerListenerTest.java
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

package com.oracle.tools.junit;

import com.oracle.tools.runtime.concurrent.RemoteChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link JUnitTestRunner.Listener} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitTestRunnerListenerTest
{
    private RemoteChannel remoteChannel;

    @Before
    public void setup()
    {
        remoteChannel = mock(RemoteChannel.class);

        JUnitTestRunner.channel = remoteChannel;
    }

    @After
    public void cleanup() throws Exception
    {
        JUnitTestRunner.channel = null;
    }

    @Test
    public void shouldRaiseEvent() throws Exception
    {
        JUnitTestListener.Event  event    = mock(JUnitTestListener.Event.class);
        JUnitTestRunner.Listener listener = new JUnitTestRunner.Listener();

        listener.raiseEvent(event);

        verify(remoteChannel).raise(same(event), same(JUnitTestRunner.STREAM_NAME));
    }


    @Test
    public void shouldCheckForClassChangeWhenCurrentClassIsNull() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo");
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.checkClassChange(description);

        assertThat(listener.getCurrentTestClass(), is("Foo"));
        assertThat(listener.getClassStartTime(), is(greaterThanOrEqualTo(start)));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        JUnitTestListener.Event event = captor.getValue();

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testClassStarted));
        assertThat(event.getClassName(), is("Foo"));
    }


    @Test
    public void shouldRaiseTestClassStartedEventOnceIfCheckClassChangeCalledTwiceWithSameClass() throws Exception
    {
        Description              description1 = Description.createTestDescription("Foo", "testFoo1");
        Description              description2 = Description.createTestDescription("Foo", "testFoo2");
        JUnitTestRunner.Listener listener     = new JUnitTestRunner.Listener();
        long                     start = System.currentTimeMillis();


        listener.checkClassChange(description1);

        // Sleep to allow some test time
        Thread.sleep(500);

        long end = System.currentTimeMillis();

        listener.checkClassChange(description2);

        assertThat(listener.getCurrentTestClass(), is("Foo"));
        assertThat(listener.getClassStartTime(), is(greaterThanOrEqualTo(start)));
        assertThat(listener.getClassStartTime(), is(lessThanOrEqualTo(end)));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(1)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        JUnitTestListener.Event event = captor.getValue();

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testClassStarted));
        assertThat(event.getClassName(), is("Foo"));
    }


    @Test
    public void shouldRaiseTestClassStartedAndEndedEventsForEachClassInCheckClassChange() throws Exception
    {
        Description              description1 = Description.createTestDescription("Foo", "testFoo");
        Description              description2 = Description.createTestDescription("Bar", "testBar");
        JUnitTestRunner.Listener listener     = new JUnitTestRunner.Listener();

        // First class
        listener.checkClassChange(description1);
        // Pause to ensure we have some test time
        Thread.sleep(100);
        // Second class
        listener.checkClassChange(description2);

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(3)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> events = captor.getAllValues();

        assertThat(events.size(), is(3));

        JUnitTestListener.Event event1 = events.get(0);
        JUnitTestListener.Event event2 = events.get(1);
        JUnitTestListener.Event event3 = events.get(2);

        assertThat(event1, is(notNullValue()));
        assertThat(event1.getType(), is(JUnitTestListener.Event.Type.testClassStarted));
        assertThat(event1.getClassName(), is("Foo"));

        assertThat(event2, is(notNullValue()));
        assertThat(event2.getType(), is(JUnitTestListener.Event.Type.testClassFinished));
        assertThat(event2.getClassName(), is("Foo"));
        assertThat(event2.getTime(), is(not(0L)));

        assertThat(event3, is(notNullValue()));
        assertThat(event3.getType(), is(JUnitTestListener.Event.Type.testClassStarted));
        assertThat(event3.getClassName(), is("Bar"));
    }


    @Test
    public void shouldHandleTestRunStarted() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.testRunStarted(description);

        assertThat(listener.getCurrentTestClass(), is(nullValue()));
        assertThat(listener.getRunStartTime(), is(greaterThanOrEqualTo(start)));
        assertThat(listener.hasTestFailed(), is(false));
        assertThat(listener.getTestRunDescription(), is(description));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        JUnitTestListener.Event event = captor.getValue();

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testRunStarted));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getProperties(), is(System.getProperties()));
    }

    @Test
    public void shouldHandleTestRunFinished() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        Result                   result      = mock(Result.class);
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();

        // Signal the run start
        long start = System.currentTimeMillis();
        listener.testRunStarted(description);

        // Sleep so we have some test time
        Thread.sleep(100);
        long end = System.currentTimeMillis();
        Thread.sleep(100);

        // Signal the run end
        listener.testRunFinished(result);

        assertThat(listener.getTestRunDescription(), is(description));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(2)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(2));

        JUnitTestListener.Event event = eventList.get(1);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testRunFinished));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getTime(), is(greaterThanOrEqualTo(end - start)));
    }


    @Test
    public void shouldHandleTestStarted() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.testStarted(description);

        assertThat(listener.getCurrentTestClass(), is("Foo"));
        assertThat(listener.getTestStartTime(), is(greaterThanOrEqualTo(start)));
        assertThat(listener.hasTestFailed(), is(false));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(2)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(2));

        JUnitTestListener.Event event = eventList.get(1);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testStarted));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getClassName(), is("Foo"));
    }


    @Test
    public void shouldHandleTestFinishedWithoutFailure() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.testStarted(description);

        // Sleep to allow some test time
        Thread.sleep(200);
        long end = System.currentTimeMillis();
        Thread.sleep(100);

        listener.testFinished(description);

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(3)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(3));

        JUnitTestListener.Event event = eventList.get(2);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testSuccess));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getClassName(), is("Foo"));
        assertThat(event.getTime(), is(greaterThanOrEqualTo(end - start)));
    }


    @Test
    public void shouldHandleTestFinishedAfterFailure() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();

        listener.testStarted(description);
        listener.setTestFailed(true);
        listener.testFinished(description);

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(2)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(2));
    }


    @Test
    public void shouldHandleTestIgnored() throws Exception
    {
        String                   message     = "test has been ignored";
        Ignore                   ignore      = IgnoreSupplier.class.getAnnotation(Ignore.class);
        Description              description = Description.createTestDescription("Foo", "testFoo1", ignore);
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();

        listener.testStarted(description);
        listener.testIgnored(description);

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(3)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(3));

        JUnitTestListener.Event event = eventList.get(2);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testIgnored));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getClassName(), is("Foo"));
        assertThat(event.getMessage(), is(message));
    }


    @Test
    public void shouldHandleTestAssumptionFailure() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        Throwable                throwable   = new RuntimeException("Computer says No!");
        Failure                  failure     = new Failure(description, throwable);
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.testStarted(description);

        // Sleep to allow some test time
        Thread.sleep(200);
        long end = System.currentTimeMillis();
        Thread.sleep(100);

        listener.testAssumptionFailure(failure);

        assertThat(listener.hasTestFailed(), is(true));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(3)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(3));

        JUnitTestListener.Event event = eventList.get(2);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testAssumptionFailure));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getClassName(), is("Foo"));
        assertThat(event.getTime(), is(greaterThanOrEqualTo(end - start)));
        assertThat(event.getMessage(), is(throwable.getMessage()));
        assertThat(event.getStackTrace(), is(throwable.getStackTrace()));
    }


    @Test
    public void shouldHandleTestFailure() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        Throwable                throwable   = new AssertionError("Computer says No!");
        Failure                  failure     = new Failure(description, throwable);
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.testStarted(description);

        // Sleep to allow some test time
        Thread.sleep(200);
        long end = System.currentTimeMillis();
        Thread.sleep(100);

        listener.testFailure(failure);

        assertThat(listener.hasTestFailed(), is(true));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(3)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(3));

        JUnitTestListener.Event event = eventList.get(2);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testFailure));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getClassName(), is("Foo"));
        assertThat(event.getTime(), is(greaterThanOrEqualTo(end - start)));
        assertThat(event.getException(), is(throwable.getClass().getCanonicalName()));
        assertThat(event.getMessage(), is(throwable.getMessage()));
        assertThat(event.getStackTrace(), is(throwable.getStackTrace()));
    }


    @Test
    public void shouldHandleTestFailureThatIsAnError() throws Exception
    {
        Description              description = Description.createTestDescription("Foo", "testFoo1");
        Throwable                throwable   = new RuntimeException("Computer says No!");
        Failure                  failure     = new Failure(description, throwable);
        JUnitTestRunner.Listener listener    = new JUnitTestRunner.Listener();
        long                     start       = System.currentTimeMillis();

        listener.testStarted(description);

        // Sleep to allow some test time
        Thread.sleep(200);
        long end = System.currentTimeMillis();

        listener.testFailure(failure);

        assertThat(listener.hasTestFailed(), is(true));

        ArgumentCaptor<JUnitTestListener.Event> captor = ArgumentCaptor.forClass(JUnitTestListener.Event.class);

        verify(remoteChannel, times(3)).raise(captor.capture(), same(JUnitTestRunner.STREAM_NAME));

        List<JUnitTestListener.Event> eventList = captor.getAllValues();

        assertThat(eventList.size(), is(3));

        JUnitTestListener.Event event = eventList.get(2);

        assertThat(event, is(notNullValue()));
        assertThat(event.getType(), is(JUnitTestListener.Event.Type.testError));
        assertThat(event.getName(), is(description.getDisplayName()));
        assertThat(event.getClassName(), is("Foo"));
        assertThat(event.getTime(), is(greaterThanOrEqualTo(end - start)));
        assertThat(event.getException(), is(throwable.getClass().getCanonicalName()));
        assertThat(event.getMessage(), is(throwable.getMessage()));
        assertThat(event.getStackTrace(), is(throwable.getStackTrace()));
    }


    /**
     * A dummy class to use to get an {@link Ignore} annotation to use in tests
     * as it is not easy to create a new annotation in code.
     */
    @Ignore(value = "test has been ignored")
    public static class IgnoreSupplier
    {
    }
}
