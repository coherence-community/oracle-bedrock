/*
 * File: SimpleApplicationSchemaTest.java
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

import com.oracle.tools.runtime.options.EnvironmentVariables;

import org.hamcrest.Matchers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.contains;

import static org.junit.Assert.assertThat;

import java.io.File;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

/**
 * Unit Tests for {@link SimpleApplicationSchema}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleApplicationSchemaTest
{
    /**
     * Ensure that the Executable Name is returned as expected.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseExecutableName() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh");

        assertThat(schema.getExecutableName(), is("test.sh"));
    }


    /**
     * Ensure the Application working directory is set.
     *
     * @throws Exception
     */
    @Test
    public void shouldSetDirectory() throws Exception
    {
        File                    directory = new File("/tmp");
        SimpleApplicationSchema schema    = new SimpleApplicationSchema("test.sh");

        schema.setWorkingDirectory(directory);
        assertThat(schema.getWorkingDirectory(), Matchers.is(directory));
    }


    /**
     * Ensure an Environment Variable can be set.
     *
     * @throws Exception
     */
    @Test
    public void shouldSetEnvironmentVariable() throws Exception
    {
        EnvironmentVariables    environmentVariables = EnvironmentVariables.custom().set("test", "value");

        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").addOption(environmentVariables);

        Properties              variables = schema.getOptions().get(EnvironmentVariables.class).getBuilder().realize();

        assertThat(variables.getProperty("test"), Matchers.is("value"));
    }


    /**
     * Ensure an Environment Variables can be set.
     *
     * @throws Exception
     */
    @Test
    public void shouldSetEnvironmentVariables() throws Exception
    {
        EnvironmentVariables environmentVariables = EnvironmentVariables.custom().set("key-1",
                                                                                      "value-1").set("key-2",
                                                                                                     "value-2");

        SimpleApplicationSchema schema    = new SimpleApplicationSchema("test.sh").addOption(environmentVariables);

        Properties              variables = schema.getOptions().get(EnvironmentVariables.class).getBuilder().realize();

        assertThat(variables.getProperty("key-1"), Matchers.is("value-1"));
        assertThat(variables.getProperty("key-2"), Matchers.is("value-2"));
    }


    /**
     * Ensure Environment Variables can be set from an Iterator.
     *
     * @throws Exception
     */
    @Test
    public void shouldSetEnvironmentVariablesFromIterator() throws Exception
    {
        Iterator                iterator             = Arrays.asList("value-1", "value-2").iterator();

        EnvironmentVariables    environmentVariables = EnvironmentVariables.custom().set("test", iterator);

        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").addOption(environmentVariables);

        Properties              variables = schema.getOptions().get(EnvironmentVariables.class).getBuilder().realize();

        assertThat(variables.getProperty("test"), Matchers.is("value-1"));
    }


    /**
     * Ensure application arguments can be set.
     *
     * @throws Exception
     */
    @Test
    public void shouldSetArguments() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh");

        schema.addArgument("arg1");
        schema.addArgument("arg2");

        assertThat(schema.getArguments(), contains("arg1", "arg2"));
    }
}
