/*
 * File: Duration.java
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

package com.oracle.tools.util;

import java.util.concurrent.TimeUnit;

/**
 * A length of time in a specified {@link TimeUnit}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Duration
{
    /**
     * A {@link Duration} of zero.
     */
    public static final Duration ZERO = Duration.of(0, TimeUnit.MILLISECONDS);

    /**
     * The amount of the {@link Duration} in {@link #units}.
     */
    private long amount;

    /**
     * The {@link TimeUnit} of the {@link Duration}.
     */
    private TimeUnit units;


    /**
     * Privately constructs a {@link Duration}.
     *
     * @param amount  the amount for the {@link Duration}
     * @param units   the {@link TimeUnit}s for the duration of the {@link Duration}
     */
    private Duration(long     amount,
                     TimeUnit units)
    {
        this.amount = amount;
        this.units  = units;
    }


    /**
     * Obtains the amount of the {@link Duration}.
     *
     * @return  the amount of the {@link Duration}
     */
    public long getAmount()
    {
        return amount;
    }


    /**
     * Obtains the units of the amount for the {@link Duration}.
     *
     * @return  the {@link TimeUnit}s for the {@link Duration}
     */
    public TimeUnit getUnits()
    {
        return units;
    }


    /**
     * Obtains the {@link Duration} amount in the specified {@link TimeUnit}.
     *
     * @param units  the desired {@link TimeUnit}
     *
     * @return  the amount measured in the specified {@link TimeUnit}
     */
    public long to(TimeUnit units)
    {
        return units.convert(getAmount(), getUnits());
    }


    @Override
    public String toString()
    {
        return getAmount() + getUnits().name();
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof Duration))
        {
            return false;
        }

        Duration timeout = (Duration) other;

        if (amount != timeout.amount)
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
        int result = (int) (amount ^ (amount >>> 32));

        result = 31 * result + units.hashCode();

        return result;
    }


    /**
     * Obtains a {@link Duration} for a specific time.
     *
     * @param duration  the duration for the {@link Duration}
     * @param units     the {@link TimeUnit}s for the duration of the {@link Duration}
     *
     * @return  the {@link Duration}
     */
    public static Duration of(long     duration,
                              TimeUnit units)
    {
        return new Duration(duration, units);
    }


    /**
     * Obtains a {@link Duration} for a specified time represented as a {@link String}
     * formatted as (0-9)+['ms'|'s'|'m'|'h'].  If no units are specified, the unit
     * of 'ms' is assumed.
     *
     * @param duration  the timeout string
     *
     * @return  a {@link Duration}
     */
    public static Duration of(String duration)
    {
        duration = duration.trim().toLowerCase();

        TimeUnit units;

        if (duration.endsWith("ms"))
        {
            units    = TimeUnit.MILLISECONDS;
            duration = duration.substring(0, duration.length() - 2).trim();
        }
        else if (duration.endsWith("s"))
        {
            units    = TimeUnit.SECONDS;
            duration = duration.substring(0, duration.length() - 1).trim();
        }
        else if (duration.endsWith("m"))
        {
            units    = TimeUnit.MINUTES;
            duration = duration.substring(0, duration.length() - 1).trim();
        }
        else if (duration.endsWith("h"))
        {
            units    = TimeUnit.HOURS;
            duration = duration.substring(0, duration.length() - 1).trim();
        }
        else
        {
            // assume milliseconds
            units = TimeUnit.MILLISECONDS;
        }

        return of(Long.valueOf(duration), units);
    }
}
