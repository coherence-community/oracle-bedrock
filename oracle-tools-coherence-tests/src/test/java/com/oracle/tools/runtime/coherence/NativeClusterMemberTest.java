/*
 * File: NativeClusterMemberTest.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.ContainerBasedJavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.NativeJavaApplicationBuilder;
import com.oracle.tools.runtime.java.container.Container;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.tangosol.net.*;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static org.hamcrest.CoreMatchers.is;

import java.io.Serializable;

import java.util.concurrent.Callable;

/**
 * Functional Test for {@link com.oracle.tools.runtime.coherence.ClusterMember}
 * using a {@link com.oracle.tools.runtime.java.NativeJavaApplicationBuilder}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NativeClusterMemberTest extends AbstractClusterMemberTest
{
    /**
     * {@inheritDoc}
     */
    @Override
    public JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> newJavaApplicationBuilder()
    {
        return new NativeJavaApplicationBuilder<ClusterMember, ClusterMemberSchema>();
    }


    /**
     * Ensure that we can start and stop a single Coherence Cluster Member.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartSingletonCluster() throws Exception
    {
        AvailablePortIterator                                      portIterator = Container.getAvailablePorts();

        int                                                        clusterPort  = portIterator.next();
        int                                                        jmxPort      = portIterator.next();

        ClusterMemberSchema schema = new ClusterMemberSchema().setClusterPort(portIterator).setSingleServerMode();

        JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> builder      = newJavaApplicationBuilder();
        ClusterMember                                              member       = null;

        try
        {
            member = builder.realize(schema, "TEST", new SystemApplicationConsole());

            assertThat(member, new GetClusterMemberId(), is(1));
        }
        finally
        {
            if (member != null)
            {
                member.destroy();
            }
        }
    }


    /**
     * A {@link java.util.concurrent.Callable} to return the Member ID of a Cluster Member.
     */
    public static class GetClusterMemberId implements Callable<Integer>, Serializable
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Integer call() throws Exception
        {
            com.tangosol.net.Cluster cluster  = CacheFactory.getCluster();

            int                      memberId = cluster == null ? -1 : cluster.getLocalMember().getId();

            return memberId;
        }
    }
}
