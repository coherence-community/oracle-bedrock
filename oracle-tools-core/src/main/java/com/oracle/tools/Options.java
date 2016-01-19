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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * A collection of zero or more {@link Option}s, internally arranged as a map,
 * keyed by the concrete type of each {@link Option} in the collection.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Options
{
    /**
     * A map of the {@link Options} values, keyed by their concrete class.
     */
    private LinkedHashMap<Class<? extends Option>, Option> options;


    /**
     * Constructs a {@link Options} given an array of {@link Option}s
     *
     * @param options the {@link Option}s being managed
     */
    public Options(Option... options)
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
     * Obtains the {@link Option} for a specified concrete type from the collection.
     *
     * <p>Should the {@link Option} not exist in the collection, an attempt is made
     * to determine a suitable default based on the use of the {@link Default}
     * annotation in the specified class, firstly by looking for and evaluating
     * the annotated "public static T getter()" method, failing that, looking for
     * and evaluating the annotated "public static T value = ...;" field, failing
     * that, looking for an evaluating the annotated public no args constructor
     * and finally, failing that, looking for an annotated field on an enum
     * (assuming the class is an enum).  Failing these approaches,
     * <code>null</code> is returned.</p>
     *
     * @param classOfOption the concrete type of {@link Option} to obtain
     * @param <T>           the type of value
     *
     * @return the {@link Option} of the specified type or if undefined, the
     * suitable default value (or <code>null</code> if one can't be
     * determined)
     */
    public <T extends Option> T get(Class<T> classOfOption)
    {
        return get(classOfOption, getDefaultFor(classOfOption));
    }


    /**
     * Obtains the {@link Option} of a specified concrete type from the collection.
     * <p>
     * Should the type of {@link Option} not exist, the specified default is returned.
     *
     * @param classOfOption the type of {@link Option} to obtain
     * @param defaultOption the {@link Option} to return if the specified type is not defined
     * @param <T>           the type of value
     *
     * @return the option of the specified type or
     * the default if it's not defined
     */
    public <T extends Option> T get(Class<T> classOfOption,
                                    T        defaultOption)
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
                return defaultOption;
            }
            else
            {
                return option;
            }
        }
    }


    /**
     * Determines if an {@link Option} of the specified concrete type is in the
     * collection.
     *
     * @param classOfOption the class of {@link Option}
     *
     * @return <code>true</code> if the class of {@link Option} is in the {@link Options}
     * <code>false</code> otherwise
     */
    public boolean contains(Class<? extends Option> classOfOption)
    {
        return get(classOfOption) != null;
    }


    /**
     * Determines if the specified {@link Option} (and type) is in the {@link Options}.
     *
     * @param option the {@link Option}
     *
     * @return <code>true</code> if the {@link Option} is defined,
     * <code>false</code> otherwise
     */
    public boolean contains(Option option)
    {
        Class<? extends Option> classOfOption = option.getClass();

        return get(classOfOption).equals(option);
    }


    /**
     * Obtains an {@link Iterable} over all of the {@link Option}s in the collection
     * that are an instance of the specified class.
     *
     * @param requiredClass the required class
     * @param <O>           the type of option
     *
     * @return the options of the required class
     */
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
                for (Option collectable : ((Option.Collector<?, ?>) option))
                {
                    if (requiredClass.isInstance(collectable))
                    {
                        result.add((O) collectable);
                    }
                }
            }
        }

        return result;
    }


    /**
     * Obtains the current collection of {@link Option}s as an array.
     *
     * @return an array of options
     */
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


    /**
     * Constructs an {@link Options} collection given an array of {@link Option}s
     *
     * @param options the array of {@link Option}s
     *
     * @return an {@link Options} collection
     */
    @SafeVarargs
    public static Options from(Option... options)
    {
        return new Options(options);
    }


    /**
     * Adds an option to the collection, replacing an
     * existing {@link Option} of the same concrete type if one exists.
     *
     * @param option the {@link Option} to add
     *
     * @return the {@link Options} to permit fluent-style method calls
     */
    public Options add(Option option)
    {
        if (option == null)
        {
            return this;
        }
        else
        {
            if (option instanceof Option.Collectable)
            {
                Option.Collectable collectable = (Option.Collectable) option;

                // determine the type of Collector in which we'll collect the Collectable
                Class<? extends Option> classOfCollector = getClassOf(collectable.getCollectorClass());

                // attempt to locate an existing Collector
                Option.Collector collector = (Option.Collector) options.get(classOfCollector);

                // create a new collector if we don't have one
                if (collector == null)
                {
                    // attempt to create a new collector (using the @Option.Default annotation)
                    collector = (Option.Collector) getDefaultFor(classOfCollector);
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
                Class<? extends Option> classOfOption = getClassOf(option);

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

            return this;
        }
    }


    /**
     * Adds an {@link Option} to the collection if and only if an {@link Option} of the
     * same type is not already present.
     *
     * @param option the {@link Option} to add
     *
     * @return the {@link Options} to permit fluent-style method calls
     */
    public Options addIfAbsent(Option option)
    {
        Class<? extends Option> classOfOption = getClassOf(option);

        if (!options.containsKey(classOfOption))
        {
            add(option);
        }

        return this;
    }


    /**
     * Adds an array of {@link Option}s to the collection.
     *
     * @param options the {@link Option}s to add
     *
     * @return the {@link Options} to permit fluent-style method calls
     */
    public Options addAll(Option[] options)
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
     * Adds all of the {@link Options} in the specified {@link Options}
     * to this collection.
     *
     * @param options the {@link Options} to add
     *
     * @return the {@link Options} to permit fluent-style method calls
     */
    public Options addAll(Options options)
    {
        for (Option option : options.asArray())
        {
            add(option);
        }

        return this;
    }


    /**
     * Removes the specified type of {@link Option}
     *
     * @param classOfOption the class of {@link Option}
     *
     * @return <code>true</code> if the {@link Option} was removed,
     * <code>false</code> otherwise
     */
    public <T extends Option> boolean remove(Class<T> classOfOption)
    {
        if (classOfOption == null)
        {
            return false;
        }
        else
        {
            Option option = options.remove(getClassOf(classOfOption));

            return option != null;
        }
    }


    /**
     * Removes the specific {@link Option}
     *
     * @param option the {@link Option} to remove
     *
     * @return <code>true</code> if the {@link Option} was removed,
     * <code>false</code> otherwise, perhaps because it wasn't defined
     */
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
                Class<? extends Option> classOfCollector = getClassOf(collectable.getCollectorClass());

                // attempt to locate an existing Collector
                Option.Collector collector = (Option.Collector) options.get(classOfCollector);

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
                Class<? extends Option> classOfOption  = getClassOf(option);

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
     * Obtains the concrete type that directly implements / extends the specified {@link Option}.
     *
     * @param option the {@link Option}
     *
     * @return the concrete {@link Class} that directly extends / implements the {@link Option} interface
     * or <code>null</code> if the {@link Option} is <code>null</code>
     */
    public static Class<? extends Option> getClassOf(Option option)
    {
        return option == null ? null : getClassOf(option.getClass());
    }


    /**
     * Obtains the concrete type that directly implements / extends the {@link Option} interface,
     * implemented by the specified class.
     *
     * @param classOfOption the class that somehow implements the {@link Option} interface
     *
     * @return the concrete {@link Class} that directly extends / implements the {@link Option} interface
     * or <code>null</code> if the specified {@link Class} doesn't implement {@link Option}
     */
    public static Class<? extends Option> getClassOf(Class<?> classOfOption)
    {
        // the hierarchy of classes we've visited
        // (so that we can traverse it later to find non-abstract classes)
        Stack<Class<?>> hierarchy = new Stack<>();

        while (classOfOption != null)
        {
            // remember the current class
            hierarchy.push(classOfOption);

            for (Class<?> interfaceClass : classOfOption.getInterfaces())
            {
                if (Option.class.equals(interfaceClass)
                    || ComposableOption.class.equals(interfaceClass)
                    || Option.Collector.class.equals(interfaceClass))
                {
                    // when the Option/ComposableOption is directly implemented by a class,
                    // we return the first non-abstract class in the hierarchy.
                    while (classOfOption != null
                           && Modifier.isAbstract(classOfOption.getModifiers())
                           &&!classOfOption.isInterface())
                    {
                        classOfOption = hierarchy.isEmpty() ? null : hierarchy.pop();
                    }

                    return (Class<? extends Option>) classOfOption;
                }
                else if (Option.class.isAssignableFrom(interfaceClass))
                {
                    // ensure that we have a concrete class in our hierarchy
                    while (classOfOption != null
                           && Modifier.isAbstract(classOfOption.getModifiers())
                           &&!classOfOption.isInterface())
                    {
                        classOfOption = hierarchy.isEmpty() ? null : hierarchy.pop();
                    }

                    if (classOfOption == null)
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

            classOfOption = classOfOption.getSuperclass();
        }

        return null;
    }


    /**
     * Attempts to determine a "default" value for a given class.
     *
     * <p>Aan attempt is made to determine a suitable default based on the use
     * of the {@link Default} annotation in the specified class, firstly by
     * looking for and evaluating the annotated "public static U getter()"
     * method, failing that, looking for and evaluating the annotated
     * "public static U value = ...;" field, failing that, looking for an
     * evaluating the annotated public no args constructor and finally, failing
     * that, looking for an annotated field on an enum
     * (assuming the class is an enum).  Failing these approaches,
     * <code>null</code> is returned.</p>
     *
     * @param classOfOption the class
     * @param <T>           the type of value
     *
     * @return a default value or <code>null</code> if a default can't be
     * determined
     */
    protected <T extends Option> T getDefaultFor(Class<T> classOfOption)
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
                    && method.getParameterTypes().length == 0
                    && Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers)
                    && classOfOption.isAssignableFrom(method.getReturnType()))
                {
                    try
                    {
                        return (T) method.invoke(null);
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

        try
        {
            Constructor constructor = classOfOption.getConstructor();

            int         modifiers   = constructor.getModifiers();

            if (constructor.getAnnotation(Default.class) != null && Modifier.isPublic(modifiers))
            {
                try
                {
                    return (T) constructor.newInstance();
                }
                catch (Exception e)
                {
                    // carry on... perhaps we can use another approach?
                }
            }
        }
        catch (NoSuchMethodException e)
        {
            // carry on... there's no no-args constructor
        }

        // couldn't find a default so let's return null
        return null;
    }


    /**
     * Defines how an {@link Options} collection may automatically determine a
     * suitable default value for a specific class of option at runtime
     * when the said option does not exist in an {@link Options} collection.
     *
     * For example, the {@link Default} annotation can be used to specify that
     * a public static no-args method can be used to determine a default value.
     * <pre><code>
     * public class Color {
     *     ...
     *     &#64;Options.Default
     *     public static Color getDefault() {
     *         ...
     *     }
     *     ...
     * }
     * </code></pre>
     *
     * Similarly, the {@link Default} annotation can be used to specify a
     * public static field to use for determining a default value.
     * <pre><code>
     * public class Color {
     *     ...
     *     &#64;Options.Default
     *     public static Color BLUE = ...;
     *     ...
     * }
     * </code></pre>
     *
     * Alternatively, the {@link Default} annotation can be used to specify that
     * the public no-args constructor for a public class may be used for
     * constructing a default value.
     * <pre><code>
     * public class Color {
     *     ...
     *     &#64;Options.Default
     *     public Color() {
     *         ...
     *     }
     *     ...
     * }
     * </code></pre>
     *
     * Lastly when used with an enum, the {@link Default} annotation
     * can be used to specify the default enum constant.
     * <pre><code>
     * public enum Color {
     *     RED,
     *
     *     GREEN,
     *
     *     &#64;Options.Default
     *     BLUE;   // blue is the default color
     * }
     * </code></pre>
     *
     * @see Options#get(Class)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
    public @interface Default
    {
    }
}
