/*
 * File: DockerMachinePlatformTest.java
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

package com.oracle.tools.runtime.containers.docker.machine;

import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.remote.RemotePlatform;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DockerMachinePlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerMachinePlatformTest
{
    @Test
    public void shouldCreateRemoteClient() throws Exception
    {
        DockerMachine machine = mock(DockerMachine.class);
        Platform      client  = mock(Platform.class);
        InetAddress   address = InetAddress.getLocalHost();

        when(machine.getClientPlatform()).thenReturn(client);
        when(machine.getAddress(anyString())).thenReturn(address);

        DockerMachinePlatform platform = new DockerMachinePlatform(machine, "foo");
        Platform              result   = platform.getClientPlatform();

        assertThat(result, is(sameInstance(client)));
    }


    @Test
    public void shouldExecutePlatformCloseBehaviour() throws Exception
    {
        DockerMachine         machine   = mock(DockerMachine.class);
        Platform              client    = mock(Platform.class);
        InetAddress           address   = InetAddress.getLocalHost();
        MachineCloseBehaviour behaviour = mock(MachineCloseBehaviour.class);

        when(machine.getClientPlatform()).thenReturn(client);
        when(machine.getAddress(anyString())).thenReturn(address);

        DockerMachinePlatform platform = new DockerMachinePlatform(machine, "foo", behaviour);

        platform.close();

        verify(behaviour).accept(same(platform));
    }


    @Test
    public void shouldExecuteSpecifiedCloseBehaviour() throws Exception
    {
        DockerMachine         machine    = mock(DockerMachine.class);
        Platform              client     = mock(Platform.class);
        InetAddress           address    = InetAddress.getLocalHost();
        MachineCloseBehaviour behaviour1 = mock(MachineCloseBehaviour.class, "1");
        MachineCloseBehaviour behaviour2 = mock(MachineCloseBehaviour.class, "2");

        when(machine.getClientPlatform()).thenReturn(client);
        when(machine.getAddress(anyString())).thenReturn(address);

        DockerMachinePlatform platform = new DockerMachinePlatform(machine, "foo", behaviour1);

        platform.close(behaviour2);

        verify(behaviour1, never()).accept(same(platform));
        verify(behaviour2).accept(same(platform));
    }
}
