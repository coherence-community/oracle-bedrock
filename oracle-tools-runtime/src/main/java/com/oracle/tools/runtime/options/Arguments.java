/*
 * File: Arguments.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.lang.ExpressionEvaluator;

import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.java.JavaApplicationSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An immutable {@link Collector} of {@link Argument}s, used to define a collection
 * of command line arguments for an application.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Arguments implements Option.Collector<Argument, Arguments>
{
    private static final Arguments EMPTY = new Arguments();

    private final List<Argument> arguments;

    private Arguments()
    {
        arguments = new ArrayList<>();
    }


    /**
     * Creates {@link List} of {@link String} values representing the command line arguments to pass to
     * an application
     * <p>
     * If the value of a {@link Argument} is defined as an {@link Iterator}, the next value from the
     * said {@link Iterator} will be used as a argument value. If the value of a {@link Argument} is
     * defined as a {@link Argument.ContextSensitiveArgument}, the
     * {@link Argument.ContextSensitiveArgument#getValue(Platform, ApplicationSchema)} method is called
     * to resolve the value.
     *
     * @param platform        the target {@link Platform} for the returned {@link List} of arguments
     * @param schema          the target {@link JavaApplicationSchema} for the returned {@link List} of arguments
     * @param realizeOptions  the {@link Option}s for realizing the {@link List} of arguments
     *
     * @return a new {@link List} of application command line arguments
     */
    public List<String> realize(Platform          platform,
                                ApplicationSchema schema,
                                Option...         realizeOptions)
    {
        Options             options   = new Options(realizeOptions);
        ExpressionEvaluator evaluator = new ExpressionEvaluator(options);
        List<String>        argList   = new ArrayList<>();

        for (Argument argument : this.arguments)
        {
            String name      = argument.getName();
            char   separator = argument.getSeparator();
            String value     = argument.realizeValue(platform, schema, evaluator);

            if (name != null && !name.isEmpty())
            {
                if (value != null && !value.isEmpty())
                {
                    if (separator == ' ')
                    {
                        argList.add(name);
                        argList.add(value);
                    }
                    else
                    {
                        argList.add(name + separator + value);
                    }
                }
            }
            else if (value != null && !value.isEmpty())
            {
                argList.add(value);
            }
        }

        return argList;
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the specified arguments
     * appended.
     *
     * @param arguments  the arguments to add
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the specified arguments
     *          appended
     */
    public Arguments with(Object... arguments)
    {
        return with(Arrays.asList(arguments));
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the specified arguments
     * appended.
     *
     * @param arguments  the arguments to add
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the specified arguments
     *          appended
     */
    public Arguments with(List<?> arguments)
    {
        Arguments newArguments = new Arguments();

        newArguments.arguments.addAll(this.arguments);

        for (Object argument : arguments)
        {
            if (argument instanceof Argument)
            {
                newArguments.arguments.add((Argument) argument);
            }
            else
            {
                newArguments.arguments.add(new Argument(argument));
            }
        }

        return newArguments;
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the specified arguments
     * appended.
     *
     * @param arguments  the arguments to add
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the specified arguments
     *          appended
     */
    public Arguments with(Argument... arguments)
    {
        return with(Arrays.asList(arguments));
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the specified argument
     * appended.
     *
     * @param argument  the {@link Argument} to add
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the specified argument
     *          appended
     */
    @Override
    public Arguments with(Argument argument)
    {
        Arguments newArguments = new Arguments();

        newArguments.arguments.addAll(this.arguments);
        newArguments.arguments.add(argument);

        return newArguments;
    }


    /**
     * Create a new {@link Arguments} instance that is a concatenation
     * of this {@link Arguments} instance with all of the {@link Argument}s
     * contained in the specified {@link Arguments} instance.
     *
     * @param other  the {@link Arguments} to concatenate with this instcane
     *
     * @return  a new {@link Arguments} instance that is a concatenation
     *          of this {@link Arguments} instance and the specified
     *          {@link Arguments} instance
     */
    public Arguments with(Arguments other)
    {
        Arguments newArguments = new Arguments();

        newArguments.arguments.addAll(this.arguments);
        newArguments.arguments.addAll(other.arguments);

        return newArguments;
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the first occurrence of
     * the specified argument removed.
     *
     * @param argument  the argument to remove
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the first occurrence
     *          of the specified argument removed
     */
    public Arguments without(Object argument)
    {
        Argument arg = (argument instanceof Argument) ? (Argument) argument : new Argument(argument);

        return without(arg);
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the first occurrence of
     * the specified argument removed.
     *
     * @param argument  the argument to remove
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the first occurrence
     *          of the specified argument removed
     */
    @Override
    public Arguments without(Argument argument)
    {
        Arguments newArguments = new Arguments();

        newArguments.arguments.addAll(this.arguments);
        newArguments.arguments.remove(argument);

        return newArguments;
    }


    /**
     * Obtain a new {@link Arguments} that is the same as this
     * {@link Arguments} instance with the all occurrences of
     * any {@link Argument} with the specified name removed.
     *
     * @param name  the name of the argument to remove
     *
     * @return  a new {@link Arguments} that is the same as this
     *          {@link Arguments} instance with the all occurrences
     *          of the specified named {@link Argument} removed
     */
    public Arguments withoutNamed(String name)
    {
        Arguments newArguments = new Arguments();

        for (Argument argument : this.arguments)
        {
            String argName = argument.getName();

            if (!safeEquals(argName, name))
            {
                newArguments.arguments.add(argument);
            }
        }

        return newArguments;
    }


    @Override
    public Iterator<Argument> iterator()
    {
        return Collections.unmodifiableCollection(arguments).iterator();
    }


    /**
     * Replace the first {@link Argument} with the specified name
     * with a new {@link Argument} with the specified value
     *
     * @param name   the name of the argument
     * @param value  the value of the argument
     *
     * @return  a new {@link Arguments} instance with the specified
     *          argument replaced
     */
    public Arguments replace(String name, Object value)
    {
        Argument argument = Argument.of(name, value);

        return replace(argument);
    }


    /**
     * Replace the first {@link Argument} with the same name
     * as the specified {@link Argument } with the specified
     * {@link Argument}.
     *
     * @param argument  the {@link Argument} to use to replace
     *                  an existing argument
     *
     * @return  a new {@link Arguments} instance with the specified
     *          argument replaced
     */
    public Arguments replace(Argument argument)
    {
        String    name         = argument.getName();
        Arguments newArguments = new Arguments();
        boolean   replaced     = false;

        for (Argument arg : this.arguments)
        {
            if (!replaced && safeEquals(arg.getName(), name))
            {
                newArguments.arguments.add(argument);

                replaced = true;
            }
            else
            {
                newArguments.arguments.add(arg);
            }
        }

        if (!replaced)
        {
            newArguments.arguments.add(argument);
        }

        return newArguments;
    }


    private boolean safeEquals(String s1, String s2)
    {
        if (s1 == null && s2 == null)
        {
            return true;
        }

        if (s1 != null && s2 == null)
        {
            return false;
        }

        return s1 != null && s1.equals(s2);
    }


    /**
     * Create an {@link Arguments} instance with the specified
     * arguments.
     *
     * @param arguments  the argument values
     *
     * @return  an {@link Arguments} instance
     */
    public static Arguments of(Object... arguments)
    {
        return Arguments.of(Arrays.asList(arguments));
    }


    /**
     * Create an {@link Arguments} instance with the specified
     * arguments.
     *
     * @param arguments  the argument values
     *
     * @return  an {@link Arguments} instance
     */
    public static Arguments of(Argument... arguments)
    {
        return EMPTY.with(arguments);
    }


    /**
     * Create an {@link Arguments} instance with the specified
     * arguments.
     *
     * @param arguments  the argument values
     *
     * @return  an {@link Arguments} instance
     */
    public static Arguments of(List<?> arguments)
    {
        return EMPTY.with(arguments);
    }


    /**
     * Create an empty {@link Arguments} instance.
     *
     * @return  an empty {@link Arguments} instance
     */
    @Options.Default
    public static Arguments empty()
    {
        return EMPTY;
    }
}
