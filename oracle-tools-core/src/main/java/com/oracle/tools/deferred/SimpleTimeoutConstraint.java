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

import com.oracle.tools.util.Duration;

import java.util.Iterator;

import java.util.concurrent.TimeUnit;

/**
 * A simple implementation of a {@link TimeoutConstraint}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleTimeoutConstraint implements TimeoutConstraint
{
    /**
     * The initial {@link Duration} to wait before attempting
     * to acquire an object reference.
     */
    private Duration initialDelay;

    /**
     * The maximum {@link Duration} to wait between attempts
     * to acquire an object reference.
     */
    private Duration maximumPollingDelay;

    /**
     * The maximum {@link Duration} to wait before giving up on
     * acquiring an object reference.
     */
    private Duration maximumRetryDuration;

    /**
     * An {@link Iterable} providing an {@link Iterator} of retry {@link Duration}s.
     */
    private Iterable<Duration> retryDurations;


    /**
     * Constructs a default {@link SimpleTimeoutConstraint}, making
     * use of any of the standard system properties.
     */
    public SimpleTimeoutConstraint()
    {
        this.initialDelay         = Duration.of(0, TimeUnit.MILLISECONDS);
        this.maximumPollingDelay  = DeferredHelper.getDefaultEnsuredMaximumPollingDuration();
        this.maximumRetryDuration = DeferredHelper.getDefaultEnsuredMaximumRetryDuration();
        this.retryDurations       = DeferredHelper.getDefaultEnsuredRetryDurationsIterable();
    }


    /**
     * Constructs a {@link SimpleTimeoutConstraint}.
     *
     * @param initialDelay          the initial {@link Duration} to wait
     *                              before attempting to acquire an object reference
     * @param maximumPollingDelay   the maximum {@link Duration} to wait
     *                              between attempts to acquire an object reference
     * @param maximumRetryDuration  the maximum {@link Duration} to wait
     *                              for an object reference to become available
     * @param retryDurations        an {@link Iterable} of retry {@link Duration}s
     */
    public SimpleTimeoutConstraint(Duration           initialDelay,
                                   Duration           maximumPollingDelay,
                                   Duration           maximumRetryDuration,
                                   Iterable<Duration> retryDurations)
    {
        this.initialDelay         = initialDelay;
        this.maximumPollingDelay  = maximumPollingDelay;
        this.maximumRetryDuration = maximumRetryDuration;
        this.retryDurations       = retryDurations;
    }


    @Override
    public Duration getInitialDelay()
    {
        return initialDelay;
    }


    @Override
    public Duration getMaximumRetryDuration()
    {
        return maximumRetryDuration;
    }


    @Override
    public Duration getMaximumPollingDelay()
    {
        return maximumPollingDelay;
    }


    @Override
    public Iterable<Duration> getRetryDelayDurations()
    {
        return retryDurations;
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
        this.maximumRetryDuration = Duration.of(duration, units);

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
        this.initialDelay = Duration.of(duration, units);

        return this;
    }
}
