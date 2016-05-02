/*
 * File: Variables.java
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

package com.oracle.bedrock.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link Option.Collector} of {@link Variable}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Variable
 */
public class Variables implements Option.Collector<Variable, Variables>
{
    /**
     * The {@link Variable}s collected by the {@link Variables}.
     */
    private LinkedHashMap<String, Variable> variables;


    /**
     * Constructs an empty {@link Variables}.
     */
    @Options.Default
    public Variables()
    {
        this.variables = new LinkedHashMap<>();
    }


    /**
     * Constructs a {@link Variables} with properties based on a
     * {@link Map} of name-value pairs.
     *
     * @param variables the {@link Map} of properties to use as the basis for
     *                   the {@link Variables}
     */
    public Variables(Map<String, String> variables)
    {
        this();

        for (String name : variables.keySet())
        {
            this.variables.put(name, Variable.with(name, variables.get(name)));
        }
    }


    /**
     * Constructs a {@link Variables} based on a standard
     * {@link Properties} representation.
     *
     * @param properties the {@link Properties} to use as the basis for the
     *                   {@link Variables}
     */
    public Variables(Properties properties)
    {
        this();

        for (String key : properties.stringPropertyNames())
        {
            this.variables.put(key, Variable.with(key, properties.getProperty(key)));
        }
    }


    /**
     * Constructs a {@link Variables} based on the properties defined
     * in another {@link Variables}.
     *
     * @param variables the {@link Variables} on which to base
     *                   the new {@link Variables}
     */
    public Variables(Variable... variables)
    {
        this();

        if (variables != null)
        {
            for (Variable property : variables)
            {
                add(property);
            }
        }
    }


    /**
     * Constructs a {@link Variables} based on the properties defined
     * in another {@link Variables}.
     *
     * @param variables the {@link Variables} on which to base
     *                         the new {@link Variables}
     */
    public Variables(Variables variables)
    {
        this();

        this.variables.putAll(variables.variables);
    }


    /**
     * Obtains the number of {@link Variable}s contained
     * by the {@link Variables}.
     *
     * @return the number of {@link Variable}s
     */
    public int size()
    {
        return variables.size();
    }


    /**
     * Determines if the {@link Variables} is empty (contains no {@link Variable}s)
     *
     * @return <code>true</code> if the {@link Variables} is empty, <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return variables.isEmpty();
    }


    /**
     * Adds the specified {@link Variable} to the {@link Variables}, returning a new
     * {@link Variables} containing the {@link Variable}.
     *
     * @param variable the {@link Variable} to add
     *
     * @return the a new {@link Variables} instance, including the existing {@link Variable}s and the new {@link Variable}
     */
    public Variables add(Variable variable)
    {
        if (variable == null)
        {
            return this;
        }
        else
        {
            Variables result = new Variables(this);

            result.variables.put(variable.getName(), variable);

            return result;
        }
    }


    /**
     * Adds the specified {@link Variable} to the {@link Variables}, returning a new
     * {@link Variables} containing the {@link Variable}
     * (if and only if a {@link Variable} with the same name doesn't already exist).
     *
     * @param variable the {@link Variable} to add
     *
     * @return the a new {@link Variables} instance, including the existing {@link Variable}s and the new {@link Variable}
     */
    public Variables addIfAbsent(Variable variable)
    {
        if (variable == null || variables.containsKey(variable.getName()))
        {
            return this;
        }
        else
        {
            return add(variable);
        }
    }


    /**
     * Removes the specified {@link Variable} named property from the {@link Variables}, returning a new
     * {@link Variables} without the said {@link Variable}.
     *
     * @param name the {@link Variable} to remove
     *
     * @return the a new {@link Variables} instance, excluding the specified {@link Variable}
     */
    public Variables remove(String name)
    {
        if (name == null || name.isEmpty() ||!contains(name))
        {
            return this;
        }
        else
        {
            Variables result = new Variables(this);

            result.variables.remove(name);

            return result;
        }
    }


    /**
     * Adds all of the specified properties represented as a {@link Map} of name-value pairs
     * as individual {@link Variable}s, returning a new {@link Variables}.
     *
     * @param properties the {@link Map} of properties
     */
    public Variables addAll(Map<String, Object> properties)
    {
        Variables result = new Variables(this);

        for (String name : properties.keySet())
        {
            result.variables.put(name, Variable.with(name, properties.get(name)));
        }

        return result;
    }


    /**
     * Adds all of the {@link Variables} to this {@link Variables}
     * returning a new {@link Variables}.
     *
     * @param variables the {@link Variables}
     */
    public Variables addAll(Variables variables)
    {
        Variables result = new Variables(this);

        for (Variable variable : variables)
        {
            result.variables.put(variable.getName(), variable);
        }

        return result;
    }


    /**
     * Determines if the {@link Variables} contains a {@link Variable}
     * with the specified name.
     *
     * @param name the name of the {@link Variable}
     *
     * @return <code>true</code> if the {@link Variables} contains a {@link Variable}
     * with the specified name, <code>false</code> otherwise
     */
    public boolean contains(String name)
    {
        return variables.containsKey(name);
    }


    /**
     * Obtains the {@link Variable} with the specified name, returning <code>null</code> if
     * one is not found.
     *
     * @param name the name of the desired {@link Variable}
     *
     * @return the {@link Variable} of <code>null</code> if not defined
     */
    public Variable get(String name)
    {
        return variables.get(name);
    }


    @Override
    public Variables with(Variable variable)
    {
        return add(variable);
    }


    @Override
    public Variables without(Variable variable)
    {
        return remove(variable.getName());
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(Variable.class))
        {
            return (Iterable<O>) variables.values();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public Iterator<Variable> iterator()
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

        if (!(o instanceof Variables))
        {
            return false;
        }

        Variables that = (Variables) o;

        return variables.equals(that.variables);

    }


    @Override
    public int hashCode()
    {
        return variables.hashCode();
    }
}
