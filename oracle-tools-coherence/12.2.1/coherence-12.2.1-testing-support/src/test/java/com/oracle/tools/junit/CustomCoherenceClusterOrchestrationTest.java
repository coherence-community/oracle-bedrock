/*
 * File: CustomCoherenceClusterOrchestrationTest.java
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

package com.oracle.tools.junit;

import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.java.options.SystemProperty;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Functional Tests for the {@link CoherenceClusterOrchestration}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CustomCoherenceClusterOrchestrationTest
{
    /**
     * A {@link CoherenceClusterOrchestration} for a default {@link CoherenceCluster}.
     */
    @ClassRule
    public static CoherenceClusterOrchestration orchestration =
        new CoherenceClusterOrchestration().setStorageMemberCount(3)
        .withStorageMemberOptions(SystemProperty.of("test.property",
                                                    "storageMember"))
                                                    .withProxyMemberOptions(SystemProperty.of("test.property",
                                                                                              "proxyServer"));


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

        assertThat(setNames, contains("proxy-1", "storage-1", "storage-2", "storage-3"));
    }


    @Test
    public void shouldHaveSetProxyServerProperty() throws Exception
    {
        for (CoherenceClusterMember member : orchestration.getCluster().getAll("proxy"))
        {
            assertThat(member.getSystemProperty("test.property"), is("proxyServer"));
        }
    }


    @Test
    public void shouldHaveSetStorageMemberProperty() throws Exception
    {
        for (CoherenceClusterMember member : orchestration.getCluster().getAll("storage"))
        {
            assertThat(member.getSystemProperty("test.property"), is("storageMember"));
        }
    }


    @Test
    public void shouldPerformRollingRestart() throws Exception
    {
        orchestration.restartStorageMembers();

        CoherenceCluster       cluster = orchestration.getCluster();
        CoherenceClusterMember member1 = cluster.get("storage-1");
        CoherenceClusterMember member2 = cluster.get("storage-2");
        CoherenceClusterMember member3 = cluster.get("storage-3");

        assertThat(member1, is(notNullValue()));
        assertThat(member2, is(notNullValue()));
        assertThat(member3, is(notNullValue()));

        assertThat(member1.getLocalMemberId(), is(5));
        assertThat(member2.getLocalMemberId(), is(6));
        assertThat(member3.getLocalMemberId(), is(7));
    }
}
