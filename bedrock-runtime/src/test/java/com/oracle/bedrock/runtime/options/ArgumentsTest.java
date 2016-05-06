/*
 * File: ArgumentsTest.java
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

import com.oracle.bedrock.Options;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.options.Variable;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.oracle.bedrock.runtime.options.Argument.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the {@link Arguments} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ArgumentsTest
{
    @Test
    public void shouldBeEmpty() throws Exception
    {
        Arguments arguments = Arguments.empty();

        assertThat(arguments.resolve(null, null), is(emptyIterable()));
    }


    @Test
    public void shouldHaveArgumentsFromVarArgs() throws Exception
    {
        Arguments arguments = Arguments.of("foo", "bar");

        assertThat(arguments.resolve(null, null), contains("foo", "bar"));
    }


    @Test
    public void shouldHaveArgumentsFromList() throws Exception
    {
        Arguments arguments = Arguments.of(Arrays.asList("foo", "bar"));

        assertThat(arguments.resolve(null, null), contains("foo", "bar"));
    }


    @Test
    public void shouldBeImmutable() throws Exception
    {
        Arguments arguments1 = Arguments.of("1", "2");
        Arguments arguments2 = arguments1.with("3");

        assertThat(arguments1.resolve(null, null), contains("1", "2"));
        assertThat(arguments2.resolve(null, null), contains("1", "2", "3"));
    }


    @Test
    public void shouldAddArgument() throws Exception
    {
        Arguments arguments = Arguments.empty().with(of("foo"));

        assertThat(arguments.resolve(null, null), contains("foo"));
    }


    @Test
    public void shouldAddUsingVarArgs() throws Exception
    {
        Arguments arguments = Arguments.empty().with("foo", "bar");

        assertThat(arguments.resolve(null, null), contains("foo", "bar"));
    }


    @Test
    public void shouldAddUsingList() throws Exception
    {
        Arguments arguments = Arguments.empty().with(Arrays.asList("foo", "bar"));

        assertThat(arguments.resolve(null, null), contains("foo", "bar"));
    }


    @Test
    public void shouldCompose() throws Exception
    {
        Arguments arguments = Arguments.of("1", "2").with(Arguments.of("3", "4"));

        assertThat(arguments.resolve(null, null), contains("1", "2", "3", "4"));
    }


    @Test
    public void shouldRemoveArgument() throws Exception
    {
        Arguments arguments = Arguments.empty().with(Arrays.asList("1", "2", "3")).without(of("2"));

        assertThat(arguments.resolve(null, null), contains("1", "3"));
    }


    @Test
    public void shouldRemoveStringArgument() throws Exception
    {
        Arguments arguments = Arguments.empty().with(Arrays.asList("1", "2", "3")).without("2");

        assertThat(arguments.resolve(null, null), contains("1", "3"));
    }


    @Test
    public void shouldAddArgumentsAsOptions() throws Exception
    {
        Options options = new Options();

        assertThat(options.get(Arguments.class).resolve(null, null), is(emptyIterable()));

        options.add(of("1"));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("1"));

        options.add(of("2"));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("1", "2"));
    }


    @Test
    public void shouldRemoveArgumentsAsOption() throws Exception
    {
        Options options = new Options();

        options.add(of("1"));
        options.add(of("2"));
        options.add(of("3"));

        options.remove(of("2"));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("1", "3"));
    }


    @Test
    public void shouldEvaluateObject() throws Exception
    {
        Options options = new Options();

        options.add(of(123));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("123"));
    }


    @Test
    public void shouldEvaluateIterator() throws Exception
    {
        Options options = new Options();

        options.add(of(Collections.singletonList(123).iterator()));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("123"));
    }


    @Test
    public void shouldEvaluateContextSensitiveValue() throws Exception
    {
        Argument.ContextSensitiveArgument value = new Argument.ContextSensitiveArgument()
        {
            @Override
            public Object resolve(Platform platform,
                                  Options  options)
            {
                return "123";
            }
        };

        Options options = new Options();

        options.add(of(value));

        assertThat(options.get(Arguments.class).resolve(LocalPlatform.get(), options), contains("123"));
    }


    @Test
    public void shouldEvaluateExpression() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("${foo}"));

        assertThat(options.get(Arguments.class).resolve(null, new Options(Variable.with("foo", "bar"))),
                   contains("bar"));
    }


    @Test
    public void shouldRealizeArgumentWithNameAndSpaceSeparator() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("foo", "bar"));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("foo", "bar"));
    }


    @Test
    public void shouldRealizeArgumentWithNameAndEqualsSeparator() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("foo", "bar").withSeparator('='));

        assertThat(options.get(Arguments.class).resolve(null, null), contains("foo=bar"));
    }


    @Test
    public void shouldRemoveAllNamedArgs() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("A", "A1"));
        options.add(Argument.of("A", "A2"));
        options.add(Argument.of("A", "A3"));
        options.add(Argument.of("B", "B1"));
        options.add(Argument.of("B", "B2"));
        options.add(Argument.of("B", "B3"));
        options.add(Argument.of("C1"));
        options.add(Argument.of("C2"));

        assertThat(options.get(Arguments.class).resolve(null, null),
                   contains("A", "A1", "A", "A2", "A", "A3", "B", "B1", "B", "B2", "B", "B3", "C1", "C2"));

        Arguments arguments = options.get(Arguments.class).withoutNamed("B");

        assertThat(arguments.resolve(null, null), contains("A", "A1", "A", "A2", "A", "A3", "C1", "C2"));
    }


    @Test
    public void shouldRemoveAllNamedArgsWhereNameIsNull() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("A", "A1"));
        options.add(Argument.of("A", "A2"));
        options.add(Argument.of("A", "A3"));
        options.add(Argument.of("B", "B1"));
        options.add(Argument.of("B", "B2"));
        options.add(Argument.of("B", "B3"));
        options.add(Argument.of("C1"));
        options.add(Argument.of("C2"));

        assertThat(options.get(Arguments.class).resolve(null, null),
                   contains("A", "A1", "A", "A2", "A", "A3", "B", "B1", "B", "B2", "B", "B3", "C1", "C2"));

        Arguments arguments = options.get(Arguments.class).withoutNamed(null);

        assertThat(arguments.resolve(null, null),
                   contains("A", "A1", "A", "A2", "A", "A3", "B", "B1", "B", "B2", "B", "B3"));
    }


    @Test
    public void shouldReplaceArgument() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("A1", "A1"));
        options.add(Argument.of("A2", "A21"));
        options.add(Argument.of("A2", "A22"));
        options.add(Argument.of("A3", "A3"));

        Arguments arguments = options.get(Arguments.class).replace("A2", "changed");

        assertThat(arguments.resolve(null, null), contains("A1", "A1", "A2", "changed", "A2", "A22", "A3", "A3"));
    }


    @Test
    public void shouldReplaceArgumentWhenNameIsNull() throws Exception
    {
        Options options = new Options();

        options.add(Argument.of("A1", "A1"));
        options.add(Argument.of("A21"));
        options.add(Argument.of("A22"));
        options.add(Argument.of("A3", "A3"));

        Arguments arguments = options.get(Arguments.class).replace(null, "changed");

        assertThat(arguments.resolve(null, null), contains("A1", "A1", "changed", "A22", "A3", "A3"));
    }


    @Test
    public void shouldCallResolveHandlers() throws Exception
    {
        Argument.ResolveHandler handler1 = mock(Argument.ResolveHandler.class);
        Argument.ResolveHandler handler2 = mock(Argument.ResolveHandler.class);
        Argument.ResolveHandler handler3 = mock(Argument.ResolveHandler.class);
        Options                 options  = new Options();

        options.add(Argument.of("foo", "foo-value", Decoration.of(handler1), Decoration.of(handler2)));

        options.add(Argument.of("bar",
                                new Argument.Multiple("bar1", "bar2"),
                                Decoration.of(handler1),
                                Decoration.of(handler3)));

        options.get(Arguments.class).resolve(LocalPlatform.get(), options);

        ArgumentCaptor<String> names1  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List>   values1 = ArgumentCaptor.forClass(List.class);

        verify(handler1, times(2)).onResolve(names1.capture(), values1.capture(), same(options));

        assertThat(names1.getAllValues(), contains("foo", "bar"));
        assertThat(((List<String>) values1.getAllValues().get(0)), contains("foo-value"));
        assertThat(((List<String>) values1.getAllValues().get(1)), contains("bar1", "bar2"));

        ArgumentCaptor<String> names2  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List>   values2 = ArgumentCaptor.forClass(List.class);

        verify(handler2, times(1)).onResolve(names2.capture(), values2.capture(), same(options));

        assertThat(names2.getAllValues(), contains("foo"));
        assertThat(((List<String>) values2.getAllValues().get(0)), contains("foo-value"));

        ArgumentCaptor<String> names3  = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List>   values3 = ArgumentCaptor.forClass(List.class);

        verify(handler3, times(1)).onResolve(names3.capture(), values3.capture(), same(options));

        assertThat(names3.getAllValues(), contains("bar"));
        assertThat(((List<String>) values3.getAllValues().get(0)), contains("bar1", "bar2"));
    }
}
