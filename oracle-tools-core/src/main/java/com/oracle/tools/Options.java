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

import java.lang.reflect.Modifier;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;

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
                add(option);
            }
        }
    }


    /**
     * Adds an {@link Option} to the collection if and only if an {@link Option} of the
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
     * Adds an {@link Option} to the collection.  If the {@link Option} is composable
     * (ie: it implements {@link ComposableOption}), the {@link Option} will be composed with
     * an existing {@link Option} of the same type.  If the {@link Option} is not composable,
     * it will replace the existing {@link Option} of the same type.
     *
     * @param option  the {@link Option} to add
     *
     * @return  the {@link Options} to permit fluent-style method calls
     */
    public Options add(Option option)
    {
        // determine the class of the option
        Class<Option> classOfOption = getClassOfOption(option);

        // add the option, composing it with an existing option if necessary
        Option existingOption = this.options.get(classOfOption);

        if (existingOption instanceof ComposableOption)
        {
            Option composedOption = ((ComposableOption) existingOption).compose((ComposableOption) option);

            options.put(classOfOption, composedOption);
        }
        else
        {
            options.put(classOfOption, option);
        }

        // make note that we're not using the option yet as it's new/changed
        usedOptions.remove(option);

        return this;
    }


    /**
     * Adds a collection of {@link Option}s to the collection.  If an {@link Option} is composable
     * (ie: it implements {@link ComposableOption}), the {@link Option} will be composed with
     * an existing {@link Option} of the same type.  If an {@link Option} is not composable,
     * it will replace the existing {@link Option} of the same type.
     *
     * @param options  the {@link Option}s to add
     *
     * @return  the {@link Options} to permit fluent-style method calls
     */
    public Options addAll(Option... options)
    {
        if (options != null)
        {
            for (Option option : options)
            {
                add(option);
            }
        }

        return this;
    }


    /**
     * Replaces an existing type of {@link Option} with a new {@link Option}, regardless
     * of whether it is composable or not (ie: it implements {@link ComposableOption}).
     *
     * @param option  the new {@link Option}, replacing any existing {@link Option}
     *                of the same type
     *
     * @return  the {@link Options} to permit fluent-style method calls
     */
    public Options replace(Option option)
    {
        // determine the class of the option
        Class<Option> classOfOption = getClassOfOption(option);

        // overwrite the existing option (if there is one)
        options.put(classOfOption, option);

        // make note that we're not using the option yet as it's new/changed
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
     * Obtains the specific type of {@link Option} in the collection.
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
     * Obtains the specific type of {@link Option} in the collection.
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
     * Obtains an {@link Iterable} over all of the {@link Option}s
     * that are currently an instance of the specified class.
     *
     * @param instanceOf  the type of the required {@link Option}s
     *
     * @return  the {@link Option}s of the specified class
     */
    public <T> Iterable<T> getAll(Class<T> instanceOf)
    {
        LinkedList<T> list = new LinkedList<T>();

        for (Option option : options.values())
        {
            if (instanceOf.isInstance(option))
            {
                list.add((T) option);
            }
        }

        return list;
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


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("Options{");

        boolean first = true;

        for (Option option : options.values())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                builder.append(", ");
            }

            builder.append(option);
        }

        builder.append("}");

        return builder.toString();
    }


    /**
     * Obtains the concrete type that directly implements / extends the specified {@link Option}.
     *
     * @param option  the {@link Option}
     *
     * @return  the concrete {@link Class} that directly extends / implements the {@link Option} interface
     *          or <code>null</code> if the {@link Option} is <code>null</code>
     */
    public static Class<Option> getClassOfOption(Option option)
    {
        return option == null ? null : getClassOfOption(option.getClass());
    }


    /**
     * Obtains the concrete type that directly implements / extends the {@link Option} interface,
     * implemented by the specified class.
     *
     * @param aClass  the class that somehow implements the {@link Option} interface
     *
     * @return  the concrete {@link Class} that directly extends / implements the {@link Option} interface
     *          or <code>null</code> if the specified {@link Class} doesn't implement {@link Option}
     */
    public static Class<Option> getClassOfOption(Class<?> aClass)
    {
        // the hierarchy of classes we've visited
        // (so that we can traverse it later to find non-abstract classes)
        Stack<Class<?>> hierarchy = new Stack<>();

        while (aClass != null)
        {
            // remember the current class
            hierarchy.push(aClass);

            for (Class<?> interfaceClass : aClass.getInterfaces())
            {
                if (Option.class.equals(interfaceClass) || ComposableOption.class.equals(interfaceClass))
                {
                    // when the Option/ComposableOption is directly implemented by a class,
                    // we return the first non-abstract class in the hierarchy.
                    while (aClass != null && Modifier.isAbstract(aClass.getModifiers()) &&!aClass.isInterface())
                    {
                        aClass = hierarchy.isEmpty() ? null : hierarchy.pop();
                    }

                    return (Class<Option>) aClass;
                }
                else if (Option.class.isAssignableFrom(interfaceClass))
                {
                    // ensure that we have a concrete class in our hierarchy
                    while (aClass != null && Modifier.isAbstract(aClass.getModifiers()) &&!aClass.isInterface())
                    {
                        aClass = hierarchy.isEmpty() ? null : hierarchy.pop();
                    }

                    if (aClass == null)
                    {
                        // when the hierarchy is entirely abstract, we can't determine a concrete Option type
                        return null;
                    }
                    else
                    {
                        // when the Option is a super class of an interface,
                        // we return the interface that's directly extending it.

                        // TODO: we should search to find the interface that is directly
                        // extending Option (that is not a ComposableOption),
                        // and not just assume that the interfaceClass is directly implementing it
                        return (Class<Option>) interfaceClass;
                    }
                }
            }

            aClass = aClass.getSuperclass();
        }

        return null;
    }
}
