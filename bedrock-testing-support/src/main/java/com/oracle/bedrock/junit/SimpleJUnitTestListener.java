/*
 * File: SimpleJUnitTestListener.java
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

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple implementation of a {@link JUnitTestListener}.
 * <p>
 * This implementation will maintain a count of tests, skipped tests,
 * failures and errors as well as capturing the test JVM's System properties.
 * <p>
 * This implementation has the functionality to await completion of a test run.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleJUnitTestListener implements JUnitTestListener
{
    /**
     * The System properties of the JVM running the tests.
     */
    private Properties testProperties;

    /**
     * The number of tests executed.
     * <p>
     * This figure only includes tests actually started so will
     * not include tests annotated with {@link org.junit.Ignore}
     * but will include tests skipped due to an {@link org.junit.Assume}
     * exception.
     */
    private AtomicInteger testCount = new AtomicInteger(0);

    /**
     * The number of tests skipped.
     * <p>
     * This figure includes both tests annotated
     * with {@link org.junit.Ignore} and tests
     * skipped due to an {@link org.junit.Assume}
     * exception.
     */
    private AtomicInteger skipCount = new AtomicInteger(0);

    /**
     * The number of test failures.
     * <p>
     * This is the number of tests that failed due
     * to an {@link AssertionError}.
     */
    private AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * The number of errors running tests.
     * <p>
     * This is the number of tests that failed due
     * to an exception other than an {@link AssertionError}.
     */
    private AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * The {@link CountDownLatch} to use to await completion of all tests.
     */
    private CountDownLatch completed = new CountDownLatch(1);


    /**
     * Create a {@link SimpleJUnitTestListener}.
     */
    public SimpleJUnitTestListener()
    {
    }


    /**
     * Obtain the System properties of the JVM running the tests.
     *
     * @return  the System properties of the JVM running the tests
     */
    public Properties getTestProperties()
    {
        return testProperties;
    }


    /**
     * The number of tests executed.
     * <p>
     * This figure only includes tests actually started so will
     * not include tests annotated with {@link org.junit.Ignore}
     * but will include tests skipped due to an {@link org.junit.Assume}
     * exception.
     *
     * @return  the number of tests executed
     */
    public int getTestCount()
    {
        return testCount.get();
    }


    /**
     * The number of tests skipped.
     * <p>
     * This figure includes both tests annotated
     * with {@link org.junit.Ignore} and tests
     * skipped due to an {@link org.junit.Assume}
     * exception.
     *
     * @return  the number of tests skipped
     */
    public int getSkipCount()
    {
        return skipCount.get();
    }


    /**
     * The number of test failures.
     * <p>
     * This is the number of tests that failed due
     * to an {@link AssertionError}.
     *
     * @return  the number of test failures
     */
    public int getFailureCount()
    {
        return failureCount.get();
    }


    /**
     * The number of errors running tests.
     * <p>
     * This is the number of tests that failed due
     * to an exception other than an {@link AssertionError}.
     *
     * @return  the number of errors running tests
     */
    public int getErrorCount()
    {
        return errorCount.get();
    }


    /**
     * Determine whether there were any failures or errors in the test run.
     *
     * @return  {@code true} if there were any failures or errors in the test
     *          run, or returns {@code false} if all tests completed successfully
     */
    public boolean hasTestFailures()
    {
        return errorCount.get() > 0 || failureCount.get() > 0;
    }


    /**
     * Causes the current thread to wait until the JUnit application
     * has completed the entire test run, unless the thread is
     * {@linkplain Thread#interrupt interrupted},  or the specified
     * waiting time elapses.
     * <p>
     * If the JUnit application has already completed then this method
     * returns immediately with the value {@code true}.
     * <p>
     * If JUnit application has not yet completed then the current
     * thread becomes disabled for thread scheduling purposes and lies
     * dormant until one of three things happen:
     * <ul>
     * <li>The JUnit application completes the test run; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts}
     * the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     *
     * <p>If the JUnit application completes the test run then the
     * method returns with the value {@code true}.
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then the value {@code false}
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout  the maximum time to wait
     * @param unit     the time unit of the {@code timeout} argument
     *
     * @return  {@code true} if the JUnit application completed the test
     *          run and {@code false} if the waiting time elapsed before
     *          the JUnit application completed
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException
    {
        return completed.await(timeout, unit);
    }


    /**
     * Obtain the current {@link SimpleJUnitTestListener} as an {@link Option}.
     *
     * @return  the current {@link SimpleJUnitTestListener} as an {@link Option}
     */
    public Option asOption()
    {
        return Decoration.of(this);
    }


    @Override
    public void junitStarted(Event event)
    {
    }


    @Override
    public void junitCompleted(Event event)
    {
        completed.countDown();
    }


    @Override
    public void testRunStarted(Event event)
    {
        testProperties = event.getProperties();
    }


    @Override
    public void testRunFinished(Event event)
    {
    }


    @Override
    public void testStarted(Event event)
    {
        testCount.incrementAndGet();
    }


    @Override
    public void testClassStarted(Event event)
    {
    }


    @Override
    public void testClassFinished(Event event)
    {
    }


    @Override
    public void testSucceeded(Event event)
    {
    }


    @Override
    public void testIgnored(Event event)
    {
        skipCount.incrementAndGet();
    }


    @Override
    public void testFailed(Event event)
    {
        failureCount.incrementAndGet();
    }


    @Override
    public void testError(Event event)
    {
        errorCount.incrementAndGet();
    }


    @Override
    public void testAssumptionFailure(Event event)
    {
        skipCount.incrementAndGet();
    }
}