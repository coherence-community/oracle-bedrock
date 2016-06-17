/*
 * File: Timeout.java
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

package com.oracle.bedrock.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.util.Duration;

import java.util.concurrent.TimeUnit;

/**
 * An {@link Option} to define a {@link Timeout}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Timeout implements Option
{
    /**
     * The {@link Duration} of the {@link Timeout}.
     */
    private Duration duration;


    /**
     * Privately constructs a {@link Timeout} {@link Option}.
     *
     * @param duration  the {@link Duration} for the {@link Timeout}
     */
    private Timeout(Duration duration)
    {
        this.duration = duration;
    }


    /**
     * Obtains the {@link Duration} of the {@link Timeout}.
     *
     * @return  the {@link Duration} of the {@link Timeout}
     */
    public Duration getDuration()
    {
        return duration;
    }


    /**
     * Obtains the {@link Timeout} {@link Duration} in the specified {@link TimeUnit}.
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

        if (!(other instanceof Timeout))
        {
            return false;
        }

        Timeout timeout = (Timeout) other;

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
        return "Timeout{" + getDuration() + "}";
    }


    /**
     * Obtains the {@link Timeout} by auto-detecting it from the configuration
     * and environment.
     *
     * @return  the default {@link Timeout}
     */
    @Options.Default
    public static Timeout autoDetect()
    {
        return new Timeout(Duration.of(1, TimeUnit.MINUTES));
    }


    /**
     * Obtains a {@link Timeout} for a specific time.
     *
     * @param duration  the duration for the {@link Timeout}
     * @param units     the {@link TimeUnit}s for the duration of the {@link Timeout}
     *
     * @return  the {@link Timeout}
     */
    public static Timeout after(long     duration,
                                TimeUnit units)
    {
        return new Timeout(Duration.of(duration, units));
    }


    /**
     * Obtains a {@link Timeout} for a specific time.
     *
     * @param duration  the duration for the {@link Timeout}
     *
     * @return  the {@link Timeout}
     */
    public static Timeout after(Duration duration)
    {
        return new Timeout(duration);
    }


    /**
     * Obtains a {@link Timeout} for a specified time represented as a {@link String}
     * formatted as (0-9)+['ms'|'s'|'m'|'h'].  If no units are specified, the unit
     * of 'ms' is assumed.
     *
     * @param duration  the timeout string
     *
     * @return  a {@link Timeout}
     */
    public static Timeout after(String duration)
    {
        return new Timeout(Duration.of(duration));
    }


    /**
     * Obtains a {@link Timeout} for a specific time.
     *
     * @param duration  the duration for the {@link Timeout}
     * @param units     the {@link TimeUnit}s for the duration of the {@link Timeout}
     *
     * @return  the {@link Timeout}
     */
    public static Timeout of(long     duration,
                             TimeUnit units)
    {
        return Timeout.after(duration, units);
    }


    /**
     * Obtains a {@link Timeout} for a specific time.
     *
     * @param duration  the duration for the {@link Timeout}
     *
     * @return  the {@link Timeout}
     */
    public static Timeout of(Duration duration)
    {
        return Timeout.after(duration);
    }


    /**
     * Obtains a {@link Timeout} for a specified time represented as a {@link String}
     * formatted as (0-9)+['ms'|'s'|'m'|'h'].  If no units are specified, the unit
     * of 'ms' is assumed.
     *
     * @param duration  the timeout string
     *
     * @return  a {@link Timeout}
     */
    public static Timeout of(String duration)
    {
        return Timeout.after(duration);
    }
}
