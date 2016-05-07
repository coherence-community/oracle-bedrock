/*
 * File: VagrantProperty.java
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

package com.oracle.bedrock.runtime.virtual.vagrant.options;

import com.oracle.bedrock.Option;

import java.io.PrintWriter;
import java.util.Iterator;

/**
 * A {@link Collectable} {@link Option} representing a Vagrant Property, consisting
 * of a name and value.
 * <p>
 * Values of a {@link VagrantProperty} may be more than simple Strings.   For example,
 * if they are an {@link Iterator}, the value of a Vagrant Property will be the next
 * value taken from the {@link Iterator}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see VagrantProperties
 */
public class VagrantProperty implements Option.Collectable
{
    /**
     * The name of the {@link VagrantProperty}.
     */
    private final String name;

    /**
     * The value of the {@link VagrantProperty}.
     */
    private final Object value;


    /**
     * Constructs a {@link VagrantProperty}.
     *
     * @param name     the name of the {@link VagrantProperty}
     * @param value    the value of the {@link VagrantProperty}
     */
    private VagrantProperty(String name,
                            Object value)
    {
        this.name  = name;
        this.value = value;
    }


    /**
     * Constructs a {@link VagrantProperty}.
     *
     * @param name     the name of the {@link VagrantProperty}
     * @param value    the value of the {@link VagrantProperty}
     *
     * @return a {@link VagrantProperty}
     */
    public static VagrantProperty of(String name,
                                     String value)
    {
        return new VagrantProperty(name, value);
    }


    /**
     * Constructs a {@link VagrantProperty}.
     *
     * @param name     the name of the {@link VagrantProperty}
     * @param iterator the iterator that can provide values for the {@link VagrantProperty}
     *
     * @return a {@link VagrantProperty}
     */
    public static VagrantProperty of(String   name,
                                     Iterator iterator)
    {
        return new VagrantProperty(name, iterator);
    }


    /**
     * Constructs a {@link VagrantProperty}.
     *
     * @param name   the name of the {@link VagrantProperty}
     * @param object the values for the {@link VagrantProperty}
     *
     * @return a {@link VagrantProperty}
     */
    public static VagrantProperty of(String name,
                                     Object object)
    {
        return new VagrantProperty(name, object);
    }


    /**
     * Obtains the name of the {@link VagrantProperty}.
     *
     * @return the name of the {@link VagrantProperty}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains the value of the {@link VagrantProperty}.
     *
     * @return the value of the {@link VagrantProperty}
     */
    public Object getValue()
    {
        return value;
    }


    /**
     * Writes the {@link VagrantProperty} to the specified {@link PrintWriter}.
     *
     * @param writer   the {@link PrintWriter}
     * @param sPrefix  the prefix for property
     * @param sPad     the padding for the property
     */
    public void write(PrintWriter writer,
                      String      sPrefix,
                      String      sPad)
    {
        // ----- resolve the value when it's an iterator -----

        Object resolvedValue = this.value;

        while (resolvedValue instanceof Iterator)
        {
            Iterator iterator = (Iterator) resolvedValue;

            if (iterator.hasNext())
            {
                resolvedValue = iterator.next();
            }
            else
            {
                throw new IndexOutOfBoundsException(String.format("No more values available for the property [%s]",
                                                                  name));

            }
        }

        // ----- create a string representation of the resolved value -----

        String valueString;

        if (resolvedValue instanceof String)
        {
            valueString = "'" + String.valueOf(resolvedValue) + "'";
        }
        else if (resolvedValue != null)
        {
            valueString = String.valueOf(resolvedValue);
        }
        else
        {
            valueString = "";
        }

        // ----- write the resolved string value -----

        writer.printf("%s    %s%s = %s\n", sPad, sPrefix, name, valueString);
    }


    @Override
    public String toString()
    {
        return String.format("{name=%s, value=%s}", name, value);
    }


    @Override
    public Class<VagrantProperties> getCollectorClass()
    {
        return VagrantProperties.class;
    }
}
