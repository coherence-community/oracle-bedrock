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

package com.oracle.tools.options;

import com.oracle.tools.Option;

import java.sql.Time;

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
     * The duration of the {@link Timeout} in {@link #units}.
     */
    private long duration;

    /**
     * The {@link TimeUnit} of the {@link Timeout}.
     */
    private TimeUnit units;


    /**
     * Privately constructs a {@link Timeout} {@link Option}.
     *
     * @param duration  the duration for the {@link Timeout}
     * @param units     the {@link TimeUnit}s for the duration of the {@link Timeout}
     */
    private Timeout(long     duration,
                    TimeUnit units)
    {
        this.duration = duration;
        this.units    = units;
    }


    /**
     * Obtains the duration of the {@link Timeout}.
     *
     * @return  the duration of the {@link Timeout}
     */
    public long getDuration()
    {
        return duration;
    }


    /**
     * Obtains the units of the duration for the {@link Timeout}.
     *
     * @return  the {@link TimeUnit}s for the {@link Timeout}
     */
    public TimeUnit getUnits()
    {
        return units;
    }


    /**
     * Obtains the {@link Timeout} duration in the specified {@link TimeUnit}.
     *
     * @param units  the desired {@link TimeUnit}
     *
     * @return  the duration measured in the specified {@link TimeUnit}
     */
    public long to(TimeUnit units)
    {
        return units.convert(getDuration(), getUnits());
    }


    @Override
    public String toString()
    {
        return "Timeout{" + getDuration() + " " + getUnits().name() + "}";
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

        if (duration != timeout.duration)
        {
            return false;
        }

        if (units != timeout.units)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = (int) (duration ^ (duration >>> 32));

        result = 31 * result + units.hashCode();

        return result;
    }


    /**
     * Obtains the {@link Timeout} by auto-detecting it from the configuration
     * and environment.
     *
     * @return  the default {@link Timeout}
     */
    public static Timeout autoDetect()
    {
        return new Timeout(1, TimeUnit.MINUTES);
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
        return new Timeout(duration, units);
    }
}
