/*
 * File: AbstractClusterBuilderTest.java
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

import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.coherence.ClusterMemberSchema.JMXManagementMode;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.ContainerBasedJavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import com.oracle.tools.runtime.java.container.Container;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import junit.framework.Assert;

import org.hamcrest.Matchers;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.is;

import java.util.HashSet;

import javax.management.ObjectName;

/**
 * Functional Tests for the {@link com.oracle.tools.runtime.coherence.ClusterBuilder} class.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractClusterBuilderTest extends AbstractTest
{
    /**
     * Creates a new {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
     * to use for a tests in this class and/or sub-classes.
     *
     * @return the {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
     */
    public abstract JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> newJavaApplicationBuilder();


    /**
     * Ensure we can build and destroy a {@link com.oracle.tools.runtime.coherence.Cluster}
     * of storage enabled members.
     *
     * @throws Exception
     */
    @Test
    public void shouldBuildStorageEnabledCluster() throws Exception
    {
        final int             CLUSTER_SIZE = 3;

        AvailablePortIterator portIterator = Container.getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setEnvironmentInherited(false)
                .setSingleServerMode().setClusterPort(portIterator.next()).setJMXPort(portIterator)
                .setJMXManagementMode(JMXManagementMode.LOCAL_ONLY);

        schema.setEnvironmentInherited(true);
        
        Cluster cluster = null;

        try
        {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(newJavaApplicationBuilder(), schema, "DCCF", CLUSTER_SIZE);

            cluster = builder.realize(new SystemApplicationConsole());

            assertThat(eventually(invoking(cluster).getClusterSize()), is(CLUSTER_SIZE));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
        finally
        {
            if (cluster != null)
            {
                cluster.destroy();
            }
        }
    }


    /**
     * Ensure we can build and destroy a {@link com.oracle.tools.runtime.coherence.Cluster}
     * of storage enabled members with a proxy server.
     *
     * @throws Exception
     */
    @Test
    public void shouldBuildStorageAndProxyCluster() throws Exception
    {
        AvailablePortIterator jmxPorts    = Container.getAvailablePorts();

        int                   clusterPort = jmxPorts.next();

        ClusterMemberSchema storageSchema =
            new ClusterMemberSchema().setEnvironmentInherited(false).setClusterPort(clusterPort).setStorageEnabled(true)
                .setCacheConfigURI("test-cache-config.xml").setJMXSupport(true).setRemoteJMXManagement(true)
                .setJMXManagementMode(ClusterMemberSchema.JMXManagementMode.LOCAL_ONLY).setJMXPort(jmxPorts)
                .setSingleServerMode();

        storageSchema.setEnvironmentInherited(true);

        ClusterMemberSchema extendSchema =
            new ClusterMemberSchema().setEnvironmentInherited(false).setStorageEnabled(false).setClusterPort(clusterPort)
                .setCacheConfigURI("test-extend-proxy-config.xml").setJMXSupport(true).setRemoteJMXManagement(true)
                .setSystemProperty("coherence.extend.port",
                                   jmxPorts).setJMXManagementMode(ClusterMemberSchema.JMXManagementMode.LOCAL_ONLY)
                                       .setJMXPort(jmxPorts).setSingleServerMode();

        extendSchema.setEnvironmentInherited(true);

        SystemApplicationConsole console = new SystemApplicationConsole();

        Cluster                  cluster = null;

        try
        {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(newJavaApplicationBuilder(), storageSchema, "storage", 2);
            builder.addBuilder(newJavaApplicationBuilder(), extendSchema, "extend", 1);

            cluster = builder.realize(new SystemApplicationConsole());

            // ensure the cluster size is as expected
            assertThat(eventually(invoking(cluster).getClusterSize()), is(3));

            // ensure the member id's are different
            HashSet<Integer> memberIds = new HashSet<Integer>();

            for (ClusterMember member : cluster)
            {
                memberIds.add(member.getLocalMemberId());
            }

            Assert.assertEquals(3, memberIds.size());

            // ensure the there's only one extend service instance
            ObjectName objectName =
                new ObjectName("Coherence:type=ConnectionManager,name=ExtendTcpProxyService,nodeId=*");

            ClusterMember extendMember = cluster.getApplication("extend-1");

            assertThat(eventually(invoking(extendMember).queryMBeans(objectName, null).size()), Matchers.is(1));

            for (ClusterMember storageMember : cluster.getApplications("storage"))
            {
                assertThat(eventually(invoking(storageMember).queryMBeans(objectName, null).size()), Matchers.is(0));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
        finally
        {
            if (cluster != null)
            {
                cluster.destroy();
            }
        }
    }
}
