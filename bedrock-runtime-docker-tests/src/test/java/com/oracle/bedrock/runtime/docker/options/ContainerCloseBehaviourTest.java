/*
 * File: ContainerCloseBehaviourTest.java
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

package com.oracle.bedrock.runtime.docker.options;

import com.oracle.bedrock.runtime.docker.DockerContainer;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link ContainerCloseBehaviour}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ContainerCloseBehaviourTest
{
    @Test
    public void shouldDoNothingWithContainer() throws Exception
    {
        DockerContainer container = mock(DockerContainer.class);

        ContainerCloseBehaviour.none().accept(container);

        verifyNoMoreInteractions(container);
    }


    @Test
    public void shouldStopContainer() throws Exception
    {
        DockerContainer container = mock(DockerContainer.class);

        ContainerCloseBehaviour.stop().accept(container);

        verify(container).stop();
    }


    @Test
    public void shouldStopAndRemoveContainer() throws Exception
    {
        DockerContainer container = mock(DockerContainer.class);

        ContainerCloseBehaviour.remove().accept(container);

        InOrder inOrder = inOrder(container);
        inOrder.verify(container).stop();
        inOrder.verify(container).remove(true);
    }
}
