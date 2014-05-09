/*
 * File: Quadruple.java
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

/**
 * An immutable sequence of four type-safe values.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of the first value of the {@link Quadruple}
 * @param <B>  the type of the second value of the {@link Quadruple}
 * @param <C>  the type of the third value of the {@link Quadruple}
 * @param <D>  the type of the fourth value of the {@link Quadruple}
 */
public class Quadruple<A, B, C, D> implements Tuple
{
    /**
     * The first value of the {@link Quadruple}.
     */
    private A a;

    /**
     * The second value of the {@link Quadruple}.
     */
    private B b;

    /**
     * The third value of the {@link Quadruple}.
     */
    private C c;

    /**
     * The fourth value of the {@link Quadruple}.
     */
    private D d;


    /**
     * Constructs a {@link Quadruple}.
     *
     * @param a  the first value of the {@link Quadruple}
     * @param b  the second value of the {@link Quadruple}
     * @param c  the third value of the {@link Quadruple}
     * @param d  the fourth value of the {@link Quadruple}
     */
    public Quadruple(A a,
                     B b,
                     C c,
                     D d)
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }


    /**
     * {@inheritDoc}
     */
    public Object get(int index) throws IndexOutOfBoundsException
    {
        if (index == 0)
        {
            return a;
        }
        else if (index == 1)
        {
            return b;
        }
        else if (index == 2)
        {
            return c;
        }
        else if (index == 3)
        {
            return d;
        }
        else
        {
            throw new IndexOutOfBoundsException(String.format("%d is an illegal index for a Quadruple`", index));
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
     * Obtains the first value of the {@link Quadruple}.
     *
     * @return  the first value of the {@link Quadruple}
     */
    public A getA()
    {
        return a;
    }


    /**
     * Obtains the second value of the {@link Quadruple}.
     *
     * @return  the second value of the {@link Quadruple}
     */
    public B getB()
    {
        return b;
    }


    /**
     * Obtains the third value of the {@link Quadruple}.
     *
     * @return  the third value of the {@link Quadruple}
     */
    public C getC()
    {
        return c;
    }


    /**
     * Obtains the fourth value of the {@link Quadruple}.
     *
     * @return  the fourth value of the {@link Quadruple}
     */
    public D getD()
    {
        return d;
    }


    @Override
    public String toString()
    {
        return String.format("Quadruple<%s, %s, %s, %s>",
                             a == null ? "null" : a.toString(),
                             b == null ? "null" : b.toString(),
                             c == null ? "null" : c.toString(),
                             d == null ? "null" : d.toString());
    }
}
