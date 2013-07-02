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

import com.oracle.tools.util.ConstantIterator;

import java.util.Iterator;

import java.util.concurrent.TimeUnit;

/**
 * An {@link Ensured} is a specialized {@link Deferred} implementation that
 * does it's best to guarantee a non-<code>null</code> object will be
 * returned when a call to {@link Ensured#get()} is made.
 * <p>
 * {@link Ensured} will suitably wait and retry until the object becomes
 * available, for some maximum amount of time.
 * <p>
 * ie: An {@link Ensured} is a {@link Deferred} adapter.
 * <p>
 * If a non-<code>null</code> reference can not be acquired with in a specified
 * period of time, or the adapted {@link Deferred} throws an
 * {@link UnresolvableInstanceException}, the {@link UnresolvableInstanceException}
 * is immediately (re-)thrown.
 * <p>
 * The default behavior of {@link #get()} is to attempt to acquire the
 * underlying resource from the specified {@link Deferred}, retrying a number
 * of times, waiting for at most the configured duration.   The delay
 * between each successful failure and retry is specified by an
 * {@link Iterator}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Ensured<T> implements Deferred<T>
{
    /**
     * The {@link Deferred} being adapted.
     */
    private Deferred<T> m_deferred;

    /**
     * The total duration (in milliseconds) allowed possibly wait when
     * attempting to acquire the {@link Deferred}.
     */
    private long m_totalDurationMS;

    /**
     * An {@link Iterator} that provides the next retry duration
     * to use (in milliseconds).  Each of these are the
     * represent the duration to wait between attempts to acquire
     * the {@link Deferred}.
     */
    private Iterator<Long> m_retryDurationsMS;


    /**
     * Construct an {@link Ensured} adapting the specified {@link Deferred}.
     *
     * @param deferred          the {@link Deferred} to adapt
     * @param retryDurationsMS  an {@link Iterator} providing individual retry
     *                          durations (in milliseconds) for each time the
     *                          {@link Ensured} needs to wait
     * @param totalDurationMS   the maximum duration (in milliseconds) to wait
     *                          for the {@link Deferred} to become available
     */
    public Ensured(Deferred<T>    deferred,
                   Iterator<Long> retryDurationsMS,
                   long           totalDurationMS)
    {
        // when we're ensuring an ensured, use the adapted deferred
        // (this is to ensure that we don't attempt to ensure another ensured)
        m_deferred         = deferred instanceof Ensured ? ((Ensured<T>) deferred).getDeferred() : deferred;

        m_retryDurationsMS = retryDurationsMS;
        m_totalDurationMS  = totalDurationMS < 0 ? 0 : totalDurationMS;
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
    public T get() throws UnresolvableInstanceException, InstanceUnavailableException
    {
        // determine the maximum time we can wait
        long remainingRetryDurationMS = m_totalDurationMS;

        do
        {
            // the time the most recent acquisition took
            long acquisitionDurationMS = 0;

            try
            {
                long started = System.currentTimeMillis();

                T    object  = m_deferred.get();

                long stopped = System.currentTimeMillis();

                // the time spent trying to access the resource
                // is considered as part of the remaining time
                acquisitionDurationMS    = stopped - started;
                remainingRetryDurationMS -= acquisitionDurationMS < 0 ? 0 : acquisitionDurationMS;

                if (object == null)
                {
                    throw new InstanceUnavailableException(this);
                }
                else
                {
                    return object;
                }
            }
            catch (UnresolvableInstanceException e)
            {
                // give up immediately and rethrow an UnresolvableInstanceException
                throw e;
            }
            catch (UnsupportedOperationException e)
            {
                // give up immediately when an operation is not supported
                throw new UnresolvableInstanceException(this, e);
            }
            catch (InstanceUnavailableException e)
            {
                // SKIP: we will retry if the instance was unavailable
            }
            catch (RuntimeException e)
            {
                // SKIP: we assume all other runtime exceptions
                // simply means that we should retry
            }

            // as no object was produced we should wait before retrying
            if (m_totalDurationMS < 0 || remainingRetryDurationMS > 0)
            {
                // we can only retry while we have retry durations
                if (m_retryDurationsMS.hasNext())
                {
                    try
                    {
                        long durationMS = m_retryDurationsMS.next();

                        if (remainingRetryDurationMS - durationMS < 0)
                        {
                            durationMS = remainingRetryDurationMS;
                        }

                        if (durationMS > 0)
                        {
                            TimeUnit.MILLISECONDS.sleep(durationMS);
                        }

                        remainingRetryDurationMS -= durationMS;
                    }
                    catch (InterruptedException e)
                    {
                        throw new UnresolvableInstanceException(m_deferred, e);
                    }
                }
                else
                {
                    throw new UnresolvableInstanceException(m_deferred);
                }
            }
        }
        while (m_totalDurationMS < 0 || remainingRetryDurationMS > 0);

        throw new UnresolvableInstanceException(m_deferred);
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
