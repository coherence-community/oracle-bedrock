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

import java.util.HashSet;
import java.util.LinkedHashMap;

import java.util.logging.Logger;

/**
 * Manages and tracks use of a typed set of {@link Option}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Options
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(Options.class.getName());

    /**
     * A map of the {@link Options}, by their defining class.
     */
    private LinkedHashMap<Class<Option>, Option> options;

    /**
     * The set of {@link Option}s that have been accessed.
     */
    private HashSet<Option> usedOptions;


    /**
     * Constructs a {@link Options} given an array of {@link Option}
     *
     * @param options  the {@link Option}s being managed
     */
    public Options(Option... options)
    {
        this.options     = new LinkedHashMap<>();
        this.usedOptions = new HashSet<>();

        if (options != null)
        {
            for (Option option : options)
            {
                // determine the class of the option
                Class<Option> classOfOption = getClassOfOption(option);

                // add the option
                Option previousOption = this.options.put(classOfOption, option);

                if (previousOption != null)
                {
                    LOGGER.warning("The option [" + option + "] will replace the previously defined option ["
                                   + previousOption + "]");
                }
            }
        }
    }


    /**
     * Adds an {@link Option} to the collection if and only if one of the
     * same type is not already present.
     *
     * @param option  the {@link Option} to add
     *
     * @return  the {@link Options} to permit fluent-style method calls
     */
    public Options addIfAbsent(Option option)
    {
        Class<Option> classOfOption = getClassOfOption(option);

        if (!options.containsKey(classOfOption))
        {
            options.put(classOfOption, option);
        }

        return this;
    }


    /**
     * Adds an {@link Option} to the collection, overriding a previously existing
     * {@link Option} of the same type.
     *
     * @param option  the {@link Option} to add
     *
     * @return  the {@link Options} to permit fluent-style method calls
     */
    public Options add(Option option)
    {
        Class<Option> classOfOption = getClassOfOption(option);

        options.put(classOfOption, option);
        usedOptions.remove(option);

        return this;
    }


    /**
     * Removes the specified type of {@link Option}
     *
     * @param optionClass  the class of {@link Option}
     *
     * @return  <code>true</code> if the {@link Option} was removed,
     *          <code>false</code> otherwise
     */
    public <T extends Option> boolean remove(Class<T> optionClass)
    {
        Class<Option> classOfOption = getClassOfOption(optionClass);

        Option        option        = options.remove(classOfOption);

        usedOptions.remove(option);

        return option != null;
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
        Class<Option> classOfOption = getClassOfOption(optionClass);

        return classOfOption == null ? null : get(optionClass, null);
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
        Class<Option> classOfOption = getClassOfOption(optionClass);

        Option        option        = options.get(classOfOption);

        if (option == null)
        {
            return defaultIfNotDefined;
        }
        else
        {
            usedOptions.add(option);

            return (T) option;
        }
    }


    /**
     * Determines if a class of {@link Option} is being managed
     * by the {@link Options}.
     *
     * @param optionClass  the class of {@link Option}
     *
     * @return  <code>true</code> if the class of {@link Option} is being managed,
     *          <code>false</code> otherwise
     */
    public boolean contains(Class<Option> optionClass)
    {
        return get(optionClass) != null;
    }


    /**
     * Determines if the specified {@link Option} is in the set of
     * managed {@link Option}s.
     *
     * @param option  the {@link Option}
     * @return  <code>true</code> if the {@link Option} is defined,
     *          <code>false</code> otherwise
     */
    public boolean contains(Option option)
    {
        return get(option.getClass()).equals(option);
    }


    /**
     * Obtains the current set of {@link Option}s as an array.
     *
     * @return  an array of {@link Option}
     */
    public Option[] asArray()
    {
        Option[] optionArray = new Option[options.size()];

        int      i           = 0;

        for (Option option : options.values())
        {
            optionArray[i++] = option;
        }

        return optionArray;
    }


    /**
     * Obtains the class of the {@link Option} that directly extends / implements the
     * {@link Option} interface.
     *
     * @param option  the {@link Option}
     *
     * @return  the {@link Class} that directly implements the {@link Option} interface
     *          or <code>null</code> if the {@link Option} is <code>null</code>
     */
    public static Class<Option> getClassOfOption(Option option)
    {
        return option == null ? null : getClassOfOption(option.getClass());
    }


    /**
     * Obtains the class that directly extends / implements the {@link Option} interface.
     *
     * @param aClass  the class that somehow implements the {@link Option} interface
     *
     * @return  the {@link Class} that directly implements the {@link Option} interface
     *          or <code>null</code> if the specified {@link Class} doesn't implement {@link Option}
     */
    public static Class<Option> getClassOfOption(Class<?> aClass)
    {
        while (aClass != null)
        {
            for (Class<?> interfaceClass : aClass.getInterfaces())
            {
                if (Option.class.equals(interfaceClass))
                {
                    // when the Option is directly implemented,
                    // we return the class itself that's implementing it.
                    return (Class<Option>) aClass;
                }
                else if (Option.class.isAssignableFrom(interfaceClass))
                {
                    // when the Option is a super class of an interface,
                    // we return the interface that's directly extending it.

                    // TODO: we should search to find the directly-most interface
                    // that is extending Option, not just assume that the
                    // interfaceClass is directly implementing it
                    return (Class<Option>) interfaceClass;
                }
            }

            aClass = aClass.getSuperclass();
        }

        return null;
    }
}
