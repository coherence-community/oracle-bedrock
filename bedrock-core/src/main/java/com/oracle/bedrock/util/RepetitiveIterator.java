/*
 * File: RepetitiveIterator.java
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
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that returns the same value a number of times.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RepetitiveIterator<T> implements Iterator<T>
{
    /**
     * The value to return for each {@link #next()} call.
     */
    private T value;

    /**
     * The number of times to return the value.
     */
    private int count;


    /**
     * Constructs an {@link RepetitiveIterator}.
     *
     * @param value  the value to return
     * @param count  the number of times to return the value
     */
    public RepetitiveIterator(T   value,
                              int count)
    {
        this.value = value;
        this.count = count;
    }


    @Override
    public boolean hasNext()
    {
        return count > 0;
    }


    @Override
    public T next()
    {
        if (count <= 0)
        {
            throw new NoSuchElementException("No values remain in the iteration");
        }
        else
        {
            count--;

            return value;
        }
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("PerpetualIterator's don't support removing values");
    }
}
