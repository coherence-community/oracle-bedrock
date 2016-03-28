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

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.matchers.MapMatcher;

import com.oracle.tools.options.Diagnostics;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.coherence.callables.GetAutoStartServiceNames;
import com.oracle.tools.runtime.coherence.callables.GetClusterName;
import com.oracle.tools.runtime.coherence.callables.GetClusterSize;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberId;
import com.oracle.tools.runtime.coherence.callables.GetServiceStatus;
import com.oracle.tools.runtime.coherence.options.CacheConfig;
import com.oracle.tools.runtime.coherence.options.ClusterPort;
import com.oracle.tools.runtime.coherence.options.LocalHost;
import com.oracle.tools.runtime.coherence.options.OperationalOverride;
import com.oracle.tools.runtime.coherence.options.RoleName;
import com.oracle.tools.runtime.coherence.options.SiteName;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.features.JmxFeature;
import com.oracle.tools.runtime.java.options.SystemProperty;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.oracle.tools.runtime.options.Discriminator;

import com.oracle.tools.util.Capture;

import com.tangosol.net.NamedCache;

import com.tangosol.util.aggregator.LongSum;

import com.tangosol.util.extractor.IdentityExtractor;

import com.tangosol.util.filter.PresentFilter;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static com.oracle.tools.deferred.Eventually.assertThat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
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
public abstract class AbstractCoherenceCacheServerTest extends AbstractTest
{
    /**
     * Obtains the {@link Platform} to use when realizing applications.
     */
    public abstract Platform getPlatform();


