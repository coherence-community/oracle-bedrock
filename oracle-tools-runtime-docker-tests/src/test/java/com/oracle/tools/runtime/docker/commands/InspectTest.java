/*
 * File: InspectTest.java
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

package com.oracle.tools.runtime.docker.commands;

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationConsoleBuilder;
import com.oracle.tools.runtime.MetaClass;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.docker.Docker;
import com.oracle.tools.runtime.options.Argument;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.json.JsonArray;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Inspect}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class InspectTest extends AbstractCommandTest
{
    @Test
    public void shouldInspectContainer() throws Exception
    {
        Inspect      inspect   = Inspect.container("Foo", "Bar");

        List<String> arguments = resolveArguments(inspect);

        assertThat(arguments.size(), is(not(0)));
        assertThat(arguments.get(0), is("inspect"));
        assertThat(arguments, containsInAnyOrder("inspect", "--type=container", "Foo", "Bar"));
    }


    @Test
    public void shouldInspectImage() throws Exception
    {
        Inspect      inspect   = Inspect.image("Foo", "Bar");

        List<String> arguments = resolveArguments(inspect);

        assertThat(arguments.size(), is(not(0)));
        assertThat(arguments.get(0), is("inspect"));
        assertThat(arguments, containsInAnyOrder("inspect", "--type=image", "Foo", "Bar"));
    }


    @Test
    public void shouldImmutablySetFormat() throws Exception
    {
        Inspect      inspect1 = Inspect.container("foo");
        List<String> before   = resolveArguments(inspect1);
        Inspect      inspect2 = inspect1.format("bar");

        assertThat(inspect1, is(not(sameInstance(inspect2))));

        List<String> arguments1 = resolveArguments(inspect1);
        List<String> arguments2 = resolveArguments(inspect2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--format=bar"));
    }


    @Test
    public void shouldImmutablyReplaceFormat() throws Exception
    {
        Inspect      inspect1 = Inspect.container("foo").format("A");
        List<String> before   = resolveArguments(inspect1);
        Inspect      inspect2 = inspect1.format("B");

        assertThat(inspect1, is(not(sameInstance(inspect2))));

        List<String> arguments1 = resolveArguments(inspect1);
        List<String> arguments2 = resolveArguments(inspect2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--format=A"), is(true));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--format=B"));
    }


    @Test
    public void shouldImmutablyIncludeSizes() throws Exception
    {
        Inspect      inspect1 = Inspect.container("foo");
        List<String> before   = resolveArguments(inspect1);
        Inspect      inspect2 = inspect1.includeSizes(true);

        assertThat(inspect1, is(not(sameInstance(inspect2))));

        List<String> arguments1 = resolveArguments(inspect1);
        List<String> arguments2 = resolveArguments(inspect2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--size"));
    }


    @Test
    public void shouldImmutablyExcludeSizes() throws Exception
    {
        Inspect      inspect1 = Inspect.container("foo").includeSizes(true);
        List<String> before   = resolveArguments(inspect1);
        Inspect      inspect2 = inspect1.includeSizes(false);

        assertThat(inspect1, is(not(sameInstance(inspect2))));

        List<String> arguments1 = resolveArguments(inspect1);
        List<String> arguments2 = resolveArguments(inspect2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--size"), is(true));
        assertThat(arguments2.contains("--size"), is(false));
    }


    @Test
    public void shouldImmutablySetCustomArguments() throws Exception
    {
        Inspect      inspect1 = Inspect.container("foo");
        List<String> before   = resolveArguments(inspect1);
        Inspect      inspect2 = inspect1.withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(inspect1, is(not(sameInstance(inspect2))));

        List<String> arguments1 = resolveArguments(inspect1);
        List<String> arguments2 = resolveArguments(inspect2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--test1", "--test2"));
    }


    @Test
    public void shouldImmutablyRemoveArguments() throws Exception
    {
        Inspect inspect1 = Inspect.container("foo").withCommandArguments(Argument.of("--test1"),
                                                                         Argument.of("--test2"));
        List<String> before   = resolveArguments(inspect1);
        Inspect      inspect2 = inspect1.withoutCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(inspect1, is(not(sameInstance(inspect2))));

        List<String> arguments1 = resolveArguments(inspect1);
        List<String> arguments2 = resolveArguments(inspect2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--test1"), is(true));
        assertThat(arguments1.contains("--test2"), is(true));
        assertThat(arguments2.contains("--test1"), is(false));
        assertThat(arguments2.contains("--test2"), is(false));
    }


    @Test
    public void shouldRunInspect() throws Exception
    {
        Platform    platform    = mock(Platform.class);
        Application application = mock(Application.class);
        Docker      docker      = Docker.auto();

        when(application.waitFor(anyVararg())).thenReturn(0);
        when(platform.launch(any(MetaClass.class), anyVararg())).then(new LaunchAnswer(application));

        JsonArray jsonArray = (JsonArray) Inspect.image("foo").run(platform, docker);

        assertThat(jsonArray.getJsonObject(0).getString("Id"), is("foo-id"));
    }


    private class LaunchAnswer implements Answer<Application>
    {
        private Application application;

        public LaunchAnswer(Application application)
        {
            this.application = application;
        }

        @Override
        public Application answer(InvocationOnMock invocation) throws Throwable
        {
            Options options = new Options();

            Arrays.stream(invocation.getArguments())
                  .filter(arg -> arg instanceof Option)
                  .forEach(arg -> options.add((Option) arg));

            ApplicationConsoleBuilder builder = options.get(ApplicationConsoleBuilder.class);
            ApplicationConsole console = builder.build("Foo");

            PrintWriter writer = console.getOutputWriter();

            writer.println("[{\"Id\": \"foo-id\"}]");
            writer.flush();

            return application;
        }
    }
}
