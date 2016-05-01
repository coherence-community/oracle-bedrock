/*
 * File: VagrantProperties.java
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

package com.oracle.tools.runtime.virtual.vagrant.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link Collector} of {@link VagrantProperty}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VagrantProperties implements Option.Collector<VagrantProperty, VagrantProperties>
{
    /**
     * The {@link VagrantProperty}s collected by the {@link VagrantProperties}.
     */
    private LinkedHashMap<String, VagrantProperty> properties;


    /**
     * Constructs an empty {@link VagrantProperties}.
     */
    @Options.Default
    public VagrantProperties()
    {
        this.properties = new LinkedHashMap<>();
    }


    /**
     * Constructs a {@link VagrantProperties} with properties based on a
     * {@link Map} of name-value pairs.
     *
     * @param properties the {@link Map} of properties to use as the basis for
     *                   the {@link VagrantProperties}
     */
    public VagrantProperties(Map<String, String> properties)
    {
        this();

        for (String name : properties.keySet())
        {
            this.properties.put(name, VagrantProperty.of(name, properties.get(name)));
        }
    }


    /**
     * Constructs a {@link VagrantProperties} based on a standard
     * {@link Properties} representation.
     *
     * @param properties the {@link Properties} to use as the basis for the
     *                   {@link VagrantProperties}
     */
    public VagrantProperties(Properties properties)
    {
        this();

        for (String key : properties.stringPropertyNames())
        {
            this.properties.put(key, VagrantProperty.of(key, properties.getProperty(key)));
        }
    }


    /**
     * Constructs a {@link VagrantProperties} based on the properties defined
     * in another {@link VagrantProperties}.
     *
     * @param vagrantProperties the {@link VagrantProperties} on which to base
     *                         the new {@link VagrantProperties}
     */
    public VagrantProperties(VagrantProperties vagrantProperties)
    {
        this();

        this.properties.putAll(vagrantProperties.properties);
    }


    /**
     * Constructs a {@link VagrantProperties} based on the properties defined
     * in another {@link VagrantProperties}.
     *
     * @param properties the {@link VagrantProperties} on which to base
     *                   the new {@link VagrantProperties}
     */
    public VagrantProperties(VagrantProperty... properties)
    {
        this();

        if (properties != null)
        {
            for (VagrantProperty property : properties)
            {
                add(property);
            }
        }
    }


    /**
     * Obtains the number of {@link VagrantProperty}s contained
     * by the {@link VagrantProperties}.
     *
     * @return the number of {@link VagrantProperty}s
     */
    public int size()
    {
        return properties.size();
    }


    /**
     * Determines if the {@link VagrantProperties} is empty (contains no {@link VagrantProperty}s)
     *
     * @return <code>true</code> if the {@link VagrantProperties} is empty, <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return properties.isEmpty();
    }


    /**
     * Adds the specified {@link VagrantProperty} to the {@link VagrantProperties}, returning a new
     * {@link VagrantProperties} containing the {@link VagrantProperty}.
     *
     * @param properties the {@link VagrantProperty}s to add
     *
     * @return  the a new {@link VagrantProperties} instance, including
     *          the existing {@link VagrantProperty}s and the new {@link VagrantProperty}
     */
    public VagrantProperties add(VagrantProperty... properties)
    {
        if (properties == null || properties.length == 0)
        {
            return this;
        }
        else
        {
            VagrantProperties vagrantProperties = new VagrantProperties(this);

            for (VagrantProperty property : properties)
            {
                vagrantProperties.properties.put(property.getName(), property);
            }

            return vagrantProperties;
        }
    }


    /**
     * Adds the specified {@link VagrantProperty} to the {@link VagrantProperties}, returning a new
     * {@link VagrantProperties} containing the {@link VagrantProperty}
     * (if and only if a {@link VagrantProperty} with the same name doesn't already exist).
     *
     * @param property the {@link VagrantProperty} to add
     *
     * @return the a new {@link VagrantProperties} instance, including the existing {@link VagrantProperty}s and the new {@link VagrantProperty}
     */
    public VagrantProperties addIfAbsent(VagrantProperty property)
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
     * Removes the specified {@link VagrantProperty} named property from the {@link VagrantProperties}, returning a new
     * {@link VagrantProperties} without the said {@link VagrantProperty}.
     *
     * @param propertyName the {@link VagrantProperty} to remove
     *
     * @return the a new {@link VagrantProperties} instance, excluding the specified {@link VagrantProperty}
     */
    public VagrantProperties remove(String propertyName)
    {
        if (propertyName == null || propertyName.isEmpty() ||!contains(propertyName))
        {
            return this;
        }
        else
        {
            VagrantProperties vagrantProperties = new VagrantProperties(this);

            vagrantProperties.properties.remove(propertyName);

            return vagrantProperties;
        }
    }


    /**
     * Adds all of the specified properties represented as a {@link Map} of name-value pairs
     * as individual {@link VagrantProperty}s, returning a new {@link VagrantProperties}.
     *
     * @param properties the {@link Map} of properties
     */
    public VagrantProperties addAll(Map<String, Object> properties)
    {
        VagrantProperties vagrantProperties = new VagrantProperties(this);

        for (String name : properties.keySet())
        {
            vagrantProperties.properties.put(name, VagrantProperty.of(name, properties.get(name)));
        }

        return vagrantProperties;
    }


    /**
     * Adds all of the {@link VagrantProperties} to this {@link VagrantProperties}
     * returning a new {@link VagrantProperties}.
     *
     * @param properties the {@link VagrantProperties}
     */
    public VagrantProperties addAll(VagrantProperties properties)
    {
        VagrantProperties vagrantProperties = new VagrantProperties(this);

        for (VagrantProperty property : properties)
        {
            vagrantProperties.properties.put(property.getName(), property);
        }

        return vagrantProperties;
    }


    /**
     * Determines if the {@link VagrantProperties} contains a {@link VagrantProperty}
     * with the specified name.
     *
     * @param propertyName the name of the {@link VagrantProperty}
     *
     * @return <code>true</code> if the {@link VagrantProperties} contains a {@link VagrantProperty}
     * with the specified name, <code>false</code> otherwise
     */
    public boolean contains(String propertyName)
    {
        return properties.containsKey(propertyName);
    }


    /**
     * Obtains the {@link VagrantProperty} with the specified name, returning <code>null</code> if
     * one is not found.
     *
     * @param propertyName the name of the desired {@link VagrantProperty}
     *
     * @return the {@link VagrantProperty} of <code>null</code> if not defined
     */
    public VagrantProperty get(String propertyName)
    {
        return properties.get(propertyName);
    }


    /**
     * Obtains the current value of the specified {@link VagrantProperty}.  If the property has
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
            VagrantProperty property = properties.get(name);

            return property.getValue();
        }
        else
        {
            return null;
        }
    }


    @Override
    public VagrantProperties with(VagrantProperty property)
    {
        return add(property);
    }


    @Override
    public VagrantProperties without(VagrantProperty property)
    {
        return remove(property.getName());
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(VagrantProperty.class))
        {
            return (Iterable<O>) properties.values();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public Iterator<VagrantProperty> iterator()
    {
        return properties.values().iterator();
    }


    /**
     * Writes the {@link VagrantProperties} to the specified {@link PrintWriter}.
     *
     * @param writer   the {@link PrintWriter}
     * @param sPrefix  the prefix for each property
     * @param sPad     the padding for each property
     */
    public void write(PrintWriter writer,
                      String      sPrefix,
                      String      sPad)
    {
        for (VagrantProperty property : properties.values())
        {
            property.write(writer, sPrefix, sPad);
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof VagrantProperties))
        {
            return false;
        }

        VagrantProperties that = (VagrantProperties) o;

        return properties.equals(that.properties);

    }


    @Override
    public int hashCode()
    {
        return properties.hashCode();
    }
}
