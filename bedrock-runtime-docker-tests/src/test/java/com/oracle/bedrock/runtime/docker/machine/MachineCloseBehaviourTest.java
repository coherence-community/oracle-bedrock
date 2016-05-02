/*
 * File: MachineCloseBehaviourTest.java
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

package com.oracle.bedrock.runtime.docker.machine;

import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MachineCloseBehaviour}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class MachineCloseBehaviourTest
{
    @Test
    public void shouldDoNothing() throws Exception
    {
        DockerMachinePlatform platform  = mock(DockerMachinePlatform.class);
        MachineCloseBehaviour behaviour = MachineCloseBehaviour.none();

        behaviour.accept(platform);

        verifyNoMoreInteractions(platform);
    }


    @Test
    public void shouldStopMachine() throws Exception
    {
        String                name      = "foo";
        DockerMachinePlatform platform  = mock(DockerMachinePlatform.class);
        DockerMachine         machine   = mock(DockerMachine.class);
        MachineCloseBehaviour behaviour = MachineCloseBehaviour.stop();

        when(platform.getName()).thenReturn(name);
        when(platform.getDockerMachine()).thenReturn(machine);
        when(machine.stop(anyVararg())).thenReturn(0);

        behaviour.accept(platform);

        verify(machine).stop(name);
        verifyNoMoreInteractions(machine);
    }


    @Test
    public void shouldKillWhenStopFails() throws Exception
    {
        String                name      = "foo";
        DockerMachinePlatform platform  = mock(DockerMachinePlatform.class);
        DockerMachine         machine   = mock(DockerMachine.class);
        MachineCloseBehaviour behaviour = MachineCloseBehaviour.stop();

        when(platform.getName()).thenReturn(name);
        when(platform.getDockerMachine()).thenReturn(machine);
        when(machine.stop(anyVararg())).thenReturn(1);

        behaviour.accept(platform);

        InOrder inOrder = inOrder(machine);

        inOrder.verify(machine).stop(name);
        inOrder.verify(machine).kill(name);

        verifyNoMoreInteractions(machine);
    }


    @Test
    public void shouldStopAndRemoveMachine() throws Exception
    {
        String                name      = "foo";
        DockerMachinePlatform platform  = mock(DockerMachinePlatform.class);
        DockerMachine         machine   = mock(DockerMachine.class);
        MachineCloseBehaviour behaviour = MachineCloseBehaviour.remove();

        when(platform.getName()).thenReturn(name);
        when(platform.getDockerMachine()).thenReturn(machine);
        when(machine.stop(anyVararg())).thenReturn(0);

        behaviour.accept(platform);

        InOrder inOrder = inOrder(machine);

        inOrder.verify(machine).stop(name);
        inOrder.verify(machine).remove(true, name);

        verifyNoMoreInteractions(machine);
    }
}
