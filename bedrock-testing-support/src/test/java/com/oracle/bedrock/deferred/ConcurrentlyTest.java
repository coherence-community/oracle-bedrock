/*
 * File: ConcurrentlyTest.java
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

package com.oracle.bedrock.deferred;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.deferred.options.FailFast;
import com.oracle.bedrock.matchers.RecordingMatcher;
import com.oracle.bedrock.util.StopWatch;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

/**
 * Functional Tests for {@link Concurrently}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ConcurrentlyTest
{
    /**
     * Ensure that a constant concurrently matches a value.
     */
    @Test
    public void shouldConcurrentlyAssertConstant() throws InterruptedException
    {
        RecordingMatcher<Integer> matcher = RecordingMatcher.of(is(42));

        try (Concurrent.Assertion assertion = Concurrently.assertThat(42, matcher))
        {
            Eventually.assertThat(invoking(matcher).attempted(), is(true));

            assertion.check();

            Assert.assertThat(matcher.hasSucceeded(), is(true));
            Assert.assertThat(matcher.hasFailed(), is(false));
        }
    }


    /**
     * Ensure a {@link Concurrently#assertThat(String, Deferred, Matcher, Option...)} fails.
     */
    @Test
    public void shouldNotConcurrentlyAssertIncorrectConstant() throws InterruptedException
    {
        RecordingMatcher<Integer> matcher = RecordingMatcher.of(is(1));

        try (Concurrent.Assertion assertion = Concurrently.assertThat(42, matcher))
        {
            Eventually.assertThat(invoking(matcher).attempted(), is(true));

            assertion.check();
        }
        catch (AssertionError error)
        {
            Assert.assertThat(matcher.hasSucceeded(), is(false));
            Assert.assertThat(matcher.hasFailed(), is(true));
        }
    }


    /**
     * Ensure that a function concurrently matches a value.
     */
    @Test
    public void shouldConcurrentlyAssertFunction() throws InterruptedException
    {
        RecordingMatcher<Integer> matcher = RecordingMatcher.of(is(42));

        try (Concurrent.Assertion assertion = Concurrently.assertThat(42, (value) -> value, matcher))
        {
            Eventually.assertThat(invoking(matcher).attempted(), is(true));

            assertion.check();

            Assert.assertThat(matcher.hasSucceeded(), is(true));
            Assert.assertThat(matcher.hasFailed(), is(false));
        }
    }                                                                                       


    /**
     * Ensure that a {@link Concurrent.Assertion} can be closed without causing an
     * {@link AssertionError}.
     */
    @Test
    public void shouldCloseWithoutCausingAssertionError()
    {
        RecordingMatcher<Integer> matcher = RecordingMatcher.of(is(42));

        try (Concurrent.Assertion assertion = Concurrently.assertThat(42, matcher))
        {
            Eventually.assertThat(invoking(matcher).attempted(), is(true));

            assertion.check();

            Assert.assertThat(assertion.isClosed(), is(false));

            assertion.close();

            Eventually.assertThat(invoking(assertion).isClosed(), is(true));

            assertion.check();

            Assert.assertThat(matcher.hasSucceeded(), is(true));
            Assert.assertThat(matcher.hasFailed(), is(false));
        }
    }


    /**
     * Ensure a {@link Concurrently#assertThat(String, Deferred, Matcher, Option...)} fails fast when requested.
     */
    @Test
    public void shouldFailFast()
    {
        RecordingMatcher<Integer> matcher = RecordingMatcher.of(is(1));
        StopWatch                 watch   = new StopWatch();

        watch.start();

        try (Concurrent.Assertion assertion = Concurrently.assertThat(42, matcher, FailFast.enabled()))
        {
            // attempt to sleep for a long time (we won't as we'll be interrupted)
            Thread.sleep(5000);

            Assert.fail("Should have been interrupted!");
        }
        catch (InterruptedException e)
        {
            Assert.assertThat(matcher.attempted(), is(true));
            Assert.assertThat(matcher.hasSucceeded(), is(false));
            Assert.assertThat(matcher.hasFailed(), is(true));

            // ensure that the AssertionError can be found in those that were suppressed
            // (occurred while closing the Concurrently.Assertion)
            Assert.assertThat(e.getSuppressed().length, is(1));
            Assert.assertThat(e.getSuppressed()[0], instanceOf(AssertionError.class));
            Assert.assertThat(e.getSuppressed()[0].toString(), containsString("succeeded 0"));

            // ensure we didn't sleep that long!
            assertThat(watch.getElapsedTimeIn(TimeUnit.SECONDS), lessThan(1L));
        }
    }


    /**
     * Ensure a {@link Concurrently#assertThat(String, Deferred, Matcher, Option...)} fails fast when requested
     * and re-throws the exception.
     */
    @Test
    public void shouldFailFastWithAssertion()
    {
        RecordingMatcher<Integer> matcher = RecordingMatcher.of(is(1));
        StopWatch                 watch   = new StopWatch();

        watch.start();

        try
        {
            try (Concurrent.Assertion assertion = Concurrently.assertThat(42, matcher, FailFast.enabled()))
            {
                // attempt to sleep for a long time (we won't as we'll be interrupted)
                Thread.sleep(5000);

                Assert.fail("Should have been interrupted!");
            }
            catch (InterruptedException e)
            {
                Concurrent.Assertion.check(e);

                Assert.fail("Should have a suppressed AssertionError!");
            }
        }
        catch (AssertionError e)
        {
            Assert.assertThat(matcher.attempted(), is(true));
            Assert.assertThat(matcher.hasSucceeded(), is(false));
            Assert.assertThat(matcher.hasFailed(), is(true));
            Assert.assertThat(e.getMessage(), containsString("succeeded 0"));
        }
    }
}
