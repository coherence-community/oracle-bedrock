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
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

import com.oracle.tools.util.ReflectionHelper;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

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
     * A {@link ThreadLocal} to capture the most recent {@link Deferred}
     * method call on a proxy created by {@link #invoking(Deferred)}.
     * <p>
     * See {@link #invoking(Deferred)} and {@link #eventually(Object)}
     * for more information.
     */
    private static final ThreadLocal<Deferred<?>> m_deferred = new ThreadLocal<Deferred<?>>();


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
     * Obtains an ensured of the specified {@link Deferred}
     * (configured using default {@link Ensured} timeouts)
     *
     * @param deferred  the {@link Deferred} to ensure
     *
     * @return an {@link Ensured} of the {@link Deferred}
     */
    public static <T> Deferred<T> ensured(Deferred<T> deferred)
    {
        return deferred instanceof Notified || deferred instanceof Ensured ? deferred : new Ensured<T>(deferred);
    }


    /**
     * Obtains an ensured of the specified {@link Deferred}.
     *
     * @param deferred                 the {@link Deferred} to ensure
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return an {@link Ensured} of the {@link Deferred}
     */
    public static <T> Deferred<T> ensured(Deferred<T> deferred,
                                          long totalRetryDuration,
                                          TimeUnit totalRetryDurationUnits)
    {
        return deferred instanceof Notified || deferred instanceof Ensured ? deferred : new Ensured<T>(deferred,
                                                                                                       totalRetryDuration,
                                                                                                       totalRetryDurationUnits);
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
     */
    public static <T> Deferred<T> ensured(Deferred<T> deferred,
                                          long retryDelayDuration,
                                          TimeUnit retryDelayDurationUnits,
                                          long totalRetryDuration,
                                          TimeUnit totalRetryDurationUnits)
    {
        return deferred instanceof Notified || deferred instanceof Ensured ? deferred : new Ensured<T>(deferred,
                                                                                                       retryDelayDuration,
                                                                                                       retryDelayDurationUnits,
                                                                                                       totalRetryDuration,
                                                                                                       totalRetryDurationUnits);
    }


    /**
     * Obtains an {@link Ensured} of the specified {@link Deferred}.
     *
     * @param deferred              the {@link Deferred} to ensure
     * @param totalRetryDurationMS  the maximum duration (in milliseconds) to retry
     *
     * @return an {@link Ensured} of the {@link Deferred}
     */
    public static <T> Deferred<T> ensured(Deferred<T> deferred,
                                          long totalRetryDurationMS)
    {
        return deferred instanceof Ensured ? (Ensured<T>) deferred : new Ensured<T>(deferred,
                                                                                    totalRetryDurationMS,
                                                                                    TimeUnit.MILLISECONDS);
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
     * {@link #ensure(Deferred, long, TimeUnit)}.
     *
     * @param deferred                 the {@link Deferred} to ensure
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return the value of the {@link Deferred}
     */
    public static <T> T ensure(Deferred<T> deferred,
                               long totalRetryDuration,
                               TimeUnit totalRetryDurationUnits)
    {
        return ensured(deferred, totalRetryDuration, totalRetryDurationUnits).get();
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
     */
    public static <T> T ensure(Deferred<T> deferred,
                               long retryDelayDuration,
                               TimeUnit retryDelayDurationUnits,
                               long totalRetryDuration,
                               TimeUnit totalRetryDurationUnits)
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
     */
    public static <T> T ensure(Deferred<T> deferred,
                               long totalRetryDurationMS)
    {
        return ensured(deferred, totalRetryDurationMS).get();
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
     * Obtains a {@link Notified} for the specified object.
     *
     * @param object  the object that must be notified before it is available
     *
     * @return a {@link Notified}
     */
    public static <T> Deferred<T> notified(T object)
    {
        return new Notified<T>(object);
    }


    /**
     * Obtains a {@link Notified} for the specified object.
     *
     * @param object                   the object that must be notified before
     *                                 it is available
     * @param totalRetryDuration       the maximum duration to wait to notification
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return a {@link Notified}
     */
    public static <T> Deferred<T> notified(T object,
                                           long totalRetryDuration,
                                           TimeUnit totalRetryDurationUnits)
    {
        return new Notified<T>(object, totalRetryDuration, totalRetryDurationUnits);
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
    public static <T> Deferred<T> future(Class<T> clzOfResult,
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
            // set the current deferred as a thread local so that
            // we can "eventually" evaluate and return it.
            m_deferred.set(deferred);

            // FUTURE: we should raise a soft exception here if the deferred
            // class is final or perhaps native as we can't proxy them.

            // create a proxy of the specified object class that will record
            // methods calls on the object and represent them as a deferred on a thread local
            return ReflectionHelper.createProxyOf(deferred.getDeferredClass(), new DeferredMethodInteceptor());
        }
        else
        {
            throw new UnsupportedOperationException("An attempt was made to call 'invoking' after a previous call was made outside an 'eventually'."
                                                    + "Alternatively two or more calls to 'invoking' have been made sequentially."
                                                    + "Calls to 'invoking' must be made inside an 'eventually' call.");
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
     * @param t                        the value returned from an call to 'invoking'
     * @param totalRetryDuration       the maximum duration for retrying
     * @param totalRetryDurationUnits  the {@link TimeUnit}s for the duration
     *
     * @return a {@link Deferred} representation of a previous call
     * {@link #invoking(Object)}
     */
    @SuppressWarnings("unchecked")
    public static <T> Deferred<T> eventually(T t,
                                             long totalRetryDuration,
                                             TimeUnit totalRetryDurationUnits)
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

        return ensured(deferred, totalRetryDuration, totalRetryDurationUnits);
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
     */
    @SuppressWarnings("unchecked")
    public static <T> Deferred<T> eventually(Deferred<T> t,
                                             long totalRetryDuration,
                                             TimeUnit totalRetryDurationUnits)
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

        return ensured(deferred, totalRetryDuration, totalRetryDurationUnits);
    }


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
        public Object intercept(Object self,
                                Method method,
                                Object[] args,
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
                return Array.newInstance(resultType, 0);
            }
            else
            {
                // as the return type is an object type, create a proxy of it
                // so we can continue to capture and defer method calls
                return ReflectionHelper.createProxyOf(resultType, new DeferredMethodInteceptor());
            }
        }
    }
}
