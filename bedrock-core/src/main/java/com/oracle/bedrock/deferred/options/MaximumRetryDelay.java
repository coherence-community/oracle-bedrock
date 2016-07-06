/*
 * File: MaximumRetryDelay.java
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

import java.util.concurrent.TimeUnit;

/**
 * An {@link Option} to define the maximum delay that may occur between successive attempts
 * to resolve an {@link Ensured} when calling {@link Ensured#get()}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Ensured
 */
public class MaximumRetryDelay implements Option
{
    /**
     * The {@link Duration} of the {@link MaximumRetryDelay}.
     */
    private Duration duration;


    /**
     * Privately constructs a {@link MaximumRetryDelay} {@link Option}.
     *
     * @param duration  the {@link Duration} for the {@link MaximumRetryDelay}
     */
    private MaximumRetryDelay(Duration duration)
    {
        this.duration = duration;
    }


    /**
     * Obtains the {@link Duration} of the {@link MaximumRetryDelay}.
     *
     * @return  the {@link Duration} of the {@link MaximumRetryDelay}
     */
    public Duration getDuration()
    {
        return duration;
    }


    /**
     * Obtains the {@link MaximumRetryDelay} {@link Duration} in the specified {@link TimeUnit}.
     *
     * @param units  the desired {@link TimeUnit}
     *
     * @return  the duration measured in the specified {@link TimeUnit}
     */
    public long to(TimeUnit units)
    {
        return duration.to(units);
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof MaximumRetryDelay))
        {
            return false;
        }

        MaximumRetryDelay timeout = (MaximumRetryDelay) other;

        if (!duration.equals(timeout.duration))
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return duration.hashCode();
    }


    @Override
    public String toString()
    {
        return "MaximumRetryDelay{" + getDuration() + "}";
    }


    /**
     * Obtains the default {@link MaximumRetryDelay} (of 1 second).
     *
     * @return  a {@link MaximumRetryDelay}
     */
    @OptionsByType.Default
    public static MaximumRetryDelay standard()
    {
        return new MaximumRetryDelay(Duration.of(1, TimeUnit.SECONDS));
    }


    /**
     * Obtains a {@link MaximumRetryDelay} of a specified {@link Duration}.
     *
     * @param duration  the duration for the {@link MaximumRetryDelay}
     *
     * @return  the {@link MaximumRetryDelay}
     */
    public static MaximumRetryDelay of(Duration duration)
    {
        return new MaximumRetryDelay(duration);
    }


    /**
     * Obtains a {@link MaximumRetryDelay} of a specified duration.
     *
     * @param duration  the duration for the {@link MaximumRetryDelay}
     * @param units     the {@link TimeUnit}s for the duration of the {@link MaximumRetryDelay}
     *
     * @return  the {@link MaximumRetryDelay}
     */
    public static MaximumRetryDelay of(long     duration,
                                       TimeUnit units)
    {
        return new MaximumRetryDelay(Duration.of(duration, units));
    }


    /**
     * Obtains a {@link MaximumRetryDelay} of a specified time represented as a {@link String}
     * formatted as (0-9)+['ms'|'s'|'m'|'h'].  If no units are specified, the unit
     * of 'ms' is assumed.
     *
     * @param duration  the timeout string
     *
     * @return  a {@link MaximumRetryDelay}
     */
    public static MaximumRetryDelay of(String duration)
    {
        return new MaximumRetryDelay(Duration.of(duration));
    }
}
