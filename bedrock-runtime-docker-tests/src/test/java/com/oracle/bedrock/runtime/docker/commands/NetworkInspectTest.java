/*
 * File: NetworkInspectTest.java
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
 * Tests for the {@link Network.Inspect} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class NetworkInspectTest extends AbstractCommandTest
{
    @Test
    public void shouldInspectNetwork() throws Exception
    {
        Network.Inspect command = Network.inspect("foo", "bar");
        List<String>    args    = resolveArguments(command);

        assertThat(args, contains("network", "inspect", "foo", "bar"));
    }


    @Test
    public void shouldImmutablyAddFormat() throws Exception
    {
        Network.Inspect command1 = Network.inspect("foo", "bar");
        List<String>    before   = resolveArguments(command1);
        Network.Inspect command2 = command1.format("a");

        assertThat(command1, is (not(sameInstance(command2))));

        List<String> arguments1 = resolveArguments(command1);
        List<String> arguments2 = resolveArguments(command2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, containsInAnyOrder("--format=a"));
    }
}
