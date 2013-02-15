/*
 * File: Notified.java
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link Notified} is a {@link Deferred} that represents a synchronized
 * object that should be {@link #notify()}ed, including with {@link #notifyAll()},
 *  before it can be used.
 * <p>
 * Important:  This implementation assumes that spurious Operating System
 * "wake ups" on a synchronized object during a {@link #wait()} call are
 * equivalent to a {@link #notify()} or {@link #notifyAll()} call on the said
 * object.  That is, this implementation does not distinguish between an
 * Operating System spurious wake up of an object or an call to
 * {@link #notify()} or {@link #notifyAll()} on an object.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Notified<T> implements Deferred<T>
{
    /**
     * The total number of seconds to wait to be notified before giving up.
     */
    public static final long DEFAULT_TOTAL_RETRY_DURATION_SECS = 30;

    /**
     * The object that must be {@link #notify()}ed before it can be returned.
     */
    private T m_object;

    /**
     * The total duration allowed to invest in acquiring the {@link Deferred}.
     */
    private long m_totalDuration;

    /**
     * The unit of time for the {@link #m_totalDuration}.
     */
    private TimeUnit m_totalDurationUnits;

    /**
     * A flag to indicate if we are currently waiting to be notified.
     */
    private AtomicBoolean m_isWaiting;

    /**
     * The result of waiting.  <code>null</code> means the object was not notified.s
     */
    private volatile T m_result;


    /**
     * Construct an {@link Ensured} adapting the specified {@link Deferred}.
     *
     * @param object  the {@link Object} that must be notified before it is returned
     */
    public Notified(T object)
    {
        this(object, DEFAULT_TOTAL_RETRY_DURATION_SECS, TimeUnit.SECONDS);
    }


    /**
     * Constructs a {@link Notified}.
     *
     * @param object              the {@link Object} that must be notified before
     *                            it is returned
     * @param totalDuration       the maximum duration to wait to be notified
     * @param totalDurationUnits  the {@link TimeUnit}s for the duration
     */
    public Notified(T        object,
                    long     totalDuration,
                    TimeUnit totalDurationUnits)
    {
        m_object             = object;
        m_totalDuration      = totalDuration;
        m_totalDurationUnits = totalDurationUnits;
        m_isWaiting          = new AtomicBoolean(true);
        m_result             = null;

        // start a thread immediately to wait for the object to be notified
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (m_object)
                {
                    try
                    {
                        // get an estimate of the current time
                        long startTime = System.nanoTime();

                        // wait to be notified
                        m_totalDurationUnits.timedWait(m_object, m_totalDuration);

                        // get the end time
                        long endTime = System.nanoTime();

                        // did we wait for "around" the totalDuration
                        // note: this is just a best guess/estimate.
                        long durationWaited = endTime - startTime;

                        // note: we may have been woken up due to a spurious
                        // operating system request.  we assume this is never the
                        // case
                        if (durationWaited <= m_totalDurationUnits.toNanos(m_totalDuration))
                        {
                            m_result = m_object;
                        }
                        else
                        {
                            // we waited too long, so there is no result.
                            m_result = null;
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // if we are interrupted, there is no result
                        m_result = null;
                    }
                    finally
                    {
                        // we're no longer waiting
                        m_isWaiting.set(false);
                    }
                }

            }
        });

        thread.setDaemon(true);
        thread.start();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws ObjectNotAvailableException
    {
        if (m_isWaiting.get())
        {
            return null;
        }
        else
        {
            if (m_result == null)
            {
                throw new ObjectNotAvailableException(this);
            }
            else
            {
                return m_result;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getDeferredClass()
    {
        return (Class<T>) m_object.getClass();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("Notified{%s}", m_object);
    }
}
