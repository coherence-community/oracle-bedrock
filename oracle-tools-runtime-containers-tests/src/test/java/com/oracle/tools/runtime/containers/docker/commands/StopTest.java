/*
 * File: StopTest.java
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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link Stop}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class StopTest
{
    @Test
    public void shouldCreateStopCommand() throws Exception
    {
        Stop         stop = Stop.containers("foo", "bar");
        List<String> args = resolveArguments(stop);

        assertThat(args, contains("stop", "foo", "bar"));
    }


    @Test
    public void shouldImmutablySetTimeToKill() throws Exception
    {
        Stop         stop1  = Stop.containers("foo");
        List<String> before = resolveArguments(stop1);
        Stop         stop2  = stop1.timeUntilKill(100);

        assertThat(stop1, is (not(sameInstance(stop2))));

        List<String> arguments1 = resolveArguments(stop1);
        List<String> arguments2 = resolveArguments(stop2);

        assertThat(arguments1, is(before));

        arguments2.removeAll(arguments1);

        assertThat(arguments2, contains("--time=100"));
    }


    private List<String> resolveArguments(Stop command)
    {
        Options options  = new Options();
        Platform platform = LocalPlatform.get();

        command.onLaunch(platform, options);

        Arguments arguments = options.get(Arguments.class);

        return arguments.resolve(platform, options);
    }
}
