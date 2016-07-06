/*
 * File: InitialDelay.java
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
 * An {@link Option} to define the initial delay when attempting to resolve an {@link Ensured}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class InitialDelay implements Option
{
    /**
     * The {@link Duration} of the {@link InitialDelay}.
     */
    private Duration duration;


    /**
     * Privately constructs a {@link InitialDelay} {@link Option}.
     *
     * @param duration  the {@link Duration} for the {@link InitialDelay}
     */
    private InitialDelay(Duration duration)
    {
        this.duration = duration;
    }


    /**
     * Obtains the {@link Duration} of the {@link InitialDelay}.
     *
     * @return  the {@link Duration} of the {@link InitialDelay}
     */
    public Duration getDuration()
    {
        return duration;
    }


    /**
     * Obtains the {@link InitialDelay} {@link Duration} in the specified {@link TimeUnit}.
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

        if (!(other instanceof InitialDelay))
        {
            return false;
        }

        InitialDelay timeout = (InitialDelay) other;

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
        return "InitialDelay{" + getDuration() + "}";
    }


    /**
     * Obtains a {@link InitialDelay} that represents no delay.
     *
     * @return  a {@link InitialDelay}
     */
    @OptionsByType.Default
    public static InitialDelay none()
    {
        return new InitialDelay(Duration.ZERO);
    }


    /**
     * Obtains a {@link InitialDelay} for a specific time.
     *
     * @param duration  the duration for the {@link InitialDelay}
     * @param units     the {@link TimeUnit}s for the duration of the {@link InitialDelay}
     *
     * @return  the {@link InitialDelay}
     */
    public static InitialDelay of(long     duration,
                                  TimeUnit units)
    {
        return new InitialDelay(Duration.of(duration, units));
    }


    /**
     * Obtains a {@link InitialDelay} for a specified time represented as a {@link String}
     * formatted as (0-9)+['ms'|'s'|'m'|'h'].  If no units are specified, the unit
     * of 'ms' is assumed.
     *
     * @param duration  the timeout string
     *
     * @return  a {@link InitialDelay}
     */
    public static InitialDelay of(String duration)
    {
        return new InitialDelay(Duration.of(duration));
    }
}
