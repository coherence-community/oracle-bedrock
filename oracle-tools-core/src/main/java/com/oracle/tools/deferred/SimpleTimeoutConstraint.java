/*
 * File: SimpleTimeoutConstraint.java
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
 * Created by brianoliver on 7/27/14.
 */
public class SimpleTimeoutConstraint implements TimeoutConstraint
{
    /**
     * The initial number of milliseconds to wait before attempting
     * to acquire an object reference.
     */
    private long initialDelayMilliseconds;

    /**
     * The maximum number of milliseconds to wait before giving up on
     * acquiring an object reference.
     */
    private long maximumRetryMilliseconds;

    /**
     * An {@link Iterable} providing an {@link Iterator} of retry durations
     * in milliseconds.
     */
    private Iterable<Long> retryDelayMillisecondsIterable;


    /**
     * Constructs a default {@link SimpleTimeoutConstraint}, making
     * use of any of the standard system properties.
     */
    public SimpleTimeoutConstraint()
    {
        this.initialDelayMilliseconds       = 0;
        this.maximumRetryMilliseconds       = DeferredHelper.getDefaultEnsuredTimeoutMS();
        this.retryDelayMillisecondsIterable = DeferredHelper.getDefaultEnsuredRetryDurationsMSIterable();
    }


    /**
     * Constructs a {@link SimpleTimeoutConstraint}.
     *
     * @param initialDelayMilliseconds        the initial number of milliseconds to wait
     *                                        before attempting to acquire an object reference
     * @param maximumRetryMilliseconds        the maximum number of milliseconds to wait
     *                                        for an object reference to become available
     * @param retryDelayMillisecondsIterable  an {@link Iterable} of retry milliseconds
     */
    public SimpleTimeoutConstraint(long           initialDelayMilliseconds,
                                   long           maximumRetryMilliseconds,
                                   Iterable<Long> retryDelayMillisecondsIterable)
    {
        this.initialDelayMilliseconds       = initialDelayMilliseconds;
        this.maximumRetryMilliseconds       = maximumRetryMilliseconds;
        this.retryDelayMillisecondsIterable = retryDelayMillisecondsIterable;
    }


    @Override
    public long getInitialDelayMilliseconds()
    {
        return initialDelayMilliseconds;
    }


    @Override
    public long getMaximumRetryMilliseconds()
    {
        return maximumRetryMilliseconds;
    }


    @Override
    public Iterable<Long> getRetryDelayMillisecondsIterable()
    {
        return retryDelayMillisecondsIterable;
    }


    /**
     * Sets the maximum retry duration.
     *
     * @param duration  the maximum duration
     * @param units     the maximum duration units
     *
     * @return  this {@link TimeoutConstraint}
     */
    public SimpleTimeoutConstraint within(long     duration,
                                          TimeUnit units)
    {
        this.maximumRetryMilliseconds = units.toMillis(duration);

        return this;
    }


    /**
     * Sets the initial delay duration.
     *
     * @param duration  the initial delay duration
     * @param units     the initial delay duration units
     *
     * @return  a {@link TimeoutConstraint}
     */
    public SimpleTimeoutConstraint delayedBy(long     duration,
                                             TimeUnit units)
    {
        this.initialDelayMilliseconds = units.toMillis(duration);

        return this;
    }
}
