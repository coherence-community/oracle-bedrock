/*
 * File: CommonCommandTests.java
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
import com.oracle.bedrock.runtime.options.EnvironmentVariable;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import static org.mockito.Mockito.mock;

/**
 * Common tests for all Helm commands.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class CommonCommandTests<C extends HelmCommand>
{
    @Test
    public void shouldSetEnvironmentVariableFromString() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        C             command  = newInstance();

        CLI           copy     = command.withEnvironment("foo-var");

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getFlags(), is(command.getFlags()));

        copy.onLaunching(platform, options);

        EnvironmentVariables variables = options.get(EnvironmentVariables.class);

        assertThat(variables, is(notNullValue()));

        Properties properties = variables.realize(platform);

        assertThat(properties, is(notNullValue()));
        assertThat(properties.containsKey("foo-var"), is(true));
    }

    @Test
    public void shouldSetEnvironmentVariableFromNameAndValue() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        C             command  = newInstance();

        CLI           copy     = command.withEnvironment("foo-var", "foo-value");

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getFlags(), is(command.getFlags()));

        copy.onLaunching(platform, options);

        EnvironmentVariables variables = options.get(EnvironmentVariables.class);

        assertThat(variables, is(notNullValue()));

        Properties properties = variables.realize(platform);

        assertThat(properties, is(notNullValue()));
        assertThat(properties.get("foo-var"), is("foo-value"));
    }

    @Test
    public void shouldSetEnvironmentVariablesFromEnvironmentables() throws Exception
    {
        Platform             platform = mock(Platform.class);
        OptionsByType        options  = OptionsByType.empty();
        C                    command  = newInstance();
        EnvironmentVariables vars     = EnvironmentVariables.custom()
                                            .with(EnvironmentVariable.of("foo", "foo-value"))
                                            .with(EnvironmentVariable.of("bar", "bar-value"));
        CLI                  copy     = command.withEnvironment(vars);

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getFlags(), is(command.getFlags()));

        copy.onLaunching(platform, options);

        EnvironmentVariables variables = options.get(EnvironmentVariables.class);

        assertThat(variables, is(notNullValue()));

        Properties properties = variables.realize(platform);

        assertThat(properties, is(notNullValue()));
        assertThat(properties.get("foo"), is("foo-value"));
        assertThat(properties.get("bar"), is("bar-value"));
    }

    @Test
    public void shouldSetEnvironmentVariables() throws Exception
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();
        C             command  = newInstance();

        CLI           copy     = command.withEnvironment(EnvironmentVariable.of("foo", "foo-value"),
                                                         EnvironmentVariable.of("bar", "bar-value"));

        assertThat(copy, is(notNullValue()));
        assertThat(copy, is(instanceOf(command.getClass())));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getFlags(), is(command.getFlags()));

        copy.onLaunching(platform, options);

        EnvironmentVariables variables = options.get(EnvironmentVariables.class);

        assertThat(variables, is(notNullValue()));

        Properties properties = variables.realize(platform);

        assertThat(properties, is(notNullValue()));
        assertThat(properties.get("foo"), is("foo-value"));
        assertThat(properties.get("bar"), is("bar-value"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddDebugOption() throws Exception
    {
        C command = newInstance();
        C copy    = (C) command.debug();

        assertThat(copy, is(not(sameInstance(command))));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));
        assertThat(copy.getFlags(), contains(Argument.of("--debug")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddHomeOption() throws Exception
    {
        C command = newInstance();
        C copy    = (C) command.home("helm-home");

        assertThat(copy, is(not(sameInstance(command))));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));
        assertThat(copy.getFlags(), contains(Argument.of("--home", "helm-home")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddHostOption() throws Exception
    {
        C command = newInstance();
        C copy    = (C) command.host("helm-host");

        assertThat(copy, is(not(sameInstance(command))));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));
        assertThat(copy.getFlags(), contains(Argument.of("--host", "helm-host")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddKubeConfigOption() throws Exception
    {
        C command = newInstance();
        C copy    = (C) command.kubeConfig("my-kube-config");

        assertThat(copy, is(not(sameInstance(command))));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getFlags(), is(command.getFlags()));
        assertThat(copy.getEnvironment(), contains(EnvironmentVariable.of("KUBECONFIG", "my-kube-config")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddKubeContextOption() throws Exception
    {
        C command = newInstance();
        C copy    = (C) command.kubeContext("my-kube-context");

        assertThat(copy, is(not(sameInstance(command))));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));
        assertThat(copy.getFlags(), contains(Argument.of("--kube-context", "my-kube-context")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddTillerNamespaceOption() throws Exception
    {
        C command = newInstance();
        C copy    = (C) command.tillerNamespace("my-namespace");

        assertThat(copy, is(not(sameInstance(command))));
        assertThat(copy.getCommands(), is(command.getCommands()));
        assertThat(copy.getArguments(), is(command.getArguments()));
        assertThat(copy.getEnvironment(), is(command.getEnvironment()));
        assertThat(copy.getFlags(), contains(Argument.of("--tiller-namespace", "my-namespace")));
    }


    void assertCommand(C command, String... expected)
    {
        Platform      platform = mock(Platform.class);
        OptionsByType options  = OptionsByType.empty();

        command.onLaunching(platform, options);

        Arguments arguments = options.get(Arguments.class);

        assertThat(arguments, is(notNullValue()));
        assertThat(arguments.resolve(platform, options), contains(expected));
    }

    abstract C newInstance();
}
