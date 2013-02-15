/*
 * File: AbstractApplicationSchemaTest.java
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

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit and Functional tests for {@link AbstractApplicationSchema}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractApplicationSchemaTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetExecutable() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh");

        assertThat(schema.getExecutableName(), is("test.sh"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetDirectory() throws Exception
    {
        File                    directory = new File("/tmp");
        SimpleApplicationSchema schema    = new SimpleApplicationSchema("test.sh");

        schema.setWorkingDirectory(directory);
        assertThat(schema.getWorkingDirectory(), is(directory));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetEnvironmentVariable() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").setEnvironmentVariable("test", "value");

        assertThat((String) schema.getEnvironmentVariablesBuilder().getProperty("test"), is("value"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetEnvironmentVariables() throws Exception
    {
        PropertiesBuilder environment = new PropertiesBuilder().setProperty("key-1",
                                                                            "value-1").setProperty("key-2", "value-2");

        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").setEnvironmentVariables(environment);

        assertThat((String) schema.getEnvironmentVariablesBuilder().getProperty("key-1"), is("value-1"));
        assertThat((String) schema.getEnvironmentVariablesBuilder().getProperty("key-2"), is("value-2"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetEnvironmentVariablesFromIterator() throws Exception
    {
        SimpleApplicationSchema schema   = new SimpleApplicationSchema("test.sh");

        Iterator                iterator = Arrays.asList("value-1", "value-2").iterator();

        schema.setEnvironmentVariable("test", iterator);
        assertThat((Iterator) schema.getEnvironmentVariablesBuilder().getProperty("test"), is(iterator));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldClearEnvironmentVariables() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").setEnvironmentVariable("test", "value");

        schema.clearEnvironmentVariables();
        assertThat(schema.getEnvironmentVariablesBuilder().size(), is(0));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetCloneEnvironmentFlagToTrue() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").setIsInherited(true);

        assertThat(schema.isInherited(), is(true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetCloneEnvironmentFlagToFalse() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh").setIsInherited(false);

        assertThat(schema.isInherited(), is(false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldDefaultCloneEnvironmentFlagToTrue() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh");

        assertThat(schema.isInherited(), is(false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldSetArguments() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh");

        schema.setArgument("arg1");
        schema.setArgument("arg2");

        assertThat(schema.getArguments(), contains("arg1", "arg2"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldAddArguments() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("test.sh");

        schema.addArgument("arg1");
        schema.addArgument("arg2");

        assertThat(schema.getArguments(), contains("arg1", "arg2"));
    }
}
