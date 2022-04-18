/*
 * File: Eventually.java
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
import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.deferred.DeferredFunction;
import com.oracle.bedrock.deferred.DeferredHelper;
import com.oracle.bedrock.deferred.Existing;
import com.oracle.bedrock.deferred.PermanentlyUnavailableException;
import com.oracle.bedrock.deferred.options.InitialDelay;
import com.oracle.bedrock.deferred.options.MaximumRetryDelay;
import com.oracle.bedrock.deferred.options.RetryFrequency;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.java.JavaApplication;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.io.NotSerializableException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.valueOf;

/**
 * Commonly used "assertThat" methods for the purposes of asserting conditions
 * concerning the state of {@link Deferred} values.
 * <p>
 * To customize assertion behavior and timeouts, this class allows extensive use
 * of {@link Option}s.   For example, the following {@link Option}s may be used
 * to customize timeout constraints when asserting {@link Deferred} values;
 * {@link Timeout}, {@link MaximumRetryDelay}, Initial {@link InitialDelay} and
 * {@link RetryFrequency}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @see Repetitively
 * @see Concurrently
 *
 * @author Brian Oliver
 */
public class Eventually
{
    /**
     * Asserts that a value returned by a specified {@link Deferred} will
     * eventually satisfy a {@link Matcher} using the provided {@link Option}s.
     *
     * @param <T>      the type of the value
     * @param deferred the deferred value
     * @param matcher  the {@link Matcher} for the value
     * @param options  the {@link Option}s for the assertion
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertDeferred(Deferred<T> deferred,
                                          Matcher<? super T> matcher,
                                          Option... options) throws AssertionError
    {
        assertDeferred(null, deferred, matcher, options);
    }

    /**
     * Asserts that a value returned by a specified {@link Deferred} will
     * eventually satisfy a {@link Matcher} using the provided {@link Option}s.
     *
     * @param <T>      the type of the value
     * @param message  the message for the AssertionError (<code>null</code> ok)
     * @param deferred the deferred of value
     * @param matcher  the {@link Matcher} for the value
     * @param options  the {@link Option}s
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertDeferred(String message,
                                          Deferred<T> deferred,
                                          Matcher<? super T> matcher,
                                          Option... options) throws AssertionError
    {
        assertThat(message, deferred, matcher, options);
    }

    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}
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
     * Asserts that a value will eventually satisfy the specified {@link Matcher}.
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
     * Asserts that a value will eventually satisfy the specified {@link Matcher}
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
     * Asserts that a specified function, when applied to a value, will eventually satisfy a
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
     * @throws AssertionError if the test fails
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
     * eventually satisfy a {@link Matcher} using the provided {@link Option}s.
     *                                                       
     * @param <T>       the type of the {@link Deferred} value
     * @param <R>       the return type of the function
     *
     * @param deferred  the {@link Deferred}
     * @param function  the function to apply to the value
     * @param matcher   the {@link Matcher}
     * @param options   the {@link Option}s
     *
     * @throws AssertionError if the test fails
     */
    public static <T, R> void assertThat(Deferred<T>        deferred,
                                         Function<T, R>     function,
                                         Matcher<? super R> matcher,
                                         Option...          options) throws AssertionError
    {
        assertThat(null, new DeferredFunction<>(deferred, function), matcher, options);
    }


    /**
     * Asserts that a {@link Deferred} value, when it becomes available,
     * will eventually satisfy the specified {@link Matcher}
     * within the bounds of the provided {@link Option}s.
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
        // a DeferredMatcher does the heavy lifting
        DeferredMatch<T> deferredMatch = new DeferredMatch<>(deferred, matcher);

        try
        {
            DeferredHelper.ensure(deferredMatch, options);
        }
        catch (PermanentlyUnavailableException e)
        {
            AssertionError error;
            String         cause = "";

            if (deferredMatch.getLastUsedMatchValue() == null)
            {
                if (e.getCause() instanceof NotSerializableException)
                {
                    cause = " (NotSerializableException thrown)";
                }

                error = new AssertionError((message == null ? "" : message + ": ") + "Failed to resolve a value for ["
                                                                                   + deferredMatch.getDeferred()
                                                                                   + "] to evaluate with matcher ["
                                                                                   + deferredMatch.getMatcher() + "]"
                                                                                   + cause);
                error.initCause(e);
            }
            else
            {
                if (deferred instanceof Existing)
                {
                    cause = ", (Deferred was not retried as it was a simple value)";
                }

                // ask the matcher to describe the matching problem
                StringDescription description = new StringDescription();

                matcher.describeMismatch(deferredMatch.getLastUsedMatchValue(), description);

                // create the assertion
                error = new AssertionError((message == null ? "" : message + ": ") + "Matcher [" + matcher
                    + "] failed to match last resolved value [" + deferredMatch.getLastUsedMatchValue() + "] for ["
                    + deferredMatch.getDeferred() + "]" + cause + " due to " + description.toString());
                error.initCause(e);
            }

            throw error;
        }
        catch (Exception e)
        {
            AssertionError error =
                new AssertionError((message == null
                                    ? ""
                                    : message + ": ") + "Unexpected exception when attempting to resolve a value for ["
                                                      + deferredMatch.getDeferred() + "] to evaluate with matcher ["
                                                      + deferredMatch.getMatcher() + "]");

            error.initCause(e);

            throw error;
        }
    }


    /**
     * Asserts that the specified {@link RemoteCallable} submitted to the
     * {@link JavaApplication} will eventually match the specified matcher
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
     * the {@link JavaApplication} will eventually satisfy the specified matcher
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


    /**
     * Obtains a {@link Timeout} with the specified duration.
     *
     * @param duration  the duration
     * @param units     the duration units
     *
     * @return  a {@link Timeout}
     */
    public static Timeout within(long     duration,
                                 TimeUnit units)
    {
        return Timeout.after(duration, units);
    }


    /**
     * Obtains a {@link InitialDelay} for the specified values.
     *
     * @param duration  the delay duration
     * @param units     the delay duration units
     *
     * @return  a {@link InitialDelay}
     */
    public static InitialDelay delayedBy(long     duration,
                                         TimeUnit units)
    {
        return InitialDelay.of(duration, units);
    }
}
