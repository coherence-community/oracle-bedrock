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

package com.oracle.bedrock.deferred;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.deferred.options.InitialDelay;
import com.oracle.bedrock.deferred.options.MaximumRetryDelay;
import com.oracle.bedrock.deferred.options.RetryFrequency;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.java.JavaApplication;
import org.hamcrest.Matcher;

import java.io.NotSerializableException;
import java.util.concurrent.TimeUnit;

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

                error = new AssertionError((message == null ? "" : message + ": ") + "Matcher [" + matcher
                    + "] failed to match last resolved value [" + deferredMatch.getLastUsedMatchValue() + "] for ["
                    + deferredMatch.getDeferred() + "]" + cause);
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
