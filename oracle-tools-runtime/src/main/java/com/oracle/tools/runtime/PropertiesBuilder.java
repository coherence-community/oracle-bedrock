/*
 * File: PropertiesBuilder.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.runtime.java.options.SystemProperties;

import java.io.IOException;

import java.net.URL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link PropertiesBuilder} defines a set of property definitions that when
 * realized may be used as a traditional {@link Map} of name value pair properties.
 * <p>
 * Unlike traditional {@link Map}-based implementations of properties, a
 * {@link PropertiesBuilder} provides the ability to specify an {@link Iterator}
 * for named property values, that when accessed will in turn return an acquire actual
 * property value, when the said {@link PropertiesBuilder} is realized.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PropertiesBuilder
{
    /**
     * The standard Java System properties.
     */
    public static final HashSet<String> STANDARD_SYSTEM_PROPERTIES = SystemProperties.STANDARD_SYSTEM_PROPERTY_NAMES;

    /**
     * The properties defined by the {@link PropertiesBuilder}.
     */
    private LinkedHashMap<String, Property> properties;


    /**
     * Constructs a {@link PropertiesBuilder} that will realize an empty
     * {@link PropertiesBuilder} if no properties definitions are added.
     */
    public PropertiesBuilder()
    {
        properties = new LinkedHashMap<String, Property>();
    }


    /**
     * Constructs a {@link PropertiesBuilder} with properties based on a
     * {@link Map} of name-value pairs.
     *
     * @param properties  the {@link Map} of properties to use as the basis for
     *                    the {@link PropertiesBuilder}
     */
    public PropertiesBuilder(Map<String, String> properties)
    {
        this();

        for (String name : properties.keySet())
        {
            this.properties.put(name, new Property(name, properties.get(name)));
        }
    }


    /**
     * Constructs a {@link PropertiesBuilder} based on a standard
     * {@link Properties} representation.
     *
     * @param properties  the {@link Properties} to use as the basis for the
     *                    {@link PropertiesBuilder}
     */
    public PropertiesBuilder(Properties properties)
    {
        this();

        for (String key : properties.stringPropertyNames())
        {
            this.properties.put(key, new Property(key, properties.getProperty(key)));
        }
    }


    /**
     * Constructs a {@link PropertiesBuilder} based on the properties defined
     * in another {@link PropertiesBuilder}.
     *
     * @param propertiesBuilder  the {@link PropertiesBuilder} on which to base
     *                           the new {@link PropertiesBuilder}
     */
    public PropertiesBuilder(PropertiesBuilder propertiesBuilder)
    {
        this();

        for (String name : propertiesBuilder.getPropertyNames())
        {
            this.properties.put(name, new Property(propertiesBuilder.properties.get(name)));
        }
    }


    /**
     * Obtains the number of properties defined by the {@link PropertiesBuilder}.
     *
     * @return the number of properties in this {@link PropertiesBuilder}
     */
    public int size()
    {
        return properties.size();
    }


    /**
     * Sets the specified named property to use an {@link Iterator}.
     * (to provide successive requests for the property with values from the said
     * {@link Iterator} when the {@link PropertiesBuilder} is realized)
     *
     * @param name      the name of the property
     * @param iterator  an {@link Iterator} that will provide successive property
     *                  values for the property when the {@link PropertiesBuilder}
     *                  is realized
     *
     * @return  the {@link PropertiesBuilder} to which the property was added so
     *          that further chained method calls, like to other
     *          <code>setProperty(...)</code> methods on this class may be used.
     */
    public PropertiesBuilder setProperty(String      name,
                                         Iterator<?> iterator)
    {
        if (containsProperty(name))
        {
            properties.get(name).setValue(iterator);
        }
        else
        {
            properties.put(name, new Property(name, iterator));
        }

        return this;
    }


    /**
     * Optionally sets the specified named property to use an {@link Iterator}
     * (to provide successive requests for the property with values from the said
     * {@link Iterator} when the {@link PropertiesBuilder} is realized)
     *
     * @param name      the name of the property
     * @param iterator  an {@link Iterator} that will provide successive property
     *                  values for the property when the {@link PropertiesBuilder}
     *                  is realized
     *
     * @return  the {@link PropertiesBuilder} to which the property was added so
     *          that further chained method calls, like to other
     *          <code>setProperty(...)</code> methods on this class may be used.
     */
    public PropertiesBuilder setPropertyIfAbsent(String      name,
                                                 Iterator<?> iterator)
    {
        if (containsProperty(name))
        {
            return this;
        }
        else
        {
            return setProperty(name, iterator);
        }
    }


    /**
     * Sets the specified named property to have the specified value.
     *
     * @param name   the name of the property
     * @param value  the value of the property
     *
     * @return  the {@link PropertiesBuilder} to which the property was added so
     *          that further chained method calls, like to other
     *          <code>setProperty(...)</code> methods on this class may be used
     */
    public PropertiesBuilder setProperty(String name,
                                         Object value)
    {
        if (containsProperty(name))
        {
            properties.get(name).setValue(value);
        }
        else
        {
            properties.put(name, new Property(name, value));
        }

        return this;
    }


    /**
     * Optionally sets the specified named property to have the specified value.
     *
     * @param name   the name of the property
     * @param value  the value of the property
     *
     * @return  the {@link PropertiesBuilder} to which the property was added so
     *          that further chained method calls, like to other
     *          <code>setProperty(...)</code> methods on this class may be used
     */
    public PropertiesBuilder setPropertyIfAbsent(String name,
                                                 Object value)
    {
        if (containsProperty(name))
        {
            return this;
        }
        else
        {
            return setProperty(name, value);
        }
    }


    /**
     * Adds and/or overrides the properties defined in the
     * {@link PropertiesBuilder} with those from the specified {@link PropertiesBuilder}.
     *
     * @param propertiesBuilder  the {@link PropertiesBuilder} containing the
     *                           properties to add to this {@link PropertiesBuilder}.
     *
     * @return  the {@link PropertiesBuilder} to which the property was added so
     *          that further chained method calls, like to other
     *          <code>setProperty(...)</code> methods on this class may be used
     */
    public PropertiesBuilder setProperties(PropertiesBuilder propertiesBuilder)
    {
        properties.putAll(propertiesBuilder.properties);

        return this;
    }


    /**
     * Adds and/or overrides the properties defined in the {@link PropertiesBuilder}
     * with those from the specified {@link PropertiesBuilder}.
     *
     * @param propertiesBuilder  the {@link PropertiesBuilder} containing the
     * properties to add to this {@link PropertiesBuilder}.
     *
     * @return  the {@link PropertiesBuilder} to which the property was added so
     *          that further chained method calls, like to other
     *          <code>setProperty(...)</code> methods on this class may be used
     */
    public PropertiesBuilder addProperties(PropertiesBuilder propertiesBuilder)
    {
        setProperties(propertiesBuilder);

        return this;
    }


    /**
     * Determines if the specified named property is defined by the
     * {@link PropertiesBuilder}.
     *
     * @param name  the name of the property
     *
     * @return <code>true</code> if the property is defined by the
     *         {@link PropertiesBuilder}, <code>false</code> otherwise
     */
    public boolean containsProperty(String name)
    {
        return properties.containsKey(name);
    }


    /**
     * Obtains the current value of the specified property.  If the property has
     * a value specified, that value will be used.  If the property is unknown,
     * <code>null</code> will be returned.
     *
     * @param name  the name of the property
     *
     * @return an {@link Object}
     */
    public Object getProperty(String name)
    {
        if (properties.containsKey(name))
        {
            Property property = properties.get(name);

            return property.getValue();
        }
        else
        {
            return null;
        }
    }


    /**
     * Removes the specified named property from the {@link PropertiesBuilder}.
     * <p>
     * If the specified property is not contained by the {@link PropertiesBuilder},
     * nothing happens.
     *
     * @param name The name of the property to remove.
     */
    public void removeProperty(String name)
    {
        properties.remove(name);
    }


    /**
     * Removes the specified named property value from {@link PropertiesBuilder}.
     * <p>
     * If the specified property is not contained by the {@link PropertiesBuilder},
     * nothing happens.
     *
     * @param name The name of the property value to remove.
     */
    @Deprecated
    public void removePropertyValue(String name)
    {
        Property property = properties.get(name);

        if (property != null)
        {
            property.setValue(null);
        }
    }


    /**
     * Clears all of the currently defined properties from the
     * {@link PropertiesBuilder}.
     */
    public void clear()
    {
        properties.clear();
    }


    /**
     * Obtains an {@link Iterable} over the property names defined by the
     * {@link PropertiesBuilder}.
     *
     * @return an {@link Iterable}
     */
    public Iterable<String> getPropertyNames()
    {
        return properties.keySet();
    }


    /**
     * Creates a new {@link Properties} instance containing name, value pairs
     * defined by the {@link PropertiesBuilder}.
     * <p>
     * If a property with in the {@link PropertiesBuilder} is defined as an
     * {@link Iterator}, the next value from the said {@link Iterator} is used
     * as a value for the property.
     *
     * @param overrides  (optional may be <code>null</code>) a
     *                   {@link PropertiesBuilder} specifying properties that
     *                   will must be used to override those present in this
     *                   {@link PropertiesBuilder} when realizing the
     *                   {@link Properties}
     *
     * @return a new {@link Properties} instance as defined by the {@link PropertiesBuilder}
     */
    public Properties realize(PropertiesBuilder overrides)
    {
        return realize(overrides, null);
    }


    /**
     * Creates a new {@link Properties} instance containing name, value pairs
     * defined by the {@link PropertiesBuilder}.
     * <p>
     * If a property with in the {@link PropertiesBuilder} is defined as an
     * {@link Iterator}, the next value from the said {@link Iterator} is used
     * as a value for the property.
     *
     * @param overrides  (optional may be <code>null</code>) a
     *                   {@link PropertiesBuilder} specifying properties that
     *                   will must be used to override those present in this
     *                   {@link PropertiesBuilder} when realizing the
     *                   {@link Properties}
     * @param platform   the {@link Platform} that the {@link Properties} are being realized for
     *
     * @return a new {@link Properties} instance as defined by the {@link PropertiesBuilder}
     */
    public Properties realize(PropertiesBuilder overrides,
                              Platform          platform)
    {
        Properties properties = new Properties();

        // add all of the override properties first
        if (overrides != null)
        {
            properties.putAll(overrides.realize());
        }

        for (String name : getPropertyNames())
        {
            if (!properties.containsKey(name))
            {
                Object value = getProperty(name);

                if (value != null)
                {
                    if (value instanceof Iterator<?>)
                    {
                        Iterator<?> iterator = (Iterator<?>) value;

                        if (iterator.hasNext())
                        {
                            properties.put(name, iterator.next().toString());
                        }
                        else
                        {
                            throw new IndexOutOfBoundsException(String
                                .format("No more values available for the property [%s]", name));
                        }
                    }
                    else
                    {
                        properties.put(name, value.toString());
                    }
                }
            }
        }

        return properties;
    }


    /**
     * Creates a new {@link Properties} instance containing name, value pairs
     * defined by the {@link PropertiesBuilder}.
     * <p>
     * If a property with in the {@link PropertiesBuilder} is defined as an
     * {@link Iterator}, the next value from the said {@link Iterator} is used
     * as a value for the property.
     *
     * @return  a new {@link Properties} instance as defined by the {@link PropertiesBuilder}
     */
    public Properties realize()
    {
        return realize(null);
    }


    /**
     * A helper to construct a {@link PropertiesBuilder} based on the
     * properties defined in the specified Java properties file.
     *
     * @param fileName  the name of the file (including path if required) from
     *                  which to load the properties
     *
     * @return a {@link PropertiesBuilder}
     *
     * @throws IOException  should a problem occur while loading the properties
     */
    public static PropertiesBuilder fromPropertiesFile(String fileName) throws IOException
    {
        Properties properties = new Properties();
        URL        url        = ClassLoader.getSystemResource(fileName);

        properties.load(url.openStream());

        return new PropertiesBuilder(properties);
    }


    /**
     * A helper to construct a {@link PropertiesBuilder} based on the
     * operating system environment variables defined for the
     * currently executing process.
     *
     * @return  a {@link PropertiesBuilder}
     */
    public static PropertiesBuilder fromCurrentEnvironmentVariables()
    {
        return new PropertiesBuilder(System.getenv());
    }


    /**
     * A helper to construct a {@link PropertiesBuilder} based on the
     * non-standard Java system properties, ie: those that aren't in the set
     * {@link #STANDARD_SYSTEM_PROPERTIES}, defined by the currently
     * executing process.
     *
     * @return  a {@link PropertiesBuilder}
     *
     * @see #STANDARD_SYSTEM_PROPERTIES
     */
    public static PropertiesBuilder fromCurrentNonStandardSystemProperties()
    {
        // grab a copy of the current system properties
        Properties systemProperties = System.getProperties();

        // filter out the standard system properties
        Properties properties = new Properties();

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            if (!STANDARD_SYSTEM_PROPERTIES.contains(propertyName))
            {
                properties.put(propertyName, systemProperties.get(propertyName));
            }
        }

        // return a builder of the filtered properties
        return new PropertiesBuilder(properties);
    }


    /**
     * A {@link Property} represents the defined value for a specified named property.
     */
    private static class Property
    {
        /**
         * The name of the {@link Property}.
         */
        private String name;

        /**
         * The value of the {@link Property}.
         */
        private Object value;


        /**
         * Constructs a {@link Property} based on another {@link Property}.
         *
         * @param property  the {@link Property} from which to construct
         *                  (copy) the new {@link Property}
         */
        public Property(Property property)
        {
            this.name  = property.getName();
            this.value = property.getValue();
        }


        /**
         * Constructs a {@link Property}.
         *
         * @param name          the name of the {@link Property}
         * @param value         the value of the {@link Property}
         */
        public Property(String name,
                        Object value)
        {
            this.name  = name;
            this.value = value;
        }


        /**
         * Obtains the name of the {@link Property}.
         *
         * @return  the name of the {@link Property}
         */
        public String getName()
        {
            return name;
        }


        /**
         * Obtains the value of the {@link Property}
         *
         * @return  the value of the {@link Property}
         */
        public Object getValue()
        {
            return value;
        }


        /**
         * Sets the value of the {@link Property}
         *
         * @param value  the value of the {@link Property}
         */
        public void setValue(Object value)
        {
            this.value = value;
        }


        /**
         * Determines if the {@link Property} has a defined value.
         *
         * @return  <code>true</code> if the {@link Property} has a defined value
         *          or <code>false</code> otherwise
         */
        public boolean hasValue()
        {
            return value != null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return String.format("{name=%s, value=%s}", name, value);
        }
    }
}
