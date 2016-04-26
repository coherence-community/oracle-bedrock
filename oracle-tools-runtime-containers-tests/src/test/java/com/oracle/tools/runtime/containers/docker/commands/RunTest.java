/*
 * File: RunTest.java
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
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link Run} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RunTest
{

    @Test
    public void shouldImmutableAddSinglePort() throws Exception
    {
        Run          command1 = Run.image("foo");
        List<String> before   = resolveArguments(command1);
        Run          command2 = command1.publish(80);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, contains("--publish=80"));
    }


    @Test
    public void shouldImmutableAddMultipleSinglePorts() throws Exception
    {
        Run          command1 = Run.image("foo");
        List<String> before   = resolveArguments(command1);
        Run          command2 = command1.publish(80).publish(8080);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--publish=80", "--publish=8080"));
    }


    @Test
    public void shouldImmutableAddMultiplePorts() throws Exception
    {
        Run          command1 = Run.image("foo");
        List<String> before   = resolveArguments(command1);
        Run          command2 = command1.publish(80, 8080);

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--publish=80", "--publish=8080"));
    }


    @Test
    public void shouldImmutableAddFullPortMapping() throws Exception
    {
        Run          command1 = Run.image("foo");
        List<String> before   = resolveArguments(command1);
        Run          command2 = command1.publish("127.0.0.1:80:5000");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, contains("--publish=127.0.0.1:80:5000"));
    }


    @Test
    public void shouldNotAddEmptyPorts() throws Exception
    {
        Run command1 = Run.image("foo");
        Run command2 = command1.publish(Collections.emptyList());

        assertThat(command1, is(sameInstance(command2)));
    }


    @Test
    public void shouldImmutablySetCGroupParent() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.cgroupParent("my-parent");

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cgroup-parent=my-parent"));
    }


    @Test
    public void shouldImmutablySetCPUPeriod() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.cpuPeriod(99);

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpu-period=99"));
    }


    @Test
    public void shouldImmutablySetCPUQuota() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.cpuQuota(123);

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpu-quota=123"));
    }


    @Test
    public void shouldImmutablySetCPUSetCPUs() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.cpuSetCPUs("bar");

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpuset-cpus=bar"));
    }


    @Test
    public void shouldImmutablySetCPUSetMems() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.cpuSetMems("bar");

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpuset-mems=bar"));
    }


    @Test
    public void shouldImmutablySetCPUShares() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.cpuShares(100);

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--cpu-shares=100"));
    }


    @Test
    public void shouldImmutablySetDisableContentTrust() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.disableContentTrust();

        assertThat(run1, is (not(sameInstance(run2))));

        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--disable-content-trust=true"));
    }


    @Test
    public void shouldImmutablySetEnableContentTrust() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.enableContentTrust();

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--disable-content-trust=false"));
    }


    @Test
    public void shouldImmutablySetForceRM() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.rm();

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--rm"));
    }


    @Test
    public void shouldImmutablySetIsolation() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.isolation("bar");

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--isolation=bar"));
    }


    @Test
    public void shouldImmutablyAddLabels() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.labels("bar1", "bar2");

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--label=bar1", "--label=bar2"));
    }


    @Test
    public void shouldImmutablySetMemory() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.memory("bar");

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--memory=bar"));
    }


    @Test
    public void shouldImmutablySetMemorySwap() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.memorySwap(100);

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--memory-swap=100"));
    }


    @Test
    public void shouldImmutablySetSHMSize() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.shmSize("A", "B", "C");

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--shm-size=A", "--shm-size=B", "--shm-size=C"));
    }


    @Test
    public void shouldImmutablySetULimit() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.ulimit("A", "B", "C");

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ulimit=A", "--ulimit=B", "--ulimit=C"));
    }


    @Test
    public void shouldImmutablySetCustomArguments() throws Exception
    {
        Run          run1   = Run.image("foo");
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--test1", "--test2"));
    }


    @Test
    public void shouldImmutablyRemoveArguments() throws Exception
    {
        Run          run1   = Run.image("foo").withCommandArguments(Argument.of("--test1"), Argument.of("--test2"));
        List<String> before = resolveArguments(run1);
        Run          run2   = run1.withoutCommandArguments(Argument.of("--test1"), Argument.of("--test2"));

        assertThat(run1, is (not(sameInstance(run2))));


        List<String> arguments1 = resolveArguments(run1);
        List<String> arguments2 = resolveArguments(run2);

        assertThat(arguments1, is(before));
        assertThat(arguments1.contains("--test1"), is(true));
        assertThat(arguments1.contains("--test2"), is(true));
        assertThat(arguments2.contains("--test1"), is(false));
        assertThat(arguments2.contains("--test2"), is(false));
    }


    private List<String> resolveArguments(Run command)
    {
        Options options  = new Options();
        Platform platform = LocalPlatform.get();

        command.onFinalize(platform, options);

        Arguments arguments = options.get(Arguments.class);

        return arguments.resolve(platform, options);
    }
}