    /**
     * Ensure we can start and connect to the Coherence using the {@link JmxFeature}.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartJMXConnection() throws Exception
    {
        AvailablePortIterator availablePorts = LocalPlatform.get().getAvailablePorts();

        Platform              platform       = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.from(availablePorts),
                                                           LocalHost.only(),
                                                           RoleName.of("test-role"),
                                                           SiteName.of("test-site"),
                                                           SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_PORT,
                                                                             availablePorts),
                                                           SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_SSL,
                                                                             "false"),
                                                           SystemProperty.of(JmxFeature
                                                               .SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE,
                                                                             "false"),
                                                           JMXManagementMode.LOCAL_ONLY))

        {
            assertThat(invoking(server).getClusterSize(), is(1));
            assertThat(server.getRoleName(), is("test-role"));
            assertThat(server.getSiteName(), is("test-site"));

            JmxFeature jmxFeature = server.get(JmxFeature.class);

            assertThat(jmxFeature, is(notNullValue()));

            // use JMX to determine the cluster size
            int size = jmxFeature.getMBeanAttribute(new ObjectName("Coherence:type=Cluster"),
                                                    "ClusterSize",
                                                    Integer.class);

            assertThat(size, is(1));
        }
    }


    /**
     * Ensure that we can start and stop a single Coherence Cluster Member.
     */
    @Test
    public void shouldStartSingletonCluster()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           Diagnostics.enabled(),
                                                           SystemApplicationConsole.builder()))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));
            assertThat(server, new GetServiceStatus("PartitionedCache"), is(ServiceStatus.ENDANGERED));
        }
    }


    /**
     * Ensure that we can start and stop Coherence Members on the same port
     * continuously (quickly) and there is only ever one member.
     */
    @Test
    public void shouldStartStopMultipleTimes()
    {
        AvailablePortIterator availablePorts = LocalPlatform.get().getAvailablePorts();

        Platform              platform       = getPlatform();

        ClusterPort           clusterPort    = ClusterPort.from(new Capture<>(availablePorts));

        for (int i = 1; i <= 10; i++)
        {
            System.out.println("Building Instance: " + i);

            try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                               clusterPort,
                                                               LocalHost.only(),
                                                               RoleName.of("test-role"),
                                                               SiteName.of("test-site"),
                                                               Diagnostics.enabled(),
                                                               Discriminator.of(i),
                                                               SystemApplicationConsole.builder()))
            {
                assertThat(invoking(server).getClusterSize(), is(1));
                assertThat(server.getRoleName(), is("test-role"));
                assertThat(server.getSiteName(), is("test-site"));
            }
        }
    }


    /**
     * Ensure that we can start and stop a Coherence Cluster Member
     * that uses a specific operational override.
     */
    @Test
    public void shouldUseCustomOperationalOverride()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           OperationalOverride.of("test-operational-override.xml"),
                                                           LocalHost.only(),
                                                           Diagnostics.enabled(),
                                                           SystemApplicationConsole.builder()))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));
            assertThat(server, new GetClusterName(), is("MyCluster"));
        }
    }


    /**
     * Ensure that we can get all of the auto-start service names of a Coherence Cluster Member.
     */
    @Test
    public void shouldEnsureAutoStartServicesAreStarted()
    {
        AvailablePortIterator availablePorts = LocalPlatform.get().getAvailablePorts();

        Platform              platform       = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.from(availablePorts),
                                                           LocalHost.only(),
                                                           CacheConfig.of("test-autostart-services-cache-config.xml"),
                                                           SystemApplicationConsole.builder(),
                                                           Diagnostics.enabled()))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));

            Set<String> serviceNames = server.submit(new GetAutoStartServiceNames());

            for (String serviceName : serviceNames)
            {
                Eventually.assertThat(invoking(server).isServiceRunning(serviceName), is(true));
            }
        }
    }


    /**
     * Ensure that we can create and use a NamedCache via a CoherenceCacheServer.
     */
    @Test
    public void shouldAccessNamedCache()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           SystemApplicationConsole.builder()))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));

            NamedCache<String, String> namedCache = server.getCache("dist-example", String.class, String.class);

            // ----- use NamedCache.clear -----
            namedCache.clear();

            assertThat(namedCache.size(), is(0));

            // ----- use NamedCache.put and NamedCache.get -----
            namedCache.put("key", "hello");

            assertThat(namedCache.size(), is(1));
            assertThat(namedCache.get("key"), is("hello"));

            // ----- use NamedCache.invokeAll -----
            Map<String, String> map = namedCache.invokeAll(PresentFilter.INSTANCE, new GetProcessor());

            assertThat(map, notNullValue());
            assertThat(map.size(), is(1));
            assertThat(map.get("key"), is("hello"));

            // ----- use NamedCache.keySet -----
            Set keySet = namedCache.keySet();

            assertThat(keySet, notNullValue());
            assertThat(keySet.size(), is(1));
            assertThat(keySet.contains("key"), is(true));

            // ----- use NamedCache.entrySet -----
            Set<Map.Entry<String, String>> entrySet = namedCache.entrySet();

            assertThat(entrySet, notNullValue());
            assertThat(entrySet.size(), is(1));
            assertThat(entrySet.contains(new AbstractMap.SimpleEntry("key", "hello")), is(true));

            // ----- use NamedCache.values -----
            Collection values = namedCache.values();

            assertThat(values, notNullValue());
            assertThat(values.size(), is(1));
            assertThat(values.contains("hello"), is(true));

            namedCache.clear();

            assertThat(invoking(namedCache).size(), is(0));

            namedCache.put("key", "hello");

            assertThat(namedCache.size(), is(1));
            assertThat(namedCache.get("key"), is("hello"));

            // ----- use NamedCache.remove -----
            namedCache.remove("key");

            assertThat(namedCache.size(), is(0));

            // ----- use NamedCache.truncate -----
            namedCache.put("key", "value");

            namedCache.truncate();

            assertThat(namedCache.size(), is(0));

            // ----- use NamedCache.putAll -----
            NamedCache<String, Integer> otherNamedCache = server.getCache("dist-other", String.class, Integer.class);

            HashMap<String, Integer>    putAllMap       = new HashMap<>();
            long                        sum             = 0;

            for (int i = 1; i < 5; i++)
            {
                String key = Integer.toString(i);

                putAllMap.put(key, i);

                sum += i;
            }

            otherNamedCache.putAll(putAllMap);

            assertThat(otherNamedCache.size(), is(putAllMap.size()));

            // ----- use NamedCache.getAll -----
            Map<String, Integer> getAllResults = otherNamedCache.getAll(putAllMap.keySet());

            assertThat(getAllResults, MapMatcher.sameAs(putAllMap));

            Map<String, Integer> otherGetAllResults = otherNamedCache.getAll(otherNamedCache.keySet());

            assertThat(otherGetAllResults, MapMatcher.sameAs(putAllMap));

            // ----- use NamedCache.aggregate -----
            long longSum = (Long) otherNamedCache.aggregate(PresentFilter.INSTANCE,
                    new LongSum(IdentityExtractor.INSTANCE));

            assertThat(longSum, is(sum));

            long anotherLongSum = (Long) otherNamedCache.aggregate(putAllMap.keySet(),
                    new LongSum(IdentityExtractor.INSTANCE));

            assertThat(anotherLongSum, is(sum));
        }
    }
}
