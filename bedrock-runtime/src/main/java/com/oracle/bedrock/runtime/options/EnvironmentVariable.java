/*
 * File: EnvironmentVariable.java
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
import com.oracle.bedrock.runtime.Platform;

import java.util.Iterator;

/**
 * A {@link Collectable} {@link Option} representing en Environment Variable, consisting
 * of a name and value.
 * <p>
 * Values of a {@link EnvironmentVariable} may be more than simple Strings.   For example,
 * if they are an {@link Iterator}, the value of a Environment Variable will be the next
 * value taken from the {@link Iterator}.   When the value is a
 * {@link ContextSensitiveValue}, the value will be based on a provided {@link Platform}
 * and {@link Option}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @see EnvironmentVariables
 */
public class EnvironmentVariable implements Option.Collectable
{
    /**
     * The name of the {@link EnvironmentVariable}.
     */
    private String name;

    /**
     * The value of the {@link EnvironmentVariable}.
     */
    private Object value;


    /**
     * Constructs a {@link EnvironmentVariable} based on another {@link EnvironmentVariable}.
     *
     * @param property the {@link EnvironmentVariable} from which to construct
     *                 (copy) the new {@link EnvironmentVariable}
     */
    private EnvironmentVariable(EnvironmentVariable property)
    {
        this.name  = property.getName();
        this.value = property.getValue();
    }


    /**
     * Constructs a {@link EnvironmentVariable} with no value.
     *
     * @param name the name of the {@link EnvironmentVariable}
     */
    private EnvironmentVariable(String name)
    {
        this.name  = name;
        this.value = "";
    }


    /**
     * Constructs a {@link EnvironmentVariable}.
     *
     * @param name  the name of the {@link EnvironmentVariable}
     * @param value the value of the {@link EnvironmentVariable}
     */
    private EnvironmentVariable(String name,
                                Object value)
    {
        this.name  = name;
        this.value = value;
    }


    /**
     * Constructs a {@link EnvironmentVariable} with no value.
     *
     * @param name the name of the {@link EnvironmentVariable}
     *
     * @return a new {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(String name)
    {
        return new EnvironmentVariable(name, "");
    }


    /**
     * Constructs a {@link EnvironmentVariable}.
     *
     * @param name  the name of the {@link EnvironmentVariable}
     * @param value the value of the {@link EnvironmentVariable}
     *
     * @return a new {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(String name,
                                         String value)
    {
        return new EnvironmentVariable(name, value);
    }


    /**
     * Constructs a {@link EnvironmentVariable}.
     *
     * @param name  the name of the {@link EnvironmentVariable}
     * @param value the value of the {@link EnvironmentVariable}
     *
     * @return a new {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(String                name,
                                         ContextSensitiveValue value)
    {
        return new EnvironmentVariable(name, value);
    }


    /**
     * Constructs a {@link EnvironmentVariable}.
     *
     * @param name     the name of the {@link EnvironmentVariable}
     * @param iterator the iterator that can provide values for the {@link EnvironmentVariable}
     *
     * @return a new {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(String   name,
                                         Iterator iterator)
    {
        return new EnvironmentVariable(name, iterator);
    }


    /**
     * Constructs a {@link EnvironmentVariable}.
     *
     * @param name   the name of the {@link EnvironmentVariable}
     * @param object the values for the {@link EnvironmentVariable}
     *
     * @return a new {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(String name,
                                         Object object)
    {
        return new EnvironmentVariable(name, object);
    }


    /**
     * Obtains the name of the {@link EnvironmentVariable}.
     *
     * @return the name of the {@link EnvironmentVariable}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains the value of the {@link EnvironmentVariable}.
     *
     * @return the value of the {@link EnvironmentVariable}
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
    public Class<EnvironmentVariables> getCollectorClass()
    {
        return EnvironmentVariables.class;
    }


    /**
     * A context sensitive value, possibly based on the {@link Platform} and/or
     * {@link Application} in which the {@link EnvironmentVariable} is being used.
     */
    public interface ContextSensitiveValue
    {
        /**
         * Obtains the value for the {@link EnvironmentVariable}, possibly based on the provided
         * {@link Platform} and {@link Option}s.
         *
         * @param name      the name of the {@link EnvironmentVariable}
         * @param platform  the {@link Platform} in which {@link EnvironmentVariable} is being used
         * @param options   the {@link Option}s used for launching the {@link Application}
         *
         * @return the value
         */
        Object getValue(String    name,
                        Platform  platform,
                        Option... options);
    }
}
