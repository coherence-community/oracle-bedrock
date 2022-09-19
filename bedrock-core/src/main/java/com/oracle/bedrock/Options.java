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

package com.oracle.bedrock;

import com.oracle.bedrock.annotations.Internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * An internal implementation of an {@link OptionsByType}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
class Options implements OptionsByType
{
    /**
     * A map of the {@link Options} values, keyed by their concrete class.
     */
    private final LinkedHashMap<Class<? extends Option>, Option> options;


    /**
     * Constructs a {@link Options} given an array of {@link Option}s
     *
     * @param options the {@link Option}s being managed
     */
    Options(Option... options)
    {
        this.options = new LinkedHashMap<>();

        if (options != null)
        {
            for (Option option : options)
            {
                add(option);
            }
        }
    }


    /**
     * A copy constructor that creates an {@link Options} containing all
     * of the {@link Option}s from the specified {@link Options} instance.
     *
     * @param optionsByType the {@link OptionsByType} to copy
     */
    Options(OptionsByType optionsByType)
    {
        this.options = new LinkedHashMap<>();

        addAll(optionsByType);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends Option> T get(Class<T>  classOfOption,
                                    Object... arguments)
    {
        if (classOfOption == null)
        {
            return null;
        }
        else
        {
            T option = (T) options.get(classOfOption);

            if (option == null)
            {
                option = getDefaultFor(classOfOption, arguments);

                add(option);
            }

            return option;
        }
    }


    @Override
    public <T extends Option> Optional<T> optionally(Class<T> classOfOption, Object... arguments) {
        return Optional.ofNullable(get(classOfOption, arguments));
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Option, D extends T> T getOrDefault(Class<T> classOfOption,
                                                          D        defaultOption)
    {
        if (classOfOption == null)
        {
            return null;
        }
        else
        {
            T option = (T) options.get(classOfOption);

            if (option == null)
            {
                option = defaultOption;
            }

            return option;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends Option, D extends T> T getOrSetDefault(Class<T> classOfOption,
                                                             D        defaultOption)
    {
        if (classOfOption == null)
        {
            return null;
        }
        else
        {
            T option = (T) options.get(classOfOption);

            if (option == null && defaultOption != null)
            {
                option = defaultOption;

                add(option);
            }

            return option;
        }
    }


    @Override
    public boolean contains(Class<? extends Option> classOfOption)
    {
        return get(classOfOption) != null;
    }


    @Override
    public boolean contains(Option option)
    {
        Class<? extends Option> classOfOption = option.getClass();

        return get(classOfOption).equals(option);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        ArrayList<O> result = new ArrayList<>();

        for (Option option : options.values())
        {
            if (requiredClass.isInstance(option))
            {
                result.add((O) option);
            }

            if (option instanceof Option.Collector)
            {
                for (O o : ((Option.Collector<?, ?>) option).getInstancesOf(requiredClass))
                {
                    result.add(o);
                }
            }
        }

        return result;
    }


    @Override
    public Option[] asArray()
    {
        Option[] aOptions = new Option[options.size()];
        int      i        = 0;

        for (Option option : options.values())
        {
            aOptions[i++] = option;
        }

        return aOptions;
    }


    @Override
    public String toString()
    {
        StringBuilder bldrResult = new StringBuilder();

        bldrResult.append("Options{");

        boolean fFirst = true;

        for (Option option : options.values())
        {
            if (fFirst)
            {
                fFirst = false;
            }
            else
            {
                bldrResult.append(", ");
            }

            bldrResult.append(option);
        }

        bldrResult.append("}");

        return bldrResult.toString();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public OptionsByType add(Option option)
    {
    if (option != null)
        {
        if (option instanceof Option.Collectable)
            {
            Option.Collectable collectable = (Option.Collectable) option;

            // determine the type of Collector in which we'll collect the Collectable
            Class<? extends Option> classOfCollector = OptionsByType.getClassOf(collectable.getCollectorClass());

            // attempt to locate an existing Collector
            Option.Collector<Option.Collectable, ?> collector
                    = (Option.Collector<Option.Collectable, ?>) options.get(classOfCollector);

            // create a new collector if we don't have one
            if (collector == null)
                {
                // attempt to create a new collector (using the @Option.Default annotation)
                collector = (Option.Collector<Option.Collectable, ?>) getDefaultFor(classOfCollector);
                }

            if (collector == null)
                {
                throw new IllegalStateException("Failed to instantiate a default Collector of type "
                                                        + classOfCollector + " for " + option);
                }
            else
                {
                // collect the collectable into the collector
                collector = collector.with(collectable);

                // replace the collector in the options
                options.put(classOfCollector, collector);
                }
            }
        else
            {
            // determine the class of option
            Class<? extends Option> classOfOption = OptionsByType.getClassOf(option);

            // compose the option if it's composable
            if (option instanceof ComposableOption)
                {
                Option existing = options.get(classOfOption);

                if (existing != null)
                    {
                    option = ((ComposableOption) existing).compose((ComposableOption) option);
                    }
                }

            options.put(classOfOption, option);
            }

        }
    return this;
    }


    @Override
    public OptionsByType addIfAbsent(Option option)
    {
        Class<? extends Option> classOfOption = OptionsByType.getClassOf(option);

        if (!options.containsKey(classOfOption))
        {
            add(option);
        }

        return this;
    }


    @Override
    public OptionsByType addAll(Option... options)
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


    @Override
    public OptionsByType addAll(OptionsByType options)
    {
        for (Option option : options.asArray())
        {
            add(option);
        }

        return this;
    }


    @Override
    public <T extends Option> boolean remove(Class<T> classOfOption)
    {
        if (classOfOption == null)
        {
            return false;
        }
        else
        {
            Option option = options.remove(OptionsByType.getClassOf(classOfOption));

            return option != null;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Option option)
    {
        if (option == null)
        {
            return false;
        }
        else
        {
            if (option instanceof Option.Collectable)
            {
                Option.Collectable collectable = (Option.Collectable) option;

                // determine the type of Collector
                Class<? extends Option> classOfCollector = OptionsByType.getClassOf(collectable.getCollectorClass());

                // attempt to locate an existing Collector
                Option.Collector<Option.Collectable, ?> collector
                        = (Option.Collector<Option.Collectable, ?>) options.get(classOfCollector);

                if (collector == null)
                {
                    return false;
                }
                else
                {
                    // remove the collectable
                    collector = collector.without(collectable);

                    // replace the collector
                    options.put(classOfCollector, collector);

                    return true;
                }
            }
            else
            {
                Class<? extends Option> classOfOption  = OptionsByType.getClassOf(option);

                Option                  existingOption = get(classOfOption);

                if (existingOption == null ||!existingOption.equals(option))
                {
                    return false;
                }
                else
                {
                    options.remove(classOfOption);

                    return true;
                }
            }
        }
    }


    /**
     * Attempts to determine a "default" value for a given class.
     *
     * <p>An attempt is made to determine a suitable default based on the use
     * of the {@link Default} annotation in the specified class, firstly by
     * looking for and evaluating the annotated "public static U getter()"
     * method (using the provided arguments if supplied), failing that,
     * looking for and evaluating the annotated "public static U value = ...;"
     * field, failing that, looking for and evaluating the annotated public
     * constructor (using the provided arguments if supplied) and finally, failing
     * that, looking for an annotated field on an enum
     * (assuming the class is an enum).  Failing these approaches,
     * <code>null</code> is returned.</p>
     *
     * @param classOfOption  the class
     * @param arguments      the optional arguments for static methods / constructors
     * @param <T>            the type of value
     *
     * @return a default value or <code>null</code> if a default can't be
     * determined
     */
    @SuppressWarnings("unchecked")
    protected <T extends Option> T getDefaultFor(Class<T>  classOfOption,
                                                 Object... arguments)
    {
        if (classOfOption == null)
        {
            return null;
        }
        else
        {
            for (Method method : classOfOption.getMethods())
            {
                int modifiers = method.getModifiers();

                if (method.getAnnotation(Default.class) != null
                    && method.getParameterTypes().length == arguments.length
                    && Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers)
                    && classOfOption.isAssignableFrom(method.getReturnType()))
                {
                    try
                    {
                        return (T) method.invoke(null, arguments);
                    }
                    catch (Exception e)
                    {
                        // carry on... perhaps we can use another approach?
                    }
                }
            }
        }

        for (Field field : classOfOption.getFields())
        {
            int modifiers = field.getModifiers();

            if (field.getAnnotation(Default.class) != null
                && Modifier.isStatic(modifiers)
                && Modifier.isPublic(modifiers)
                && classOfOption.isAssignableFrom(field.getType()))
            {
                try
                {
                    return (T) field.get(null);
                }
                catch (Exception e)
                {
                    // carry on... perhaps we can use another approach?
                }
            }
        }

        for (Constructor<?> constructor : classOfOption.getConstructors())
        {
            int modifiers = constructor.getModifiers();

            if (constructor.getAnnotation(Default.class) != null
                && Modifier.isPublic(modifiers)
                && constructor.getParameterTypes().length == arguments.length)
            {
                try
                {
                    return (T) constructor.newInstance(arguments);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    // carry on... perhaps we can use another approach?
                }
            }
        }

        // couldn't find a default so let's return null
        return null;
    }
}
