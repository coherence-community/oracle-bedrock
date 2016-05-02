/*
 * File: TestClassesIncludeExcludePredicateTest.java
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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link TestClasses.IncludeExcludePredicate} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class TestClassesIncludeExcludePredicateTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void shouldEvaluateTrueIfIncludeAndExcludeAreEmpty() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>();
        Set<TestClasses.TestMatcher> excludes = new HashSet<>();
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);

        assertThat(predicate.test(Integer.class), is(true));

        verify(inner).test(Integer.class);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void shouldEvaluateFalseIfIncludeAndExcludeAreEmptyButPredicateReturnsFalse() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>();
        Set<TestClasses.TestMatcher> excludes = new HashSet<>();
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(false);

        assertThat(predicate.test(Integer.class), is(false));

        verify(inner).test(Integer.class);
    }


    @Test
    public void shouldEvaluateTrueIfIncludesMatchAndExcludesEmpty() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      include1 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      include2 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>(Arrays.asList(include1, include2));
        Set<TestClasses.TestMatcher> excludes = new HashSet<>();
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(include1.hasClassPattern()).thenReturn(true);
        when(include1.matches(anyString())).thenReturn(false);
        when(include2.hasClassPattern()).thenReturn(true);
        when(include2.matches(anyString())).thenReturn(true);

        assertThat(predicate.test(Integer.class), is(true));

        verify(include1, atMost(1)).matches(Integer.class.getCanonicalName());
        verify(include2).matches(Integer.class.getCanonicalName());
    }


    @Test
    public void shouldEvaluateFalseIfIncludesDoNotMatchAndExcludesEmpty() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      include1 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      include2 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>(Arrays.asList(include1, include2));
        Set<TestClasses.TestMatcher> excludes = new HashSet<>();
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(include1.hasClassPattern()).thenReturn(true);
        when(include1.matches(anyString())).thenReturn(false);
        when(include2.hasClassPattern()).thenReturn(true);
        when(include2.matches(anyString())).thenReturn(false);

        assertThat(predicate.test(Integer.class), is(false));

        verify(include1).matches(Integer.class.getCanonicalName());
        verify(include2).matches(Integer.class.getCanonicalName());
    }


    @Test
    public void shouldEvaluateTrueIfIncludesMatchAndExcludesDoNotMatch() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      include1 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      include2 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      exclude1 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      exclude2 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>(Arrays.asList(include1, include2));
        Set<TestClasses.TestMatcher> excludes = new HashSet<>(Arrays.asList(exclude1, exclude2));
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(include1.hasClassPattern()).thenReturn(true);
        when(include1.matches(anyString())).thenReturn(false);
        when(include2.hasClassPattern()).thenReturn(true);
        when(include2.matches(anyString())).thenReturn(true);
        when(exclude1.hasMethodPattern()).thenReturn(false);
        when(exclude1.matches(anyString())).thenReturn(false);
        when(exclude2.hasMethodPattern()).thenReturn(false);
        when(exclude2.matches(anyString())).thenReturn(false);

        assertThat(predicate.test(Integer.class), is(true));

        verify(include1, atMost(1)).matches(Integer.class.getCanonicalName());
        verify(include2).matches(Integer.class.getCanonicalName());

        verify(exclude1).matches(Integer.class.getCanonicalName());
        verify(exclude2).matches(Integer.class.getCanonicalName());
    }


    @Test
    public void shouldEvaluateTrueIfIncludesEmptyAndExcludesDoNotMatch() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      exclude1 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      exclude2 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>();
        Set<TestClasses.TestMatcher> excludes = new HashSet<>(Arrays.asList(exclude1, exclude2));
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(exclude1.hasMethodPattern()).thenReturn(false);
        when(exclude1.matches(anyString())).thenReturn(false);
        when(exclude2.hasMethodPattern()).thenReturn(false);
        when(exclude2.matches(anyString())).thenReturn(false);

        assertThat(predicate.test(Integer.class), is(true));

        verify(exclude1).matches(Integer.class.getCanonicalName());
        verify(exclude2).matches(Integer.class.getCanonicalName());
    }


    @Test
    public void shouldEvaluateFalseIfIncludesEmptyAndExcludesMatch() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      exclude1 = mock(TestClasses.TestMatcher.class);
        TestClasses.TestMatcher      exclude2 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>();
        Set<TestClasses.TestMatcher> excludes = new HashSet<>(Arrays.asList(exclude1, exclude2));
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(exclude1.hasMethodPattern()).thenReturn(false);
        when(exclude1.matches(anyString())).thenReturn(false);
        when(exclude2.hasMethodPattern()).thenReturn(false);
        when(exclude2.matches(anyString())).thenReturn(true);

        assertThat(predicate.test(Integer.class), is(false));

        verify(exclude1, atMost(1)).matches(Integer.class.getCanonicalName());
        verify(exclude2).matches(Integer.class.getCanonicalName());
    }


    @Test
    public void shouldNotEvaluateIncludesWithoutClassPattern() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      include1 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = Collections.singleton(include1);
        Set<TestClasses.TestMatcher> excludes = new HashSet<>();
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(include1.hasClassPattern()).thenReturn(false);
        when(include1.matches(anyString())).thenReturn(false);

        assertThat(predicate.test(Integer.class), is(true));

        verify(include1, never()).matches(anyString());
    }


    @Test
    public void shouldNotEvaluateExcludesWithMethodPattern() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      exclude1 = mock(TestClasses.TestMatcher.class);
        Set<TestClasses.TestMatcher> includes = new HashSet<>();
        Set<TestClasses.TestMatcher> excludes = Collections.singleton(exclude1);
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);
        when(exclude1.hasMethodPattern()).thenReturn(true);
        when(exclude1.matches(anyString())).thenReturn(true);

        assertThat(predicate.test(Integer.class), is(true));

        verify(exclude1, never()).matches(anyString());
    }


    @Test
    public void shouldEvaluate() throws Exception
    {
        Predicate<Class<?>>          inner    = mock(Predicate.class);
        TestClasses.TestMatcher      include1 = new TestClasses.TestMatcher("java.lang.Integer#bar1");
        TestClasses.TestMatcher      include2 = new TestClasses.TestMatcher("java.lang.String");
        TestClasses.TestMatcher      include3 = new TestClasses.TestMatcher("java.lang.Double");
        TestClasses.TestMatcher      exclude1 = new TestClasses.TestMatcher("java.lang.Double");
        TestClasses.TestMatcher      exclude2 = new TestClasses.TestMatcher("java.lang.String#bar2");
        Set<TestClasses.TestMatcher> includes = new HashSet<>(Arrays.asList(include1, include2, include3));
        Set<TestClasses.TestMatcher> excludes = new HashSet<>(Arrays.asList(exclude1, exclude2));
        ;
        TestClasses.IncludeExcludePredicate predicate = new TestClasses.IncludeExcludePredicate(inner,
                                                                                                includes,
                                                                                                excludes);

        when(inner.test(any(Class.class))).thenReturn(true);

        assertThat(predicate.test(Integer.class), is(true));
        assertThat(predicate.test(String.class), is(true));
        assertThat(predicate.test(Object.class), is(false));
        assertThat(predicate.test(Double.class), is(false));
    }
}
