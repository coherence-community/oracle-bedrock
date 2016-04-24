/*
 * File: FibonacciIterator.java
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

import java.util.Iterator;

/**
 * An {@link Iterator} over the
 * <a href="http://en.wikipedia.org/wiki/Fibonacci_number">Fibonacci</a> sequence.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class FibonacciIterator implements Iterator<Long>
{
    /**
     * The iteration (digit) of the Fibonacci sequence the
     * {@link Iterator} is currently up to.
     */
    private long iteration;

    /**
     * The last two generated Fibonacci numbers
     */
    private long[] previousValues;


    /**
     * Constructs a {@link FibonacciIterator}.
     */
    public FibonacciIterator()
    {
        iteration      = 0;
        previousValues = new long[2];
    }


    @Override
    public boolean hasNext()
    {
        return true;
    }


    @Override
    public Long next()
    {
        if (iteration == 0)
        {
            previousValues[0] = 0;
            previousValues[1] = 0;
        }
        else if (iteration == 1)
        {
            previousValues[0] = 0;
            previousValues[1] = 1;
        }

        long result = previousValues[0] + previousValues[1];

        if (iteration > 1)
        {
            previousValues[0] = previousValues[1];
            previousValues[1] = result;
        }

        iteration++;

        return result;
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Can't remove from a " + this.getClass().getName());
    }
}
