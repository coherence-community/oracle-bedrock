/*
 * File: OptionsByType.java
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.Stack;

/**
 * A collection of zero or more {@link Option}s, keyed by the concrete type of
 * each {@link Option} in the collection.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface OptionsByType
{
    /**
     * Constructs a new {@link OptionsByType} based on the specified {@link Option}s.
     *
     * @param options  the {@link Option}s
     *
     * @return an {@link OptionsByType}
     */
    @SafeVarargs
    static OptionsByType of(Option... options)
    {
        return new Options(options);
    }


    /**
     * Constructs a new {@link OptionsByType} based on another {@link OptionsByType}
     *
     * @param optionsByType  the {@link OptionsByType}s
     *
     * @return  a new {@link OptionsByType} containing the {@link Option}s in the
     *          specified {@link OptionsByType} parameter
     */
    static OptionsByType of(OptionsByType optionsByType)
    {
        return new Options(optionsByType);
    }


    /**
     * Constructs an empty {@link OptionsByType} collection.
     *
     * @return an empty {@link OptionsByType} collection
     */
    static OptionsByType empty()
    {
        return new Options();
    }


    /**
     * Obtains the {@link Option} for a specified concrete type from the collection.
     *
     * <p>Should the {@link Option} not exist in the collection, an attempt is made
     * to determine an annotated {@link Default}.
     *
     * @param classOfOption  the concrete type of {@link Option} to obtain
     * @param arguments      the optional arguments for determining the default
     * @param <T>            the type of value
     *
     * @return the {@link Option} of the specified type or if undefined, the
     *         suitable default value (or <code>null</code> if one can't be determined)
     */
    <T extends Option> T get(Class<T>  classOfOption,
                             Object... arguments);


    /**
     * Obtains the {@link Option} of a specified concrete type from the collection.  Should the type of
     * {@link Option} not exist, the specified default is added to the collection and returned.
     *
     * @param <T>            the type of value
     * @param <D>            the type of the default value
     *
     * @param classOfOption  the type of {@link Option} to obtain
     * @param defaultOption  the {@link Option} to return if the specified type is not defined
     *
     * @return the option of the specified type or the default if it's not defined
     */
    <T extends Option, D extends T> T getOrDefault(Class<T> classOfOption,
                                                   D        defaultOption);


    /**
     * Determines if an {@link Option} of the specified concrete type is in the
     * collection.
     *
     * @param classOfOption the class of {@link Option}
     *
     * @return <code>true</code> if the class of {@link Option} is in the {@link OptionsByType}
     * <code>false</code> otherwise
     */
    boolean contains(Class<? extends Option> classOfOption);


    /**
     * Determines if the specified {@link Option} (and type) is in the {@link OptionsByType}.
     *
     * @param option the {@link Option}
     *
     * @return <code>true</code> if the {@link Option} is defined,
     * <code>false</code> otherwise
     */
    boolean contains(Option option);


    /**
     * Obtains an {@link Iterable} over all of the {@link Option}s in the collection
     * that are an instance of the specified class.
     *
     * @param requiredClass the required class
     * @param <O>           the type of option
     *
     * @return the options of the required class
     */
    <O> Iterable<O> getInstancesOf(Class<O> requiredClass);


    /**
     * Obtains the current collection of {@link Option}s as an array.
     *
     * @return an array of options
     */
    Option[] asArray();


    /**
     * Adds an option to the collection, replacing an
     * existing {@link Option} of the same concrete type if one exists.
     *
     * @param option the {@link Option} to add
     *
     * @return the {@link OptionsByType} to permit fluent-style method calls
     */
    OptionsByType add(Option option);


    /**
     * Adds an {@link Option} to the collection if and only if an {@link Option} of the
     * same type is not already present.
     *
     * @param option the {@link Option} to add
     *
     * @return the {@link OptionsByType} to permit fluent-style method calls
     */
    OptionsByType addIfAbsent(Option option);


    /**
     * Adds an array of {@link Option}s to the collection.
     *
     * @param options the {@link Option}s to add
     *
     * @return the {@link OptionsByType} to permit fluent-style method calls
     */
    OptionsByType addAll(Option... options);


    /**
     * Adds all of the {@link OptionsByType} in the specified {@link OptionsByType}
     * to this collection.
     *
     * @param options the {@link OptionsByType} to add
     *
     * @return the {@link OptionsByType} to permit fluent-style method calls
     */
    OptionsByType addAll(OptionsByType options);


    /**
     * Removes the specified type of {@link Option}
     *
     * @param classOfOption  the class of {@link Option}
     * @param <T>            the type of the {@link Option}
     *
     * @return <code>true</code> if the {@link Option} was removed,
     *         <code>false</code> otherwise
     */
    <T extends Option> boolean remove(Class<T> classOfOption);


    /**
     * Removes the specific {@link Option}
     *
     * @param option the {@link Option} to remove
     *
     * @return <code>true</code> if the {@link Option} was removed,
     * <code>false</code> otherwise, perhaps because it wasn't defined
     */
    boolean remove(Option option);


    /**
     * Obtains the concrete type that directly implements / extends the specified {@link Option}.
     *
     * @param option the {@link Option}
     *
     * @return the concrete {@link Class} that directly extends / implements the {@link Option} interface
     * or <code>null</code> if the {@link Option} is <code>null</code>
     */
    static Class<? extends Option> getClassOf(Option option)
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
    static Class<? extends Option> getClassOf(Class<?> classOfOption)
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
     * Defines how an {@link OptionsByType} collection may automatically determine a
     * suitable default value for a specific class of option at runtime
     * when the said option does not exist in an {@link OptionsByType} collection.
     *
     * For example, the {@link Default} annotation can be used to specify that
     * a public static no-args method can be used to determine a default value.
     * <pre><code>
     * public class Color {
     *     ...
     *     &#64;OptionsByType.Default
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
     *     &#64;OptionsByType.Default
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
     *     &#64;OptionsByType.Default
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
     *     &#64;OptionsByType.Default
     *     BLUE;   // blue is the default color
     * }
     * </code></pre>
     *
     * @see OptionsByType#get(Class, Object...)
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
    @interface Default
    {
    }
}
