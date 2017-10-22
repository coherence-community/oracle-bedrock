/*
 * File: Repetitively.java
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

package com.oracle.bedrock.testsupport.deferred;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.deferred.DeferredFunction;
import com.oracle.bedrock.deferred.DeferredHelper;
import com.oracle.bedrock.deferred.PermanentlyUnavailableException;
import com.oracle.bedrock.deferred.TemporarilyUnavailableException;
import com.oracle.bedrock.deferred.options.InitialDelay;
import com.oracle.bedrock.deferred.options.MaximumRetryDelay;
import com.oracle.bedrock.deferred.options.RetryFrequency;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.util.Duration;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.valueOf;

/**
 * Commonly used "assertThat" methods for the purposes of repetitively asserting
 * the state of {@link Deferred} values.
 * <p>
 * To customize assertion behavior and timeouts, this class allows extensive use
 * of {@link Option}s.   For example, the following {@link Option}s may be used
 * to customize timeout constraints when asserting {@link Deferred} values;
 * {@link Timeout}, {@link MaximumRetryDelay}, Initial {@link InitialDelay} and
 * {@link RetryFrequency}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @see Eventually
 * @see Concurrently
 *
 * @author Brian Oliver
 */
public class Repetitively
{
    /**
     * Asserts that a value will repetitively satisfy the specified {@link Matcher}
     * using the specified {@link Option}s.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>      the type of the value
     *
     * @param value    the value
     * @param matcher  the {@link Matcher} for the value
     * @param options  the {@link Option}s for the assertion
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(T                  value,
                                      Matcher<? super T> matcher,
                                      Option...          options) throws AssertionError
    {
        assertThat(null, eventually(value), matcher, options);
    }


    /**
     * Asserts that a value will repetitively satisfy the specified {@link Matcher}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>       the type of the value
     *
     * @param message   the message for the AssertionError (<code>null</code> ok)
     * @param value     the value
     * @param matcher   the {@link Matcher} for the value
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String             message,
                                      T                  value,
                                      Matcher<? super T> matcher) throws AssertionError
    {
        assertThat(message, eventually(value), matcher);
    }


    /**
     * Asserts that a value will repetitively satisfy the specified {@link Matcher}
     * using the provided {@link Option}s.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>     the type of the value
     *
     * @param message  the message for the AssertionError (<code>null</code> ok)
     * @param value    the value
     * @param matcher  the {@link Matcher} for the value
     * @param options  the {@link Option}s
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String             message,
                                      T                  value,
                                      Matcher<? super T> matcher,
                                      Option...          options) throws AssertionError
    {
        assertThat(message, eventually(value), matcher, options);
    }


    /**
     * Asserts that a {@link Deferred} value, when available, repetitively satisfies
     * the specified {@link Matcher} over the {@link Timeout}
     * and other constraints defined by the provided {@link Option}s.
     *
     * @param <T>       the type of the value
     *
     * @param message   the message for the AssertionError (<code>null</code> ok)
     * @param deferred  the {@link Deferred} value
     * @param matcher   the {@link Matcher} for the value
     * @param options   the {@link Option}s
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String             message,
                                      Deferred<T>        deferred,
                                      Matcher<? super T> matcher,
                                      Option...          options) throws AssertionError
    {
        // determine the time-out and retry constraints
        OptionsByType optionsByType = OptionsByType.of(options);

        long initialDelayDurationMS = optionsByType.getOrDefault(InitialDelay.class,
                                                                 InitialDelay.none()).to(TimeUnit.MILLISECONDS);

        long maximumRetryDurationMS = optionsByType.getOrDefault(Timeout.class,
                                                                 Timeout.after(DeferredHelper.getDefaultEnsuredMaximumRetryDuration()))
                                                                 .to(TimeUnit.MILLISECONDS);

        long maximumPollingDurationMS = optionsByType.getOrDefault(MaximumRetryDelay.class,
                                                                   MaximumRetryDelay.of(DeferredHelper.getDefaultEnsuredMaximumPollingDuration()))
                                                                   .to(TimeUnit.MILLISECONDS);

        Iterator<Duration> retryDurations = optionsByType.getOrDefault(RetryFrequency.class,
                                                                       RetryFrequency.of(DeferredHelper.getDefaultEnsuredRetryDurationsIterable()))
                                                                       .get().iterator();

        // the number of times successfully matched
        int matchCount = 0;

        // the number of matches attempted
        int attemptCount = 0;

        // determine the maximum time we can wait
        long remainingRetryDurationMS = maximumRetryDurationMS;

        do
        {
            // wait the initial duration
            if (initialDelayDurationMS > 0)
            {
                try
                {
                    Thread.sleep(initialDelayDurationMS);
                }
                catch (InterruptedException e)
                {
                    throw new AssertionError("Interrupted while resolving " + deferred, e);
                }

                // reduce the remaining time
                remainingRetryDurationMS -= initialDelayDurationMS;

                // NOTE: even if there's no time remaining we'll at least
                // attempt to acquire the object reference just once!
            }

            // the time the most recent acquisition took
            long acquisitionDurationMS = 0;

            try
            {
                long started = System.currentTimeMillis();

                // set the remaining timeout
                optionsByType.add(Timeout.after(Math.min(remainingRetryDurationMS, 0), TimeUnit.MILLISECONDS));

                T    object  = DeferredHelper.ensure(deferred, optionsByType.asArray());

                long stopped = System.currentTimeMillis();

                // the time spent trying to access the resource
                // is considered as part of the remaining time
                acquisitionDurationMS    = stopped - started;
                remainingRetryDurationMS -= acquisitionDurationMS < 0 ? 0 : acquisitionDurationMS;

                // count this attempt
                attemptCount++;

                if (matcher.matches(object))
                {
                    // continue matching!
                    matchCount++;
                }
                else
                {
                    // generate a description for the match failure
                    StringDescription description = new StringDescription();

                    matcher.describeMismatch(object, description);

                    // throw an assertion with the error
                    throw new AssertionError(description.toString() + " (attempted " + attemptCount
                                             + " time(s), succeeded " + matchCount + " time(s))");
                }
            }
            catch (PermanentlyUnavailableException e)
            {
                // give up immediately!
                throw new AssertionError("Failed to resolve value for " + deferred, e);
            }
            catch (UnsupportedOperationException e)
            {
                // give up immediately when an operation is not supported
                throw new AssertionError("Failed to resolve value for " + deferred, e);
            }
            catch (TemporarilyUnavailableException e)
            {
                // SKIP: we will retry if the instance is temporarily unavailable
            }
            catch (RuntimeException e)
            {
                // SKIP: we assume all other runtime exceptions
                // simply means that we should retry
            }

            // determine if we need to retry
            if (maximumRetryDurationMS < 0 || remainingRetryDurationMS > 0)
            {
                // we can only retry while we have retry durations
                if (retryDurations.hasNext())
                {
                    try
                    {
                        Duration duration   = retryDurations.next();
                        long     durationMS = duration.to(TimeUnit.MILLISECONDS);

                        // ensure we don't wait longer than the maximum polling duration
                        if (durationMS > maximumPollingDurationMS)
                        {
                            durationMS = maximumPollingDurationMS;
                        }

                        // ensure we don't wait longer that the remaining duration
                        if (remainingRetryDurationMS - durationMS < 0)
                        {
                            durationMS = remainingRetryDurationMS;
                        }

                        // only wait if we have a duration
                        if (durationMS > 0)
                        {
                            TimeUnit.MILLISECONDS.sleep(durationMS);
                        }

                        // reduce the remaining time
                        remainingRetryDurationMS -= durationMS;
                    }
                    catch (InterruptedException e)
                    {
                        // if we're interrupted, we give up immediately
                        throw new AssertionError("Interrupted while resolving " + deferred, e);
                    }
                }
                else
                {
                    // if we run out of retry durations, we give up immediately
                    throw new AssertionError("Exhausted retry time-out durations");
                }
            }
        }
        while (maximumRetryDurationMS < 0 || remainingRetryDurationMS > 0);

        if (matchCount == 0)
        {
            throw new AssertionError("Failed to resolve a value for " + deferred);
        }
    }


    /**
     * Asserts that a specified function, when applied to a value, will repetitively satisfy a
     * {@link Matcher} using the provided {@link Option}s.
     *
     * @param <T>       the type of the value
     * @param <R>       the return type of the function
     *
     * @param value     the value
     * @param function  the function to apply to the value
     * @param matcher   the {@link Matcher}
     * @param options   the {@link Option}s
     *
     * @throws AssertionError
     */
    public static <T, R> void assertThat(T                  value,
                                         Function<T, R>     function,
                                         Matcher<? super R> matcher,
                                         Option...          options) throws AssertionError
    {
        assertThat(eventually(value), function, matcher, options);
    }


