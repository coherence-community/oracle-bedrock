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

import java.util.Iterator;

import java.util.concurrent.TimeUnit;

/**
 * A specialized {@link Deferred} implementation that attempts to guarantee a
 * non-<code>null</code> object reference will be returned when a call to
 * {@link Ensured#get()} is made.  ie: "ensuring that an object is available".
 * <p>
 * An {@link Ensured} will repetitively attempt to acquire a
 * non-<code>null</code> object reference from an associated {@link Deferred},
 * giving only after the conditions defined by a {@link TimeoutConstraint} is
 * met or an unexpected exception occurs.
 * <p>
 * If a non-<code>null</code> object reference can not be acquired with in the
 * specified constraints, an {@link UnresolvableInstanceException} will be thrown.
 * <p>
 * If the underlying {@link Deferred} throws an {@link UnresolvableInstanceException},
 * while attempting to acquire the object reference, the said exception will be
 * immediately rethrown.
 * <p>
 * The default behavior of {@link #get()} is to attempt to acquire the
 * underlying resource from the specified {@link Deferred}, retrying a number
 * of times, waiting for at most the configured duration.   The delay
 * between subsequent failures and corresponding retries is specified by an
 * {@link Iterator}, defined by the {@link TimeoutConstraint}.
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
    private Deferred<T>    deferred;
    private long           initialDelayDurationMS;
    private long           maximumRetryDurationMS;
    private Iterator<Long> retryDurationsMSIterator;


    /**
     * Constructs an {@link Ensured} using default {@link TimeoutConstraint}.
     *
     * @param deferred           the {@link Deferred} to ensure
     */
    public Ensured(Deferred<T> deferred)
    {
        this(deferred, null);
    }


    /**
     * Constructs an {@link Ensured}.
     *
     * @param deferred    the {@link Deferred} to ensure
     * @param constraint  the {@link TimeoutConstraint} for the {@link Ensured}
     *                    (<code>null</code> means use the default)
     */
    public Ensured(Deferred<T>       deferred,
                   TimeoutConstraint constraint)
    {
        // when we're ensuring an ensured, use the adapted deferred
        // (this is to ensure that we don't attempt to ensure another ensured)
        this.deferred = deferred instanceof Ensured ? ((Ensured<T>) deferred).getDeferred() : deferred;

        if (constraint == null)
        {
            this.initialDelayDurationMS   = 0;
            this.maximumRetryDurationMS   = DeferredHelper.getDefaultEnsuredTimeoutMS();
            this.retryDurationsMSIterator = DeferredHelper.getDefaultEnsuredRetryDurationsMSIterable().iterator();
        }
        else
        {
            this.initialDelayDurationMS   = constraint.getInitialDelayMilliseconds();
            this.maximumRetryDurationMS   = constraint.getMaximumRetryMilliseconds();
            this.retryDurationsMSIterator = constraint.getRetryDelayMillisecondsIterable().iterator();
        }
    }


    /**
     * Construct an {@link Ensured} adapting the specified {@link Deferred}.
     *
     * @param deferred                  the {@link Deferred} to ensure
     * @param retryDurationsMSIterator  an {@link Iterator} providing individual retry
     *                                  durations (in milliseconds) for each time the
     *                                  {@link Ensured} needs to wait
     * @param maximumRetryDurationMS    the maximum duration (in milliseconds) to wait
     *                                  for the {@link Deferred} to become available
     *
     * @deprecated  Use {@link #Ensured(Deferred, TimeoutConstraint)} instead
     */
    @Deprecated
    public Ensured(Deferred<T>    deferred,
                   Iterator<Long> retryDurationsMSIterator,
                   long           maximumRetryDurationMS)
    {
        // when we're ensuring an ensured, use the adapted deferred
        // (this is to ensure that we don't attempt to ensure another ensured)
        this.deferred                 = deferred instanceof Ensured ? ((Ensured<T>) deferred).getDeferred() : deferred;

        this.initialDelayDurationMS   = 0;
        this.maximumRetryDurationMS   = maximumRetryDurationMS;
        this.retryDurationsMSIterator = retryDurationsMSIterator;
    }


    /**
     * Obtains the adapted {@link Deferred}.
     *
     * @return  the adapted {@link Deferred}
     */
    public Deferred<T> getDeferred()
    {
        return deferred;
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
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
                    throw new PermanentlyUnavailableException(deferred, e);
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

                T    object  = deferred.get();

                long stopped = System.currentTimeMillis();

                // the time spent trying to access the resource
                // is considered as part of the remaining time
                acquisitionDurationMS    = stopped - started;
                remainingRetryDurationMS -= acquisitionDurationMS < 0 ? 0 : acquisitionDurationMS;

                if (object == null)
                {
                    throw new TemporarilyUnavailableException(this);
                }
                else
                {
                    return object;
                }
            }
            catch (PermanentlyUnavailableException e)
            {
                // give up immediately!
                throw e;
            }
            catch (UnsupportedOperationException e)
            {
                // give up immediately when an operation is not supported
                throw new PermanentlyUnavailableException(this, e);
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

            // as no object was produced we should wait before retrying
            if (maximumRetryDurationMS < 0 || remainingRetryDurationMS > 0)
            {
                // we can only retry while we have retry durations
                if (retryDurationsMSIterator.hasNext())
                {
                    try
                    {
                        long durationMS = retryDurationsMSIterator.next();

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
                        throw new PermanentlyUnavailableException(deferred, e);
                    }
                }
                else
                {
                    throw new PermanentlyUnavailableException(deferred);
                }
            }
        }
        while (maximumRetryDurationMS < 0 || remainingRetryDurationMS > 0);

        throw new PermanentlyUnavailableException(deferred);
    }


    @Override
    public Class<T> getDeferredClass()
    {
        return deferred.getDeferredClass();
    }


    @Override
    public String toString()
    {
        return String.format("Ensured{%s}", getDeferredClass(), getDeferred());
    }
}
