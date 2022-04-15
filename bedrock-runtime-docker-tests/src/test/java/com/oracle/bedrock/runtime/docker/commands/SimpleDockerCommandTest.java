/*
 * File: SimpleDockerCommandTest.java
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

package com.oracle.bedrock.runtime.docker.commands;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.options.Argument;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link SimpleDockerCommand}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleDockerCommandTest extends AbstractCommandTest
{
    @Test
    public void shouldOnlyHaveCommandNameAsArguments() throws Exception
    {
        SimpleDockerCommand command = SimpleDockerCommand.of("run");

        List<String> arguments      = command.getCommandArguments().resolve(LocalPlatform.get(), OptionsByType.empty());

        assertThat(arguments, is(Collections.singletonList("run")));
    }


    @Test
    public void shouldOnlyHaveArguments() throws Exception
    {
        SimpleDockerCommand command =
            SimpleDockerCommand.of("run").withCommandArguments(Argument.of("Foo"))
            .withCommandArguments(Argument.of("Bar"));

        List<String> arguments = command.getCommandArguments().resolve(LocalPlatform.get(), OptionsByType.empty());

        assertThat(arguments, contains("run", "Foo", "Bar"));
    }


    @Test
    public void shouldSetTimeout() throws Exception
    {
        SimpleDockerCommand command = SimpleDockerCommand.of("run").timeoutAfter(1, TimeUnit.MINUTES);

        Timeout             timeout = command.getTimeout();

        assertThat(timeout, is(notNullValue()));
        assertThat(timeout.to(TimeUnit.MINUTES), is(1L));
    }


    @Test
    public void shouldImmutablySetCustomArguments() throws Exception
    {
        SimpleDockerCommand command1 = SimpleDockerCommand.of("foo");
        List<String>        before   = resolveArguments(command1);
        SimpleDockerCommand command2 = command1.withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(command1, is(not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--test1", "--test2"));
    }


    @Test
    public void shouldImmutablyRemoveArguments() throws Exception
    {
        SimpleDockerCommand command1 = SimpleDockerCommand.of("foo").withCommandArguments(Argument.of("--test1"),
                                                                                          Argument.of("--test2"));
        List<String>        before   = resolveArguments(command1);
        SimpleDockerCommand command2 = command1.withoutCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(command1, is(not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--test1"), is(true));
        assertThat(arguments1.contains("--test2"), is(true));
        assertThat(arguments2.contains("--test1"), is(false));
        assertThat(arguments2.contains("--test2"), is(false));
    }
}
