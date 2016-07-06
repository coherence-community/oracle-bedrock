/*
 * File: HeapSize.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.OptionsByType;

import java.util.ArrayList;

/**
 * A {@link JvmOption} for configuring the {@link HeapSize} of a Java Virtual Machine.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class HeapSize implements ComposableOption<HeapSize>, JvmOption
{
    /**
     * The initial heap size in {@link #initialUnits}.
     * (-1 means use the default)
     */
    private int initial;

    /**
     * The {@link Units} of the initial heap size.
     */
    private Units initialUnits;

    /**
     * The maximum heap size in {@link #maximumUnits}.
     * (-1 means use the default)
     */
    private int maximum;

    /**
     * The {@link Units} of the maximum heap size.
     */
    private Units maximumUnits;


    /**
     * Privately constructs a default {@link HeapSize}.
     */
    private HeapSize()
    {
        this.initial      = -1;
        this.initialUnits = Units.KB;
        this.maximum      = -1;
        this.maximumUnits = Units.KB;
    }


    /**
     * A private copy constructor for a {@link HeapSize}.
     *
     * @param heapSize  the {@link HeapSize} to copy
     */
    private HeapSize(HeapSize heapSize)
    {
        this.initial      = heapSize.initial;
        this.initialUnits = heapSize.initialUnits;
        this.maximum      = heapSize.maximum;
        this.maximumUnits = heapSize.maximumUnits;
    }


    /**
     * The units of measure for the {@link HeapSize}.
     */
    public enum Units
    {
        /**
         * The number of bytes in a Kibibyte (traditional Kilobyte).
         */
        KB((long) Math.pow(1024, 1)),

        /**
         * The number of bytes in a Mebibyte (traditional Megabyte).
         */
        MB((long) Math.pow(1024, 2)),

        /**
         * The number of bytes in a Gibibyte (traditional Megabyte).
         */
        GB((long) Math.pow(1024, 3));

        /**
         * The number of bytes in the a unit.
         */
        private long bytes;


        /**
         * Constructs an individual {@link Units}.
         *
         * @param bytes  the number of bytes in the unit
         */
        Units(long bytes)
        {
            this.bytes = bytes;
        }


        /**
         * Obtains the number of bytes in a unit.
         *
         * @return the number of bytes
         */
        long getBytes()
        {
            return bytes;
        }
    }


    /**
     * Obtains a {@link HeapSize} for the defaults.
     *
     * @return  the default {@link HeapSize}
     */
    @OptionsByType.Default
    public static HeapSize useDefaults()
    {
        return new HeapSize();
    }


    /**
     * Obtains a {@link HeapSize} with the specified initial amount
     * (and a default maximum size)
     *
     * @param amount  the initial {@link HeapSize}
     * @param units   the units of the initial {@link HeapSize}
     *
     * @return the {@link HeapSize}
     */
    public static HeapSize initial(int   amount,
                                   Units units)
    {
        HeapSize heapSize = new HeapSize();

        heapSize.initial      = amount;
        heapSize.initialUnits = units;

        return heapSize;
    }


    /**
     * Obtains a {@link HeapSize} with the specified maximum amount
     * (and a default initial size).
     *
     * @param amount  the maximum {@link HeapSize}
     * @param units   the units of the maximum {@link HeapSize}
     *
     * @return the {@link HeapSize}
     */
    public static HeapSize maximum(int   amount,
                                   Units units)
    {
        HeapSize heapSize = new HeapSize();

        heapSize.maximum      = amount;
        heapSize.maximumUnits = units;

        return heapSize;
    }


    /**
     * Obtains a {@link HeapSize} with the values.
     *
     * @param initial       the initial {@link HeapSize}
     * @param initialUnits  the units of the initial {@link HeapSize}
     * @param maximum       the maximum {@link HeapSize}
     * @param maximumUnits  the units of the maximum {@link HeapSize}
     *
     * @return the {@link HeapSize}
     */
    public static HeapSize of(int   initial,
                              Units initialUnits,
                              int   maximum,
                              Units maximumUnits)
    {
        HeapSize heapSize = new HeapSize();

        heapSize.initial      = initial;
        heapSize.initialUnits = initialUnits;

        heapSize.maximum      = maximum;
        heapSize.maximumUnits = maximumUnits;

        return heapSize;
    }


    /**
     * Obtains the initial {@link HeapSize} in the specified {@link Units}.
     *
     * @param units  the required {@link Units}
     *
     * @return  the initial heap size in {@link Units}
     */
    public long getInitialSizeAs(Units units)
    {
        return initial * initialUnits.getBytes() / units.getBytes();
    }


    /**
     * Obtains the maximum {@link HeapSize} in the specified {@link Units}.
     *
     * @param units  the required {@link Units}
     *
     * @return  the maximum heap size in {@link Units}
     */
    public long getMaximumSizeAs(Units units)
    {
        return maximum * maximumUnits.getBytes() / units.getBytes();
    }


    @Override
    public Iterable<String> resolve(OptionsByType optionsByType)
    {
        ArrayList<String> values = new ArrayList<>(2);

        if (initial >= 0)
        {
            values.add("-Xms" + initial + initialUnits.toString().charAt(0));
        }

        if (maximum >= 0)
        {
            values.add("-Xmx" + maximum + maximumUnits.toString().charAt(0));
        }

        return values;
    }


    @Override
    public HeapSize compose(HeapSize other)
    {
        HeapSize result       = new HeapSize();

        long     initial      = this.initial <= 0 ? 0 : this.getInitialSizeAs(Units.KB);
        long     otherInitial = other.initial <= 0 ? 0 : other.getInitialSizeAs(Units.KB);

        if (initial > otherInitial)
        {
            result.initial      = this.initial;
            result.initialUnits = this.initialUnits;
        }
        else
        {
            result.initial      = other.initial;
            result.initialUnits = other.initialUnits;
        }

        long maximum      = this.maximum <= 0 ? 0 : this.getMaximumSizeAs(Units.KB);
        long otherMaximum = other.maximum <= 0 ? 0 : other.getMaximumSizeAs(Units.KB);

        if (maximum > otherMaximum)
        {
            result.maximum      = this.maximum;
            result.maximumUnits = this.maximumUnits;
        }
        else
        {
            result.maximum      = other.maximum;
            result.maximumUnits = other.maximumUnits;
        }

        return result;
    }


    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }

        if (!(object instanceof HeapSize))
        {
            return false;
        }

        HeapSize other = (HeapSize) object;

        if (getInitialSizeAs(Units.KB) != other.getInitialSizeAs(Units.KB))
        {
            return false;
        }

        return getMaximumSizeAs(Units.KB) == other.getMaximumSizeAs(Units.KB);

    }


    @Override
    public int hashCode()
    {
        int result = initial;

        result = 31 * result + maximum;

        return result;
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (String value : resolve(OptionsByType.empty()))
        {
            if (builder.length() > 0)
            {
                builder.append(" ");
            }

            builder.append(value);
        }

        return builder.toString();
    }
}
