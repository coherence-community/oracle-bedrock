/*
 * File: ClusterMemberVirtualizationFunctionalTest.java
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

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.VirtualizedJavaApplicationBuilder;
import com.oracle.tools.runtime.java.virtualization.Virtualization;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * Functional tests for establishing a cluster of members in-process.
 * <p>
 * Copyright (c) 2010. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class ClusterMemberVirtualizationFunctionalTest extends AbstractTest
{
    private ClusterMember storageOne;
    private ClusterMember storageTwo;
    private ClusterMember extendOne;


    /**
     * Method description
     *
     * @throws Exception
     */
    @Before
    public void startCluster() throws Exception
    {
        AvailablePortIterator jmxPorts      = Virtualization.getAvailablePorts();

        int                   clusterPort   = jmxPorts.next();

        ClusterMemberSchema   storageSchema =
            new ClusterMemberSchema().setClusterPort(clusterPort).setStorageEnabled(true)
                .setCacheConfigURI("test-cache-config.xml").setJMXSupport(true).setRemoteJMXManagement(true)
                .setJMXManagementMode(ClusterMemberSchema.JMXManagementMode.LOCAL_ONLY).setJMXPort(jmxPorts)
                .setSingleServerMode();

        ClusterMemberSchema extendSchema =
            new ClusterMemberSchema().setStorageEnabled(false).setClusterPort(clusterPort)
                .setCacheConfigURI("test-extend-proxy-config.xml").setJMXSupport(true).setRemoteJMXManagement(true)
                .setSystemProperty("coherence.extend.port",
                                   jmxPorts).setJMXManagementMode(ClusterMemberSchema.JMXManagementMode.LOCAL_ONLY)
                                       .setJMXPort(jmxPorts).setSingleServerMode();

        SystemApplicationConsole                                   console = new SystemApplicationConsole();

        JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> builder =
            new VirtualizedJavaApplicationBuilder<ClusterMember, ClusterMemberSchema>();

        extendOne  = builder.realize(extendSchema, "extend-1", console);
        storageOne = builder.realize(storageSchema, "storage-1", console);
        storageTwo = builder.realize(storageSchema, "storage-2", console);

        assertThat(eventually(invoking(extendOne).getLocalMemberId()), is(not(-1)));
        assertThat(eventually(invoking(storageOne).getLocalMemberId()), is(not(-1)));
        assertThat(eventually(invoking(storageTwo).getLocalMemberId()), is(not(-1)));
    }


    /**
     * Method description
     */
    @After
    public void stopCluster()
    {
        destroy(storageTwo);
        destroy(storageOne);
        destroy(extendOne);
    }


    private void destroy(ClusterMember member)
    {
        try
        {
            if (member != null)
            {
                member.destroy();
            }
        }
        catch (Exception e)
        {
            // ignored as we are shutting down
        }
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldHaveStartedClusterWithThreeMembers() throws Exception
    {
        assertThat(eventually(invoking(storageOne).getClusterSize()), is(3));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldHaveIsolatedJMXSoThatEachNodeHasDifferentLocalIdInClusterMBean() throws Exception
    {
        Integer nodeIdOne   = storageOne.getLocalMemberId();
        Integer nodeIdTwo   = storageTwo.getLocalMemberId();
        Integer nodeIdThree = extendOne.getLocalMemberId();

        assertThat(nodeIdOne, is(not(nodeIdTwo)));
        assertThat(nodeIdOne, is(not(nodeIdThree)));
        assertThat(nodeIdTwo, is(not(nodeIdThree)));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldOnlyHaveConnectionManagerOnExtendProxyNode() throws Exception
    {
        ObjectName objectName = new ObjectName("Coherence:type=ConnectionManager,name=ExtendTcpProxyService,nodeId=*");

        Set<ObjectInstance> storageOneMBeans = storageOne.queryMBeans(objectName, null);
        Set<ObjectInstance> storageTwoMBeans = storageTwo.queryMBeans(objectName, null);

        assertThat(storageOneMBeans.size(), is(0));
        assertThat(storageTwoMBeans.size(), is(0));

        assertThat(eventually(invoking(extendOne).queryMBeans(objectName, null).size()), is(1));
    }
}
