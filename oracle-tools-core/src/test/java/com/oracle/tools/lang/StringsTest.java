/*
 * File: StringsTest.java
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

package com.oracle.tools.lang;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

/**
 * Unit Tests for {@link Strings}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class StringsTest
{
    /**
     * Ensure {@link Strings#unquote(String)} doesn't fail for
     * <code>null</code> {@link String}s.
     */
    @Test
    public void shouldUnquoteNullString()
    {
        String string = Strings.unquote(null);

        assertThat(string, is(nullValue()));
    }


    /**
     * Ensure {@link Strings#unquote(String)} doesn't fail for
     * {@link String}s without quotes.
     */
    @Test
    public void shouldUnquoteStringWithoutQuotes()
    {
        String string1 = Strings.unquote("");

        assertThat(string1, is(""));

        String string2 = Strings.unquote("hello");

        assertThat(string2, is("hello"));

        String string3 = Strings.unquote("hello world");

        assertThat(string3, is("hello world"));
    }


    /**
     * Ensure {@link Strings#unquote(String)} removes single-quotes
     * from {@link String}s.
     */
    @Test
    public void shouldUnquoteSingleQuotedStrings()
    {
        String string1 = Strings.unquote("\'\'");

        assertThat(string1, is(""));

        String string2 = Strings.unquote(" \'\' ");

        assertThat(string2, is(""));

        String string3 = Strings.unquote("\'hello\'");

        assertThat(string3, is("hello"));

        String string4 = Strings.unquote("\'hello world\'");

        assertThat(string4, is("hello world"));

        String string5 = Strings.unquote(" \'hello world\' ");

        assertThat(string5, is("hello world"));
    }


    /**
     * Ensure {@link Strings#unquote(String)} removes double-quotes
     * from {@link String}s.
     */
    @Test
    public void shouldUnquoteDoubleQuotedStrings()
    {
        String string1 = Strings.unquote("\"\"");

        assertThat(string1, is(""));

        String string2 = Strings.unquote(" \"\" ");

        assertThat(string2, is(""));

        String string3 = Strings.unquote("\"hello\"");

        assertThat(string3, is("hello"));

        String string4 = Strings.unquote("\"hello world\"");

        assertThat(string4, is("hello world"));

        String string5 = Strings.unquote(" \"hello world\" ");

        assertThat(string5, is("hello world"));
    }


    /**
     * Ensure {@link Strings#dequote(String)} doesn't fail for
     * <code>null</code> {@link String}s.
     */
    @Test
    public void shouldDequoteNullString()
    {
        String string = Strings.dequote(null);

        assertThat(string, is(nullValue()));
    }


    /**
     * Ensure {@link Strings#dequote(String)} doesn't fail for
     * {@link String}s without quotes.
     */
    @Test
    public void shouldDequoteStringWithoutQuotes()
    {
        String string1 = Strings.dequote("");

        assertThat(string1, is(""));

        String string2 = Strings.dequote("hello");

        assertThat(string2, is("hello"));

        String string3 = Strings.dequote("hello world");

        assertThat(string3, is("hello world"));
    }


    /**
     * Ensure {@link Strings#dequote(String)} removes single-quotes
     * from {@link String}s.
     */
    @Test
    public void shouldDequoteSingleQuotedStrings()
    {
        String string1 = Strings.dequote("\'\'");

        assertThat(string1, is(""));

        String string2 = Strings.dequote(" \'\' ");

        assertThat(string2, is(""));

        String string3 = Strings.dequote("\'hello\'");

        assertThat(string3, is("hello"));

        String string4 = Strings.dequote("\'hello world\'");

        assertThat(string4, is("hello world"));

        String string5 = Strings.dequote(" \'hello world\' ");

        assertThat(string5, is("hello world"));

        String string6 = Strings.dequote("\"\'hello\'\" world\"\' ");

        assertThat(string6, is("hello world"));
    }


    /**
     * Ensure {@link Strings#dequote(String)} removes double-quotes
     * from {@link String}s.
     */
    @Test
    public void shouldDequoteDoubleQuotedStrings()
    {
        String string1 = Strings.dequote("\"\"");

        assertThat(string1, is(""));

        String string2 = Strings.dequote(" \"\" ");

        assertThat(string2, is(""));

        String string3 = Strings.dequote("\"hello\"");

        assertThat(string3, is("hello"));

        String string4 = Strings.dequote("\"hello world\"");

        assertThat(string4, is("hello world"));

        String string5 = Strings.dequote(" \"hello world\" ");

        assertThat(string5, is("hello world"));

        String string6 = Strings.dequote("\"\'hello\'\" world\"\' ");

        assertThat(string6, is("hello world"));
    }


    /**
     * Ensure {@link Strings#doubleQuoteIfNecessary(String)} does
     * not double quote a <code>null</code>.
     */
    @Test
    public void shouldNotDoubleQuoteNullString()
    {
        String string = Strings.doubleQuoteIfNecessary(null);

        assertThat(string, is(nullValue()));
    }


    /**
     * Ensure {@link Strings#doubleQuoteIfNecessary(String)} does
     * not double quote an empty {@link String}
     */
    @Test
    public void shouldNotDoubleQuoteEmptyString()
    {
        String string = Strings.doubleQuoteIfNecessary("");

        assertThat(string, is(""));
    }


    /**
     * Ensure {@link Strings#doubleQuoteIfNecessary(String)} does
     * not double quote a {@link String} that is already double-quoted.
     */
    @Test
    public void shouldNotDoubleQuoteDoubleQuotedString()
    {
        String string1 = Strings.doubleQuoteIfNecessary("\"\"");

        assertThat(string1, is("\"\""));

        String string2 = Strings.doubleQuoteIfNecessary("\"hello\"");

        assertThat(string2, is("\"hello\""));

        String string3 = Strings.doubleQuoteIfNecessary("\"hello world\"");

        assertThat(string3, is("\"hello world\""));
    }


    /**
     * Ensure {@link Strings#doubleQuoteIfNecessary(String)} does
     * not double quote a {@link String} that is already double-quoted.
     */
    @Test
    public void shouldDoubleQuoteString()
    {
        String string1 = Strings.doubleQuoteIfNecessary(" ");

        assertThat(string1, is("\" \""));

        String string2 = Strings.doubleQuoteIfNecessary("hello");

        assertThat(string2, is("hello"));

        String string3 = Strings.doubleQuoteIfNecessary("hello world");

        assertThat(string3, is("\"hello world\""));
    }
}
