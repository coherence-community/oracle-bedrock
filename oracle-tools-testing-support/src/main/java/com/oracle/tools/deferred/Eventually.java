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

package com.oracle.tools.deferred;

import com.oracle.tools.runtime.concurrent.RemoteCallable;

import com.oracle.tools.runtime.java.JavaApplication;

import org.hamcrest.Matcher;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.eventually;

import java.util.concurrent.TimeUnit;

/**
 * A helper class that defines commonly used "assertThat" methods for the
 * purposes of unit, functional and integration testing, most of which support
 * working with {@link Deferred}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Eventually
{
    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}
     * within the bounds of the default {@link TimeoutConstraint}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>       the type of value produced by the {@link Deferred}
     *
     * @param value     the value
     * @param matcher   the {@link Matcher}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(T          value,
                                      Matcher<?> matcher) throws AssertionError
    {
        assertThat(null, eventually(value), matcher);
    }


    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}
     * within the bounds of the default {@link TimeoutConstraint}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>         the type of value produced by the {@link Deferred}
     *
     * @param value       the value
     * @param matcher     the {@link Matcher}
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(T                 value,
                                      Matcher<?>        matcher,
                                      TimeoutConstraint constraint) throws AssertionError
    {
        assertThat(null, eventually(value), matcher, constraint);
    }


    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}
     * within the bounds of the default {@link TimeoutConstraint}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>       the type of value produced by the {@link Deferred}
     *
     * @param message   the message for the AssertionError (<code>null</code> ok)
     * @param value     the value
     * @param matcher   the {@link Matcher}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String     message,
                                      T          value,
                                      Matcher<?> matcher) throws AssertionError
    {
        assertThat(message, eventually(value), matcher);
    }


    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}
     * within the bounds of the provided {@link TimeoutConstraint}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>         the type of value produced by the {@link Deferred}
     *
     * @param message     the message for the AssertionError (<code>null</code> ok)
     * @param value       the value
     * @param matcher     the {@link Matcher}
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String            message,
                                      T                 value,
                                      Matcher<?>        matcher,
                                      TimeoutConstraint constraint) throws AssertionError
    {
        assertThat(message, eventually(value), matcher, constraint);
    }


    /**
     * Asserts that a {@link Deferred}, when it becomes available,
     * will eventually satisfy the specified {@link Matcher}
     * within the bounds of the default {@link TimeoutConstraint}.
     *
     * @param <T>       the type of value produced by the {@link Deferred}
     *
     * @param deferred  the {@link Deferred}
     * @param matcher   the {@link Matcher}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(Deferred<T> deferred,
                                      Matcher<?>  matcher) throws AssertionError
    {
        assertThat(null, deferred, matcher);
    }


    /**
     * Asserts that a {@link Deferred}, when it becomes available,
     * will eventually satisfy the specified {@link Matcher}
     * within the bounds of the default {@link TimeoutConstraint}.
     *
     * @param <T>       the type of value produced by the {@link Deferred}
     *
     * @param message   the message for the AssertionError (<code>null</code> ok)
     * @param deferred  the {@link Deferred}
     * @param matcher   the {@link Matcher}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String      message,
                                      Deferred<T> deferred,
                                      Matcher<?>  matcher) throws AssertionError
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint();

        assertThat(message, deferred, matcher, constraint);
    }


    /**
     * Asserts that a {@link Deferred}, when it becomes available,
     * will eventually satisfy the specified {@link Matcher}
     * within the bounds of the provided {@link TimeoutConstraint}.
     *
     * @param <T>         the type of value produced by the {@link Deferred}
     *
     * @param deferred    the {@link Deferred}
     * @param matcher     the {@link Matcher}
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(Deferred<T>       deferred,
                                      Matcher<?>        matcher,
                                      TimeoutConstraint constraint) throws AssertionError
    {
        assertThat(null, deferred, matcher, constraint);
    }


    /**
     * Asserts that a {@link Deferred}, when it becomes available,
     * will eventually satisfy the specified {@link Matcher}
     * within the bounds of the provided {@link TimeoutConstraint}.
     *
     * @param <T>         the type of value produced by the {@link Deferred}
     *
     * @param message     the message for the AssertionError (<code>null</code> ok)
     * @param deferred    the {@link Deferred}
     * @param matcher     the {@link Matcher}
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> void assertThat(String            message,
                                      Deferred<T>       deferred,
                                      Matcher<?>        matcher,
                                      TimeoutConstraint constraint) throws AssertionError
    {
        // a DeferredMatcher does the heavy lifting
        DeferredMatch<T> deferredMatch = new DeferredMatch<T>(deferred, matcher);

        try
        {
            ensure(deferredMatch, constraint);
        }
        catch (UnresolvableInstanceException e)
        {
            AssertionError error;

            if (deferredMatch.getLastUsedMatchValue() == null)
            {
                error = new AssertionError((message == null ? "" : message + ": ") + "Failed to resolve a value for ["
                                           + deferredMatch.getDeferred() + "] to evaluate with matcher ["
                                           + deferredMatch.getMatcher() + "]");
                error.initCause(e);
            }
            else
            {
                error = new AssertionError((message == null ? "" : message + ": ") + "Matcher [" + matcher
                                           + "] failed to match last resolved value ["
                                           + deferredMatch.getLastUsedMatchValue() + "] for ["
                                           + deferredMatch.getDeferred() + "]");
                error.initCause(e);
            }

            throw error;
        }
        catch (Exception e)
        {
            AssertionError error = new AssertionError((message == null ? "" : message + ": ")
                                                      + "Unexpected exception when attempting to resolve a value for ["
                                                      + deferredMatch.getDeferred() + "] to evaluate with matcher ["
                                                      + deferredMatch.getMatcher() + "]");

            error.initCause(e);

            throw error;
        }
    }


    /**
     * Asserts that the specified {@link RemoteCallable} submitted to the
     * {@link JavaApplication} will eventually match the specified matcher
     * within the bounds of the default {@link TimeoutConstraint}.
     *
     * @param <T>          the type of value produced by the {@link Deferred}
     *
     * @param application  the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable     the {@link RemoteCallable}
     * @param matcher      the {@link Matcher} representing the desire condition to match
     *
     * @throws AssertionError  if the assertion fails
     */
    public static <T> void assertThat(JavaApplication   application,
                                      RemoteCallable<T> callable,
                                      Matcher<?>        matcher) throws AssertionError
    {
        assertThat(new DeferredRemoteExecution<T>(application, callable), matcher);
    }


    /**
     * Asserts that the specified {@link RemoteCallable} submitted to
     * the {@link JavaApplication} will eventually match the specified matcher
     * within the bounds of the provided {@link TimeoutConstraint}.
     *
     * @param <T>          the type of value produced by the {@link Deferred}
     *
     * @param application  the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable     the {@link RemoteCallable}
     * @param matcher      the {@link Matcher} representing the desire condition to match
     * @param constraint   the {@link TimeoutConstraint}
     *
     * @throws AssertionError  if the assertion fails
     */
    public static <T> void assertThat(JavaApplication   application,
                                      RemoteCallable<T> callable,
                                      Matcher<?>        matcher,
                                      TimeoutConstraint constraint) throws AssertionError
    {
        assertThat(new DeferredRemoteExecution<T>(application, callable), matcher, constraint);
    }


    /**
     * Obtains a {@link TimeoutConstraint} with the specified maximum
     * duration, no initial duration and using the default ensured strategy.
     *
     * @param duration  the maximum duration
     * @param units     the maximum duration units
     *
     * @return  a {@link TimeoutConstraint}
     */
    public static SimpleTimeoutConstraint within(long     duration,
                                                 TimeUnit units)
    {
        return DeferredHelper.within(duration, units);
    }


    /**
     * Obtains a {@link TimeoutConstraint} with the specified initial delay,
     * using the default maximum retry and ensured strategy.
     *
     * @param duration  the initial delay duration
     * @param units     the initial delay duration units
     *
     * @return  a {@link TimeoutConstraint}
     */
    public static SimpleTimeoutConstraint delayedBy(long     duration,
                                                    TimeUnit units)
    {
        return DeferredHelper.delayedBy(duration, units);
    }


    // ------------------------------------------------------------------------
    // <deferred-methods> (will be removed in a later release)
    // ------------------------------------------------------------------------

    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>                      the type of value produced by the {@link Deferred}
     *
     * @param value                    the value
     * @param matcher                  the {@link Matcher}
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @throws AssertionError if the assertion fails
     *
     * @deprecated use {@link #assertThat(Object, Matcher, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> void assertThat(T          value,
                                      Matcher<?> matcher,
                                      long       totalRetryDuration,
                                      TimeUnit   totalRetryDurationUnits) throws AssertionError
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   DeferredHelper
                                                                       .getDefaultEnsuredRetryDurationsMSIterable());

        assertThat(null, eventually(value), matcher, constraint);
    }


    /**
     * Asserts that a value will eventually satisfy the specified {@link Matcher}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>                      the type of value produced by the {@link Deferred}
     *
     * @param message                  the message for the AssertionError (<code>null</code> ok)
     * @param value                    the value
     * @param matcher                  the {@link Matcher}
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @throws AssertionError if the assertion fails
     *
     * @deprecated use {@link #assertThat(String, Object, Matcher, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> void assertThat(String     message,
                                      T          value,
                                      Matcher<?> matcher,
                                      long       totalRetryDuration,
                                      TimeUnit   totalRetryDurationUnits) throws AssertionError
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   DeferredHelper
                                                                       .getDefaultEnsuredRetryDurationsMSIterable());

        assertThat(message, eventually(value), matcher, constraint);
    }


    /**
     * Asserts that a {@link Deferred}, when it becomes available,
     * will eventually (after the specified amount of time) satisfy the
     * specified {@link Matcher}.
     *
     * @param <T>                      the type of value produced by the {@link Deferred}
     *
     * @param deferred                 the {@link Deferred}
     * @param matcher                  the {@link Matcher}
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @throws AssertionError if the assertion fails
     *
     * @deprecated use {@link #assertThat(Deferred, Matcher, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> void assertThat(Deferred<T> deferred,
                                      Matcher<?>  matcher,
                                      long        totalRetryDuration,
                                      TimeUnit    totalRetryDurationUnits) throws AssertionError
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   DeferredHelper
                                                                       .getDefaultEnsuredRetryDurationsMSIterable());

        assertThat(null, deferred, matcher, constraint);
    }


    /**
     * Asserts that a {@link Deferred}, when it becomes available,
     * will eventually (after the specified amount of time) satisfy the
     * specified {@link Matcher}.
     *
     * @param <T>                      the type of value produced by the {@link Deferred}
     *
     * @param message                  the message for the AssertionError (<code>null</code> ok)
     * @param deferred                 the {@link Deferred}
     * @param matcher                  the {@link Matcher}
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @throws AssertionError if the assertion fails
     *
     * @deprecated use {@link #assertThat(String, Deferred, Matcher, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> void assertThat(String      message,
                                      Deferred<T> deferred,
                                      Matcher<?>  matcher,
                                      long        totalRetryDuration,
                                      TimeUnit    totalRetryDurationUnits) throws AssertionError
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   DeferredHelper
                                                                       .getDefaultEnsuredRetryDurationsMSIterable());

        assertThat(message, deferred, matcher, constraint);
    }


    /**
     * Asserts that the specified {@link RemoteCallable} submitted to the {@link JavaApplication}
     * will eventually match the specified matcher.
     *
     * @param <T>                      the type of value produced by the {@link Deferred}
     *
     * @param application              the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable                 the {@link RemoteCallable}
     * @param matcher                  the {@link Matcher} representing the desire condition to match
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @throws AssertionError  if the assertion fails
     *
     * @deprecated use {@link #assertThat(JavaApplication, RemoteCallable, Matcher, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> void assertThat(JavaApplication   application,
                                      RemoteCallable<T> callable,
                                      Matcher<?>        matcher,
                                      long              totalRetryDuration,
                                      TimeUnit          totalRetryDurationUnits) throws AssertionError
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   DeferredHelper
                                                                       .getDefaultEnsuredRetryDurationsMSIterable());

        assertThat(new DeferredRemoteExecution<T>(application, callable), matcher, constraint);
    }

    // ------------------------------------------------------------------------
    // </deferred-methods> (will be removed in a later release)
    // ------------------------------------------------------------------------
}
