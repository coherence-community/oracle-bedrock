/*
 * File: PortsTest.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link Ports} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class PortsTest
{
    @Test
    public void shouldCreatePorts() throws Exception
    {
        Ports.Port port1 = new Ports.Port("foo.1", 1, 2);
        Ports.Port port2 = new Ports.Port("foo.2", 2, 3);
        Ports.Port port3 = new Ports.Port("bar.1", 1, 2);

        Ports      ports = Ports.of(port1, port2, port3);

        assertThat(ports.getPorts(), containsInAnyOrder(port1, port2, port3));
    }


    @Test
    public void shouldCreateEmptyPorts() throws Exception
    {
        Ports ports = Ports.empty();

        assertThat(ports.getPorts(), is(emptyIterable()));
    }


    @Test
    public void shouldBeEmptyByDefault() throws Exception
    {
        OptionsByType optionsByType = OptionsByType.empty();
        Ports         ports         = optionsByType.get(Ports.class);

        assertThat(ports, is(notNullValue()));
        assertThat(ports.getPorts(), is(emptyIterable()));
    }


    @Test
    public void shouldComposePorts() throws Exception
    {
        Ports.Port    port1         = new Ports.Port("foo.1", 1, 2);
        Ports.Port    port2         = new Ports.Port("foo.2", 2, 3);
        Ports.Port    port3         = new Ports.Port("bar.1", 1, 1);
        Ports.Port    port4         = new Ports.Port("bar.2", 2, 2);
        Ports.Port    port5         = new Ports.Port("bar.3", 3, 3);

        OptionsByType optionsByType = OptionsByType.of(Ports.of(port1, port2), Ports.of(port3, port4, port5));

        Ports         ports         = optionsByType.get(Ports.class);

        assertThat(ports.getPorts(), containsInAnyOrder(port1, port2, port3, port4, port5));
    }


    @Test
    public void shouldCapturePorts() throws Exception
    {
        OptionsByType optionsByType = OptionsByType.of(SystemProperty.of("test-port", "19", Ports.capture()),
                                                       Argument.of("--port1", 20, Ports.capture()),
                                                       Argument.of("--port2", 100, Ports.capture()));

        SystemProperties properties = optionsByType.get(SystemProperties.class);
        Arguments        arguments  = optionsByType.get(Arguments.class);

        properties.resolve(mock(Platform.class), optionsByType);
        arguments.resolve(mock(Platform.class), optionsByType);

        Ports      ports = optionsByType.get(Ports.class);

        Ports.Port port1 = new Ports.Port("test-port", 19, 19);
        Ports.Port port2 = new Ports.Port("--port1", 20, 20);
        Ports.Port port3 = new Ports.Port("--port2", 100, 100);

        assertThat(ports.getPorts(), containsInAnyOrder(port1, port2, port3));
    }
}
