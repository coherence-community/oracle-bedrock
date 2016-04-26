/*
 * File: SystemProperties.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.lang.ExpressionEvaluator;

import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.java.JavaApplication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link Collector} of {@link SystemProperty}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SystemProperties implements Option.Collector<SystemProperty, SystemProperties>
{
    /**
     * The names of Java System properties.
     */
    public static final HashSet<String> STANDARD_SYSTEM_PROPERTY_NAMES = new HashSet<String>()
    {
        {
            add("java.version");
            add("java.vendor");
            add("java.vendor.url");
            add("java.home");
            add("java.vm.specification.version");
            add("java.vm.specification.vendor");
            add("java.vm.specification.name");
            add("java.vm.version");
            add("java.vm.vendor");
            add("java.vm.name");
            add("java.specification.version");
            add("java.specification.vendor");
            add("java.specification.name");
            add("java.class.version");
            add("java.class.path");
            add("java.library.path");
            add("java.io.tmpdir");
            add("java.compiler");
            add("java.ext.dirs");
            add("os.name");
            add("os.arch");
            add("os.version");
            add("file.separator");
            add("path.separator");
            add("line.separator");
            add("user.name");
            add("user.home");
            add("user.dir");
        }
    };

    /**
     * The {@link SystemProperty}s collected by the {@link SystemProperties}.
     */
    private LinkedHashMap<String, SystemProperty> properties;


    /**
     * Constructs an empty {@link SystemProperties}.
     */
    @Options.Default
    public SystemProperties()
    {
        this.properties = new LinkedHashMap<>();
    }


    /**
     * Constructs a {@link SystemProperties} with properties based on a
     * {@link Map} of name-value pairs.
     *
     * @param properties the {@link Map} of properties to use as the basis for
     *                   the {@link SystemProperties}
     */
    public SystemProperties(Map<String, String> properties)
    {
        this();

        for (String name : properties.keySet())
        {
            this.properties.put(name, SystemProperty.of(name, properties.get(name)));
        }
    }


    /**
     * Constructs a {@link SystemProperties} based on a standard
     * {@link Properties} representation.
     *
     * @param properties the {@link Properties} to use as the basis for the
     *                   {@link SystemProperties}
     */
    public SystemProperties(Properties properties)
    {
        this();

        for (String key : properties.stringPropertyNames())
        {
            this.properties.put(key, SystemProperty.of(key, properties.getProperty(key)));
        }
    }


    /**
     * Constructs a {@link SystemProperties} based on the properties defined
     * in another {@link SystemProperties}.
     *
     * @param systemProperties the {@link SystemProperties} on which to base
     *                         the new {@link SystemProperties}
     */
    public SystemProperties(SystemProperties systemProperties)
    {
        this();

        this.properties.putAll(systemProperties.properties);
    }


    /**
     * Constructs a {@link SystemProperties} based on the properties defined
     * in another {@link SystemProperties}.
     *
     * @param properties the {@link SystemProperties} on which to base
     *                   the new {@link SystemProperties}
     */
    public SystemProperties(SystemProperty... properties)
    {
        this();

        if (properties != null)
        {
            for (SystemProperty property : properties)
            {
                add(property);
            }
        }
    }


    /**
     * Obtains the number of {@link SystemProperty}s contained
     * by the {@link SystemProperties}.
     *
     * @return the number of {@link SystemProperty}s
     */
    public int size()
    {
        return properties.size();
    }


    /**
     * Determines if the {@link SystemProperties} is empty (contains no {@link SystemProperty}s)
     *
     * @return <code>true</code> if the {@link SystemProperties} is empty, <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return properties.isEmpty();
    }


    /**
     * Adds the specified {@link SystemProperty} to the {@link SystemProperties}, returning a new
     * {@link SystemProperties} containing the {@link SystemProperty}.
     *
     * @param properties the {@link SystemProperty}s to add
     *
     * @return  the a new {@link SystemProperties} instance, including
     *          the existing {@link SystemProperty}s and the new {@link SystemProperty}
     */
    public SystemProperties add(SystemProperty... properties)
    {
        if (properties == null || properties.length == 0)
        {
            return this;
        }
        else
        {
            SystemProperties systemProperties = new SystemProperties(this);

            for (SystemProperty property : properties)
            {
                systemProperties.properties.put(property.getName(), property);
            }

            return systemProperties;
        }
    }


    /**
     * Adds the specified {@link SystemProperty} to the {@link SystemProperties}, returning a new
     * {@link SystemProperties} containing the {@link SystemProperty}
     * (if and only if a {@link SystemProperty} with the same name doesn't already exist).
     *
     * @param property the {@link SystemProperty} to add
     *
     * @return the a new {@link SystemProperties} instance, including the existing {@link SystemProperty}s and the new {@link SystemProperty}
     */
    public SystemProperties addIfAbsent(SystemProperty property)
    {
        if (property == null || properties.containsKey(property.getName()))
        {
            return this;
        }
        else
        {
            return add(property);
        }
    }


    /**
     * Removes the specified {@link SystemProperty} named property from the {@link SystemProperties}, returning a new
     * {@link SystemProperties} without the said {@link SystemProperty}.
     *
     * @param propertyName the {@link SystemProperty} to remove
     *
     * @return the a new {@link SystemProperties} instance, excluding the specified {@link SystemProperty}
     */
    public SystemProperties remove(String propertyName)
    {
        if (propertyName == null || propertyName.isEmpty() ||!contains(propertyName))
        {
            return this;
        }
        else
        {
            SystemProperties systemProperties = new SystemProperties(this);

            systemProperties.properties.remove(propertyName);

            return systemProperties;
        }
    }


    /**
     * Adds all of the specified properties represented as a {@link Map} of name-value pairs
     * as individual {@link SystemProperty}s, returning a new {@link SystemProperties}.
     *
     * @param properties the {@link Map} of properties
     */
    public SystemProperties addAll(Map<String, Object> properties)
    {
        SystemProperties systemProperties = new SystemProperties(this);

        for (String name : properties.keySet())
        {
            systemProperties.properties.put(name, SystemProperty.of(name, properties.get(name)));
        }

        return systemProperties;
    }


    /**
     * Adds all of the {@link SystemProperties} to this {@link SystemProperties}
     * returning a new {@link SystemProperties}.
     *
     * @param properties the {@link SystemProperties}
     */
    public SystemProperties addAll(SystemProperties properties)
    {
        SystemProperties systemProperties = new SystemProperties(this);

        for (SystemProperty property : properties)
        {
            systemProperties.properties.put(property.getName(), property);
        }

        return systemProperties;
    }


    /**
     * Determines if the {@link SystemProperties} contains a {@link SystemProperty}
     * with the specified name.
     *
     * @param propertyName the name of the {@link SystemProperty}
     *
     * @return <code>true</code> if the {@link SystemProperties} contains a {@link SystemProperty}
     * with the specified name, <code>false</code> otherwise
     */
    public boolean contains(String propertyName)
    {
        return properties.containsKey(propertyName);
    }


    /**
     * Obtains the {@link SystemProperty} with the specified name, returning <code>null</code> if
     * one is not found.
     *
     * @param propertyName the name of the desired {@link SystemProperty}
     *
     * @return the {@link SystemProperty} of <code>null</code> if not defined
     */
    public SystemProperty get(String propertyName)
    {
        return properties.get(propertyName);
    }


    /**
     * Obtains the current value of the specified {@link SystemProperty}.  If the property has
     * a value specified, that value will be used.  If the property is unknown,
     * <code>null</code> will be returned.
     *
     * @param name the name of the property
     *
     * @return an {@link Object}
     */
    public Object getProperty(String name)
    {
        if (properties.containsKey(name))
        {
            SystemProperty property = properties.get(name);

            return property.getValue();
        }
        else
        {
            return null;
        }
    }


    /**
     * Creates a standard java {@link Properties} instance containing the
     * {@link SystemProperty}s based on the specified {@link Platform} and {@link JavaApplication}
     * launch {@link Option}s.
     * <p>
     * If the value of a {@link SystemProperty} is defined as an {@link Iterator}, the next value from the
     * said {@link Iterator} will be used as a value for the returned property.  If the value of a
     * {@link SystemProperty} is defined as a {@link SystemProperty.ContextSensitiveValue}, the
     * {@link SystemProperty.ContextSensitiveValue#resolve(String, Platform, Options)} is called
     * to resolve the value.
     *
     * @param platform  the target {@link Platform} for the returned {@link Properties}
     * @param options   the {@link Options} for resolving the {@link Properties}
     *
     * @return a new {@link Properties} instance
     */
    public Properties resolve(Platform platform,
                              Options  options)
    {
        ExpressionEvaluator evaluator  = new ExpressionEvaluator(options);

        Properties          properties = new Properties();

        for (SystemProperty property : this.properties.values())
        {
            String name  = property.getName();
            Object value = property.getValue();

            if (value != null)
            {
                if (value instanceof SystemProperty.ContextSensitiveValue)
                {
                    SystemProperty.ContextSensitiveValue contextSensitiveValue =
                        (SystemProperty.ContextSensitiveValue) value;

                    value = contextSensitiveValue.resolve(name, platform, options);
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
                        throw new IndexOutOfBoundsException(String.format("No more values available for the property [%s]",
                                                                          name));
                    }
                }

                if (value != null)
                {
                    String expression = value.toString().trim();

                    if (!expression.isEmpty())
                    {
                        // resolve the use of expressions in the value
                        Object result = evaluator.evaluate(expression, Object.class);

                        expression = result == null ? "" : result.toString();
                    }

                    Options                                 propertyOptions = property.getOptions();
                    Iterable<SystemProperty.ResolveHandler> handlers        = propertyOptions
                            .getInstancesOf(SystemProperty.ResolveHandler.class);

                    for (SystemProperty.ResolveHandler handler : handlers)
                    {
                        try
                        {
                            handler.onResolve(name, expression, options);
                        }
                        catch (Throwable t)
                        {
                            t.printStackTrace();
                        }
                    }

                    // record the property
                    properties.put(name, expression);
                }
            }
        }

        return properties;
    }


    @Override
    public SystemProperties with(SystemProperty property)
    {
        return add(property);
    }


    @Override
    public SystemProperties without(SystemProperty property)
    {
        return remove(property.getName());
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(SystemProperty.class))
        {
            return (Iterable<O>) properties.values();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public Iterator<SystemProperty> iterator()
    {
        return properties.values().iterator();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof SystemProperties))
        {
            return false;
        }

        SystemProperties that = (SystemProperties) o;

        return properties.equals(that.properties);

    }


    @Override
    public int hashCode()
    {
        return properties.hashCode();
    }
}
