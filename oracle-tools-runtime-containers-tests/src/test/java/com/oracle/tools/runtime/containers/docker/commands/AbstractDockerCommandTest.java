/*
 * File: AbstractDockerCommandTest.java
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

package com.oracle.tools.runtime.containers.docker.commands;

import com.oracle.tools.Options;
import com.oracle.tools.options.Timeout;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.containers.docker.Docker;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.EnvironmentVariable;
import com.oracle.tools.runtime.options.EnvironmentVariables;
import com.oracle.tools.runtime.options.Executable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AbstractDockerCommand}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractDockerCommandTest
{
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void shouldHaveCommandNameAsFirstArgument() throws Exception
    {
        AbstractDockerCommand command   = new CommandStub("foo");
        Arguments             arguments = command.getCommandArguments();
        List<String>          argList   = arguments.resolve(LocalPlatform.get(), new Options());

        assertThat(argList, contains("foo"));
    }


    @Test
    public void shouldHaveImplementationClass() throws Exception
    {
        AbstractDockerCommand command   = new CommandStub("foo");

        Class implClass = command.getImplementationClass(LocalPlatform.get(), new Options());

        assertThat(implClass, is(notNullValue()));
        assertThat(SimpleApplication.class.equals(implClass), is(true));
    }


    @Test
    public void shouldSetTimeout() throws Exception
    {
        AbstractDockerCommand command = new CommandStub("run")
                                            .timeoutAfter(1, TimeUnit.MINUTES);

        Timeout               timeout = command.getTimeout();

        assertThat(timeout, is(notNullValue()));
        assertThat(timeout.to(TimeUnit.MINUTES), is(1L));
    }


    @Test
    public void shouldNotAcceptNullTimeout() throws Exception
    {
        AbstractDockerCommand command = new CommandStub("run");

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("The timeout cannot be null");

        command.timeoutAfter(null);
    }


    @Test
    public void shouldSetExecutableOnFinalize() throws Exception
    {
        Docker                docker     = Docker.auto();
        Options               options    = new Options(docker);
        AbstractDockerCommand command    = new CommandStub("foo");

        command.onLaunch(LocalPlatform.get(), options);

        Executable executable = options.get(Executable.class);

        assertThat(executable, is(notNullValue()));
        assertThat(executable.getName(), is(docker.getDockerExecutable()));
    }


    @Test
    public void shouldAddEnvironmentArgsBeforeCommandNameOnFinalize() throws Exception
    {
        Docker                docker     = Docker.auto().withCommandOptions(Argument.of("env1"), Argument.of("env2"));
        Arguments             existing   = Arguments.of(Argument.of("should-not-be-present"));
        Options               options    = new Options(docker, existing);
        AbstractDockerCommand command    = new CommandStub("foo");

        command.onLaunch(LocalPlatform.get(), options);

        Arguments    arguments = options.get(Arguments.class);
        List<String> argList   = arguments.resolve(LocalPlatform.get(), options);

        assertThat(argList, contains("env1", "env2", "foo"));
    }


    @Test
    public void shouldAddEnvironmentVariablesOnFinalize() throws Exception
    {
        Docker                docker     = Docker.auto().withEnvironmentVariables(EnvironmentVariable.of("foo", "foo1"),
                                                                                  EnvironmentVariable.of("bar", "bar1"));
        Options               options    = new Options(docker);
        AbstractDockerCommand command    = new CommandStub("foo");

        command.onLaunch(LocalPlatform.get(), options);

        EnvironmentVariables env  = options.get(EnvironmentVariables.class);
        Properties           vars = env.realize(LocalPlatform.get());

        assertThat(vars.get("foo"), is("foo1"));
        assertThat(vars.get("bar"), is("bar1"));
    }


    static class CommandStub extends AbstractDockerCommand<CommandStub>
    {
        public CommandStub(String command)
        {
            super(command);
        }

        public CommandStub(Arguments commandArguments)
        {
            super(commandArguments);
        }

        @Override
        public CommandStub withCommandArguments(Argument... args)
        {
            return new CommandStub(Arguments.of(this.getCommandArguments()).with(args));
        }


        @Override
        public CommandStub withoutCommandArguments(Argument... args)
        {
            return new CommandStub(getCommandArguments().without(args));
        }
    }
}
