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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.lang.ExpressionEvaluator;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
    private final String name;

    /**
     * The optional separator to use between the name and
     * value.
     */
    private final char separator;

    /**
     * The value of the {@link Argument}.
     */
    private final Object value;

    /**
     * The {@link Option}s for this {@link Argument}.
     */
    private final Option[] options;


    /**
     * Create an {@link Argument} with the specified value.
     *
     * @param value    the value of the {@link Argument}
     * @param options  the {@link Option}s for the {@link Argument}
     */
    public Argument(Object    value,
                    Option... options)
    {
        this(null, ' ', value, options);
    }


    /**
     * Create an {@link Argument} with the specified name,
     * and value.
     *
     * @param name     the name of the {@link Argument}
     * @param value    the value of the {@link Argument}
     * @param options  the {@link Option}s for the {@link Argument}
     */
    public Argument(String    name,
                    Object    value,
                    Option... options)
    {
        this(name, ' ', value, options);
    }


    /**
     * Create an {@link Argument} with the specified name,
     * separator and value.
     *
     * @param name       the name of the {@link Argument}
     * @param separator  the separator to use between the name and value
     * @param value      the value of the {@link Argument}
     * @param options    the {@link Option}s for this {@link Argument}
     */
    public Argument(String    name,
                    char      separator,
                    Object    value,
                    Option... options)
    {
        this.name      = name;
        this.separator = separator;
        this.value     = value;
        this.options   = options;
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
     * <p>
     * <strong>Note</strong> {@link Argument}s are immutable so calling
     * this method will return a copy of this {@link Argument} with
     * the specified separator.
     *
     * @param separator  the separator to use
     *
     * @return  this {@link Argument}
     */
    public Argument withSeparator(char separator)
    {
        if (separator == this.separator)
        {
            return this;
        }

        return new Argument(this.name, separator, this.value);
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
     * Obtain the {@link OptionsByType}s for this {@link Argument}.
     *
     * @return  the {@link OptionsByType}s for this {@link Argument}
     */
    public OptionsByType getOptions()
    {
        return OptionsByType.of(options);
    }


    /**
     * Resolve the representation of this {@link Argument} using the provided {@link Platform}
     * and {@link Application} launch {@link Option}s.
     * <p>
     * An {@link Argument} may resolve to multiple values, which are each returned in the {@link List}
     * and the command line will then contain a name/value pair for each entry returned.
     * For example, if this {@link Argument} had the name "-foo" and this method returned
     * a {@link List} containing the Strings "bar1" and "bar2" the command line would contain
     * the values "-foo bar1 -foo bar2".
     *
     * @param platform       the {@link Platform} that the {@link Argument} is being realized for
     * @param evaluator      the {@link ExpressionEvaluator} to use for expression values
     * @param optionsByType  the {@link Application} launch {@link OptionsByType}s
     *
     * @return  the String representation of this {@link Argument}
     */
    public List<String> resolve(Platform            platform,
                                ExpressionEvaluator evaluator,
                                OptionsByType       optionsByType)
    {
        if (value == null)
        {
            return Collections.emptyList();
        }

        List<String> argList;

        if (value instanceof Multiple)
        {
            argList = new ArrayList<>();

            for (Object argValue : ((Multiple) value).getValues())
            {
                Object result = resolveValue(argValue, platform, evaluator, optionsByType);

                if (result != null)
                {
                    argList.add(String.valueOf(result));
                }
            }
        }
        else
        {
            Object result = resolveValue(value, platform, evaluator, optionsByType);

            if (result != null)
            {
                argList = Collections.singletonList(String.valueOf(result));
            }
            else
            {
                argList = Collections.emptyList();
            }
        }

        OptionsByType            argOptions = getOptions();
        Iterable<ResolveHandler> handlers   = argOptions.getInstancesOf(ResolveHandler.class);

        for (ResolveHandler handler : handlers)
        {
            try
            {
                handler.onResolve(this.name, Collections.unmodifiableList(argList), optionsByType);
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }

        return argList;
    }


    private Object resolveValue(Object              argValue,
                                Platform            platform,
                                ExpressionEvaluator evaluator,
                                OptionsByType       optionsByType)
    {
        if (argValue instanceof Argument.ContextSensitiveArgument)
        {
            Argument.ContextSensitiveArgument contextSensitiveValue = (Argument.ContextSensitiveArgument) argValue;

            argValue = contextSensitiveValue.resolve(platform, optionsByType);
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
                throw new IndexOutOfBoundsException(String.format("No more values available for the argument [%s]",
                                                                  value));
            }
        }

        if (argValue == null)
        {
            return null;
        }

        String expression = argValue.toString().trim();

        // resolve the use of expressions in the value
        Object result = evaluator.evaluate(expression, Object.class);

        return result;
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
     * @param arg      the value of the {@link Argument}
     * @param options  the {@link Option}s for this {@link Argument}
     *
     * @return  an {@link Argument} with the specified value
     */
    public static Argument of(Object    arg,
                              Option... options)
    {
        return new Argument(arg, options);
    }


    /**
     * Create an {@link Argument} with the specified value.
     *
     * @param name     the name of the argument
     * @param arg      the value of the {@link Argument}
     * @param options  the {@link Option}s for this {@link Argument}
     *
     * @return  an {@link Argument} with the specified value
     */
    public static Argument of(String    name,
                              Object    arg,
                              Option... options)
    {
        return new Argument(name, arg, options);
    }


    /**
     * Create an {@link Argument} with the specified name,
     * value and separator.
     *
     * @param name      the name of the argument
     * @param separator the separator to use between the name
     *                  and value when resolving this argument
     * @param arg       the value of the {@link Argument}
     * @param options   the {@link Option}s for this {@link Argument}
     *
     * @return  an {@link Argument} with the specified value
     */
    public static Argument of(String    name,
                              char      separator,
                              Object    arg,
                              Option... options)
    {
        return new Argument(name, separator, arg, options);
    }


    /**
     * A context sensitive argument, possibly based on the {@link Platform} and/or {@link Application}
     * launch {@link Option}s in which the {@link Argument} is being used.
     */
    public interface ContextSensitiveArgument
    {
        /**
         * Obtains the value for the {@link Argument}, possibly based on the provided
         * {@link Platform} and {@link Option}s.
         *
         * @param platform       the {@link Platform} in which {@link Argument} is being used
         * @param optionsByType  the {@link Application} launch {@link OptionsByType}
         *
         * @return the value
         */
        Object resolve(Platform      platform,
                       OptionsByType optionsByType);
    }


    /**
     * A handler that is called whenever an {@link Argument}'s value(s) are
     * resolved.
     */
    @FunctionalInterface
    public interface ResolveHandler
    {
        /**
         * Called by an {@link Argument} whenever its value(s) are resolved.
         *
         * @param name           the name of the argument (may be null if no name was specified)
         * @param values         an immutable {@link List} of the resolved values
         * @param optionsByType  the {@link OptionsByType} used to resolve the values
         */
        void onResolve(String        name,
                       List<String>  values,
                       OptionsByType optionsByType);
    }


    /**
     * A value used to denote that a given argument
     * occurs multiple times with each of the values
     * contained within this {@link Multiple}.
     */
    public static class Multiple
    {
        /**
         * The values for the argument.
         */
        private final List<?> values;


        /**
         * Create a {@link Multiple} with the
         * specified values.
         *
         * @param values  the values for the argument
         */
        public Multiple(Collection<?> values)
        {
            this.values = values == null ? Collections.emptyList() : new ArrayList<>(values);
        }


        /**
         * Create a {@link Multiple} with the
         * specified values.
         *
         * @param values  the values for the argument
         */
        public Multiple(Object... values)
        {
            this.values = Arrays.asList(values);
        }


        /**
         * Obtain the values for this {@link Multiple}.
         *
         * @return  the values for this {@link Multiple}
         */
        public List<?> getValues()
        {
            return values;
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

            Multiple multiple = (Multiple) o;

            return values.equals(multiple.values);

        }


        @Override
        public int hashCode()
        {
            return values.hashCode();
        }


        @Override
        public String toString()
        {
            return String.valueOf(values);
        }
    }
}
