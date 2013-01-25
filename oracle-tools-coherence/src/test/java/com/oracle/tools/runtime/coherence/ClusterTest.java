/*
 * File: ClusterTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.junit.AbstractTest;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link Cluster}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ClusterTest extends AbstractTest
{
    /**
     * Ensure that {@link Cluster#getClusterSize()} returns the correct
     * value.
     */
    @Test
    public void testExpectedClusterSize()
    {
        ClusterMember member1 = mock(ClusterMember.class);
        ClusterMember member2 = mock(ClusterMember.class);

        when(member1.getName()).thenReturn("m1");
        when(member1.getClusterSize()).thenReturn(2);
        when(member2.getName()).thenReturn("m2");
        when(member2.getClusterSize()).thenReturn(2);

        List<ClusterMember> members = Arrays.asList(member1, member2);

        Cluster             cluster = new Cluster(members);

        assertThat(eventually(invoking(cluster).getClusterSize()), is(2));
    }
}
