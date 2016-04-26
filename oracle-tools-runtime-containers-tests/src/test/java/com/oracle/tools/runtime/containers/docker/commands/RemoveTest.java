/*
 * File: RemoveTest.java
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
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.options.Arguments;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Remove}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemoveTest
{
    @Test
    public void shouldCreateRemoveContainerCommand() throws Exception
    {
        Remove.RemoveContainer command = Remove.containers("foo", "bar");

        assertThat(command, is(notNullValue()));

        List<String> args = resolveArguments(command);

        assertThat(args, contains("rm", "foo", "bar"));
    }


    @Test
    public void shouldImmutablySetForceForContainer() throws Exception
    {
        Remove.RemoveContainer command1 = Remove.containers("foo");
        List<String>           before   = resolveArguments(command1);
        Remove.RemoveContainer command2 = command1.force(true);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--force"));
    }


    @Test
    public void shouldImmutablyRemoveForceForContainer() throws Exception
    {
        Remove.RemoveContainer command1 = Remove.containers("foo").force();
        Remove.RemoveContainer command2 = command1.force(false);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1.contains("--force"), is(true));
        assertThat(arguments2.contains("--force"), is(false));
    }


    @Test
    public void shouldImmutablySetAndVolumes() throws Exception
    {
        Remove.RemoveContainer command1 = Remove.containers("foo");
        List<String>           before   = resolveArguments(command1);
        Remove.RemoveContainer command2 = command1.andVolumes(true);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--volumes"));
    }


    @Test
    public void shouldImmutablyRemoveAndVolumes() throws Exception
    {
        Remove.RemoveContainer command1 = Remove.containers("foo").andVolumes();
        Remove.RemoveContainer command2 = command1.andVolumes(false);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1.contains("--volumes"), is(true));
        assertThat(arguments2.contains("--volumes"), is(false));
    }


    @Test
    public void shouldCreateRemoveImageCommand() throws Exception
    {
        Remove.RemoveImage command = Remove.images("foo", "bar");

        assertThat(command, is(notNullValue()));

        List<String> args = resolveArguments(command);

        assertThat(args, contains("rmi", "foo", "bar"));
    }


    @Test
    public void shouldImmutablySetForceForImage() throws Exception
    {
        Remove.RemoveImage command1 = Remove.images("foo");
        List<String>       before   = resolveArguments(command1);
        Remove.RemoveImage command2 = command1.force(true);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--force"));
    }


    @Test
    public void shouldImmutablyRemoveForceForImage() throws Exception
    {
        Remove.RemoveImage command1 = Remove.images("foo").force();
        Remove.RemoveImage command2 = command1.force(false);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1.contains("--force"), is(true));
        assertThat(arguments2.contains("--force"), is(false));
    }


    @Test
    public void shouldImmutablySetNoPrune() throws Exception
    {
        Remove.RemoveImage command1 = Remove.images("foo");
        List<String>       before   = resolveArguments(command1);
        Remove.RemoveImage command2 = command1.noPrune(true);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--no-prune"));
    }


    @Test
    public void shouldImmutablyRemoveNoPrune() throws Exception
    {
        Remove.RemoveImage command1 = Remove.images("foo").noPrune();
        Remove.RemoveImage command2 = command1.noPrune(false);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1.contains("--no-prune"), is(true));
        assertThat(arguments2.contains("--no-prune"), is(false));
    }


    @Test
    public void shouldCreateRemoveLinkCommand() throws Exception
    {
        Remove command = Remove.link("foo", "bar");

        assertThat(command, is(notNullValue()));

        List<String> args = resolveArguments(command);

        assertThat(args, contains("rm", "--link", "foo/bar"));
    }


    private List<String> resolveArguments(AbstractDockerCommand command)
    {
        Options options  = new Options();
        Platform platform = LocalPlatform.get();

        command.onFinalize(platform, options);

        Arguments arguments = options.get(Arguments.class);

        return arguments.resolve(platform, options);
    }
}
