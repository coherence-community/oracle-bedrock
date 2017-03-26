/*
 * File: Pair.java
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

import java.util.Map;

/**
 * An immutable sequence of two type-safe values.
 *
 * @param <X>  the type of the first value of the {@link Pair}
 * @param <Y>  the type of the second value of the {@link Pair}
 * <p>
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Pair<X, Y> implements Tuple
{
    /**
     * The first value of the {@link Pair}.
     */
    private X x;

    /**
     * The second value of the {@link Pair}.
     */
    private Y y;


    /**
     * Constructs a {@link Pair} based on a Map.Entry.
     *
     * @param entry  a Map.Entry from which to create a {@link Pair}
     */
    public Pair(Map.Entry<X, Y> entry)
    {
        this.x = entry.getKey();
        this.y = entry.getValue();
    }


    /**
     * Constructs a {@link Pair}.
     *
     * @param x  the first value of the {@link Pair}
     * @param y  the second value of the {@link Pair}
     */
    public Pair(X x,
                Y y)
    {
        this.x = x;
        this.y = y;
    }


    /**
     * Constructs a {@link Pair}.
     *
     * @param x  the first value of the {@link Pair}
     * @param y  the second value of the {@link Pair}
     *
     * @param <X>  the type of the first value of the {@link Pair}
     * @param <Y>  the type of the second value of the {@link Pair}
     *
     * @return a {@link Pair}
     */
    public static <X, Y> Pair<X, Y> of(X x,
                                       Y y)
    {
        return new Pair<X, Y>(x, y);
    }


    @Override
    public Object get(int index) throws IndexOutOfBoundsException
    {
        if (index == 0)
        {
            return x;
        }
        else if (index == 1)
        {
            return y;
        }
        else
        {
            throw new IndexOutOfBoundsException(String.format("%d is an illegal index for a Pair", index));
        }
    }


    @Override
    public int size()
    {
        return 2;
    }


    /**
     * Obtains the first value of the {@link Pair}.
     *
     * @return  the first value of the {@link Pair}
     */
    public X getX()
    {
        return x;
    }


    /**
     * Obtains the second value of the {@link Pair}.
     *
     * @return  the second value of the {@link Pair}
     */
    public Y getY()
    {
        return y;
    }


    @Override
    public String toString()
    {
        return String.format("Pair<%s, %s>", x == null ? "null" : x.toString(), y == null ? "null" : y.toString());
    }
}
