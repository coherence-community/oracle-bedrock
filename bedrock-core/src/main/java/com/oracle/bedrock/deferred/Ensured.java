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

package com.oracle.bedrock.deferred;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.deferred.options.InitialDelay;
import com.oracle.bedrock.deferred.options.MaximumRetryDelay;
import com.oracle.bedrock.deferred.options.RetryFrequency;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.util.Duration;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * A specialized {@link Deferred} implementation that attempts to guarantee a
 * an object reference will be returned when a call to {@link Ensured#get()} is
 * made.  ie: "ensuring that an object is available".
 * <p>
 * An {@link Ensured} will repetitively attempt to acquire a object reference,
 * including <code>null</code> from an underlying {@link Deferred},
 * giving up only after the timeout conditions are met, an unexpected exception
 * or {@link PermanentlyUnavailableException} occurs.
 * <p>
 * The timeout constraints are defined through the use of the following {@link Option}s,
 * Maximum Retry {@link Timeout}, Initially {@link InitialDelay} By, {@link MaximumRetryDelay}
 * and the {@link RetryFrequency}.  When none are specified, default are auto-detected
 * from the current environment and configuration.
 * <p>
 * If an object reference or <code>null</code> can not be acquired with in the
 * specified constraints, a {@link PermanentlyUnavailableException} will be thrown.
 * <p>
 * If the underlying {@link Deferred} throws an {@link PermanentlyUnavailableException},
 * while attempting to acquire the object reference, the said exception will be
 * immediately rethrown.
 * <p>
 * The default behavior of {@link #get()} is to attempt to acquire the
 * underlying resource from the specified {@link Deferred}, retrying a number
 * of times, waiting for at most the maximum {@link Timeout} duration.   The delay
 * between subsequent failures and corresponding retries is specified by an
 * {@link Iterator}, defined by the {@link RetryFrequency}.   An initial delay
 * defined by the {@link InitialDelay} {@link Option} will be applied if specified, and
 * subsequent delays between retries will not be any larger than the specified
 * {@link MaximumRetryDelay}.
 * </p>
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Timeout
 * @see InitialDelay
 * @see MaximumRetryDelay
 * @see RetryFrequency
 */
public class Ensured<T> implements Deferred<T>
{
    /**
     * The {@link Deferred} being adapted.
     */
    private Deferred<T> deferred;

    /**
     * The initial delay before starting to ensure the {@link Deferred}.
     */
    private long initialDelayDurationMS;

    /**
     * The maximum polling time between attempts to acquire the {@link Deferred}.
     */
    private long maximumPollingDurationMS;

    /**
     * The total maximum retry duration before giving up on the {@link Deferred}.
     */
    private long maximumRetryDurationMS;

    /**
     * An {@link Iterator} provide retry/waiting delays {@link Duration}s to be used
     * between attempts to acquire the {@link Deferred}.
     */
    private Iterator<Duration> retryDurations;


    /**
     * Constructs an {@link Ensured}.
     *
     * @param deferred  the {@link Deferred} to ensure
     * @param options   the {@link Option} for the {@link Ensured}
     */
    public Ensured(Deferred<T> deferred,
                   Option...   options)
    {
        // when we're ensuring an ensured, use the adapted deferred
        // (this is to ensure that we don't attempt to ensure another ensured)
        this.deferred = deferred instanceof Ensured ? ((Ensured<T>) deferred).getDeferred() : deferred;

        // determine the timeout constraints based on the provided options
        Options optionsByType = Options.from(options);

        this.initialDelayDurationMS = optionsByType.getOrDefault(InitialDelay.class,
                                                                 InitialDelay.none()).to(TimeUnit.MILLISECONDS);

        this.maximumRetryDurationMS = optionsByType.getOrDefault(Timeout.class,
                                                                 Timeout.after(DeferredHelper.getDefaultEnsuredMaximumRetryDuration()))
                                                                 .to(TimeUnit.MILLISECONDS);

        this.maximumPollingDurationMS = optionsByType.getOrDefault(MaximumRetryDelay.class,
                                                                   MaximumRetryDelay.of(DeferredHelper.getDefaultEnsuredMaximumPollingDuration()))
                                                                   .to(TimeUnit.MILLISECONDS);

        this.retryDurations = optionsByType.getOrDefault(RetryFrequency.class,
                                                         RetryFrequency.of(DeferredHelper.getDefaultEnsuredRetryDurationsIterable()))
                                                         .get().iterator();
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

                return object;
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
                        throw new PermanentlyUnavailableException(deferred, e);
                    }
                }
                else
                {
                    // if we run out of retry times, we give up immediately
                    throw new PermanentlyUnavailableException(deferred);
                }
            }
        }
        while (maximumRetryDurationMS < 0 || remainingRetryDurationMS > 0);

        // we give up if we've timed-out
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
