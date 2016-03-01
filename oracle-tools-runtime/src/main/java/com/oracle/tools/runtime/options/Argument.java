/*
 * File: Argument.java
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

import com.oracle.tools.lang.ExpressionEvaluator;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import java.util.Iterator;

/**
 * A representation of a command line argument to an application.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Argument implements Option.Collectable
{
    /**
     * The optional name of this argument
     */
    private String name;

    /**
     * The optional separator to use between the name and
     * value.
     */
    private char separator;

    /**
     * The value of the {@link Argument}.
     */
    private final Object value;

    /**
     * Create an {@link Argument} with the specified value.
     *
     * @param value  the value of the {@link Argument}
     */
    public Argument(Object value)
    {
        this(null, ' ', value);
    }


    /**
     * Create an {@link Argument} with the specified name,
     * and value.
     *
     * @param name       the name of the {@link Argument}
     * @param value      the value of the {@link Argument}
     */
    public Argument(String name, Object value)
    {
        this(name, ' ', value);
    }


    /**
     * Create an {@link Argument} with the specified name,
     * separator and value.
     *
     * @param name       the name of the {@link Argument}
     * @param separator  the separator to use between the name and value
     * @param value      the value of the {@link Argument}
     */
    public Argument(String name, char separator, Object value)
    {
        this.name      = name;
        this.separator = separator;
        this.value     = value;
    }


    /**
     * Obtain the optional name of this argument.
     *
     * @return  the optional name of this argument
     */
    public String getName()
    {
        return name;
    }


    /**
     * Set the separator to use between the name and value when
     * realizing the arguments for an application.
     *
     * @param separator  the separator to use
     *
     * @return  this {@link Argument}
     */
    public Argument withSeparator(char separator)
    {
        this.separator = separator;

        return this;
    }


    /**
     * The separator to use between the name and value
     * when realizing the arguments for an application.
     *
     * @return  separator to use between the name and value
     */
    public char getSeparator()
    {
        return separator;
    }


    /**
     * Obtain the value of this {@link Argument}.
     *
     * @return  the value of this {@link Argument}
     */
    public Object getValue()
    {
        return value;
    }


    /**
     * Realize the String representation of this {@link Argument}.
     *
     * @param platform   the {@link Platform} that the {@link Argument} is being realized for
     * @param schema     the {@link ApplicationSchema} that the {@link Argument} is being realized for
     * @param evaluator  the {@link ExpressionEvaluator} to use for expression values
     *
     * @return  the String representation of this {@link Argument}
     */
    public String realizeValue(Platform platform, ApplicationSchema<?> schema, ExpressionEvaluator evaluator)
    {
        if (value == null)
        {
            return null;
        }

        Object argValue = value;

        if (argValue instanceof Argument.ContextSensitiveArgument)
        {
            Argument.ContextSensitiveArgument contextSensitiveValue =
                    (Argument.ContextSensitiveArgument) argValue;

            argValue = contextSensitiveValue.getValue(platform, schema);
        }

        if (argValue instanceof Iterator<?>)
        {
            Iterator<?> iterator = (Iterator<?>) argValue;

            if (iterator.hasNext())
            {
                argValue = iterator.next().toString();
            }
            else
            {
                throw new IndexOutOfBoundsException(
                        String.format("No more values available for the argument [%s]", value));
            }
        }

        if (argValue == null)
        {
            return null;
        }

        String expression = argValue.toString().trim();

        // resolve the use of expressions in the value
        Object result = evaluator.evaluate(expression, Object.class);

        return result == null ? "" : result.toString();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Argument argument = (Argument) o;

        if (separator != argument.separator)
        {
            return false;
        }

        if (name != null ? !name.equals(argument.name) : argument.name != null)
        {
            return false;
        }

        return value != null ? value.equals(argument.value) : argument.value == null;
    }


    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) separator;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }


    @Override
    public String toString()
    {
        return name != null ? name + separator + value : String.valueOf(value);
    }


    @Override
    public Class<? extends Option.Collector> getCollectorClass()
    {
        return Arguments.class;
    }


    /**
     * Create an {@link Argument} with the specified value.
     *
     * @param arg  the value of the {@link Argument}
     *
     * @return  an {@link Argument} with the specified value
     */
    public static Argument of(Object arg)
    {
        return new Argument(arg);
    }


    /**
     * Create an {@link Argument} with the specified value.
     *
     * @param arg  the value of the {@link Argument}
     *
     * @return  an {@link Argument} with the specified value
     */
    public static Argument of(String name, Object arg)
    {
        return new Argument(name, arg);
    }


    /**
     * A context sensitive argument, possibly based on the {@link Platform} and/or {@link ApplicationSchema} in
     * which the {@link Argument} is being used.
     */
    public interface ContextSensitiveArgument
    {
        /**
         * Obtains the value for the {@link Argument}, possibly based on the provided
         * {@link Platform} and {@link ApplicationSchema}.
         *
         * @param platform the {@link Platform} in which {@link Argument} is being used.
         * @param schema   the {@link ApplicationSchema} in which {@link Argument} is being used.
         *
         * @return the value
         */
        Object getValue(Platform          platform,
                        ApplicationSchema schema);
    }
}

