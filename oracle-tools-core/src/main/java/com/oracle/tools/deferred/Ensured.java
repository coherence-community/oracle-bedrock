/*
 * File: Ensured.java
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

import java.util.concurrent.TimeUnit;

/**
 * An {@link Ensured} is a specialized {@link Deferred} implementation that
 * does it's best to guarantee a <strong>non-<code>null</code></strong>
 * reference will be returned when a call to {@link Ensured#get()} is made.
 * {@link Ensured} will suitably wait and retry until the object becomes
 * available, for some maximum amount of time.
 * <p>
 * ie: An {@link Ensured} is a {@link Deferred} adapter.
 * <p>
 * If a non-<code>null</code> reference can not be acquired with in a specified
 * period of time, or the adapted {@link Deferred} throws an
 * {@link ObjectNotAvailableException}, a {@link ObjectNotAvailableException}
 * is immediately (re-)thrown.
 * <p>
 * The default behavior of {@link #get()} is to attempt to acquire the
 * underlying resource from the specified {@link Deferred},
 * waiting for at most the {@link #DEFAULT_TOTAL_RETRY_DURATION_SECS} seconds
 * (not including the time the adapted {@link Deferred} takes to perform
 * {@link #get()}).  If the adapted {@link Deferred} fails to provide the
 * required object, the {@link Ensured} will wait a default "poll" period of
 * {@link #DEFAULT_RETRY_DURATION_MS}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Ensured<T> implements Deferred<T>
{
    /**
     * The total number of seconds to retry acquiring the object
     * from the specified {@link Deferred} before giving up.
     */
    public static final long DEFAULT_TOTAL_RETRY_DURATION_SECS = 30;

    /**
     * The number of milliseconds to wait between attempting to acquire
     * the object from the specified {@link Deferred}.
     */
    public static final long DEFAULT_RETRY_DURATION_MS = 250;

    /**
     * The {@link Deferred} being adapted.
     */
    private Deferred<T> m_deferred;

    /**
     * The (poll wait) duration between attempts to acquire the {@link Deferred}.
     */
    private long m_retryDelayDuration;

    /**
     * The unit of time for the {@link #m_retryDelayDuration}.
     */
    private TimeUnit m_retryDelayDurationUnits;

    /**
     * The total duration allowed to invest in acquiring the {@link Deferred}.
     */
    private long m_totalDuration;

    /**
     * The unit of time for the {@link #m_totalDuration}.
     */
    private TimeUnit m_totalDurationUnits;


    /**
     * Construct an {@link Ensured} adapting the specified {@link Deferred}.
     *
     * @param deferred  the {@link Deferred} to adapt.
     */
    public Ensured(Deferred<T> deferred)
    {
        this(deferred,
             DEFAULT_RETRY_DURATION_MS,
             TimeUnit.MILLISECONDS,
             DEFAULT_TOTAL_RETRY_DURATION_SECS,
             TimeUnit.SECONDS);
    }


    /**
     * Construct an {@link Ensured} adapting the specified {@link Deferred}.
     *
     * @param deferred            the {@link Deferred} to adapt
     * @param totalDuration       the maximum duration for retrying
     * @param totalDurationUnits  the {@link TimeUnit}s for the duration
     */
    public Ensured(Deferred<T> deferred,
                   long        totalDuration,
                   TimeUnit    totalDurationUnits)
    {
        this(deferred, DEFAULT_RETRY_DURATION_MS, TimeUnit.MILLISECONDS, totalDuration, totalDurationUnits);
    }


    /**
     * Construct an {@link Ensured} adapting the specified {@link Deferred}.
     *
     * @param deferred                 the {@link Deferred} to adapt
     * @param retryDelayDuration       the delay between attempting to retry
     *                                 acquiring the object
     * @param retryDelayDurationUnits  the {@link TimeUnit}s for the retry duration
     * @param totalDuration            the maximum duration for retrying
     * @param totalDurationUnits       the {@link TimeUnit}s for the total duration
     */
    public Ensured(Deferred<T> deferred,
                   long        retryDelayDuration,
                   TimeUnit    retryDelayDurationUnits,
                   long        totalDuration,
                   TimeUnit    totalDurationUnits)
    {
        // when we're ensuring an ensured, use the adapted deferred
        // (this is to ensure that we don't attempt to over ensure a deferred)
        m_deferred                = deferred instanceof Ensured ? ((Ensured<T>) deferred).getDeferred() : deferred;

        m_retryDelayDuration      = retryDelayDuration < 0 ? 0 : retryDelayDuration;
        m_retryDelayDurationUnits = retryDelayDurationUnits;

        m_totalDuration           = totalDuration;
        m_totalDurationUnits      = totalDurationUnits;
    }


    /**
     * Obtains the adapted {@link Deferred}.
     *
     * @return  the adapted {@link Deferred}
     */
    public Deferred<T> getDeferred()
    {
        return m_deferred;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws ObjectNotAvailableException
    {
        // determine how much longer we can perform retries
        long remainingRetryDurationMS = m_totalDurationUnits.toMillis(m_totalDuration);

        do
        {
            try
            {
                T object = m_deferred.get();

                if (object != null)
                {
                    return object;
                }
            }
            catch (ObjectNotAvailableException e)
            {
                // give up immediately and throw an ObjectNotAvailableException
                throw e;
            }
            catch (UnsupportedOperationException e)
            {
                // give up immediately when an operation is not supported
                throw new ObjectNotAvailableException(this, e);
            }
            catch (RuntimeException e)
            {
                // SKIP: we assume all other exceptions means
                // that we can retry
            }

            // as no object was produced we should wait before retrying
            if (m_totalDuration < 0 || remainingRetryDurationMS > 0)
            {
                try
                {
                    m_retryDelayDurationUnits.sleep(m_retryDelayDuration);

                    remainingRetryDurationMS -= m_retryDelayDurationUnits.toMillis(m_retryDelayDuration);
                }
                catch (InterruptedException e)
                {
                    throw new ObjectNotAvailableException(m_deferred, e);
                }
            }

        }
        while (m_totalDuration < 0 || remainingRetryDurationMS > 0);

        throw new ObjectNotAvailableException(m_deferred);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getDeferredClass()
    {
        return m_deferred.getDeferredClass();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("Ensured{%s}", getDeferredClass(), getDeferred());
    }
}