    /**
     * Asserts that a specified function, when applied to a {@link Deferred} value, will
     * repetitively satisfy a {@link Matcher} using the provided {@link Option}s.
     *
     * @param <T>       the type of the {@link Deferred} value
     * @param <R>       the return type of the function
     *
     * @param deferred  the {@link Deferred}
     * @param function  the function to apply to the value
     * @param matcher   the {@link Matcher}
     * @param options   the {@link Option}s
     *
     * @throws AssertionError
     */
    public static <T, R> void assertThat(Deferred<T>        deferred,
                                         Function<T, R>     function,
                                         Matcher<? super R> matcher,
                                         Option...          options) throws AssertionError
    {
        assertThat(null, new DeferredFunction<>(deferred, function), matcher, options);
    }


    /**
     * Asserts that the specified {@link RemoteCallable} submitted to the
     * {@link JavaApplication} repetitively matches the specified matcher
     * using the specified {@link Option}s.
     *
     * @param <T>          the type of the value
     *
     * @param application  the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable     the {@link RemoteCallable}
     * @param matcher      the {@link Matcher} representing the desire condition to match
     *
     * @throws AssertionError  if the assertion fails
     */
    public static <T> void assertThat(JavaApplication    application,
                                      RemoteCallable<T>  callable,
                                      Matcher<? super T> matcher) throws AssertionError
    {
        assertThat(valueOf(new DeferredRemoteExecution<T>(application, callable)), matcher);
    }


    /**
     * Asserts that the specified {@link RemoteCallable} submitted to
     * the {@link JavaApplication} repetitively matches the specified matcher
     * using the provided {@link Option}s.
     *
     * @param <T>          the type of the value
     *
     * @param application  the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable     the {@link RemoteCallable}
     * @param matcher      the {@link Matcher} representing the desire condition to match
     * @param options      the {@link Option}s
     *
     * @throws AssertionError  if the assertion fails
     */
    public static <T> void assertThat(JavaApplication    application,
                                      RemoteCallable<T>  callable,
                                      Matcher<? super T> matcher,
                                      Option...          options) throws AssertionError
    {
        assertThat(valueOf(new DeferredRemoteExecution<T>(application, callable)), matcher, options);
    }
}
