/*
 * File: TestClassesSpecificClassesTest.java
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

package com.oracle.bedrock.testsupport.junit.options;

import com.oracle.bedrock.testsupport.junit.JUnit4Test;
import com.oracle.bedrock.testsupport.junit.MyOtherTest;
import com.oracle.bedrock.testsupport.junit.options.TestClasses;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link TestClasses.SpecificClasses} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class TestClassesSpecificClassesTest
{
    @Test
    public void shouldBeEqual() throws Exception
    {
        Class[]                     classes      = new Class[]{JUnit4Test.class};
        TestClasses.SpecificClasses testClasses1 = (TestClasses.SpecificClasses) TestClasses.of(classes);
        TestClasses.SpecificClasses testClasses2 = (TestClasses.SpecificClasses) TestClasses.of(classes);

        assertThat(testClasses1.equals(testClasses2), is(true));
        assertThat(testClasses2.equals(testClasses1), is(true));
    }


    @Test
    public void shouldBeNotEqual() throws Exception
    {
        TestClasses.SpecificClasses testClasses1 = (TestClasses.SpecificClasses) TestClasses.of(new Class[]{JUnit4Test.class});
        TestClasses.SpecificClasses testClasses2 = (TestClasses.SpecificClasses) TestClasses.of(new Class[]{MyOtherTest.class});

        assertThat(testClasses1.equals(testClasses2), is(false));
        assertThat(testClasses2.equals(testClasses1), is(false));
    }


    @Test
    public void shouldCreateTestClassesFromSingleClass() throws Exception
    {
        Class[]                     classes     = new Class[]{JUnit4Test.class};
        TestClasses.SpecificClasses testClasses = (TestClasses.SpecificClasses) TestClasses.of(classes);
        Set<Class<?>>               set         = testClasses.resolveTestClasses();

        assertThat(set, is(notNullValue()));
        assertThat(set, containsInAnyOrder(classes));
    }


    @Test
    public void shouldCreateTestClassesFromMultipleClasses() throws Exception
    {
        Class[]                     classes     = new Class[]{JUnit4Test.class, MyOtherTest.class};
        TestClasses.SpecificClasses testClasses = (TestClasses.SpecificClasses) TestClasses.of(classes);
        Set<Class<?>>               set        = testClasses.resolveTestClasses();

        assertThat(set, is(notNullValue()));
        assertThat(set, containsInAnyOrder(classes));
    }

    @Test
    public void shouldRecreateClassListAfterSerialization() throws Exception
    {
        Class[]                     classes     = new Class[]{JUnit4Test.class, MyOtherTest.class};
        TestClasses.SpecificClasses testClasses = (TestClasses.SpecificClasses) TestClasses.of(classes);
        ByteArrayOutputStream       bytes       = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bytes))
        {
            out.writeObject(testClasses);
        }

        ObjectInputStream           in     = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        TestClasses.SpecificClasses result = (TestClasses.SpecificClasses) in.readObject();
        Set<Class<?>>               set   = result.resolveTestClasses();

        assertThat(set, is(notNullValue()));
        assertThat(set, containsInAnyOrder(classes));
    }
}
