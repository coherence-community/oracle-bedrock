/*
 * File: Discriminator.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.Application;

/**
 * An {@link Option} to specify the a discriminator for an {@link Application}.
 * <p>
 * {@link Discriminator}s are typically used to discriminator between multiple
 * {@link Application}s that have the same underlying {@link DisplayName}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Discriminator implements Option
{
    /**
     * The discriminator for an {@link Application}.
     */
    private String value;


    /**
     * Constructs a {@link Discriminator} with the specified value.
     *
     * @param value  the value
     */
    private Discriminator(String value)
    {
        if (value == null)
        {
            this.value = "";
        }
        else
        {
            this.value = value.trim();
        }
    }


    /**
     * Obtains the value of the {@link Discriminator}.
     *
     * @return  the value of the {@link Discriminator}
     */
    public String getValue()
    {
        return value;
    }


    /**
     * Obtains a {@link Discriminator} for a specified value.
     *
     * @param value  the value of the {@link Discriminator}
     *
     * @return a {@link Discriminator} for the specified value
     */
    public static Discriminator of(String value)
    {
        return new Discriminator(value);
    }


    /**
     * Obtains a {@link Discriminator} for a specified value.
     *
     * @param value  the value of the {@link Discriminator}
     *
     * @return a {@link Discriminator} for the specified value
     */
    public static Discriminator of(int value)
    {
        return new Discriminator(Integer.toString(value));
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Discriminator))
        {
            return false;
        }

        Discriminator executable = (Discriminator) o;

        return value.equals(executable.value);

    }


    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
