/*
 * File: AbstractCoherenceCacheServerTest.java
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

import com.oracle.tools.runtime.coherence.callables.GetClusterName;
import com.oracle.tools.runtime.coherence.callables.GetClusterSize;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberId;
import com.oracle.tools.runtime.coherence.callables.GetServiceStatus;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.JavaApplicationBuilder;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.tangosol.net.NamedCache;

import com.tangosol.util.filter.PresentFilter;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static com.oracle.tools.deferred.Eventually.assertThat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

/**
 * Functional Tests for {@link CoherenceCacheServer}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractCoherenceCacheServerTest<B extends JavaApplicationBuilder<CoherenceCacheServer>>
    extends AbstractTest
{
    /**
     * Creates a new {@link JavaApplicationBuilder}
     * to use for a tests in this class and/or sub-classes.
     *
     * @return the {@link JavaApplicationBuilder}
     */
    public abstract B newJavaApplicationBuilder();


    /**
     * Ensure we can start and connect to the Coherence JMX infrastructure.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartJMXConnection() throws Exception
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setClusterPort(availablePorts).useLocalHostMode().setRoleName("test-role")
                .setSiteName("test-site").setJMXManagementMode(JMXManagementMode.LOCAL_ONLY).setJMXPort(availablePorts);

        B builder = newJavaApplicationBuilder();

        try (CoherenceCacheServer server = builder.realize(schema, "TEST", new SystemApplicationConsole()))
        {
            assertThat(invoking(server).getClusterSize(), is(1));
            assertThat(server.getRoleName(), is("test-role"));
            assertThat(server.getSiteName(), is("test-site"));

            // use JMX to determine the cluster size
            int size = server.getMBeanAttribute(new ObjectName("Coherence:type=Cluster"), "ClusterSize", Integer.class);

            assertThat(size, is(1));
        }
    }


    /**
     * Ensure that we can start and stop Coherence Members on the same port
     * continuously (quickly) and there is only ever one member.
     */
    @Test
    public void shouldStartStopMultipleTimes()
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setClusterPort(availablePorts).useLocalHostMode().setRoleName("test-role")
                .setSiteName("test-site").setDiagnosticsEnabled(true);

        B builder = newJavaApplicationBuilder();

        for (int i = 1; i <= 10; i++)
        {
            System.out.println("Building Instance: " + i);

            try (CoherenceCacheServer server = builder.realize(schema, "TEST"))
            {
                assertThat(invoking(server).getClusterSize(), is(1));
                assertThat(server.getRoleName(), is("test-role"));
                assertThat(server.getSiteName(), is("test-site"));
            }
        }
    }


    /**
     * Ensure that we can start and stop a single Coherence Cluster Member.
     */
    @Test
    public void shouldStartSingletonCluster()
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setClusterPort(availablePorts).useLocalHostMode()
                .setDiagnosticsEnabled(true);

        B builder = newJavaApplicationBuilder();

        try (CoherenceCacheServer server = builder.realize(schema, "TEST"))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));
            assertThat(server, new GetServiceStatus("DistributedCache"), is(ServiceStatus.ENDANGERED));
        }
    }


    /**
     * Ensure that we can start and stop a Coherence Cluster Member
     * that uses a specific operational override.
     */
    @Test
    public void shouldUseCustomOperationalOverride()
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setClusterPort(availablePorts)
                .setOperationalOverrideURI("test-operational-override.xml").useLocalHostMode()
                .setDiagnosticsEnabled(true);

        B builder = newJavaApplicationBuilder();

        try (CoherenceCacheServer server = builder.realize(schema, "TEST"))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));
            assertThat(server, new GetClusterName(), is("MyCluster"));
        }
    }


    /**
     * Ensure that we can create and use a NamedCache via a CoherenceCacheServer.
     */
    @Test
    public void shouldAccessNamedCache()
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setClusterPort(availablePorts).useLocalHostMode()
                .setDiagnosticsEnabled(true);

        B builder = newJavaApplicationBuilder();

        try (CoherenceCacheServer server = builder.realize(schema, "TEST"))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));

            NamedCache namedCache = server.getCache("dist-example");

            namedCache.clear();

            assertThat(namedCache.size(), is(0));

            namedCache.put("key", "hello");

            assertThat(namedCache.size(), is(1));
            assertThat(namedCache.get("key"), is("hello"));

            Map map = namedCache.invokeAll(PresentFilter.INSTANCE, new GetProcessor());

            assertThat(map, notNullValue());
            assertThat(map.size(), is(1));
            assertThat(map.get("key"), is("hello"));

            Set keySet = namedCache.keySet();

            assertThat(keySet, notNullValue());
            assertThat(keySet.size(), is(1));
            assertThat(keySet.contains("key"), is(true));

            Set entrySet = namedCache.entrySet();

            assertThat(entrySet, notNullValue());
            assertThat(entrySet.size(), is(1));
            assertThat(entrySet.contains(new AbstractMap.SimpleEntry("key", "hello")), is(true));

            Collection values = namedCache.values();

            assertThat(values, notNullValue());
            assertThat(values.size(), is(1));
            assertThat(values.contains("hello"), is(true));

            namedCache.clear();

            assertThat(namedCache.size(), is(0));
        }
    }
}
