/*
 * File: DeferredHelper.java
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

import com.oracle.tools.deferred.atomic.DeferredAtomicBoolean;
import com.oracle.tools.deferred.atomic.DeferredAtomicInteger;
import com.oracle.tools.deferred.atomic.DeferredAtomicLong;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.predicate.Predicate;

import com.oracle.tools.util.ExponentialIterator;
import com.oracle.tools.util.FibonacciIterator;
import com.oracle.tools.util.PerpetualIterator;
import com.oracle.tools.util.RandomIterator;
import com.oracle.tools.util.ReflectionHelper;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import java.util.Iterator;

import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@link DeferredHelper} defines a collection of static helper methods
 * for working with {@link Deferred}s.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredHelper
{
    /**
     * The retry strategy that will be used for {@link Ensured}s.
     * <p/>
     * Legal Values are:
     * <ol>
     *     <li>constant            = polling every 250ms</li>
     *     <li>fibonacci           = polling based on values taken in order from
     *                               the fibonacci sequence</li>
     *     <li>exponential         = polling based on values taken in order from
     *                               an exponential sequence (a rate of 50%)</li>
     *     <li>random.fibonacci    = polling based on randomized values taken in
     *                               order from the fibonacci sequence</li>
     *     <li>randomo.exponential = polling based on randomized values taken in
     *                               order from an exponential sequence
     *                               (a rate of 50%)</li>
     * </ol>
     * <p/>
     * The default strategy is "random.fibonacci"
     */
    public static final String ORACLETOOLS_DEFERRED_RETRY_STRATEGY = "oracletools.deferred.retry.strategy";

    /**
     * The maximum retry (timeout) that will be used for {@link Ensured}s.
     * <p/>
     * By default values are measured in milliseconds, however when
     * time units are specified after the amounts eg:
     * (ms = milliseconds, m = minutes, s = seconds, h = hours),
     * conversions are automatically made to milliseconds.
     */
    public static final String ORACLETOOLS_DEFERRED_RETRY_TIMEOUT = "oracletools.deferred.retry.timeout";

    /**
     * The default maximum retry timeout to use (in seconds) when a timeout
     * is not configured or specified.
     */
    public static final long ORACLETOOLS_DEFERRED_RETRY_TIMEOUT_SECS = 60;

    /**
     * A {@link ThreadLocal} to capture the most recent {@link Deferred}
     * method call on a proxy created by {@link #invoking(Deferred)}.
     * <p>
     * See {@link #invoking(Deferred)} and {@link #eventually(Object)}
     * for more information.
     */
    private static final ThreadLocal<Deferred<?>> m_deferred = new ThreadLocal<Deferred<?>>();

    /**
     * An {@link Iterable} that produces {@link Iterator}s to use for retry
     * durations (measured in milliseconds).
     */
    private static final Iterable<Long> ensuredRetryDurationsMSIterable;


    /**
     * Obtains a {@link Deferred} representation of an {@link AtomicLong}.
     *
     * @param atomic  the atomic value to be deferred
     *
     * @return a {@link Deferred} for the atomic value
     */
    public static Deferred<Long> deferred(AtomicLong atomic)
    {
        return new DeferredAtomicLong(atomic);
    }


    /**
     * Obtains a {@link Deferred} representation of an {@link AtomicInteger}.
     *
     * @param atomic  the atomic value to be deferred
     *
     * @return a {@link Deferred} for the atomic value
     */
    public static Deferred<Integer> deferred(AtomicInteger atomic)
    {
        return new DeferredAtomicInteger(atomic);
    }


    /**
     * Obtains a {@link Deferred} representation of an {@link AtomicBoolean}.
     *
     * @param atomic  the atomic value to be deferred
     *
     * @return a {@link Deferred} for the atomic value
     */
    public static Deferred<Boolean> deferred(AtomicBoolean atomic)
    {
        return new DeferredAtomicBoolean(atomic);
    }


    /**
     * Obtains the default configured retry durations {@link Iterator}
     * (each value measured in milliseconds) that can be used with
     * {@link Ensured}s.
     *
     * @return a new instance of the default configured retry durations
     *         {@link Iterator}
     */
    public static Iterator<Long> getDefaultEnsuredRetryDurationsMS()
    {
        return ensuredRetryDurationsMSIterable.iterator();
    }


    /**
     * Obtains the default configured retry durations {@link Iterable}
     * that can be used with {@link Ensured}s.
     *
     * @return an {@link Iterable}
     */
    public static Iterable<Long> getDefaultEnsuredRetryDurationsMSIterable()
    {
        return ensuredRetryDurationsMSIterable;
    }


    /**
     * Obtains the default timeout/maximum wait duration (in milliseconds)
     * for {@link Ensured}s.
     *
     * @return the default timeout (in milliseconds)
     */
    public static long getDefaultEnsuredTimeoutMS()
    {
        String timeOut = System.getProperty(ORACLETOOLS_DEFERRED_RETRY_TIMEOUT);

        if (timeOut == null)
        {
            return TimeUnit.SECONDS.toMillis(ORACLETOOLS_DEFERRED_RETRY_TIMEOUT_SECS);
        }
        else
        {
            timeOut = timeOut.trim().toLowerCase();

            TimeUnit timeUnit;

            if (timeOut.endsWith("ms"))
            {
                timeUnit = TimeUnit.MILLISECONDS;
                timeOut  = timeOut.substring(timeOut.length() - 2).trim();
            }
            else if (timeOut.endsWith("s"))
            {
                timeUnit = TimeUnit.SECONDS;
                timeOut  = timeOut.substring(timeOut.length() - 1).trim();
            }
            else if (timeOut.endsWith("m"))
            {
                timeUnit = TimeUnit.MINUTES;
                timeOut  = timeOut.substring(timeOut.length() - 1).trim();
            }
            else if (timeOut.endsWith("h"))
            {
                timeUnit = TimeUnit.HOURS;
                timeOut  = timeOut.substring(timeOut.length() - 1).trim();
            }
            else
            {
                // assume milliseconds when there's no timeout unit
                timeUnit = TimeUnit.MILLISECONDS;
            }

            return timeUnit.toMillis(Long.valueOf(timeOut));
        }
    }


    /**
     * Obtains an ensured of the specified {@link Deferred}
     * configured using default {@link TimeoutConstraint}.
     *
     * @param deferred  the {@link Deferred} to ensure
     *
     * @return an {@link Ensured} of the {@link Deferred}
     */
    public static <T> Deferred<T> ensured(Deferred<T> deferred)
    {
        return deferred instanceof Ensured ? deferred : new Ensured<T>(deferred);
    }


    /**
     * Obtains an ensured of the specified {@link Deferred}
     * using the provided {@link TimeoutConstraint}.
     *
     * @param deferred    the {@link Deferred} to ensure
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @return an {@link Ensured} of the {@link Deferred}
     */
    public static <T> Deferred<T> ensured(Deferred<T>       deferred,
                                          TimeoutConstraint constraint)
    {
        return deferred instanceof Ensured ? deferred : new Ensured<T>(deferred, constraint);
    }


    /**
     * Obtains the value of a value (this is the identity function for non-deferred).
     * <p>
     * This method is provided for API symmetry.
     *
     * @param value  the value
     *
     * @return the value
     */
    public static <T> T ensure(T value)
    {
        return value;
    }


    /**
     * Obtains the value of a {@link Deferred}.
     * <p>
     * This is functionally equivalent to calling {@link Deferred#get()} on
     * {@link #ensured(Deferred)}.
     *
     * @param deferred  the {@link Deferred} to ensure
     *
     * @return the value of the {@link Deferred}
     */
    public static <T> T ensure(Deferred<T> deferred)
    {
        return ensured(deferred).get();
    }


    /**
     * Obtains the value of a {@link Deferred}.
     * <p>
     * This is functionally equivalent to calling {@link Deferred#get()} on
     * {@link #ensured(Deferred, TimeoutConstraint)}.
     *
     * @param deferred    the {@link Deferred} to ensure
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @return the value of the {@link Deferred}
     */
    public static <T> T ensure(Deferred<T>       deferred,
                               TimeoutConstraint constraint)
    {
        return ensured(deferred, constraint).get();
    }


    public static <T> boolean ensure(Deferred<T>          deferred,
                                     Predicate<? super T> predicate)
    {
        return ensure(new DeferredPredicate<T>(deferred, predicate));
    }


    /**
     * Obtains an {@link Cached} of the specified {@link Deferred}.
     *
     * @param deferred  the {@link Deferred} to cache
     *
     * @return a {@link Cached} of the {@link Deferred}
     */
    public static <T> Cached<T> cached(Deferred<T> deferred)
    {
        return deferred instanceof Cached ? (Cached<T>) deferred : new Cached<T>(deferred);
    }


    /**
     * Obtains a {@link Deferred} representation of a Java Future.
     *
     * @param clzOfResult  the {@link Class} of result from the
     *                     {@link java.util.concurrent.Future}
     * @param future       the {@link java.util.concurrent.Future}
     *
     * @return a {@link Deferred}
     */
    public static <T> Deferred<T> future(Class<T>                       clzOfResult,
                                         java.util.concurrent.Future<T> future)
    {
        return new Future<T>(clzOfResult, future);
    }


    /**
     * Creates a dynamic proxy of an {@link Object}.  The returned proxy will
     * record interactions (method calls) against the proxy for the
     * purposes of representing the calls as {@link Deferred}s.
     * <p>
     * The results of interactions on the returned proxy are always non-sense
     * and/or other dynamic proxies.  To determine the actual result (as
     * a {@link Deferred}), one must call {@link #eventually(Object)}.
     *
     * @param <T>     the type of {@link Object}
     * @param object  the {@link Object} to proxy
     *
     * @return a recording dynamic proxy of the {@link Object}
     */
    public static <T> T invoking(T object)
    {
        return invoking(new Existing<T>(object));
    }


    /**
     * Creates a dynamic proxy of a {@link Deferred} object.  The returned proxy
     * will record interactions (method calls) against the proxy for the
     * purposes of representing the calls as {@link Deferred}s.
     * <p>
     * The results of interactions on the returned proxy are always non-sense
     * and/or other dynamic proxies.  To determine the actual result (as
     * a {@link Deferred}), one must call {@link #eventually(Object)}.
     *
     * @param <T>       the type of {@link Object}
     * @param deferred  the {@link Deferred} object to proxy
     *
     * @return a recording dynamic proxy of the {@link Object}
     */
    public static <T> T invoking(Deferred<T> deferred)
    {
        // ensure that there are no other pending invoking calls on this thread
        if (m_deferred.get() == null)
        {
            // attempt to create a proxy of the specified object class that will record
            // methods calls on the object and represent them as a deferred on a thread local

            // FUTURE: we should raise a soft exception here if the deferred
            // class is final or perhaps native as we can't proxy them.

            T proxy = ReflectionHelper.createProxyOf(deferred.getDeferredClass(), new DeferredMethodInteceptor());

            // set the current deferred as a thread local so that
            // we can "eventually" evaluate and return it.
            m_deferred.set(deferred);

            return proxy;
        }
        else
        {
            throw new UnsupportedOperationException("An attempt was made to call 'invoking' without being wrapped inside an 'eventually' call. "
                                                    + "Alternatively two or more calls to 'invoking' have been made sequentially. "
                                                    + "Calls to 'invoking' must be contained inside an 'eventually' call.");
        }
    }


    /**
     * A specialized mechanism for allowing custom {@link Deferred} implementations to be used
     * within 'eventually' / 'assertThat' calls, where no method chaining is required or used
     * (unlike {@link #invoking}).
     *
     * @param <T>       the type of {@link Object}
     * @param deferred  the {@link Deferred}
     *
     * @return  a dumby value representing the {@link Deferred}
     */
    public static <T> T valueOf(Deferred<T> deferred)
    {
        // ensure that there are no other pending invoking calls on this thread
        if (m_deferred.get() == null)
        {
            // set the current deferred as a thread local so that
            // we can "eventually" evaluate and return it.
            m_deferred.set(deferred);

            // FUTURE: we should raise a soft exception here if the deferred
            // class is final or perhaps native as we can't proxy them.

            // we return null as the deferred will be retrieved from the ThreadLocal
            // by the outer 'eventually' call
            return null;
        }
        else
        {
            throw new UnsupportedOperationException("An attempt was made to call 'valueOf' without being wrapped inside of an 'eventually' call. "
                                                    + "Alternatively two or more calls to 'valueOf' have been made sequentially. "
                                                    + "Calls to 'valueOf' must be contained inside an 'eventually' call.");
        }
    }


    /**
     * Obtains a {@link Deferred} representation of the last call to a
     * dynamic proxy created with either {@link #invoking(Object)} or
     * {@link #invoking(Deferred)}.
     *
     * @param t  the value returned from an call to 'invoking'
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     */
    @SuppressWarnings("unchecked")
    public static <T> Deferred<T> eventually(T t)
    {
        // get the last deferred value from invoking
        Deferred<T> deferred = (Deferred<T>) m_deferred.get();

        if (deferred == null)
        {
            deferred = t instanceof Deferred ? (Deferred<T>) t : new Existing<T>(t);
        }
        else
        {
            // clear the last invoking call
            m_deferred.set(null);
        }

        return ensured(deferred);
    }


    /**
     * Obtains a {@link Deferred} representation of the last call to a
     * dynamic proxy created with either {@link #invoking(Object)} or
     * {@link #invoking(Deferred)}.  If there was no call to create a
     * dynamic proxy, the provided deferred is ensured and returned.
     *
     * @param t  the deferred value (usually returned from 'invoking')
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     */
    @SuppressWarnings("unchecked")
    public static <T> Deferred<T> eventually(Deferred<T> t)
    {
        // get the last deferred value from invoking
        Deferred<T> deferred = (Deferred<T>) m_deferred.get();

        if (deferred == null)
        {
            deferred = t;
        }
        else
        {
            // clear the last invoking call
            m_deferred.set(null);
        }

        return ensured(deferred);
    }


    /**
     * Obtains a {@link Deferred} representation of the last call to a
     * dynamic proxy created with either {@link #invoking(Object)} or
     * {@link #invoking(Deferred)}.
     *
     * @param t           the value returned from an call to 'invoking'
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     */
    public static <T> Deferred<T> eventually(T                 t,
                                             TimeoutConstraint constraint)
    {
        // get the last deferred value from invoking
        Deferred<T> deferred = (Deferred<T>) m_deferred.get();

        if (deferred == null)
        {
            deferred = t instanceof Deferred ? (Deferred<T>) t : new Existing<T>(t);
        }
        else
        {
            // clear the last invoking call
            m_deferred.set(null);
        }

        return ensured(deferred, constraint);
    }


    /**
     * Obtains a {@link Deferred} representation of the last call to a
     * dynamic proxy created with either {@link #invoking(Object)} or
     * {@link #invoking(Deferred)}.   If there was no call to create a
     * dynamic proxy, the provided deferred is ensured and returned.
     *
     * @param t           the deferred value (usually returned from 'invoking')
     * @param constraint  the {@link TimeoutConstraint}
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     */
    public static <T> Deferred<T> eventually(Deferred<T>       t,
                                             TimeoutConstraint constraint)
    {
        // get the last deferred value from invoking
        Deferred<T> deferred = (Deferred<T>) m_deferred.get();

        if (deferred == null)
        {
            deferred = t;
        }
        else
        {
            // clear the last invoking call
            m_deferred.set(null);
        }

        return ensured(deferred, constraint);
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
        return new SimpleTimeoutConstraint(0, units.toMillis(duration), ensuredRetryDurationsMSIterable);
    }


    /**
     * Obtains a {@link TimeoutConstraint} based on the specified
     * {@link Timeout}, no initial duration and using the default ensured strategy.
     *
     * @param timeout  the {@link Timeout}
     *
     * @return  a {@link TimeoutConstraint}
     */
    public static SimpleTimeoutConstraint within(Timeout timeout)
    {
        return new SimpleTimeoutConstraint(0,
                                           timeout.getUnits().toMillis(timeout.getDuration()),
                                           ensuredRetryDurationsMSIterable);
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
        return new SimpleTimeoutConstraint(units.toMillis(duration),
                                           getDefaultEnsuredTimeoutMS(),
                                           ensuredRetryDurationsMSIterable);
    }


    // ------------------------------------------------------------------------
    // <deferred-methods> (will be removed in a later release)
    // ------------------------------------------------------------------------

    /**
     * Obtains an ensured of the specified {@link Deferred}.
     *
     * @param deferred                   the {@link Deferred} to ensure
     * @param maximumRetryDuration       the maximum duration for retrying
     * @param maximumRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return an {@link Ensured} of the {@link Deferred}
     *
     * @deprecated use {@link #ensured(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> Deferred<T> ensured(Deferred<T> deferred,
                                          long        maximumRetryDuration,
                                          TimeUnit    maximumRetryDurationUnits)
    {
        long maximumRetryDurationMS = maximumRetryDurationUnits.toMillis(maximumRetryDuration);

        return deferred instanceof Ensured ? deferred : new Ensured<T>(deferred,
                                                                       new SimpleTimeoutConstraint(0,
                                                                                                   maximumRetryDurationMS,
                                                                                                   ensuredRetryDurationsMSIterable));
    }


    /**
     * Obtains an ensured of the specified {@link Deferred}.
     *
     * @param deferred                 the {@link Deferred} to ensure
     * @param retryDelayDuration       the time to wait between retrying
     * @param retryDelayDurationUnits  the {@link TimeUnit}s for the retry delay duration
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return an {@link Ensured} of the {@link Deferred}
     *
     * @deprecated use {@link #ensured(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> Deferred<T> ensured(Deferred<T> deferred,
                                          long        retryDelayDuration,
                                          TimeUnit    retryDelayDurationUnits,
                                          long        totalRetryDuration,
                                          TimeUnit    totalRetryDurationUnits)
    {
        Iterator<Long> retryDurationsMS =
            new PerpetualIterator<Long>(retryDelayDurationUnits.toMillis(retryDelayDuration < 0
                                                                         ? 0 : retryDelayDuration));

        return deferred instanceof Ensured ? deferred : new Ensured<T>(deferred,
                                                                       retryDurationsMS,
                                                                       totalRetryDurationUnits
                                                                           .toMillis(totalRetryDuration));
    }


    /**
     * Obtains an {@link Ensured} of the specified {@link Deferred}.
     *
     * @param deferred              the {@link Deferred} to ensure
     * @param totalRetryDurationMS  the maximum duration (in milliseconds) to retry
     *
     * @return an {@link Ensured} of the {@link Deferred}
     *
     * @deprecated use {@link #ensured(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> Deferred<T> ensured(Deferred<T> deferred,
                                          long        totalRetryDurationMS)
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationMS,
                                                                   ensuredRetryDurationsMSIterable);

        return deferred instanceof Ensured ? (Ensured<T>) deferred : new Ensured<T>(deferred, constraint);
    }


    /**
     * Obtains the value of a {@link Deferred}.
     * <p>
     * This is functionally equivalent to calling {@link Deferred#get()} on
     * {@link #ensure(Deferred, long, TimeUnit)}.
     *
     * @param deferred                 the {@link Deferred} to ensure
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return the value of the {@link Deferred}
     *
     * @deprecated use {@link #ensure(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> T ensure(Deferred<T> deferred,
                               long        totalRetryDuration,
                               TimeUnit    totalRetryDurationUnits)
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   ensuredRetryDurationsMSIterable);

        return ensured(deferred, constraint).get();
    }


    /**
     * Obtains the value of a {@link Deferred}.
     * <p>
     * This is functionally equivalent to calling {@link Deferred#get()} on
     * {@link #ensure(Deferred, long, TimeUnit)}.
     *
     * @param deferred                 the {@link Deferred} to ensure
     * @param retryDelayDuration       the time to wait between retrying
     * @param retryDelayDurationUnits  the {@link TimeUnit}s for the retry delay duration
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return the value of the {@link Deferred}
     *
     * @deprecated use {@link #ensure(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> T ensure(Deferred<T> deferred,
                               long        retryDelayDuration,
                               TimeUnit    retryDelayDurationUnits,
                               long        totalRetryDuration,
                               TimeUnit    totalRetryDurationUnits)
    {
        return ensured(deferred,
                       retryDelayDuration,
                       retryDelayDurationUnits,
                       totalRetryDuration,
                       totalRetryDurationUnits).get();
    }


    /**
     * Obtains the value of a {@link Deferred}.
     * <p>
     * This is functionally equivalent to calling {@link Deferred#get()} on
     * {@link #ensure(Deferred, long)}.
     *
     * @param deferred              the {@link Deferred} to ensure
     * @param totalRetryDurationMS  the maximum duration (in milliseconds) to retry
     *
     * @return an {@link Ensured} of the {@link Deferred}
     *
     * @deprecated use {@link #ensure(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> T ensure(Deferred<T> deferred,
                               long        totalRetryDurationMS)
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationMS,
                                                                   ensuredRetryDurationsMSIterable);

        return ensured(deferred, constraint).get();
    }


    /**
     * Obtains a {@link Deferred} representation of the last call to a
     * dynamic proxy created with either {@link #invoking(Object)} or
     * {@link #invoking(Deferred)}.   If there was no call to create a
     * dynamic proxy, the provided deferred is ensured and returned.
     *
     * @param t                        the deferred value (usually returned
     *                                 from 'invoking'
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     *
     * @deprecated use {@link #eventually(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> Deferred<T> eventually(Deferred<T> t,
                                             long        totalRetryDuration,
                                             TimeUnit    totalRetryDurationUnits)
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   ensuredRetryDurationsMSIterable);

        return eventually(t, constraint);
    }


    /**
     * Obtains a {@link Deferred} representation of the last call to a
     * dynamic proxy created with either {@link #invoking(Object)} or
     * {@link #invoking(Deferred)}.
     *
     * @param t                        the value returned from an call to 'invoking'
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     *
     * @deprecated use {@link #eventually(Object, TimeoutConstraint)} instead
     */
    @Deprecated
    public static <T> Deferred<T> eventually(T        t,
                                             long     totalRetryDuration,
                                             TimeUnit totalRetryDurationUnits)
    {
        TimeoutConstraint constraint = new SimpleTimeoutConstraint(0,
                                                                   totalRetryDurationUnits.toMillis(totalRetryDuration),
                                                                   ensuredRetryDurationsMSIterable);

        return eventually(t, constraint);
    }


    // ------------------------------------------------------------------------
    // </deferred-methods> (will be removed in a later release)
    // ------------------------------------------------------------------------

    /**
     * A {@link MethodInterceptor} that records invocations against a
     * {@link ThreadLocal} {@link Deferred}.
     */
    private static class DeferredMethodInteceptor implements MethodInterceptor
    {
        /**
         * {@inheritDoc}
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public Object intercept(Object      self,
                                Method      method,
                                Object[]    args,
                                MethodProxy methodProxy) throws Throwable
        {
            // get the underlying deferred object on which this invocation really occurred
            Deferred<?> deferred = DeferredHelper.m_deferred.get();

            // replace the underlying deferred with a deferred method invocation
            // representing the result of this invocation
            DeferredHelper.m_deferred.set(new DeferredInvoke(deferred, method, args));

            // determine a suitable return value based on the method return type.
            // this value will actually be ignored as this method call is being
            // deferred.
            Class<?> resultType = method.getReturnType();

            if (resultType.equals(Byte.class) || resultType.equals(byte.class))
            {
                return new Byte((byte) 0);
            }
            else if (resultType.equals(Short.class) || resultType.equals(short.class))
            {
                return new Short((short) 0);
            }
            else if (resultType.equals(Integer.class) || resultType.equals(int.class))
            {
                return new Integer(0);
            }
            else if (resultType.equals(Long.class) || resultType.equals(long.class))
            {
                return new Long(0);
            }
            else if (resultType.equals(Float.class) || resultType.equals(float.class))
            {
                return new Float(0.0);
            }
            else if (resultType.equals(Double.class) || resultType.equals(double.class))
            {
                return new Double(0.0);
            }
            else if (resultType.equals(Character.class) || resultType.equals(char.class))
            {
                return new Character(' ');
            }
            else if (resultType.equals(Boolean.class) || resultType.equals(boolean.class))
            {
                return new Boolean(false);
            }
            else if (resultType.equals(AtomicBoolean.class))
            {
                return new AtomicBoolean(false);
            }
            else if (resultType.equals(AtomicInteger.class))
            {
                return new AtomicInteger(0);
            }
            else if (resultType.equals(AtomicLong.class))
            {
                return new AtomicLong(0);
            }
            else if (resultType.isArray())
            {
                return Array.newInstance(resultType.getComponentType(), 0);
            }
            else if (resultType.equals(String.class))
            {
                return "";
            }
            else
            {
                // as the return type is an object type, create a proxy of it
                // so we can continue to capture and defer method calls
                return ReflectionHelper.createProxyOf(resultType, new DeferredMethodInteceptor());
            }
        }
    }


    static
    {
        // ----------------
        // establish the ensured retry durations iterable
        String strategy = System.getProperty(ORACLETOOLS_DEFERRED_RETRY_STRATEGY);

        if (strategy == null)
        {
            strategy = "random.fibonacci";
        }

        strategy = strategy.trim().toLowerCase();

        if (strategy.equals("random.fibonacci"))
        {
            ensuredRetryDurationsMSIterable = new Iterable<Long>()
            {
                @Override
                public Iterator<Long> iterator()
                {
                    return new RandomIterator(new FibonacciIterator());
                }
            };
        }
        else if (strategy.equals("random.exponential"))
        {
            ensuredRetryDurationsMSIterable = new Iterable<Long>()
            {
                @Override
                public Iterator<Long> iterator()
                {
                    return new RandomIterator(new ExponentialIterator(0, 50));
                }
            };
        }
        else if (strategy.equals("fibonacci"))
        {
            ensuredRetryDurationsMSIterable = new Iterable<Long>()
            {
                @Override
                public Iterator<Long> iterator()
                {
                    return new FibonacciIterator();
                }
            };
        }
        else if (strategy.equals("exponential"))
        {
            ensuredRetryDurationsMSIterable = new Iterable<Long>()
            {
                @Override
                public Iterator<Long> iterator()
                {
                    return new ExponentialIterator(0, 50);
                }
            };
        }
        else
        {
            // default to perpetual polling
            ensuredRetryDurationsMSIterable = new Iterable<Long>()
            {
                @Override
                public Iterator<Long> iterator()
                {
                    return new PerpetualIterator<Long>(250L);
                }
            };
        }

    }
}
