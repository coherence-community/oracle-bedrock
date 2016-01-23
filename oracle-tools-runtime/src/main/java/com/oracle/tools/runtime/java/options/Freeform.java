/*
 * File: Freeform.java
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

import com.oracle.tools.Option;

import java.util.ArrayList;

/**
 * Represents a freeform {@link JvmOption}, that is {@link Collectable}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Freeform implements JvmOption, Option.Collectable
{
    /**
     * The freeform values for the {@link JvmOption}.
     */
    private ArrayList<String> values;


    /**
     * Constructs a {@link Freeform} {@link JvmOption} with a specified list of values
     *
     * @param values  the values for the {@link Freeform} {@link JvmOption}
     */
    public Freeform(String... values)
    {
        this.values = new ArrayList<>();

        if (values != null)
        {
            for (String value : values)
            {
                if (value != null)
                {
                    this.values.add(value.trim());
                }
            }
        }
    }


    @Override
    public Class<? extends Option.Collector> getCollectorClass()
    {
        return Freeforms.class;
    }


    @Override
    public Iterable<String> getValues(Option... options)
    {
        return values;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Freeform))
        {
            return false;
        }

        Freeform freeform = (Freeform) o;

        return values != null ? values.equals(freeform.values) : freeform.values == null;

    }


    @Override
    public int hashCode()
    {
        return values != null ? values.hashCode() : 0;
    }
}
