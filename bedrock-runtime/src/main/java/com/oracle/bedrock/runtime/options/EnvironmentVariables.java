/*
 * File: EnvironmentVariables.java
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
import com.oracle.bedrock.runtime.Platform;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * A {@link Collector} of {@link EnvironmentVariable}s, used to define operating system environment
 * variables, sourced from a specific location.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class EnvironmentVariables implements Option.Collector<EnvironmentVariable, EnvironmentVariables>
{
    /**
     * The location from which initial environment variables will be extracted.
     */
    private Source source;

    /**
     * The custom {@link EnvironmentVariable}s collected by the {@link EnvironmentVariables}
     * that will be added to those defined by the {@link #source}.
     */
    private LinkedHashMap<String, EnvironmentVariable> variables;


    /**
     * The initial source of environment variables, upon which
     * customizations may be made.
     */
    public enum Source
    {
        /**
         * The initial environment variables will be sourced and based upon
         * those defined by this application, namely those defined by
         * {@link System#getenv()}.
         */
        ThisApplication,

        /**
         * The initial environment variables will be sourced from and based upon
         * those defined by the target platform.
         */
        TargetPlatform,

        /**
         * The initial environment variables provided to an application will
         * be custom (thus starting with none).
         */
        Custom
    }


    /**
     * Constructs an {@link EnvironmentVariables} based on another {@link EnvironmentVariables}.
     *
     * @param environmentVariables the {@link EnvironmentVariables} on which to base the new
     *                             {@link EnvironmentVariables}
     */
    public EnvironmentVariables(EnvironmentVariables environmentVariables)
    {
        this.source    = environmentVariables.getSource();
        this.variables = new LinkedHashMap<>(environmentVariables.variables);
    }


    /**
     * Constructs an empty {@link EnvironmentVariables} using the specified source.
     *
     * @param source the {@link Source}
     */
    public EnvironmentVariables(Source source)
    {
        this.source    = source;
        this.variables = new LinkedHashMap<>();
    }


    /**
     * Obtains the {@link Source} of the environment variables
     *
     * @return the {@link Source}
     */
    public Source getSource()
    {
        return source;
    }


    /**
     * Constructs an {@link Option} to create {@link EnvironmentVariables} based
     * on a specific {@link Source}.
     *
     * @param source the {@link Source} of the environment variables
     *
     * @return an {@link EnvironmentVariables}
     */
    public static EnvironmentVariables of(Source source)
    {
        return new EnvironmentVariables(source);
    }


    /**
     * Constructs a custom set of {@link EnvironmentVariables}, starting
     * initially with a cleared environment.
     *
     * @return an {@link EnvironmentVariables}
     */
    public static EnvironmentVariables custom()
    {
        return new EnvironmentVariables(Source.Custom);
    }


    /**
     * Constructs a custom set of {@link EnvironmentVariables}, starting
     * initially with the environment variables defined by this application.
     *
     * @return an {@link EnvironmentVariables}
     */
    @OptionsByType.Default
    public static EnvironmentVariables inherited()
    {
        return new EnvironmentVariables(Source.ThisApplication);
    }


    /**
     * Defines a custom environment variable, overriding any previously
     * defined variable of the same name.
     *
     * @param name  the name of the environment variable
     * @param value the value of the environment variable
     *
     * @return the {@link EnvironmentVariables} {@link Option} to permit fluent-method calls
     */
    public EnvironmentVariables set(String name,
                                    Object value)
    {
        return with(EnvironmentVariable.of(name, value));
    }


    /**
     * Defines a custom environment variable, overriding any previously
     * defined variable of the same name, the value to be used to be taken
     * from the specified iterator when the {@link EnvironmentVariables} are requested.
     *
     * @param name     the name of the environment variable
     * @param iterator the {@link Iterator} providing values for the variable
     *
     * @return the {@link EnvironmentVariables} {@link Option} to permit fluent-method calls
     */
    public EnvironmentVariables set(String      name,
                                    Iterator<?> iterator)
    {
        return with(EnvironmentVariable.of(name, iterator));
    }


    /**
     * Creates a standard {@link Properties} instance containing the
     * {@link EnvironmentVariable}s based on the specified {@link Platform} and {@link Option}s.
     * <p>
     * If the value of a {@link EnvironmentVariable} is defined as an {@link Iterator}, the next value from the
     * said {@link Iterator} will be used as a value for the returned property.  If the value of a
     * {@link EnvironmentVariable} is defined as a {@link EnvironmentVariable.ContextSensitiveValue}, the
     * {@link EnvironmentVariable.ContextSensitiveValue#getValue(String, Platform, Option...)} is called
     * to resolve the value.
     *
     * @param platform  the target {@link Platform} for the returned {@link Properties}
     * @param options   the {@link Option}s for realizing the {@link Properties}
     *
     * @return a new {@link Properties} instance
     */
    public Properties realize(Platform  platform,
                              Option... options)
    {
        OptionsByType       optionsByType = OptionsByType.of(options);

        ExpressionEvaluator evaluator     = new ExpressionEvaluator(optionsByType);

        Properties          properties    = new Properties();

        for (EnvironmentVariable variable : this.variables.values())
        {
            String name  = variable.getName();
            Object value = variable.getValue();

            if (value != null)
            {
                if (value instanceof EnvironmentVariable.ContextSensitiveValue)
                {
                    EnvironmentVariable.ContextSensitiveValue contextSensitiveValue =
                        (EnvironmentVariable.ContextSensitiveValue) value;

                    value = contextSensitiveValue.getValue(name, platform, options);
                }

                if (value instanceof Iterator<?>)
                {
                    Iterator<?> iterator = (Iterator<?>) value;

                    if (iterator.hasNext())
                    {
                        value = iterator.next().toString();
                    }
                    else
                    {
                        throw new IndexOutOfBoundsException(String.format("No more values available for the variable [%s]",
                                                                          name));
                    }
                }

                if (value != null)
                {
                    String expression = value.toString().trim();

                    if (!expression.isEmpty())
                    {
                        // resolve the use of expressions in the values
                        Object result = evaluator.evaluate(expression, Object.class);

                        expression = result == null ? "" : result.toString();
                    }

                    // record the property
                    properties.put(name, expression);
                }
            }
        }

        return properties;
    }


    /**
     * Obtain a new {@link EnvironmentVariables} that is the union of
     * this {@link EnvironmentVariables} and the specified {@link Collection}
     * of {@link EnvironmentVariable} instances.
     * <p>
     * If an {@link EnvironmentVariable} with a given name is present in
     * this {@link EnvironmentVariables} and in the {@link Collection} being
     * added then the value being added takes precedence.
     *
     * @param variables  the {@link EnvironmentVariable}s to union with
     *                   this {@link EnvironmentVariables}
     *
     * @return   a new {@link EnvironmentVariables} that is the union
     *           of this {@link EnvironmentVariables} and the specified
     *           array of {@link EnvironmentVariable} instances
     */
    public EnvironmentVariables with(Collection<EnvironmentVariable> variables)
    {
        if (variables == null || variables.isEmpty())
        {
            return this;
        }

        EnvironmentVariables environmentVariables = new EnvironmentVariables(this);

        for (EnvironmentVariable variable : variables)
        {
            environmentVariables.variables.put(variable.getName(), variable);
        }

        return environmentVariables;
    }


    @Override
    public EnvironmentVariables with(EnvironmentVariable variable)
    {
        EnvironmentVariables environmentVariables = new EnvironmentVariables(this);

        environmentVariables.variables.put(variable.getName(), variable);

        return environmentVariables;
    }


    @Override
    public EnvironmentVariables without(EnvironmentVariable variable)
    {
        if (variables.containsKey(variable.getName()))
        {
            EnvironmentVariables environmentVariables = new EnvironmentVariables(this);

            environmentVariables.variables.remove(variable);

            return environmentVariables;
        }
        else
        {
            return this;
        }
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(EnvironmentVariable.class))
        {
            return (Iterable<O>) variables.values();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public Iterator<EnvironmentVariable> iterator()
    {
        return variables.values().iterator();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof EnvironmentVariables))
        {
            return false;
        }

        EnvironmentVariables that = (EnvironmentVariables) o;

        if (source != that.source)
        {
            return false;
        }

        return variables.equals(that.variables);
    }


    @Override
    public int hashCode()
    {
        int result = source.hashCode();

        result = 31 * result + variables.hashCode();

        return result;
    }
}
