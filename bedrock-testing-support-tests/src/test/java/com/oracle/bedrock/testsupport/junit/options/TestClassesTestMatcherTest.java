/*
 * File: TestClassesTestMatcherTest.java
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

import com.oracle.bedrock.testsupport.junit.options.TestClasses;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link TestClasses.TestMatcher} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class TestClassesTestMatcherTest
{
    @Test
    public void shouldHaveOnlyClassPattern() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("foo");

        assertThat(matcher.hasClassPattern(), is(true));
        assertThat(matcher.hasMethodPattern(), is(false));

        assertThat(matcher.getClassPattern(), is("foo"));
        assertThat(matcher.getMethodPattern(), is(nullValue()));
    }


    @Test
    public void shouldHaveOnlyClassPatternIfMethodPatternIsBlank() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("foo#   ");

        assertThat(matcher.hasClassPattern(), is(true));
        assertThat(matcher.hasMethodPattern(), is(false));

        assertThat(matcher.getClassPattern(), is("foo"));
        assertThat(matcher.getMethodPattern(), is(""));
    }


    @Test
    public void shouldHaveOnlyMethodPattern() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("#foo");

        assertThat(matcher.hasClassPattern(), is(false));
        assertThat(matcher.hasMethodPattern(), is(true));

        assertThat(matcher.getClassPattern(), is(nullValue()));
        assertThat(matcher.getMethodPattern(), is("foo"));
    }


    @Test
    public void shouldHaveOnlyMethodPatternIfClassPatternIsBlank() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("    #foo");

        assertThat(matcher.hasClassPattern(), is(false));
        assertThat(matcher.hasMethodPattern(), is(true));

        assertThat(matcher.getClassPattern(), is(""));
        assertThat(matcher.getMethodPattern(), is("foo"));
    }


    @Test
    public void shouldHaveClassAndMethodPattern() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("foo#bar");

        assertThat(matcher.hasClassPattern(), is(true));
        assertThat(matcher.hasMethodPattern(), is(true));

        assertThat(matcher.getClassPattern(), is("foo"));
        assertThat(matcher.getMethodPattern(), is("bar"));
    }


    @Test
    public void shouldBeEqual() throws Exception
    {
        TestClasses.TestMatcher matcher1 = new TestClasses.TestMatcher("foo#bar");
        TestClasses.TestMatcher matcher2 = new TestClasses.TestMatcher("foo#bar");

        assertThat(matcher1.equals(matcher2), is(true));
    }


    @Test
    public void shouldBeNotEqual() throws Exception
    {
        TestClasses.TestMatcher matcher1 = new TestClasses.TestMatcher("foo#bar");
        TestClasses.TestMatcher matcher2 = new TestClasses.TestMatcher("foo1#bar");
        TestClasses.TestMatcher matcher3 = new TestClasses.TestMatcher("foo");
        TestClasses.TestMatcher matcher4 = new TestClasses.TestMatcher("#bar");

        assertThat(matcher1.equals(matcher2), is(false));
        assertThat(matcher1.equals(matcher3), is(false));
        assertThat(matcher1.equals(matcher4), is(false));
    }


    @Test
    public void shouldMatchClassName() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("foo#bar");

        assertThat(matcher.matches("foo"), is(true));
        assertThat(matcher.matches("bar"), is(false));
    }


    @Test
    public void shouldMatchClassAndMethodName() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("foo#bar");

        assertThat(matcher.matches("foo", "bar"), is(true));
        assertThat(matcher.matches("foo", "bar2"), is(false));
        assertThat(matcher.matches("bar", "foo"), is(false));
    }


    @Test
    public void shouldMatchClassNameIfClassPatternIsNull() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("#bar");

        assertThat(matcher.matches("foo"), is(true));
    }


    @Test
    public void shouldMatchMethodNameIfMethodPatternIsNull() throws Exception
    {
        TestClasses.TestMatcher matcher = new TestClasses.TestMatcher("foo");

        assertThat(matcher.matches("foo", "bar"), is(true));
    }
}
