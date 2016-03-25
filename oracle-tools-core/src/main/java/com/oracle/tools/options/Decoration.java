/*
 * File: Decoration.java
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

/**
 * A {@link Collectable} {@link Option} representing a "decoration" consisting of an
 * immutable reference to a custom object.
 * <p>
 * {@link Decoration}s allow arbitrary objects to be specified as {@link Option}s.
 * </p>
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Decorations
 */
public class Decoration implements Option.Collectable
{
    /**
     * The reference to the {@link Decoration} object.
     */
    private Object object;


    /**
     * Constructs a {@link Decoration} containing the specified object.
     *
     * @param object  the object for the {@link Decoration}
     */
    private Decoration(Object object)
    {
        this.object = object;
    }


    /**
     * Constructs a {@link Decoration}.
     *
     * @param object the object for the {@link Decoration}
     */
    public static Decoration of(Object object)
    {
        return new Decoration(object);
    }


    /**
     * Obtains the object of the {@link Decoration}.
     *
     * @return the object of the {@link Decoration}
     */
    public Object get()
    {
        return object;
    }


    @Override
    public String toString()
    {
        return String.format("Decoration{%s}", object);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Decoration))
        {
            return false;
        }

        Decoration that = (Decoration) o;

        return object != null ? object.equals(that.object) : that.object == null;

    }


    @Override
    public int hashCode()
    {
        return object != null ? object.hashCode() : 0;
    }


    @Override
    public Class<Decorations> getCollectorClass()
    {
        return Decorations.class;
    }
}
