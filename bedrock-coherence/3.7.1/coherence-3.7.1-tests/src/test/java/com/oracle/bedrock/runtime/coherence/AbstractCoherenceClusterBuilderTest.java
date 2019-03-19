/*
 * File: AbstractCoherenceClusterBuilderTest.java
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

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.junit.AbstractTest;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.runtime.ApplicationListener;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.ClusterPort;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.coherence.options.LocalStorage;
import com.oracle.bedrock.runtime.coherence.options.MachineName;
import com.oracle.bedrock.runtime.coherence.options.WellKnownAddress;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.StabilityPredicate;
import com.oracle.bedrock.util.Capture;
import com.oracle.bedrock.util.Trilean;
import com.tangosol.net.NamedCache;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static com.oracle.bedrock.testsupport.deferred.Eventually.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * Functional Tests for the {@link CoherenceClusterBuilder} class.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractCoherenceClusterBuilderTest extends AbstractTest
{
    /**
     * Provides the {@link Platform} on which tests will be performed.
     */
    public abstract Platform getPlatform();


    /**
     * Ensure we can build and destroy a {@link CoherenceCluster} containing storage-enabled
     * {@link CoherenceCacheServer}s.
     */
    @Test
    public void shouldBuildStorageEnabledCluster()
    {
        final int               CLUSTER_SIZE   = 3;

        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        clusterPort,
                        LocalHost.only(),
                        ClusterName.of("Storage-Only"));

        try (CoherenceCluster cluster = builder.build(getPlatform(), Console.system()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));
        }
    }


    /**
     * Ensure we can build a stable {@link CoherenceCluster} across multiple machines.
     */
    @Test
    public void shouldBuildStableClusterAcrossMultipleMachines()
    {
        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(2, CoherenceClusterMember.class, clusterPort, LocalHost.only(), MachineName.of("machine-1"));

        builder.include(2, CoherenceClusterMember.class, clusterPort, LocalHost.only(), MachineName.of("machine-2"));

        builder.include(2, CoherenceClusterMember.class, clusterPort, LocalHost.only(), MachineName.of("machine-3"));

        try (CoherenceCluster cluster = builder.build(getPlatform(),
                                                      Console.system(),
                                                      StabilityPredicate.of(CoherenceCluster
                                                          .Predicates.autoStartServicesSafe())))
        {
            assertThat(invoking(cluster).getClusterSize(), is(6));
        }
    }


    /**
     * Ensure we can build and close a {@link CoherenceCluster}
     * of storage enabled members with a proxy server.
     */
    @Test
    public void shouldBuildStorageAndProxyCluster()
    {
        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(2,
                        CoherenceClusterMember.class,
                        DisplayName.of("storage"),
                        clusterPort,
                        ClusterName.of("Storage-Proxy"),
                        CacheConfig.of("test-cache-config.xml"),
                        LocalHost.only(),
                        LocalStorage.enabled());

        builder.include(1,
                        CoherenceClusterMember.class,
                        DisplayName.of("extend"),
                        clusterPort,
                        ClusterName.of("Storage-Proxy"),
                        CacheConfig.of("test-extend-proxy-config.xml"),
                        LocalHost.only(),
                        LocalStorage.disabled(),
                        SystemProperty.of("coherence.extend.port", availablePorts));

        try (CoherenceCluster cluster = builder.build(getPlatform(), Console.system()))
        {
            // ensure the cluster size is as expected
            assertThat(invoking(cluster).getClusterSize(), is(3));

            // ensure the member id's are different
            HashSet<Integer> memberIds = new HashSet<>();

            for (CoherenceClusterMember member : cluster)
            {
                // ensure the member id is not -1 (it may not have a member id yet)
                assertThat(invoking(member).getLocalMemberId(), is(greaterThan(0)));

                memberIds.add(member.getLocalMemberId());
            }

            Assert.assertEquals(3, memberIds.size());

            CoherenceClusterMember extendMember = cluster.get("extend-1");

            assertThat(invoking(extendMember).isServiceRunning("ExtendTcpProxyService"), is(true));

            for (CoherenceClusterMember storageMember : cluster.getAll("storage"))
            {
                assertThat(invoking(storageMember).isServiceRunning("ExtendTcpProxyService"), is(false));
            }
        }
    }


    /**
     * Ensure that we can build a cluster using WKA.
     */
    @Test
    public void shouldBuilderWKABasedStorageCluster()
    {
        Capture<Integer>        wkaPort            = new Capture<>(LocalPlatform.get().getAvailablePorts());
        ClusterPort             clusterPort = ClusterPort.of(new Capture<>(LocalPlatform.get().getAvailablePorts()));
        String                  localHost          = System.getProperty("tangosol.coherence.localhost", "127.0.0.1");

        String                  clusterName        = "WKA" + getClass().getSimpleName();

        int                     desiredClusterSize = 4;

        CoherenceClusterBuilder clusterBuilder     = new CoherenceClusterBuilder();

        clusterBuilder.include(desiredClusterSize,
                               CoherenceClusterMember.class,
                               DisplayName.of("storage"),
                               LocalStorage.enabled(),
                               WellKnownAddress.of(localHost, wkaPort),
                               ClusterName.of(clusterName),
                               LocalHost.of(localHost, wkaPort),
                               clusterPort);

        try (CoherenceCluster cluster = clusterBuilder.build(Console.system()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(desiredClusterSize));
        }
    }


    /**
     * Ensure we perform a rolling restart of a {@link CoherenceCluster}
     */
    @Test
    public void shouldPerformRollingRestartOfCluster()
    {
        final int               CLUSTER_SIZE   = 4;

        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));
        String                  clusterName    = "Rolling" + getClass().getSimpleName();

        Platform                platform       = getPlatform();

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        DisplayName.of("DCS"),
                        clusterPort,
                        ClusterName.of(clusterName),
                        LocalHost.only(),
                        Console.system());

        try (CoherenceCluster cluster = builder.build(getPlatform()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            StabilityPredicate<CoherenceCluster> predicate =
                StabilityPredicate.of(CoherenceCluster.Predicates.autoStartServicesSafe());

            cluster.filter(member -> member.isServiceRunning("ProxyService")).relaunch();

            cluster.unordered().limit(2).relaunch(predicate);

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            cluster.unordered().limit(2).relaunch(predicate);

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            cluster.unordered().limit(2).relaunch(predicate);

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // introduce a new system property for the relaunched members
            cluster.relaunch(predicate, SystemProperty.of("cloned", "yes"));

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // ensure all the members have the new system property
            for(CoherenceClusterMember member : cluster)
            {
                assertThat(member.getSystemProperty("cloned"), is("yes"));
            }
        }
    }


    /**
     * Ensure that we can create and use a NamedCache via a CoherenceCacheServer.
     */
    @Test
    public void shouldAccessNamedCache()
    {
        Capture<Integer>        wkaPort        = new Capture<>(LocalPlatform.get().getAvailablePorts());
        final int               CLUSTER_SIZE   = 3;
        String                  localHost      = System.getProperty("tangosol.coherence.localhost", "127.0.0.1");

        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        WellKnownAddress.of(localHost, wkaPort),
                        clusterPort,
                        LocalHost.of(localHost, wkaPort),
                        ClusterName.of("Access"));

        try (CoherenceCluster cluster = builder.build(getPlatform(), Console.system()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            NamedCache namedCache = cluster.getCache("dist-example");

            assertThat(namedCache.size(), is(0));
            namedCache.put("key", "hello");

            assertThat(namedCache.size(), is(1));
            assertThat((String) namedCache.get("key"), is("hello"));
        }
    }


    /**
     * Ensure that a {@link NamedCache} produced by a {@link CoherenceCluster} {@link CoherenceClusterMember}
     * is failed over to another {@link CoherenceClusterMember} when the original {@link CoherenceClusterMember}
     * is closed.
     */
    @Test
    public void shouldFailOverNamedCache()
    {
        final int               CLUSTER_SIZE   = 3;

        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        clusterPort,
                        ClusterName.of("FailOver"),
                        DisplayName.of("DCS"));

        try (CoherenceCluster cluster = builder.build(getPlatform(), Console.system()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // acquire a NamedCache from a specific cluster member
            CoherenceClusterMember member = cluster.get("DCS-1");

            NamedCache             cache  = member.getCache("dist-example");

            // use the cache to put some data
            cache.put("message", "hello");

            assertThat(cluster.get("DCS-2").getCache("dist-example").get("message"), is("hello"));

            // close the cluster member
            member.close();

            // ensure that it's not in the cluster
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE - 1));

            // attempt to use the cache
            assertThat(invoking(cache).get("message"), is("hello"));
        }
    }


    /**
     * Ensure that the explicitly closing a CoherenceCacheServer removes it from
     * the CoherenceCluster in which is it defined.
     */
    @Test
    public void shouldRemoveCoherenceClusterMemberFromCoherenceCluster()
    {
        final int               CLUSTER_SIZE   = 3;

        Capture<Integer>        wkaPort        = new Capture<>(LocalPlatform.get().getAvailablePorts());
        String                  localHost      = System.getProperty("tangosol.coherence.localhost", "127.0.0.1");
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(LocalPlatform.get().getAvailablePorts()));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        WellKnownAddress.of(localHost, wkaPort),
                        clusterPort,
                        LocalHost.of(localHost, wkaPort),
                        ClusterName.of("Access"),
                        DisplayName.of("DCS"));

        try (CoherenceCluster cluster = builder.build(getPlatform(), Console.system()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // acquire a cluster member
            CoherenceClusterMember member = cluster.get("DCS-1");

            // close it
            member.close();

            // ensure that it's not in the cluster
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE - 1));
        }
    }


    /**
     * Ensure we can expand {@link CoherenceClusterMember}s in a {@link CoherenceCluster}.
     */
    @Test
    public void shouldExpandMembersOfACluster()
    {
        final int               CLUSTER_SIZE   = 1;

        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));
        String                  clusterName    = "Expanding" + getClass().getSimpleName();
        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        DisplayName.of("DCS"),
                        clusterPort,
                        ClusterName.of(clusterName),
                        LocalHost.only(),
                        Console.system());

        try (CoherenceCluster cluster = builder.build(getPlatform()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // ensure that the DistributedCache service for the first (and only) cluster member is storage disabled
            assertThat(invoking(cluster.get("DCS-1")).isStorageEnabled("DistributedCache"), is(Trilean.TRUE));

            // ensure that the InvocationService service for the first (and only) cluster member is unknown
            assertThat(invoking(cluster.get("DCS-1")).isStorageEnabled("InvocationService"), is(Trilean.UNKNOWN));

            cluster.expand(1,
                           LocalPlatform.get(),
                           CoherenceClusterMember.class,
                           DisplayName.of("DCS"),
                           clusterPort,
                           ClusterName.of(clusterName),
                           LocalHost.only(),
                           Console.system(),
                           LocalStorage.disabled());

            // ensure that the cluster is bigger by one
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE + 1));

            // ensure that the DistributedCache service for the new member is storage disabled
            assertThat(invoking(cluster.get("DCS-2")).isStorageEnabled("DistributedCache"), is(Trilean.FALSE));

            // ensure that the InvocationService service for the new member is unknown
            assertThat(invoking(cluster.get("DCS-2")).isStorageEnabled("InvocationService"), is(Trilean.UNKNOWN));
        }
    }


    /**
     * Ensure we can clone {@link CoherenceClusterMember}s in a {@link CoherenceCluster}.
     */
    @Test
    public void shouldCloneMembersOfACluster()
    {
        final int               CLUSTER_SIZE   = 1;

        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));
        String                  clusterName    = "Cloning" + getClass().getSimpleName();
        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        builder.include(CLUSTER_SIZE,
                        CoherenceClusterMember.class,
                        DisplayName.of("DCS"),
                        clusterPort,
                        ClusterName.of(clusterName),
                        LocalHost.only(),
                        Console.system());

        try (CoherenceCluster cluster = builder.build(getPlatform()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // ensure that the DistributedCache service for the first (and only) cluster member is storage disabled
            assertThat(invoking(cluster.get("DCS-1")).isStorageEnabled("DistributedCache"), is(Trilean.TRUE));

            // ensure that the InvocationService service for the first (and only) cluster member is unknown
            assertThat(invoking(cluster.get("DCS-1")).isStorageEnabled("InvocationService"), is(Trilean.UNKNOWN));

            // ensure that we don't have the clone system property
            Assert.assertThat(cluster.get("DCS-1").getSystemProperty("cloned"), isEmptyOrNullString());

            cluster.limit(1).clone(1, LocalStorage.disabled(), SystemProperty.of("cloned", "yes"));

            // ensure that the cluster is bigger by one
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE + 1));

            // ensure that the DistributedCache service for the new member is storage disabled
            assertThat(invoking(cluster.get("DCS-2")).isStorageEnabled("DistributedCache"), is(Trilean.FALSE));

            // ensure that the InvocationService service for the new member is unknown
            assertThat(invoking(cluster.get("DCS-2")).isStorageEnabled("InvocationService"), is(Trilean.UNKNOWN));

            // ensure that we added the system property to the clone
            assertThat(cluster.get("DCS-2").getSystemProperty("cloned"), is("yes"));
        }
    }


    /**
     * Ensure we clean up an unsuccessfully created {@link CoherenceCluster}.
     */
    @Test
    public void shouldAvoidPartialClusterCreation()
    {
        AvailablePortIterator   availablePorts = LocalPlatform.get().getAvailablePorts();
        ClusterPort             clusterPort    = ClusterPort.of(new Capture<>(availablePorts));

        CoherenceClusterBuilder builder        = new CoherenceClusterBuilder();

        // these two should start
        builder.include(2, CoherenceClusterMember.class, clusterPort, LocalHost.only(), ClusterName.of("Storage-Only"));

        // this one will start but fail as the listener raises an exception
        builder.include(1,
                        CoherenceClusterMember.class,
                        clusterPort,
                        LocalHost.only(),
                        ClusterName.of("Storage-Only"),
                        Decoration.of(new ApplicationListener<CoherenceClusterMember>()
                                      {
                                          @Override
                                          public void onClosing(CoherenceClusterMember application,
                                                                OptionsByType          optionsByType)
                                          {
                                              // do nothing
                                          }

                                          @Override
                                          public void onClosed(CoherenceClusterMember application,
                                                               OptionsByType          optionsByType)
                                          {
                                              // do nothing
                                          }

                                          @Override
                                          public void onLaunched(CoherenceClusterMember application)
                                          {
                                              throw new IllegalStateException("Let's not start this application");
                                          }
                                      }));

        try (CoherenceCluster cluster = builder.build(getPlatform(), Console.system()))
        {
            Assert.fail("The cluster should not have started");
        }
        catch (RuntimeException e)
        {
            Assert.assertThat(e.getMessage(),
                              containsString("Failed to launch one of the desired CoherenceClusterMember(s)"));
        }
    }
}
