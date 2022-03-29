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

package com.oracle.bedrock.runtime.coherence;

import com.oracle.bedrock.options.Diagnostics;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.coherence.callables.*;
import com.oracle.bedrock.runtime.coherence.options.*;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.java.features.JmxFeature;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.Discriminator;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.testsupport.junit.AbstractTest;
import com.oracle.bedrock.testsupport.matchers.MapMatcher;
import com.oracle.bedrock.util.Capture;
import com.tangosol.net.NamedCache;
import com.tangosol.util.aggregator.LongSum;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.PresentFilter;
import org.junit.jupiter.api.Test;

import javax.management.ObjectName;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static com.oracle.bedrock.testsupport.deferred.Eventually.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Functional Tests for {@link CoherenceCacheServer}s.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
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
     * @throws Exception if an error occurs
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
                                                           JmxFeature.enabled(),
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
                                                           Console.system()))
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
                                                               Console.system()))
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
                                                           Console.system()))
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
                                                           Console.system(),
                                                           Diagnostics.enabled()))
        {
            assertThat(server, new GetLocalMemberId(), is(1));
            assertThat(server, new GetClusterSize(), is(1));

            Set<String> serviceNames = server.invoke(new GetAutoStartServiceNames());

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
                                                           LocalHost.only(),
                                                           WellKnownAddress.of("127.0.0.1"),
                                                           IPv4Preferred.yes(),
                                                           Console.system()))
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


    @Test
    public void shouldSendEventsFromCustomServer() throws Exception
    {
        Platform      platform = getPlatform();
        EventListener listener = new EventListener(1);
        RemoteEvent   event    = new CustomServer.Event(19);

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClassName.of(CustomServer.class),
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           Console.system()))
        {
            server.addListener(listener, StreamName.of("Foo"));

            CustomServer.fireEvent(server, "Foo", event);

            assertThat(listener.await(1, TimeUnit.MINUTES), is(true));
            assertThat(listener.getEvents().get(0), is(event));

            server.close();
        }

    }


    /**
     * An instance of a {@link RemoteEventListener} that captures events.
     */
    public static class EventListener implements RemoteEventListener
    {
        /**
         * The counter to count the number of events received.
         */
        private final CountDownLatch latch;

        /**
         * The list of events received.
         */
        private final List<RemoteEvent> events;


        /**
         * Create an {@link EventListener} to receieve the expected number of events.
         *
         * @param expected  the expected number of events
         */
        public EventListener(int expected)
        {
            latch  = new CountDownLatch(expected);
            events = new ArrayList<>();
        }


        /**
         * Causes the current thread to wait until the expected number of events
         * have been received, unless the thread is {@linkplain Thread#interrupt interrupted},
         * or the specified waiting time elapses.
         *
         * @param timeout  the maximum time to wait
         * @param unit     the time unit of the {@code timeout} argument
         *
         * @return {@code true} if the correct number of events is received and {@code false}
         *         if the waiting time elapsed before the events were received
         *
         * @throws InterruptedException if the current thread is interrupted
         *         while waiting
         */
        private boolean await(long     timeout,
                              TimeUnit unit) throws InterruptedException
        {
            return latch.await(timeout, unit);
        }


        public List<RemoteEvent> getEvents()
        {
            return events;
        }


        @Override
        public void onEvent(RemoteEvent event)
        {
            events.add(event);
            latch.countDown();
        }
    }
}
