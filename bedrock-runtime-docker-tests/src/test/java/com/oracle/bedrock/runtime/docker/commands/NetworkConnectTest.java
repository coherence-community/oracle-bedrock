/*
 * File: NetworkConnectTest.java
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

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link Network.Connect} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class NetworkConnectTest extends AbstractCommandTest
{
    @Test
    public void shouldConnectToNetwork() throws Exception
    {
        Network.Connect command = Network.connect("foo", "bar");
        List<String>    args    = resolveArguments(command);

        assertThat(args, contains("network", "connect", "foo", "bar"));
    }


    @Test
    public void shouldImmutablyAddAliases() throws Exception
    {
        Network.Connect  command1 = Network.connect("foo", "bar");
        List<String>     before   = resolveArguments(command1);
        Network.Connect  command2 = command1.alias("bar1", "bar2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--alias=bar1", "--alias=bar2"));
    }


    @Test
    public void shouldImmutablySetIPv4Address() throws Exception
    {
        Network.Connect  command1 = Network.connect("foo", "bar");
        List<String>     before   = resolveArguments(command1);
        Network.Connect  command2 = command1.ip("192.168.1.11");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ip=192.168.1.11"));
    }


    @Test
    public void shouldImmutablySetIPv6Address() throws Exception
    {
        Network.Connect  command1 = Network.connect("foo", "bar");
        List<String>     before   = resolveArguments(command1);
        Network.Connect  command2 = command1.ip6("fe80::12dd:b1ff:fea0:9d07");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ip6=fe80::12dd:b1ff:fea0:9d07"));
    }


    @Test
    public void shouldImmutablyAddLinks() throws Exception
    {
        Network.Connect  command1 = Network.connect("foo", "bar");
        List<String>     before   = resolveArguments(command1);
        Network.Connect  command2 = command1.link("container1", "container2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--link=container1", "--link=container2"));
    }
}
