/*
 * File: EventuallyTest.java
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

package com.oracle.tools.deferred;

import com.oracle.tools.util.StopWatch;

import org.junit.Assert;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.within;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;

import static org.hamcrest.core.Is.is;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Eventually}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */

public class EventuallyTest
{
    /**
     * Ensure that a {@link Eventually#assertThat(Deferred, org.hamcrest.Matcher)}
     * waits at least the default amount of time before throwing an exception when
     * a {@link Deferred} is {@link NotAvailable}.
     */
    @Test
    public void shouldWaitDefaultTimeout()
    {
        StopWatch stopWatch = new StopWatch();

        try
        {
            stopWatch.start();
            Eventually.assertThat(new NotAvailable<String>(String.class), is("hello world"));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            stopWatch.stop();

            Assert.assertTrue(String
                .format("Failed to wait for the default duration of %d seconds.  Waited %s seconds",
                        DeferredHelper.ORACLETOOLS_DEFERRED_RETRY_TIMEOUT_SECS,
                        stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                              stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)
                              >= DeferredHelper.ORACLETOOLS_DEFERRED_RETRY_TIMEOUT_SECS * 0.95);
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that a {@link Eventually#assertThat(Deferred, org.hamcrest.Matcher)}
     * waits at least the specified time before throwing an exception when
     * the {@link Deferred} returns <code>null</code>.
     */
    @Test
    public void shouldWaitSpecifiedTimeBeforeTimeout()
    {
        StopWatch stopWatch         = new StopWatch();
        long      retryDurationSECS = 5;

        try
        {
            stopWatch.start();
            Eventually.assertThat(new NotAvailable<String>(String.class),
                                  is("hello world"),
                                  within(retryDurationSECS, TimeUnit.SECONDS));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            stopWatch.stop();

            Assert.assertTrue(String
                .format("Failed to wait for the specified duration of %d seconds.  Waited %s seconds",
                        retryDurationSECS, stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                              stopWatch.getElapsedTimeIn(TimeUnit.SECONDS) >= retryDurationSECS);
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that a {@link Eventually#assertThat(Deferred, org.hamcrest.Matcher)}
     * fails fast when the {@link Deferred} throws an {@link PermanentlyUnavailableException}.
     */
    @Test
    public void shouldFailFast()
    {
        StopWatch stopWatch = new StopWatch();

        try
        {
            stopWatch.start();
            Eventually.assertThat(new NeverAvailable<String>(String.class), is("hello world"));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            stopWatch.stop();

            Assert.assertTrue(String.format("Failed to fail fast.  Instead waited for Waited %s seconds",
                                            stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                              stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)
                              < DeferredHelper.ORACLETOOLS_DEFERRED_RETRY_TIMEOUT_SECS);
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that the exception thrown by {@link Eventually#assertThat(Deferred, org.hamcrest.Matcher)}
     * contains the last used value with the matcher.
     */
    @Test
    public void shouldUseLastEvaluatedValueWithMatcher()
    {
        Deferred<String> deferred = new Existing<String>("Hello World");

        try
        {
            Eventually.assertThat(deferred, is("Gudday"), within(1, TimeUnit.SECONDS));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            Assert.assertTrue(e.getMessage().contains("Hello World"));
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that Eventually.assertThat works with wrappers and primitives.
     */
    @Test
    public void shouldEventuallyAssertUsingWrappersAndPrimitives()
    {
        Eventually.assertThat(Long.valueOf(5), is(5L));

        Eventually.assertThat(5L, is(Long.valueOf(5)));
    }


    /**
     * Ensure that Eventually.assertThat works with queues (collections).
     */
    @Test
    public void shouldEventuallyAssertThatQueueContainsElement()
    {
        String                        element = "VM Started:";

        ConcurrentLinkedQueue<String> queue   = new ConcurrentLinkedQueue<>();

        queue.add("Listening at address: Hummingbird.local:30003");
        queue.add("Set uncaught java.lang.Throwable");
        queue.add("Set deferred uncaught java.lang.Throwable");
        queue.add("Initializing jdb");
        queue.add("VM Started: >");
        queue.add("The application exited, (terminated)");

        Eventually.assertThat(queue, hasItem(containsString("VM Started:")));
    }
}
