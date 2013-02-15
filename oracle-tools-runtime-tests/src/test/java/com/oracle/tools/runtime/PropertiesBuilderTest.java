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

package com.oracle.tools.runtime;

import com.oracle.tools.junit.AbstractTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit and Functional tests for {@link ProcessBuilder}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class PropertiesBuilderTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldDefaultToEmptyProperties() throws Exception
    {
        PropertiesBuilder builder    = new PropertiesBuilder();
        Properties        properties = builder.realize();

        assertThat(properties.size(), is(0));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseMapOfValuesFromConstructor() throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUsePropertiesFromConstructor() throws Exception
    {
        Properties expected = new Properties();

        expected.setProperty("Key-1", "Value-1");
        expected.setProperty("Key-2", "Value-2");

        PropertiesBuilder builder    = new PropertiesBuilder(expected);
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUsePropertiesBuilderFromConstructor() throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldAddPropertiesBuilder() throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldClear() throws Exception
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        builder.clear();

        Properties properties = builder.realize();

        assertThat(properties.size(), is(0));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldContainProperty() throws Exception
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        assertThat(builder.containsProperty("Key-1"), is(true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCopyEnvironment() throws Exception
    {
        Properties expected = new Properties();

        expected.putAll(System.getenv());

        PropertiesBuilder builder    = PropertiesBuilder.fromCurrentEnvironmentVariables();
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCopySystemProperties() throws Exception
    {
        Properties expected = new Properties();

        expected.putAll(System.getProperties());

        PropertiesBuilder builder    = PropertiesBuilder.fromCurrentSystemProperties();
        Properties        properties = builder.realize();

        assertThat(properties, is(expected));
    }


    /**
     * Method description
     *
     * @throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldRealizeWithPropertiesBuilderOverrides() throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseIteratorForPropertyValue() throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldRemoveProperty() throws Exception
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
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetPropertyFromObject() throws Exception
    {
        Object            value   = new Object();

        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", value);

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is(String.valueOf(value)));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldReturnCorrectSize() throws Exception
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-2", "Value-2");

        assertThat(builder.size(), is(2));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultIfPropertyNotSet() throws Exception
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setDefaultProperty("Key-1", "Value-1-Default");

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is("Value-1-Default"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldNotUseDefaultIfPropertySet() throws Exception
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setDefaultProperty("Key-1", "Value-1-Default");
        builder.setProperty("Key-1", "Value-1");

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is("Value-1"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultIfPropertySetThenUnset() throws Exception
    {
        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setDefaultProperty("Key-1", "Value-1-Default");
        builder.setProperty("Key-1", "Value-1");
        builder.setProperty("Key-1", null);

        Properties properties = builder.realize();

        assertThat(properties.getProperty("Key-1"), is("Value-1-Default"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultIteratorProperty() throws Exception
    {
        List<String>      values  = Arrays.asList("one", "two");

        PropertiesBuilder builder = new PropertiesBuilder();

        builder.setDefaultProperty("Key-1", values.iterator());

        Properties properties1 = builder.realize();
        Properties properties2 = builder.realize();

        assertThat(properties1.getProperty("Key-1"), is("one"));
        assertThat(properties2.getProperty("Key-1"), is("two"));
    }
}
