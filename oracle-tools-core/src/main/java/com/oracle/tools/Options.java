/*
 * File: Options.java
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

package com.oracle.tools;

import java.util.LinkedHashMap;

/**
 * A simple class to manage, look up and keep track of a collection of {@link Option}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Options
{
    /**
     * A map of {@link Option} to a boolean flag, indicating
     * if the option has been used.
     */
    private LinkedHashMap<Option, Boolean> optionsMap;


    /**
     * Constructs a {@link Options}
     *
     * @param options  the {@link Option}s being managed
     */
    public Options(Option... options)
    {
        this.optionsMap = new LinkedHashMap<>();

        if (options != null)
        {
            for (Option option : options)
            {
                this.optionsMap.put(option, false);
            }
        }
    }


    /**
     * Adds an {@link Option} to the collection if and only if it's not already present.
     *
     * @param option  the {@link Option} to add
     *
     * @return  the {@link Options} to permit fluent-style method calls
     */
    public Options addIfAbsent(Option option)
    {
        if (!optionsMap.containsKey(option))
        {
            optionsMap.put(option, false);
        }

        return this;
    }


    /**
     * Obtains the first instance of a specific type of {@link Option} in the collection.
     *
     * @param optionClass  the type of {@link Option} to obtain
     *
     * @return  the {@link Option} of the specified type or <code>null</code> if it's not defined
     */
    public <T extends Option> T get(Class<T> optionClass)
    {
        return get(optionClass, null);
    }


    /**
     * Obtains the first instance of a specific type of {@link Option} in the collection.
     * Should the {@link Option} type not be in the collection, the specified default is returned.
     *
     * @param optionClass          the type of {@link Option} to obtain
     * @param defaultIfNotDefined  the {@link Option} to return if the specified option is not defined
     *
     * @return  the {@link Option} of the specified type or the default if it's not defined
     */
    public <T extends Option> T get(Class<T> optionClass,
                                    T        defaultIfNotDefined)
    {
        for (Option option : optionsMap.keySet())
        {
            if (optionClass.isInstance(option))
            {
                optionsMap.put(option, true);

                return (T) option;
            }
        }

        return defaultIfNotDefined;
    }
}
