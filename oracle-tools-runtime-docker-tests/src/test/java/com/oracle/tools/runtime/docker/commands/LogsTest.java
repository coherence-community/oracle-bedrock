/*
 * File: LogsTest.java
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

import com.oracle.tools.Options;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Logs}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class LogsTest
{
    @Test
    public void shouldCreateLogsCommand() throws Exception
    {
        Logs         logs = Logs.from("foo");
        List<String> args = resolveArguments(logs);

        assertThat(args, contains("logs", "foo"));
    }


    @Test
    public void shouldImmutablySetFollowEnabled() throws Exception
    {
        Logs         logs1  = Logs.from("foo");
        List<String> before = resolveArguments(logs1);
        Logs         logs2  = logs1.follow(true);

        assertThat(logs1, is (not(sameInstance(logs2))));

        List<String> arguments1 = resolveArguments(logs1);
        List<String> arguments2 = resolveArguments(logs2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--follow"));
    }


    @Test
    public void shouldImmutablySetFollowDisabled() throws Exception
    {
        Logs         logs1  = Logs.from("foo").follow(true);
        List<String> before = resolveArguments(logs1);
        Logs         logs2  = logs1.follow(false);

        assertThat(logs1, is (not(sameInstance(logs2))));

        List<String> arguments1 = resolveArguments(logs1);
        List<String> arguments2 = resolveArguments(logs2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--follow"), is(true));
        assertThat(arguments2.contains("--follow"), is(false));
    }


    @Test
    public void shouldImmutablySetSince() throws Exception
    {
        Logs         logs1  = Logs.from("foo");
        List<String> before = resolveArguments(logs1);
        Logs         logs2  = logs1.since("12345");

        assertThat(logs1, is (not(sameInstance(logs2))));

        List<String> arguments1 = resolveArguments(logs1);
        List<String> arguments2 = resolveArguments(logs2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--since=12345"));
    }


    @Test
    public void shouldImmutablySetTail() throws Exception
    {
        Logs         logs1  = Logs.from("foo");
        List<String> before = resolveArguments(logs1);
        Logs         logs2  = logs1.tail("all");

        assertThat(logs1, is (not(sameInstance(logs2))));

        List<String> arguments1 = resolveArguments(logs1);
        List<String> arguments2 = resolveArguments(logs2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--tail=all"));
    }


    @Test
    public void shouldImmutablySetCustomArguments() throws Exception
    {
        Logs         logs1  = Logs.from("foo");
        List<String> before = resolveArguments(logs1);
        Logs         logs2  = logs1.withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(logs1, is (not(sameInstance(logs2))));

        List<String> arguments1 = resolveArguments(logs1);
        List<String> arguments2 = resolveArguments(logs2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--test1", "--test2"));
    }


    @Test
    public void shouldImmutablyRemoveArguments() throws Exception
    {
        Logs         logs1  = Logs.from("foo").withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));
        List<String> before = resolveArguments(logs1);
        Logs         logs2  = logs1.withoutCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(logs1, is (not(sameInstance(logs2))));

        List<String> arguments1 = resolveArguments(logs1);
        List<String> arguments2 = resolveArguments(logs2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--test1"), is(true));
        assertThat(arguments1.contains("--test2"), is(true));
        assertThat(arguments2.contains("--test1"), is(false));
        assertThat(arguments2.contains("--test2"), is(false));
    }


    private List<String> resolveArguments(Logs inspect)
    {
        Options options  = new Options();
        Platform platform = LocalPlatform.get();

        inspect.onLaunch(platform, options);

        Arguments arguments = options.get(Arguments.class);

        return arguments.resolve(platform, options);
    }
}
