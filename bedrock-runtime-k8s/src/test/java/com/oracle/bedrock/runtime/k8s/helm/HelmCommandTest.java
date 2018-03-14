/*
 * File: HelmCommandTest.java
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

package com.oracle.bedrock.runtime.k8s.helm;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.mock;

/**
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmCommandTest
            extends CommonCommandTests<HelmCommand>
{
    @Test
    public void shouldAddArgument() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        HelmCommand   command  = newInstance();

        CLI           copy     = command.withArguments(Argument.of("arg-1"), Argument.of("arg-2"));

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getFlags(), is(command.getFlags()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));

        Arguments args = copy.getArguments();

        assertThat(args, is(notNullValue()));

        List<String> list = args.resolve(platform, options);

        assertThat(list, is(notNullValue()));
        assertThat(list, contains("arg-1", "arg-2"));
    }

    @Test
    public void shouldAddArgumentValues() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        HelmCommand   command  = newInstance();

        CLI           copy     = command.withArguments("arg-1", "arg-2");

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getFlags(), is(command.getFlags()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));

        Arguments args = copy.getArguments();

        assertThat(args, is(notNullValue()));

        List<String> list = args.resolve(platform, options);

        assertThat(list, is(notNullValue()));
        assertThat(list, contains("arg-1", "arg-2"));
    }

    @Test
    public void shouldAddArguments() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        HelmCommand   command  = newInstance();

        CLI           copy     = command.withArguments(Arguments.of(Argument.of("arg-1"), Argument.of("arg-2")));

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getFlags(), is(command.getFlags()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));

        Arguments args = copy.getArguments();

        assertThat(args, is(notNullValue()));

        List<String> list = args.resolve(platform, options);

        assertThat(list, is(notNullValue()));
        assertThat(list, contains("arg-1", "arg-2"));
    }

    @Test
    public void shouldAddFlag() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        HelmCommand   command  = newInstance();

        CLI           copy     = command.withFlags(Argument.of("arg-1"), Argument.of("arg-2"));

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));

        Arguments flags = copy.getFlags();

        assertThat(flags, is(notNullValue()));

        List<String> list = flags.resolve(platform, options);

        assertThat(list, is(notNullValue()));
        assertThat(list, contains("arg-1", "arg-2"));
    }

    @Test
    public void shouldAddFlagValues() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        HelmCommand   command  = newInstance();

        CLI           copy     = command.withFlags("arg-1", "arg-2");

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));

        Arguments flags = copy.getFlags();

        assertThat(flags, is(notNullValue()));

        List<String> list = flags.resolve(platform, options);

        assertThat(list, is(notNullValue()));
        assertThat(list, contains("arg-1", "arg-2"));
    }

    @Test
    public void shouldAddFlags() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        HelmCommand   command  = newInstance();

        CLI           copy     = command.withFlags(Arguments.of(Argument.of("arg-1"), Argument.of("arg-2")));

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));

        Arguments flags = copy.getFlags();

        assertThat(flags, is(notNullValue()));

        List<String> list = flags.resolve(platform, options);

        assertThat(list, is(notNullValue()));
        assertThat(list, contains("arg-1", "arg-2"));
    }

    @Override
    HelmCommand newInstance()
    {
        return new CommandStub(HelmCommand.DEFAULT_HELM, Arguments.empty(), Arguments.empty(), EnvironmentVariables.custom());
    }

    public static class CommandStub extends HelmCommand<CommandStub>
    {
        public CommandStub(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
        {
            super(helm, arguments, flags, env, false);
        }

        @Override
        public CommandStub newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
        {
            return new CommandStub(helm, arguments, flags, env);
        }
    }
}
