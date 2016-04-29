/*
 * File: NetworkCreateTest.java
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

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link Network.Create} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class NetworkCreateTest extends AbstractCommandTest
{
    @Test
    public void shouldCreateBridgeNetwork() throws Exception
    {
        Network.Create create = Network.createBridge("foo");
        List<String>   args   = resolveArguments(create);

        assertThat(args, contains("network", "create", "--driver=bridge", "foo"));
    }


    @Test
    public void shouldCreateOverlayNetwork() throws Exception
    {
        Network.Create create = Network.createOverlay("foo");
        List<String>   args   = resolveArguments(create);

        assertThat(args, contains("network", "create", "--driver=overlay", "foo"));
    }


    @Test
    public void shouldCreateNetworkWithDriver() throws Exception
    {
        Network.Create create = Network.create("foo", "bar");
        List<String>   args   = resolveArguments(create);

        assertThat(args, contains("network", "create", "--driver=bar", "foo"));
    }


    @Test
    public void shouldImmutablyAddAuxAddresses() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.auxAddress("bar1", "bar2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--aux-address=bar1", "--aux-address=bar2"));
    }


    @Test
    public void shouldImmutablyAddDriverOptions() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.driverOpts("bar1", "bar2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--opt=bar1", "--opt=bar2"));
    }


    @Test
    public void shouldImmutablyAddGateways() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.gateway("bar1", "bar2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--gateway=bar1", "--gateway=bar2"));
    }


    @Test
    public void shouldImmutablySetInternal() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.internal();

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--internal"));
    }


    @Test
    public void shouldImmutablySetIPAMDriver() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.ipamDriver("bar");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ipam-driver=bar"));
    }


    @Test
    public void shouldImmutablyAddIPAMOptions() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.ipamOpts("bar1", "bar2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ipam-opt=bar1", "--ipam-opt=bar2"));
    }


    @Test
    public void shouldImmutablySetIPRanges() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.ipRange("172.28.5.0/24", "172.30.5.0/24");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ip-range=172.28.5.0/24", "--ip-range=172.30.5.0/24"));
    }


    @Test
    public void shouldImmutablyEnableIPv6() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.ipv6();

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--ipv6"));
    }


    @Test
    public void shouldImmutablyAddLabels() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.labels("bar1", "bar2");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--label=bar1", "--label=bar2"));
    }


    @Test
    public void shouldImmutablySetSubnets() throws Exception
    {
        Network.Create  command1 = Network.createOverlay("foo");
        List<String>    before   = resolveArguments(command1);
        Network.Create  command2 = command1.subnet("172.28.5.0/24", "172.30.5.0/24");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--subnet=172.28.5.0/24", "--subnet=172.30.5.0/24"));
    }
}
