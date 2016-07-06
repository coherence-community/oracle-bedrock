/*
 * File: RetryFrequency.java
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

package com.oracle.bedrock.deferred.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.deferred.Ensured;
import com.oracle.bedrock.util.Duration;
import com.oracle.bedrock.util.ExponentialIterator;
import com.oracle.bedrock.util.FibonacciIterator;
import com.oracle.bedrock.util.MappingIterator;
import com.oracle.bedrock.util.PerpetualIterator;
import com.oracle.bedrock.util.RandomIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * An {@link Option} to define frequency (durations to wait) when attempting to resolve an
 * {@link Ensured} when calling {@link Ensured#get()}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Ensured
 */
public class RetryFrequency implements Option
{
    /**
     * The {@link Duration}s for the {@link RetryFrequency}.
     */
    private Iterable<Duration> durations;


    /**
     * Privately constructs a {@link RetryFrequency} {@link Option}.
     *
     * @param durations  the {@link Duration}s for the {@link RetryFrequency}
     */
    private RetryFrequency(Iterable<Duration> durations)
    {
        this.durations = durations;
    }


    /**
     * Obtains the {@link Duration}s for the {@link RetryFrequency}.
     *
     * @return  the {@link Duration}s for the {@link RetryFrequency}
     */
    public Iterable<Duration> get()
    {
        return durations;
    }


    /**
     * Obtains a randomized version of this {@link RetryFrequency}, where by
     * values returned from the {@link #get()} {@link Iterable} will be
     * randomized values between 0 and the values provided by this
     * {@link RetryFrequency}.
     *
     * @return  a new randomized {@link RetryFrequency}
     */
    public RetryFrequency randomized()
    {
        return new RetryFrequency(() -> new MappingIterator<>(new RandomIterator(new MappingIterator<>(this.get()
        .iterator(),
                                                                                                       duration -> duration.to(TimeUnit
                                                                                                           .MILLISECONDS))),
                                                              duration -> Duration.of(duration,
                                                                                      TimeUnit.MILLISECONDS)));
    }


    /**
     * Obtains the standard {@link RetryFrequency} for retrying every 250 milliseconds.
     *
     * @return  a {@link RetryFrequency}
     */
    @OptionsByType.Default
    public static RetryFrequency standard()
    {
        return every(250, TimeUnit.MILLISECONDS);
    }


    /**
     * Obtains a {@link RetryFrequency} based on the specified {@link Duration}s.
     *
     * @param durations  the {@link Duration}s
     *
     * @return a {@link RetryFrequency}
     */
    public static RetryFrequency of(Iterable<Duration> durations)
    {
        return new RetryFrequency(durations);
    }


    /**
     * Obtains a {@link RetryFrequency} based on the specified times.
     * <p>
     * For example, to retry at 0, 5, 10 and 20 seconds, use the following
     * <code>
     * RetryFrequency.of(TimeUnit.SECONDS, 0, 5, 10, 20);
     * </code>
     *
     * @param unit       the {@link TimeUnit} for the durations
     * @param instants   the instants in time  (will be sorted)
     *
     * @return a {@link RetryFrequency}
     */
    public static RetryFrequency of(TimeUnit unit,
                                    long...  instants)
    {
        ArrayList<Long> frequencies = new ArrayList<>();

        if (instants.length > 0)
        {
            // ensure we have the instants in a sorted order
            Arrays.sort(instants);

            // add the lowest
            frequencies.add(instants[0]);

            long sum = instants[0];

            for (int i = 1; i < instants.length; i++)
            {
                long delta = instants[i] - sum;

                sum = sum + delta;

                frequencies.add(delta);
            }

            return new RetryFrequency(() -> new MappingIterator<>(frequencies.iterator(),
                                                                  duration -> Duration.of(duration,
                                                                                          TimeUnit.MILLISECONDS)));
        }
        else
        {
            throw new IllegalArgumentException("Failed to specify any times for the RetryFrequency");
        }
    }


    /**
     * Obtains a {@link RetryFrequency} representing fixed retry {@link Duration}s.
     *
     * @param duration  the duration
     * @param unit      the {@link TimeUnit} for the {@link Duration}
     *
     * @return a {@link RetryFrequency}
     */
    public static RetryFrequency every(long     duration,
                                       TimeUnit unit)
    {
        return new RetryFrequency(() -> new PerpetualIterator<>(Duration.of(duration, unit)));
    }


    /**
     * Obtains a {@link RetryFrequency} based on the Fibonacci sequence.
     *
     * @return a {@link RetryFrequency}
     */
    public static RetryFrequency fibonacci()
    {
        return new RetryFrequency(() -> new MappingIterator<>(new FibonacciIterator(),
                                                              duration -> Duration.of(duration,
                                                                                      TimeUnit.MILLISECONDS)));
    }


    /**
     * Obtains a {@link RetryFrequency} based on an exponential sequence, starting at 0
     * with a 50% growth rate.
     *
     * @return a {@link RetryFrequency}
     */
    public static RetryFrequency exponential()
    {
        return exponential(0, 50);
    }


    /**
     * Obtains a {@link RetryFrequency} based on an exponential sequence.
     *
     * @param initial     the initial (starting) value
     * @param percentage  the growth rate percentage (typically between 0.0 and 100.0)
     *
     * @return a {@link RetryFrequency}
     */
    public static RetryFrequency exponential(double initial,
                                             double percentage)
    {
        return new RetryFrequency(() -> new MappingIterator<>(new ExponentialIterator(initial, percentage),
                                                              duration -> Duration.of(duration,
                                                                                      TimeUnit.MILLISECONDS)));
    }
}
