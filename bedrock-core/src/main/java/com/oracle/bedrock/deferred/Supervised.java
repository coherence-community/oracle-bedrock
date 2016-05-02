/*
 * File: Supervised.java
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

import java.util.concurrent.TimeUnit;

/**
 * A {@link Supervised} is a {@link Deferred} adapter that has
 * knowledge of the last known availability of an object, the purpose of which
 * is to ensure that the underlying object is protected from repeated and
 * perhaps continuously unrelenting requests by an application to access it,
 * when it simply may be unavailable for a long period of time.
 * <p>
 * A program will use a {@link Supervised} object to avoid the situation when
 * multiple {@link Thread}s attempt to call {@link #get()} or a single
 * {@link Thread} calls {@link #get()} in a tight loop.  It protects the adapted
 * {@link Deferred#get()} method from being called repeatedly, allowing the underlying
 * resource a chance to "recover" when required.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Supervised<T> implements Deferred<T>
{
    /**
     * The {@link Deferred} being adapted and protected by the
     * {@link Supervised}.
     */
    private Deferred<T> deferred;

    /**
     * The instant in time (since the epoc) when the last attempt to acquire
     * the object failed.
     */
    private volatile long instantOfLastFailure;

    /**
     * The duration that must pass before attempting to acquire a object
     * (since the last failed attempt)
     */
    private long retryDelayDuration;

    /**
     * The {@link TimeUnit} for the {@link #retryDelayDuration}.
     */
    private TimeUnit retryDelayTimeUnit;


    /**
     * Constructor for a {@link Supervised}.
     *
     * @param deferred  the {@link Deferred} to supervise
     */
    public Supervised(Deferred<T> deferred)
    {
        this.deferred             = deferred;
        this.instantOfLastFailure = -1;
        this.retryDelayDuration   = 250L;
        this.retryDelayTimeUnit   = TimeUnit.MILLISECONDS;
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


    /**
     * Determines a request to {@link #get()} the underlying object will fail
     * immediately.
     *
     * @return <code>true</code> the object is accessible, <code>false</code> if
     *         a {@link #get()} request will fail immediately
     */
    public boolean isAccessible()
    {
        return instantOfLastFailure < 0
               || System.currentTimeMillis() > instantOfLastFailure + retryDelayTimeUnit.toMillis(retryDelayDuration);
    }


    /**
     * Applications should call this method when they determine, through some
     * external means, that the underlying object they have acquired from the
     * {@link Supervised} is no longer available.
     * <p>
     * This ensures that a {@link Supervised} can protect against
     * repeated requests for the object from parts of an application.
     */
    public void resourceNoLongerAvailable()
    {
        instantOfLastFailure = System.currentTimeMillis();
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        if (isAccessible())
        {
            // we synchronize here to protect the underlying object,
            // that may not be able to cope with a lot of concurrent access
            synchronized (this)
            {
                // the current thread may have be held up for a while waiting,
                // so we check again if the object is still accessible
                if (isAccessible())
                {
                    try
                    {
                        // attempt to get the object
                        return deferred.get();
                    }
                    catch (PermanentlyUnavailableException e)
                    {
                        // when a object is unavailable, we must re-throw it
                        resourceNoLongerAvailable();

                        throw e;
                    }
                    catch (RuntimeException e)
                    {
                        // for runtime exceptions we re-throw them
                        resourceNoLongerAvailable();

                        throw e;
                    }
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            return null;
        }
    }


    @Override
    public Class<T> getDeferredClass()
    {
        return deferred.getDeferredClass();
    }


    @Override
    public String toString()
    {
        return String.format("Supervised<%s>{%s}", getDeferredClass(), getDeferred());
    }
}
