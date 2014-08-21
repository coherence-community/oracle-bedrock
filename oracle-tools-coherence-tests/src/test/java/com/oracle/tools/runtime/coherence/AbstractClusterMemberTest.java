/*
 * File: AbstractClusterMemberTest.java
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

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.coherence.ClusterMemberSchema.JMXManagementMode;
import com.oracle.tools.runtime.coherence.callables.GetClusterName;
import com.oracle.tools.runtime.coherence.callables.GetClusterSize;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberId;
import com.oracle.tools.runtime.coherence.callables.GetServiceStatus;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.oracle.tools.runtime.options.Diagnostics;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static com.oracle.tools.deferred.Eventually.assertThat;

import static org.hamcrest.CoreMatchers.is;

import javax.management.ObjectName;

/**
 * Functional Tests for {@link com.oracle.tools.runtime.coherence.ClusterMember}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Deprecated
public abstract class AbstractClusterMemberTest extends AbstractTest
{
    /**
     * Obtains the {@link Platform} on which to realize applications being tested.
     *
     * @return  the {@link Platform}
     */
    public abstract Platform getPlatform();


    /**
     * Ensure we can start and connect to the Coherence JMX infrastructure.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartJMXConnection() throws Exception
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setClusterPort(availablePorts).useLocalHostMode().setRoleName("test-role")
                .setSiteName("test-site").setJMXManagementMode(JMXManagementMode.LOCAL_ONLY).setJMXPort(availablePorts);

        ClusterMember member = null;

        try
        {
            Platform platform = getPlatform();

            member = platform.realize(schema, "TEST", new SystemApplicationConsole());

            assertThat(invoking(member).getClusterSize(), is(1));
            assertThat(member.getRoleName(), is("test-role"));
            assertThat(member.getSiteName(), is("test-site"));

            // use JMX to determine the cluster size
            int clusterSize = member.getMBeanAttribute(new ObjectName("Coherence:type=Cluster"),
                                                       "ClusterSize",
                                                       Integer.class);

            assertThat(clusterSize, is(1));
        }
        finally
        {
            if (member != null)
            {
                member.close();
            }
        }
    }


    /**
     * Ensure that we can start and stop Coherence Members on the same port
     * continuously (quickly) and there is only ever one member.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartStopMultipleTimes() throws Exception
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setClusterPort(availablePorts).useLocalHostMode().setRoleName("test-role")
                .setSiteName("test-site");

        ClusterMember member   = null;

        Platform      platform = getPlatform();

        for (int i = 1; i <= 10; i++)
        {
            try
            {
                System.out.println("Building Instance: " + i);
                member = platform.realize(schema, "TEST", new SystemApplicationConsole(), Diagnostics.enabled());

                assertThat(invoking(member).getClusterSize(), is(1));
                assertThat(member.getRoleName(), is("test-role"));
                assertThat(member.getSiteName(), is("test-site"));
            }
            finally
            {
                if (member != null)
                {
                    member.close();
                }
            }
        }
    }


    /**
     * Ensure that we can start and stop a single Coherence Cluster Member.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartSingletonCluster() throws Exception
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        ClusterMemberSchema   schema = new ClusterMemberSchema().setClusterPort(availablePorts).useLocalHostMode();

        Platform              platform       = getPlatform();
        ClusterMember         member         = null;

        try
        {
            member = platform.realize(schema, "TEST", new SystemApplicationConsole(), Diagnostics.enabled());

            assertThat(member, new GetLocalMemberId(), is(1));
            assertThat(member, new GetClusterSize(), is(1));
            assertThat(member, new GetServiceStatus("DistributedCache"), is(ServiceStatus.ENDANGERED));
        }
        finally
        {
            if (member != null)
            {
                member.close();
            }
        }
    }


    /**
     * Ensure that we can start and stop a Coherence Cluster Member
     * that uses a specific operational override.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseCustomOperationalOverride() throws Exception
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setClusterPort(availablePorts)
                .setOperationalOverrideURI("test-operational-override.xml").useLocalHostMode();

        Platform      platform = getPlatform();
        ClusterMember member   = null;

        try
        {
            member = platform.realize(schema, "TEST", new SystemApplicationConsole(), Diagnostics.enabled());

            assertThat(member, new GetLocalMemberId(), is(1));
            assertThat(member, new GetClusterSize(), is(1));
            assertThat(member, new GetClusterName(), is("MyCluster"));
        }
        finally
        {
            if (member != null)
            {
                member.close();
            }
        }
    }
}
