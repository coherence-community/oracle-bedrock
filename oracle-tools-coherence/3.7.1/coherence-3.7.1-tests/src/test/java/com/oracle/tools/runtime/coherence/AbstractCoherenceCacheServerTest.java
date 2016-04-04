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

import com.oracle.tools.matchers.MapMatcher;

import com.oracle.tools.options.Diagnostics;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.coherence.callables.GetClusterName;
import com.oracle.tools.runtime.coherence.callables.GetClusterSize;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberId;
import com.oracle.tools.runtime.coherence.callables.GetServiceStatus;
import com.oracle.tools.runtime.coherence.options.ClusterPort;
import com.oracle.tools.runtime.coherence.options.LocalHost;
import com.oracle.tools.runtime.coherence.options.OperationalOverride;
import com.oracle.tools.runtime.coherence.options.RoleName;
import com.oracle.tools.runtime.coherence.options.SiteName;

import com.oracle.tools.runtime.concurrent.RemoteEvent;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.options.StreamName;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.features.JmxFeature;
import com.oracle.tools.runtime.java.options.ClassName;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
            assertThat(server, new GetServiceStatus("DistributedCache"), is(ServiceStatus.ENDANGERED));
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

            NamedCache namedCache = server.getCache("dist-example");

            // ----- use NamedCache.clear -----
            namedCache.clear();

            assertThat(namedCache.size(), is(0));

            // ----- use NamedCache.put and NamedCache.get -----
            namedCache.put("key", "hello");

            assertThat(namedCache.size(), is(1));
            assertThat((String) namedCache.get("key"), is("hello"));

            // ----- use NamedCache.invokeAll -----
            Map map = namedCache.invokeAll(PresentFilter.INSTANCE, new GetProcessor());

            assertThat(map, notNullValue());
            assertThat(map.size(), is(1));
            assertThat((String) map.get("key"), is("hello"));

            // ----- use NamedCache.keySet -----
            Set keySet = namedCache.keySet();

            assertThat(keySet, notNullValue());
            assertThat(keySet.size(), is(1));
            assertThat(keySet.contains("key"), is(true));

            // ----- use NamedCache.entrySet -----
            Set entrySet = namedCache.entrySet();

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
            assertThat((String) namedCache.get("key"), is("hello"));

            // ----- use NamedCache.remove -----
            namedCache.remove("key");

            assertThat(namedCache.size(), is(0));

            // ----- use NamedCache.putAll -----
            HashMap<String, Integer> putAllMap = new HashMap<>();
            long                     sum       = 0;

            for (int i = 1; i < 5; i++)
            {
                String key = Integer.toString(i);

                putAllMap.put(key, new Integer(i));

                sum += i;
            }

            namedCache.putAll(putAllMap);

            assertThat(namedCache.size(), is(putAllMap.size()));

            // ----- use NamedCache.getAll -----
            Map<String, Integer> getAllResults = namedCache.getAll(putAllMap.keySet());

            assertThat(getAllResults, MapMatcher.sameAs(putAllMap));

            Map<String, Integer> otherGetAllResults = namedCache.getAll(namedCache.keySet());

            assertThat(otherGetAllResults, MapMatcher.sameAs(putAllMap));

            // ----- use NamedCache.aggregate -----
            long longSum = (Long) namedCache.aggregate(PresentFilter.INSTANCE, new LongSum(IdentityExtractor.INSTANCE));

            assertThat(longSum, is(sum));

            long anotherLongSum = (Long) namedCache.aggregate(putAllMap.keySet(),
                                                              new LongSum(IdentityExtractor.INSTANCE));

            assertThat(anotherLongSum, is(sum));
        }
    }


    @Test
    public void shouldSendEventsFromCustomServer() throws Exception
    {
        Platform      platform = getPlatform();
        EventListener listener = new EventListener(1);
        String        name     = "Foo";
        RemoteEvent   event    = new CustomServer.Event(19);

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClassName.of(CustomServer.class),
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           SystemApplicationConsole.builder()))
        {
            server.addListener(listener, StreamName.of(name));

            CustomServer.fireEvent(server, name, event);

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
