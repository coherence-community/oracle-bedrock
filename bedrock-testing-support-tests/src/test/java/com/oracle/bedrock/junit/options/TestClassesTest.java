/*
 * File: TestClassesTest.java
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

package com.oracle.bedrock.junit.options;

import com.oracle.bedrock.runtime.java.ClassPath;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link TestClasses} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class TestClassesTest
{
    @Test
    public void shouldCreateEmptyTestClasses() throws Exception
    {
        TestClasses   testClasses = TestClasses.empty();
        Set<Class<?>> classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes.isEmpty(), is(true));
    }


    @Test
    public void shouldCreateTestClassesFromNoClasses() throws Exception
    {
        TestClasses   testClasses = TestClasses.of();
        Set<Class<?>> classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes.isEmpty(), is(true));
    }


    @Test
    public void shouldCreateTestClassesFromEmptyClassPath() throws Exception
    {
        TestClasses   testClasses = TestClasses.from(new ClassPath());
        Set<Class<?>> classes     = testClasses.resolveTestClasses();

        assertThat(classes, is(notNullValue()));
        assertThat(classes.isEmpty(), is(true));
    }





    public class TestClassesStub extends TestClasses
    {
        @Override
        public Set<Class<?>> resolveTestClasses()
        {
            return null;
        }
    }
}
