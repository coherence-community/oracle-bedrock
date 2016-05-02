/*
 * File: JUnitReporterTest.java
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
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JUnitReporter}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitReporterTest
{
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldHaveSpecifiedReportFolder() throws Exception
    {
        File folder = new File("/foo");

        JUnitReporter reporter = new JUnitReporterStub(folder);

        assertThat(reporter.getReportFolder(), is(folder));
    }


    @Test
    public void shouldEnsureTestResults() throws Exception
    {
        JUnitReporter reporter = new JUnitReporterStub(null);

        JUnitReporter.TestResults results1 = reporter.ensureTest("foo");
        JUnitReporter.TestResults results2 = reporter.ensureTest("foo");
        JUnitReporter.TestResults results3 = reporter.ensureTest("bar");

        assertThat(results1, is(notNullValue()));
        assertThat(results2, is(notNullValue()));
        assertThat(results3, is(notNullValue()));

        assertThat(results1, is(sameInstance(results2)));
        assertThat(results1, is(not(sameInstance(results3))));

        assertThat(results1.getClassName(), is("foo"));
        assertThat(results2.getClassName(), is("foo"));
        assertThat(results3.getClassName(), is("bar"));
    }


    @Test
    public void shouldCreateOption() throws Exception
    {
        JUnitReporter reporter = new JUnitReporterStub(null);
        Option        option   = reporter.asOption();

        assertThat(option, is(notNullValue()));

        Options options = new Options(option);

        Iterable<JUnitTestListener> iterable = options.getInstancesOf(JUnitTestListener.class);

        assertThat(iterable, contains(reporter));
    }


    @Test
    public void shouldHandleTestRunStartedEventAndCaptureProperties() throws Exception
    {
        Properties              properties = new Properties();
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        properties.put("some key", "some value");

        when(event.getClassName()).thenReturn(className);
        when(event.getProperties()).thenReturn(properties);

        reporter.testRunStarted(event);

        assertThat(reporter.getTestProperties(), is(properties));

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(0));
        assertThat(result.getFailureCount(), is(0));
        assertThat(result.getErrorCount(), is(0));
        assertThat(result.getSkipCount(), is(0));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(sameInstance(event)));
    }


    @Test
    public void shouldHandleTestStartedEvent() throws Exception
    {
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        reporter.testStarted(event);

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(1));
        assertThat(result.getFailureCount(), is(0));
        assertThat(result.getErrorCount(), is(0));
        assertThat(result.getSkipCount(), is(0));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(nullValue()));
    }


    @Test
    public void shouldHandleTestSucceededEvent() throws Exception
    {
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        reporter.testSucceeded(event);

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(0));
        assertThat(result.getFailureCount(), is(0));
        assertThat(result.getErrorCount(), is(0));
        assertThat(result.getSkipCount(), is(0));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(sameInstance(event)));
    }


    @Test
    public void shouldHandleTestIgnoredEvent() throws Exception
    {
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        reporter.testIgnored(event);

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(0));
        assertThat(result.getFailureCount(), is(0));
        assertThat(result.getErrorCount(), is(0));
        assertThat(result.getSkipCount(), is(1));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(sameInstance(event)));
    }


    @Test
    public void shouldHandleTestFailedEvent() throws Exception
    {
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        reporter.testFailed(event);

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(0));
        assertThat(result.getFailureCount(), is(1));
        assertThat(result.getErrorCount(), is(0));
        assertThat(result.getSkipCount(), is(0));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(sameInstance(event)));
    }


    @Test
    public void shouldHandleTestErrorEvent() throws Exception
    {
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        reporter.testError(event);

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(0));
        assertThat(result.getFailureCount(), is(0));
        assertThat(result.getErrorCount(), is(1));
        assertThat(result.getSkipCount(), is(0));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(sameInstance(event)));
    }


    @Test
    public void shouldHandleTestAssumptionFailedEvent() throws Exception
    {
        String                  className  = "FooTest";
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        reporter.testAssumptionFailure(event);

        JUnitReporter.TestResults result = reporter.ensureTest(className);

        assertThat(result, is(notNullValue()));
        assertThat(result.getClassName(), is(className));
        assertThat(result.getTestCount(), is(0));
        assertThat(result.getFailureCount(), is(0));
        assertThat(result.getErrorCount(), is(0));
        assertThat(result.getSkipCount(), is(1));
        assertThat(result.getTestTime(), is(0.0f));

        Queue<JUnitTestListener.Event> events = result.getEvents();

        assertThat(events, is(notNullValue()));
        assertThat(events.peek(), is(sameInstance(event)));
    }


    @Test
    public void shouldNotWriteReportIfTestClassFinishedEventHasNullClassName() throws Exception
    {
        String                  className  = null;
        JUnitTestListener.Event event      = mock(JUnitTestListener.Event.class);
        JUnitReporter           reporter   = new JUnitReporterStub(null);

        when(event.getClassName()).thenReturn(className);

        JUnitReporter reporterSpy = spy(reporter);

        reporterSpy.testClassFinished(event);

        verify(reporterSpy, never()).writeReport(any(PrintStream.class), any(JUnitReporter.TestResults.class));
    }


    @Test
    public void shouldWriteReportToSystemOut() throws Exception
    {
        String                    className  = "FooTest";
        JUnitTestListener.Event   event      = mock(JUnitTestListener.Event.class);
        JUnitReporter             reporter   = new JUnitReporterStub(null);
        JUnitReporter.TestResults results    = reporter.ensureTest(className);

        when(event.getClassName()).thenReturn(className);
        when(event.getTime()).thenReturn(10500L);

        JUnitReporter reporterSpy = spy(reporter);

        reporterSpy.testClassFinished(event);

        verify(reporterSpy).writeReport(same(System.out), same(results));

        assertThat(results.getTestTime(), is(10.5f));
    }


    @Test
    public void shouldWriteReportToFile() throws Exception
    {
        File                      folder     = temporaryFolder.newFolder();
        String                    className  = "FooTest";
        JUnitTestListener.Event   event      = mock(JUnitTestListener.Event.class);
        JUnitReporter             reporter   = new JUnitReporterStub(folder);
        JUnitReporter.TestResults results    = reporter.ensureTest(className);

        when(event.getClassName()).thenReturn(className);
        when(event.getTime()).thenReturn(10500L);

        JUnitReporter reporterSpy = spy(reporter);

        doReturn("TestFoo.txt").when(reporterSpy).getReportFileName(className);

        reporterSpy.testClassFinished(event);

        ArgumentCaptor<PrintStream> captor = ArgumentCaptor.forClass(PrintStream.class);

        verify(reporterSpy).writeReport(captor.capture(), same(results));

        assertThat(results.getTestTime(), is(10.5f));

        assertThat(captor.getValue(), is(notNullValue()));

        File expectedFile = new File(folder, "TestFoo.txt");

        assertThat(expectedFile.exists(), is(true));

        assertThat(Files.readAllLines(expectedFile.toPath()), contains("Dummy test report"));
    }


    /**
     * A stub class to use for testing the abstract class {@link JUnitReporter}
     */
    public static class JUnitReporterStub extends JUnitReporter
    {
        public JUnitReporterStub(File reportFolder)
        {
            super(reportFolder);
        }

        @Override
        public void writeReport(PrintStream out, TestResults test)
        {
            if (out != null)
            {
                out.println("Dummy test report");
            }
        }

        @Override
        public String getReportFileName(String className)
        {
            return null;
        }
    }
}
