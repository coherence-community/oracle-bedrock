/*
 * File: SimpleJUnitTestListenerTest.java
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

package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.junit.JUnitTestListener;
import com.oracle.bedrock.testsupport.junit.SimpleJUnitTestListener;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SimpleJUnitTestListener}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleJUnitTestListenerTest
{
    @Test
    public void shouldCaptureProperties() throws Exception
    {
        Properties              properties = new Properties();
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener   = new SimpleJUnitTestListener();

        properties.put("some key", "some value");

        when(event.getProperties()).thenReturn(properties);

        listener.testRunStarted(event);

        assertThat(listener.getTestProperties(), is(properties));
    }


    @Test
    public void shouldIncrementTestCount() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        assertThat(listener.getTestCount(), is(0));

        listener.testStarted(event);
        listener.testStarted(event);

        assertThat(listener.getTestCount(), is(2));
    }


    @Test
    public void shouldIncrementFailureCount() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        assertThat(listener.hasTestFailures(), is(false));
        assertThat(listener.getFailureCount(), is(0));

        listener.testFailed(event);
        listener.testFailed(event);

        assertThat(listener.hasTestFailures(), is(true));
        assertThat(listener.getFailureCount(), is(2));
    }


    @Test
    public void shouldIncrementErrorCount() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        assertThat(listener.hasTestFailures(), is(false));
        assertThat(listener.getErrorCount(), is(0));

        listener.testError(event);
        listener.testError(event);

        assertThat(listener.hasTestFailures(), is(true));
        assertThat(listener.getErrorCount(), is(2));
    }


    @Test
    public void shouldIncrementSkipCountOnTestIgnored() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        assertThat(listener.getSkipCount(), is(0));

        listener.testIgnored(event);
        listener.testIgnored(event);

        assertThat(listener.getSkipCount(), is(2));
    }


    @Test
    public void shouldIncrementSkipCountOnAssumptionFailure() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        assertThat(listener.getSkipCount(), is(0));

        listener.testAssumptionFailure(event);
        listener.testAssumptionFailure(event);

        assertThat(listener.getSkipCount(), is(2));
    }


    @Test
    public void shouldAwaitCompletion() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        CompletableFuture.runAsync(
            () -> {
                try
                {
                    Thread.sleep(500);
                    listener.junitCompleted(event);
                }
                catch (InterruptedException e)
                {
                    // ignored
                }
            });

        assertThat(listener.awaitCompletion(1, TimeUnit.MINUTES), is(true));
    }


    @Test
    public void shouldAlreadyBeComplete() throws Exception
    {
        JUnitTestListener.Event event    = mock(JUnitTestListener.Event.class);
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        listener.junitCompleted(event);

        assertThat(listener.awaitCompletion(0, TimeUnit.MINUTES), is(true));
    }


    @Test
    public void shouldTimeoutWaitingForCompletion() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        assertThat(listener.awaitCompletion(100, TimeUnit.MILLISECONDS), is(false));
    }


    @Test
    public void shouldCreateOption() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();
        Option                  option   = listener.asOption();

        assertThat(option, is(notNullValue()));

        OptionsByType               optionsByType = OptionsByType.of(option);

        Iterable<JUnitTestListener> iterable      = optionsByType.getInstancesOf(JUnitTestListener.class);

        assertThat(iterable, contains(listener));
    }
}
