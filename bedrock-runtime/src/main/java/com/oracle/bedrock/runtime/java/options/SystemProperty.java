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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A {@link Collectable} {@link Option} representing a System Property, consisting
 * of a name and value.
 * <p>
 * Values of a {@link SystemProperty} may be more than simple Strings.   For example,
 * if they are an {@link Iterator}, the value of a System Property will be the next
 * value taken from the {@link Iterator}.   When the value is a
 * {@link ContextSensitiveValue}, the value will be based on a provided {@link Platform}
 * and {@link OptionsByType}.
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
    private final String name;

    /**
     * The value of the {@link SystemProperty}.
     */
    private final Object value;

    /**
     * The {@link Option}s for this {@link SystemProperty}.
     */
    private final Option[] options;


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param value    the value of the {@link SystemProperty}
     * @param options  then {@link Option}s for this {@link SystemProperty}
     */
    private SystemProperty(String    name,
                           Object    value,
                           Option... options)
    {
        this.name    = name;
        this.value   = value;
        this.options = options;
    }


    /**
     * Constructs a {@link SystemProperty} with no value.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param options  then {@link Option}s for this {@link SystemProperty}
     *
     * @return a new {@link SystemProperty}
     */
    public static SystemProperty of(String    name,
                                    Option... options)
    {
        return new SystemProperty(name, "", options);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param value    the value of the {@link SystemProperty}
     * @param options  then {@link Option}s for this {@link SystemProperty}
     *
     * @return a new {@link SystemProperty}
     */
    public static SystemProperty of(String    name,
                                    String    value,
                                    Option... options)
    {
        return new SystemProperty(name, value, options);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param value    the value of the {@link SystemProperty}
     * @param options  then {@link Option}s for this {@link SystemProperty}
     *
     * @return a new {@link SystemProperty}
     */
    public static SystemProperty of(String                name,
                                    ContextSensitiveValue value,
                                    Option...             options)
    {
        return new SystemProperty(name, value, options);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param value    a {@link Supplier} used to supply the value of the {@link SystemProperty}
     * @param options  then {@link Option}s for this {@link SystemProperty}
     *
     * @return a new {@link SystemProperty}
     */
    public static SystemProperty of(String      name,
                                    Supplier<?> value,
                                    Option...   options)
    {
        return new SystemProperty(name, value, options);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name      the name of the {@link SystemProperty}
     * @param iterator  the iterator that can provide values for the {@link SystemProperty}
     * @param options   then {@link Option}s for this {@link SystemProperty}
     *
     * @return a new {@link SystemProperty}
     */
    public static SystemProperty of(String    name,
                                    Iterator  iterator,
                                    Option... options)
    {
        return new SystemProperty(name, iterator, options);
    }


    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param name     the name of the {@link SystemProperty}
     * @param object   the values for the {@link SystemProperty}
     * @param options  then {@link Option}s for this {@link SystemProperty}
     *
     * @return a new {@link SystemProperty}
     */
    public static SystemProperty of(String    name,
                                    Object    object,
                                    Option... options)
    {
        return new SystemProperty(name, object, options);
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


    /**
     * Obtain the {@link OptionsByType}s for this {@link SystemProperty}.
     *
     * @return  the {@link OptionsByType}s for this {@link SystemProperty}
     */
    public OptionsByType getOptions()
    {
        return OptionsByType.of(options);
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
     * A context sensitive value, possibly based on the {@link Platform} and/or {@link Option}s in
     * which the {@link SystemProperty} is being used.
     */
    public interface ContextSensitiveValue
    {
        /**
         * Obtains the value for the {@link SystemProperty}, possibly based on the provided
         * {@link Platform} and {@link Option}s.
         *
         * @param name           the name of the {@link SystemProperty}
         * @param platform       the {@link Platform} in which {@link SystemProperty} is being used.
         * @param optionsByType  the {@link OptionsByType}
         *
         * @return the value
         */
        Object resolve(String        name,
                       Platform      platform,
                       OptionsByType optionsByType);
    }


    /**
     * A handler that is called whenever an {@link SystemProperty}'s value(s) are
     * resolved.
     */
    @FunctionalInterface
    public interface ResolveHandler
    {
        /**
         * Called by a {@link SystemProperty} instance for whenever its value(s) are resolved.
         *
         * @param name           the name of the argument (may be null if no name was specified)
         * @param value          the resolved value of the system property
         * @param optionsByType  the {@link OptionsByType} used to resolve the values
         */
        void onResolve(String        name,
                       String        value,
                       OptionsByType optionsByType);
    }
}
