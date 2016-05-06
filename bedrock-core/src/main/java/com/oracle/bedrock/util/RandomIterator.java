/*
 * File: RandomIterator.java
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

package com.oracle.bedrock.util;

import java.util.Iterator;
import java.util.Random;

/**
 * An {@link Iterator} over a positive sequence of random {@link Long} numbers.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RandomIterator implements Iterator<Long>
{
    /**
     * The range for the next random number to generate.
     */
    private Iterator<Long> ranges;

    /**
     * The {@link Random} number generator.
     */
    private Random random;


    /**
     * Constructs a {@link RandomIterator} that uses the
     * ranges provided by the range {@link Iterator} for
     * each random number generated.
     * <p>
     * Ranges are from 0 to the numbers provided by the
     * specified {@link Iterator} (exclusive).
     *
     * @param ranges  an {@link Iterator} providing ranges.
     */
    public RandomIterator(Iterator<Long> ranges)
    {
        this.ranges = ranges;
        this.random = new Random(System.nanoTime());
    }


    /**
     * Constructs a {@link RandomIterator} for numbers between
     * 0 and the specified range (exclusive).
     *
     * @param range the upper bound of the range
     */
    public RandomIterator(long range)
    {
        this(new PerpetualIterator<Long>(range));
    }


    @Override
    public boolean hasNext()
    {
        return ranges.hasNext();
    }


    @Override
    public Long next()
    {
        long range  = ranges.next();
        long random = Math.abs(this.random.nextLong());

        return range == 0 ? 0 : random % range;
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Can't remove from a " + this.getClass().getName());
    }
}
