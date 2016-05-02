/*
 * File: Triple.java
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

/**
 * An immutable sequence of three type-safe values.
 *
 * @param <X>  the type of the first value of the {@link Triple}
 * @param <Y>  the type of the second value of the {@link Triple}
 * @param <Z>  the type of the third value of the {@link Triple}
 * <p>
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Triple<X, Y, Z> implements Tuple
{
    /**
     * The first value of the {@link Triple}.
     */
    private X x;

    /**
     * The second value of the {@link Triple}.
     */
    private Y y;

    /**
     * The third value of the {@link Triple}.
     */
    private Z z;


    /**
     * Constructs a {@link Triple}.
     *
     * @param x  the first value of the {@link Triple}
     * @param y  the second value of the {@link Triple}
     * @param z  the third value of the {@link Triple}
     */
    public Triple(X x,
                  Y y,
                  Z z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    /**
     * {@inheritDoc}
     */
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
        else if (index == 2)
        {
            return z;
        }
        else
        {
            throw new IndexOutOfBoundsException(String.format("%d is an illegal index for a Triple", index));
        }
    }


    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return 3;
    }


    /**
     * Obtains the first value of the {@link Triple}.
     *
     * @return  the first value of the {@link Triple}
     */
    public X getX()
    {
        return x;
    }


    /**
     * Obtains the second value of the {@link Triple}.
     *
     * @return  the second value of the {@link Triple}
     */
    public Y getY()
    {
        return y;
    }


    /**
     * Obtains the third value of the {@link Triple}.
     *
     * @return  the third value of the {@link Triple}
     */
    public Z getZ()
    {
        return z;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("Triple<%s, %s, %s>",
                             x == null ? "null" : x.toString(),
                             y == null ? "null" : y.toString(),
                             z == null ? "null" : z.toString());
    }
}
