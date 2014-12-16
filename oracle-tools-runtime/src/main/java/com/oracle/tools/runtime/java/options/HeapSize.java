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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.ComposableOption;
import com.oracle.tools.Option;

import java.util.ArrayList;

/**
 * An {@link Option} for configuring the {@link HeapSize} of a Java Virtual Machine.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class HeapSize implements ComposableOption<HeapSize>, JvmOption
{
    /**
     * The units of measure for the {@link HeapSize}.
     */
    public static enum Units {KB,
                              MB,
                              GB}


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
     * Obtains a {@link HeapSize} for the defaults.
     *
     * @return  the default {@link HeapSize}
     */
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


    @Override
    public Iterable<String> getOptions()
    {
        ArrayList<String> options = new ArrayList<>(2);

        if (initial >= 0)
        {
            options.add("-Xms" + initial + initialUnits.toString().charAt(0));
        }

        if (maximum >= 0)
        {
            options.add("-Xmx" + maximum + maximumUnits.toString().charAt(0));
        }

        return options;
    }


    @Override
    public HeapSize compose(HeapSize other)
    {
        HeapSize result       = new HeapSize();

        long     initial      = this.initial <= 0 ? 0 : this.initial * ((this.initialUnits.ordinal() ^ 10) * 1024);
        long     otherInitial = other.initial <= 0 ? 0 : other.initial * ((other.initialUnits.ordinal() ^ 10) * 1024);

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

        long maximum      = this.maximum <= 0 ? 0 : this.maximum * ((this.maximumUnits.ordinal() ^ 10) * 1024);
        long otherMaximum = other.maximum <= 0 ? 0 : other.maximum * ((other.maximumUnits.ordinal() ^ 10) * 1024);

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
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof HeapSize))
        {
            return false;
        }

        HeapSize heapSize = (HeapSize) other;

        if (initial != heapSize.initial)
        {
            return false;
        }

        if (maximum != heapSize.maximum)
        {
            return false;
        }

        if (initialUnits != heapSize.initialUnits)
        {
            return false;
        }

        if (maximumUnits != heapSize.maximumUnits)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = initial;

        result = 31 * result + (initialUnits != null ? initialUnits.hashCode() : 0);
        result = 31 * result + maximum;
        result = 31 * result + (maximumUnits != null ? maximumUnits.hashCode() : 0);

        return result;
    }
}
