/*
 * File: PropertiesBuilderTest.java
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

package com.oracle.bedrock.runtime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Unit Tests for {@link PropertiesBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class PropertiesBuilderTest
{
    /**
     * Ensure the default properties are empty.
     */
    @Test
    public void shouldDefaultToEmptyProperties()
    {
        PropertiesBuilder builder    = new PropertiesBuilder();
        Properties        properties = builder.realize();

        assertThat(properties.size(), is(0));
    }


    /**
     * Ensure values are copied when passed into a constructor.
     */
    @Test
    public void shouldUseMapOfValuesFromConstructor()
    {
        Map<String, String> values = new HashMap<String, String>();

        values.put("Key-1", "Value-1");
        values.put("Key-2", "Value-2");

        Properties expected = new Properties();

        expected.putAll(values);

        PropertiesBuilder builder    = new PropertiesBuilder(values);
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure properties are copied when passed into a constructor.
     */
    @Test
    public void shouldUsePropertiesFromConstructor()
    {
        Properties expected = new Properties();

        expected.setProperty("Key-1", "Value-1");
        expected.setProperty("Key-2", "Value-2");

        PropertiesBuilder builder    = new PropertiesBuilder(expected);
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure properties are inherited from a builder.
     */
    @Test
    public void shouldUsePropertiesBuilderFromConstructor()
    {
        Properties expected = new Properties();

        expected.setProperty("Key-1", "Value-1");
        expected.setProperty("Key-2", "Value-2");

        PropertiesBuilder parent     = new PropertiesBuilder(expected);

        PropertiesBuilder builder    = new PropertiesBuilder(parent);
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure properties can be added from a builder.
     */
    @Test
    public void shouldAddPropertiesBuilder()
    {
        Properties expected = new Properties();

        expected.setProperty("Key-1", "Value-1");
        expected.setProperty("Key-2", "Value-2");

        PropertiesBuilder parent  = new PropertiesBuilder(expected);

        PropertiesBuilder builder = new PropertiesBuilder();

        builder.addProperties(parent);

        Properties properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure cleared builder results in empty properties.
     */
    @Test
    public void shouldClear()
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        builder.clear();

        Properties properties = builder.realize();

        assertThat(properties.size(), is(0));
    }


    /**
     * Ensure a set property is remember by a builder.
     */
    @Test
    public void shouldContainProperty()
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        assertThat(builder.containsProperty("Key-1"), is(true));
    }


    /**
     * Ensure the system environment can be copied into a builder.
     */
    @Test
    public void shouldCopyEnvironment()
    {
        Properties expected = new Properties();

        expected.putAll(System.getenv());

        PropertiesBuilder builder    = PropertiesBuilder.fromCurrentEnvironmentVariables();
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure system variables can be copied into a builder.
     */
    @Test
    public void shouldCopySystemProperties()
    {
        Properties expected = new Properties();

        expected.putAll(System.getProperties());

        PropertiesBuilder builder    = new PropertiesBuilder(System.getProperties());
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure non-standard system variables are only copied into a builder.
     */
    @Test
    public void shouldCopyNonStandardSystemProperties()
    {
        // ensure we have one non-standard property
        int    count         = 0;
        String propertyName  = "com.oracle.bedrock.custom.property." + count;
        String propertyValue = "hello world";

        while (System.getProperty(propertyName) != null)
        {
            count++;
            propertyName = "com.oracle.bedrock.custom.property." + count;
        }

        System.setProperty(propertyName, propertyValue);

        Properties expected         = new Properties();

        Properties systemProperties = System.getProperties();

        for (String name : systemProperties.stringPropertyNames())
        {
            if (!PropertiesBuilder.STANDARD_SYSTEM_PROPERTIES.contains(name))
            {
                expected.put(name, systemProperties.get(name));
            }
        }

        PropertiesBuilder builder    = PropertiesBuilder.fromCurrentNonStandardSystemProperties();
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));

        // remove the property we added
        System.getProperties().remove(propertyName);
    }


    /**
     * Ensure we can load properties into a builder.
     */
    @Test
    public void shouldLoadFromPropertiesFile() throws Exception
    {
        Properties expected = new Properties();

        expected.setProperty("Key-1", "Value-1");
        expected.setProperty("Key-2", "Value-2");

        PropertiesBuilder builder    = PropertiesBuilder.fromPropertiesFile("propertiesbuilder-test.properties");
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Ensure we can override properties when realizing them.
     */
    @Test
    public void shouldRealizeWithPropertiesBuilderOverrides()
    {
        PropertiesBuilder overrides = new PropertiesBuilder();

        overrides.setProperty("Key-1", "Value-1");
        overrides.setProperty("Key-2", "Value-2");

        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1-1");
        builder.setProperty("Key-2", "Value-2-1");
        builder.setProperty("Key-3", "Value-3-1");

        Properties expected = new Properties();

        expected.setProperty("Key-1", "Value-1");
        expected.setProperty("Key-2", "Value-2");
        expected.setProperty("Key-3", "Value-3-1");

        Properties properties = builder.realize(overrides);

        assertThat(properties, is(expected));
    }


    /**
     * Ensure we can iterate over values.
     */
    @Test
    public void shouldUseIteratorForPropertyValue()
    {
        List<String>      values  = Arrays.asList("one", "two");

        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key", values.iterator());

        Properties propertiesOne = builder.realize();
        Properties propertiesTwo = builder.realize();

        assertThat(propertiesOne.getProperty("Key"), is("one"));
        assertThat(propertiesTwo.getProperty("Key"), is("two"));
    }


    /**
     * Ensure we can remove a property from a builder.
     */
    @Test
    public void shouldRemoveProperty()
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        builder.removeProperty("Key-1");

        Properties properties = builder.realize();

        assertThat(properties.size(), is(1));
        assertThat(properties.getProperty("Key-2"), is("Value-2"));
    }


    /**
     * Ensure we can set a property on a builder.
     */
    @Test
    public void shouldSetPropertyFromObject()
    {
        Object            value   = new Object();

        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", value);

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is(String.valueOf(value)));
    }


    /**
     * Ensure the correct number of properties are realized.
     */
    @Test
    public void shouldReturnCorrectSize()
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        assertThat(builder.size(), is(2));
    }


    /**
     * Ensure properties are set when using
     * {@link PropertiesBuilder#setPropertyIfAbsent(String, Object)}.
     */
    @Test
    public void shouldSetPropertyIfAbsent()
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setPropertyIfAbsent("Key-1", "Value-1-Default");

        Iterator<String> iterator = Collections.singletonList("One").iterator();

        builder.setPropertyIfAbsent("Key-2", iterator);

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is("Value-1-Default"));
        assertThat(properties.getProperty("Key-2"), is("One"));
    }


    /**
     * Ensure properties are not set when using
     * {@link PropertiesBuilder#setPropertyIfAbsent(String, Object)}.
     */
    @Test
    public void shouldNotSetPropertyIfAbsent()
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setPropertyIfAbsent("Key-1", "Value-1-Default");
        builder.setPropertyIfAbsent("Key-1", "Other");

        Iterator<String> iterator = Collections.singletonList("One").iterator();

        builder.setPropertyIfAbsent("Key-2", iterator);
        builder.setPropertyIfAbsent("Key-2", "Other");

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is("Value-1-Default"));
        assertThat(properties.getProperty("Key-2"), is("One"));
    }
}
