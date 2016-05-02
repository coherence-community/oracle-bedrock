/*
 * File: JUnitReporter.java
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
import com.oracle.bedrock.options.Decoration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A base class for generating JUnit reports.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class JUnitReporter implements JUnitTestListener
{
    /**
     * The folder to write the test report to.
     */
    private final File reportFolder;

    /**
     * The System properties of the JUnit tests.
     */
    private Properties testProperties;

    /**
     * A {@link Map} of {@link TestResults} instances keyed by the test class name.
     */
    private Map<String,TestResults> results = new ConcurrentHashMap<>();


    /**
     * Create a {@link JUnitReporter} with the specified output folder.
     * <p>
     * If the output folder is null the report will be written to {@link System#out}.
     *
     * @param reportFolder  the folder to write the report file to or null if the
     *                      report should be written to {@link System#out}
     */
    protected JUnitReporter(File reportFolder)
    {
        this.reportFolder = reportFolder;
    }


    /**
     * Obtain the folder to use to write the test reports to.
     *
     * @return  the folder to use to write the test reports to
     */
    public File getReportFolder()
    {
        return reportFolder;
    }


    /**
     * Ensure that an instance of {@link TestResults} results exists for the
     * specified test class.
     *
     * @param className  the name of the class to obtain tests results for
     *
     * @return  the {@link TestResults} instance for the specified test class name
     */
    public TestResults ensureTest(String className)
    {
        return results.computeIfAbsent(className, TestResults::new);
    }


    /**
     * Obtain the System properties used by the test run.
     *
     * @return  the System properties used by the test run
     */
    public Properties getTestProperties()
    {
        return testProperties;
    }


    /**
     * Obtain this {@link JUnitReporter} instance as an {@link Option}
     *
     * @return  this {@link JUnitReporter} instance as an {@link Option}
     */
    public Option asOption()
    {
        return Decoration.of(this);
    }


    /**
     * Write the test report for the specified {@link TestResults} to
     * the specified {@link PrintStream}.
     *
     * @param out   the {@link PrintStream} to write the test to
     * @param test  the {@link TestResults} to use to write the report
     */
    public abstract void writeReport(PrintStream out, TestResults test);


    /**
     * Obtain the file name to use for the test report for the
     * specified class name.
     *
     * @param className  the name of the test class
     *
     * @return  the file name to use for the test report for the
     *          specified class name
     */
    public abstract String getReportFileName(String className);


    @Override
    public void junitStarted(Event event)
    {
    }


    @Override
    public void junitCompleted(Event event)
    {
    }


    @Override
    public void testRunStarted(Event event)
    {
        String className = event.getClassName();
        if (className != null)
        {
            ensureTest(className).addEvent(event);
        }

        testProperties = event.getProperties();
    }


    @Override
    public void testRunFinished(Event event)
    {
    }


    @Override
    public void testStarted(Event event)
    {
        TestResults test = ensureTest(event.getClassName());

        test.incrementTestCount();
    }


    public void testClassStarted(Event event)
    {
    }


    public void testClassFinished(Event event)
    {
        String className = event.getClassName();

        if (className == null)
        {
            return;
        }

        TestResults test = ensureTest(className);
        float       time = ((float) event.getTime()) / 1000.0f;

        test.setTestTimeSeconds(time);

        File reportFolder = getReportFolder();

        if (reportFolder == null)
        {
            writeReport(System.out, test);
        }
        else
        {
            try
            {
                String fileName   = getReportFileName(className);
                File   reportFile = new File(reportFolder, fileName);

                try (PrintStream out = new PrintStream(reportFile))
                {
                    writeReport(out, test);
                }
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void testSucceeded(Event event)
    {
        TestResults test = ensureTest(event.getClassName());

        test.addEvent(event);
    }


    @Override
    public void testIgnored(Event event)
    {
        TestResults test = ensureTest(event.getClassName());

        test.addEvent(event);
        test.incrementSkipCount();
    }


    @Override
    public void testFailed(Event event)
    {
        TestResults test = ensureTest(event.getClassName());

        test.addEvent(event);
        test.incrementFailureCount();
    }


    @Override
    public void testError(Event event)
    {
        TestResults test = ensureTest(event.getClassName());

        test.addEvent(event);
        test.incrementErrorCount();
    }


    @Override
    public void testAssumptionFailure(Event event)
    {
        TestResults test = ensureTest(event.getClassName());

        test.addEvent(event);
        test.incrementSkipCount();
    }


    /**
     * A holder for the results of the tests for a particular test class.
     */
    public static class TestResults
    {
        /**
         * The name of the test class
         */
        private final String className;

        /**
         * The number of tests executed
         */
        private AtomicInteger testCount = new AtomicInteger(0);

        /**
         * The number of tests skipped, either due to being ignored or
         * due to failing {@link org.junit.Assume} assertions.
         */
        private AtomicInteger skipCount = new AtomicInteger(0);

        /**
         * The number of test failures.
         */
        private AtomicInteger failureCount = new AtomicInteger(0);

        /**
         * The number of errors.
         * <p>
         * An error is when a test method fails due to an error that
         * is not an {@link AssertionError}
         */
        private AtomicInteger errorCount = new AtomicInteger(0);

        /**
         * The {@link Queue} of test {@link Event}s.
         */
        private Queue<Event> events = new ConcurrentLinkedQueue<>();

        /**
         * The time in seconds taken for the tests to run.
         */
        private float testTimeSeconds;

        /**
         * Create a new {@link TestResults} for the specified class name.
         *
         * @param className  the name of the class
         */
        public TestResults(String className)
        {
            this.className = className;
        }


        /**
         * Add an {@link Event} to the results.
         *
         * @param event  the {@link Event} to add
         */
        public void addEvent(Event event)
        {
            events.add(event);
        }


        /**
         * Obtain the {@link Queue} of {@link Event}s fired during the test.
         *
         * @return  the {@link Queue} of {@link Event}s fired during the test
         */
        public Queue<Event> getEvents()
        {
            return events;
        }


        /**
         * Obtain the class name the results are for.
         *
         * @return  the class name the results are for
         */
        public String getClassName()
        {
            return className;
        }


        /**
         * Set the total number of seconds taken to execute the tests.
         *
         * @param seconds  the total number of seconds taken to
         *                 execute the tests
         */
        public void setTestTimeSeconds(float seconds)
        {
            this.testTimeSeconds = seconds;
        }


        /**
         * Obtain the total number of seconds taken for the tests to execute.
         *
         * @return  the total number of seconds taken for the tests to execute
         */
        public float getTestTime()
        {
            return testTimeSeconds;
        }


        /**
         * Increment the test count.
         */
        public void incrementTestCount()
        {
            testCount.incrementAndGet();
        }


        /**
         * Obtain the number of tests executed.
         *
         * @return  the number of tests executed
         */
        public int getTestCount()
        {
            return testCount.get();
        }


        /**
         * Increment the skip count.
         */
        public void incrementSkipCount()
        {
            skipCount.incrementAndGet();
        }


        /**
         * Obtain the number of skipped tests.
         *
         * @return  the number of skipped tests
         */
        public int getSkipCount()
        {
            return skipCount.get();
        }


        /**
         * Increment the failure count.
         */
        public void incrementFailureCount()
        {
            failureCount.incrementAndGet();
        }


        /**
         * Obtain the number of failed tests.
         *
         * @return  the number of failed tests
         */
        public int getFailureCount()
        {
            return failureCount.get();
        }


        /**
         * Increment the error count.
         */
        public void incrementErrorCount()
        {
            errorCount.incrementAndGet();
        }


        /**
         * Obtain the number of test errors.
         * <p>
         * An error is when a test method fails due to an error that
         * is not an {@link AssertionError}
         *
         * @return  the number of test errors
         */
        public int getErrorCount()
        {
            return errorCount.get();
        }
    }
}
