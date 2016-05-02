/*
 * File: EnvironmentVariablesTest.java
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

import com.oracle.bedrock.runtime.LocalPlatform;

import org.hamcrest.Matchers;

import org.junit.Test;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

/**
 * Tests for the {@link Executable} {@link Option}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 */
public class EnvironmentVariablesTest
{
    /**
     * Ensure we can create an {@link EnvironmentVariables} using a single key/value.
     */
    @Test
    public void shouldCreateEnvironmentVariablesWithSingleVariable()
    {
        EnvironmentVariables environmentVariables = EnvironmentVariables.custom().set("test", "value");

        Properties           variables            = environmentVariables.realize(LocalPlatform.get());

        assertThat(variables.getProperty("test"), Matchers.is("value"));
    }


    /**
     * Ensure we can create an {@link EnvironmentVariables} using a multiple keys/values.
     */
    @Test
    public void shouldCreateEnvironmentVariablesWithMultipleVariables() throws Exception
    {
        EnvironmentVariables environmentVariables = EnvironmentVariables.custom().set("key-1",
                                                                                      "value-1").set("key-2",
                                                                                                     "value-2");

        Properties variables = environmentVariables.realize(LocalPlatform.get());

        assertThat(variables.getProperty("key-1"), Matchers.is("value-1"));
        assertThat(variables.getProperty("key-2"), Matchers.is("value-2"));
    }


    /**
     * Ensure we can create an {@link EnvironmentVariables} using a {@link Iterator} for values.
     */
    @Test
    public void shouldCreateEnvironmentVariablesUsingAnIterator() throws Exception
    {
        Iterator             iterator             = Arrays.asList("value-1", "value-2").iterator();

        EnvironmentVariables environmentVariables = EnvironmentVariables.custom().set("test", iterator);

        Properties           variables            = environmentVariables.realize(LocalPlatform.get());

        assertThat(variables.getProperty("test"), Matchers.is("value-1"));
    }
}
