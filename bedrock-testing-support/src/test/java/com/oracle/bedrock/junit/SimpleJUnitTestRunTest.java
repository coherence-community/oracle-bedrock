/*
 * File: SimpleJUnitTestRunTest.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.junit.options.TestClasses;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.java.JavaApplicationProcess;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SimpleJUnitTestRun}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleJUnitTestRunTest
{
    @Test
    public void shouldAddListenersFromOptions() throws Exception
    {
        Platform                platform  = mock(Platform.class);
        JavaApplicationProcess  process   = mock(JavaApplicationProcess.class);
        SimpleJUnitTestListener listener1 = new SimpleJUnitTestListener();
        SimpleJUnitTestListener listener2 = new SimpleJUnitTestListener();
        Options                 options   = new Options(listener1.asOption(), listener2.asOption());

        try (SimpleJUnitTestRun application = new SimpleJUnitTestRun(platform, process, options))
        {
            assertThat(application.getRunListeners(), containsInAnyOrder(listener1, listener2));
        }
    }


    @Test
    public void shouldStartTests() throws Exception
    {
        Platform               platform = mock(Platform.class);
        JavaApplicationProcess process  = mock(JavaApplicationProcess.class);
        Options                options  = new Options();

        try (SimpleJUnitTestRun application = new SimpleJUnitTestRun(platform, process, options))
        {
            TestClasses        testClasses = TestClasses.empty();
            Options            testOptions = new Options(testClasses);
            SimpleJUnitTestRun spyApp      = spy(application);

            doReturn(null).when(spyApp).submit(any(RemoteCallable.class), anyVararg());

            spyApp.startTests(testOptions);

            ArgumentCaptor<RemoteCallable> captorCallable = ArgumentCaptor.forClass(RemoteCallable.class);

            verify(spyApp).submit(captorCallable.capture(), anyVararg());

            RemoteCallable callable = captorCallable.getValue();

            assertThat(callable, is(instanceOf(JUnitTestRunner.StartTests.class)));

            JUnitTestRunner.StartTests startTests = (JUnitTestRunner.StartTests) callable;

            assertThat(startTests.getOptions(), is(arrayContainingInAnyOrder(testClasses)));
        }
    }


    @Test
    public void shouldFireJUnitStartedEvent() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.junitStarted();

        fireEvent(event, listener1, listener2);

        verify(listener1).junitStarted(same(event));
        verify(listener2).junitStarted(same(event));
    }


    @Test
    public void shouldFireJUnitCompletedEvent() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.junitCompleted(1000);

        fireEvent(event, listener1, listener2);

        verify(listener1).junitCompleted(same(event));
        verify(listener2).junitCompleted(same(event));
    }


    @Test
    public void shouldFireTestClassStarted() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.testClassStarted("foo");

        fireEvent(event, listener1, listener2);

        verify(listener1).testClassStarted(same(event));
        verify(listener2).testClassStarted(same(event));
    }


    @Test
    public void shouldFireTestClassFinished() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.testClassFinished("foo", 1000);

        fireEvent(event, listener1, listener2);

        verify(listener1).testClassFinished(same(event));
        verify(listener2).testClassFinished(same(event));
    }


    @Test
    public void shouldFireTestAssumptionFailure() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.assumptionFailure("foo", "foo", 1000, null, null);

        fireEvent(event, listener1, listener2);

        verify(listener1).testAssumptionFailure(same(event));
        verify(listener2).testAssumptionFailure(same(event));
    }


    @Test
    public void shouldFireTestError() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.error("foo", "foo", 1000, null, null, null);

        fireEvent(event, listener1, listener2);

        verify(listener1).testError(same(event));
        verify(listener2).testError(same(event));
    }


    @Test
    public void shouldFireTestFailure() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.failure("foo", "foo", 1000, null, null, null);

        fireEvent(event, listener1, listener2);

        verify(listener1).testFailed(same(event));
        verify(listener2).testFailed(same(event));
    }


    @Test
    public void shouldFireTestIgnored() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.ignored("foo", "foo", "foo");

        fireEvent(event, listener1, listener2);

        verify(listener1).testIgnored(same(event));
        verify(listener2).testIgnored(same(event));
    }


    @Test
    public void shouldFireTestRunStarted() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.testRunStarted("foo", null);

        fireEvent(event, listener1, listener2);

        verify(listener1).testRunStarted(same(event));
        verify(listener2).testRunStarted(same(event));
    }


    @Test
    public void shouldFireTestRunFinished() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.testRunFinsihed("foo", 1000);

        fireEvent(event, listener1, listener2);

        verify(listener1).testRunFinished(same(event));
        verify(listener2).testRunFinished(same(event));
    }


    @Test
    public void shouldFireTestStarted() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.testStarted("foo", "foo");

        fireEvent(event, listener1, listener2);

        verify(listener1).testStarted(same(event));
        verify(listener2).testStarted(same(event));
    }


    @Test
    public void shouldFireTestSucceeded() throws Exception
    {
        JUnitTestListener       listener1 = mock(JUnitTestListener.class, "1");
        JUnitTestListener       listener2 = mock(JUnitTestListener.class, "2");
        JUnitTestListener.Event event     = JUnitTestListener.Event.testSucceded("foo", "foo", 1000);

        fireEvent(event, listener1, listener2);

        verify(listener1).testSucceeded(same(event));
        verify(listener2).testSucceeded(same(event));
    }


    public void fireEvent(JUnitTestListener.Event event,
                          JUnitTestListener...    listeners) throws Exception
    {
        Platform               platform = mock(Platform.class);
        JavaApplicationProcess process  = mock(JavaApplicationProcess.class);
        Options                options  = new Options();

        for (JUnitTestListener listener : listeners)
        {
            options.add(Decoration.of(listener));
        }

        try (SimpleJUnitTestRun application = new SimpleJUnitTestRun(platform, process, options))
        {
            ArgumentCaptor<RemoteEventListener> captorListener = ArgumentCaptor.forClass(RemoteEventListener.class);
            ArgumentCaptor<Option>              captorOptions  = ArgumentCaptor.forClass(Option.class);

            verify(process).addListener(captorListener.capture(), captorOptions.capture());

            RemoteEventListener eventListener = captorListener.getValue();
            List<Option>        listenerOpts  = captorOptions.getAllValues();

            assertThat(eventListener, is(notNullValue()));
            assertThat(listenerOpts, containsInAnyOrder(JUnitTestRunner.STREAM_NAME));

            eventListener.onEvent(event);
        }

    }
}
