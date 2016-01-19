/*
 * File: SystemProperty.java
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

import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.java.JavaApplicationSchema;

import java.util.Iterator;

/**
 * A {@link Collectable} {@link Option} representing a System Property, consisting
 * of a name and value.
 * <p>
 * Values of a {@link SystemProperty} may be more than simple Strings.   For example,
 * if they are an {@link Iterator}, the value of a System Property will be the next
 * value taken from the {@link Iterator}.   When the value is a
 * {@link ContextSensitiveValue}, the value will be based on a provided {@link Platform}
 * and {@link ApplicationSchema}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @see SystemProperties
 */
public class SystemProperty implements Option.Collectable
{
    /**
     * The name of the {@link SystemProperty}.
     */
    private String name;

    /**
     * The value of the {@link SystemProperty}.
     */
    private Object value;


    /**
     * Constructs a {@link SystemProperty} with no value.
     *
     * @param name the name of the {@link SystemProperty}
     */
    private SystemProperty(String name)
    {
        this.name  = name;
        this.value = "";
    }


    /**
     * Constructs a {@link SystemProperty} based on another {@link SystemProperty}.
     *
     * @param property the {@link SystemProperty} from which to construct
     *                 (copy) the new {@link SystemProperty}
     */
    private SystemProperty(SystemProperty property)
    {
        this.name  = property.getName();
        this.value = property.getValue();
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name  the name of the {@link SystemProperty}
     * @param value the value of the {@link SystemProperty}
     */
    private SystemProperty(String name,
                           Object value)
    {
        this.name  = name;
        this.value = value;
    }


    /**
     * Constructs a {@link SystemProperty} with no value.
     *
     * @param name the name of the {@link SystemProperty}
     */
    public static SystemProperty of(String name)
    {
        return new SystemProperty(name, "");
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name  the name of the {@link SystemProperty}
     * @param value the value of the {@link SystemProperty}
     */
    public static SystemProperty of(String name,
                                    String value)
    {
        return new SystemProperty(name, value);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name  the name of the {@link SystemProperty}
     * @param value the value of the {@link SystemProperty}
     */
    public static SystemProperty of(String                name,
                                    ContextSensitiveValue value)
    {
        return new SystemProperty(name, value);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param iterator the iterator that can provide values for the {@link SystemProperty}
     */
    public static SystemProperty of(String   name,
                                    Iterator iterator)
    {
        return new SystemProperty(name, iterator);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name   the name of the {@link SystemProperty}
     * @param object the values for the {@link SystemProperty}
     */
    public static SystemProperty of(String name,
                                    Object object)
    {
        return new SystemProperty(name, object);
    }


    /**
     * Obtains the name of the {@link SystemProperty}.
     *
     * @return the name of the {@link SystemProperty}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains the value of the {@link SystemProperty}.
     *
     * @return the value of the {@link SystemProperty}
     */
    public Object getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        return String.format("{name=%s, value=%s}", name, value);
    }


    @Override
    public Class<SystemProperties> getCollectorClass()
    {
        return SystemProperties.class;
    }


    /**
     * A context sensitive value, possibly based on the {@link Platform} and/or {@link JavaApplicationSchema} in
     * which the {@link SystemProperty} is being used.
     */
    public interface ContextSensitiveValue
    {
        /**
         * Obtains the value for the {@link SystemProperty}, possibly based on the provided
         * {@link Platform} and {@link JavaApplicationSchema}.
         *
         * @param name     the name of the {@link SystemProperty}
         * @param platform the {@link Platform} in which {@link SystemProperty} is being used.
         * @param schema   the {@link ApplicationSchema} in which {@link SystemProperty} is being used.
         *
         * @return the value
         */
        Object getValue(String                name,
                        Platform              platform,
                        JavaApplicationSchema schema);
    }
}
