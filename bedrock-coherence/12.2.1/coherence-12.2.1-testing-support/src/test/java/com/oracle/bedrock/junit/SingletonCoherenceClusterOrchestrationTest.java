/*
 * File: SingletonCoherenceClusterOrchestrationTest.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Functional Tests for the {@link CoherenceClusterOrchestration}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SingletonCoherenceClusterOrchestrationTest
{
    /**
     * A {@link CoherenceClusterOrchestration} for a default {@link CoherenceCluster}.
     */
    @ClassRule
    public static CoherenceClusterOrchestration orchestration =
        new CoherenceClusterOrchestration()
                .setStorageMemberCount(1)
                .withOptions(ClusterName.of("SingletonCluster"))
                .withStorageMemberOptions(SystemProperty.of("test.property", "storageMember"));


    /**
     * Ensure that a default cluster is formed by a {@link CoherenceClusterOrchestration}.
     */
    @Test
    public void shouldFormCorrectSizeCluster()
    {
        CoherenceCluster cluster  = orchestration.getCluster();
        Set<String>      setNames = new TreeSet<>();

        for (CoherenceClusterMember member : cluster)
        {
            setNames.add(member.getName());
        }

        assertThat(setNames, contains("storage-1"));
    }


    @Test
    public void shouldHaveSetStorageMemberProperty() throws Exception
    {
        for (CoherenceClusterMember member : orchestration.getCluster().getAll("storage"))
        {
            assertThat(member.getSystemProperty("test.property"), is("storageMember"));
        }
    }
}
