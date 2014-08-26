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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.predicate.Predicate;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.actions.InteractiveActionExecutor;
import com.oracle.tools.runtime.actions.PerpetualAction;

import com.oracle.tools.runtime.coherence.actions.RestartCoherenceClusterMemberAction;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.JavaApplicationBuilder;

import com.oracle.tools.runtime.network.AvailablePortIterator;
import com.oracle.tools.runtime.network.Constants;

import com.oracle.tools.util.Capture;

import com.tangosol.net.NamedCache;

import org.junit.Assert;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static com.oracle.tools.deferred.Eventually.assertThat;

import static org.hamcrest.CoreMatchers.is;

import java.util.HashSet;

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
     * Creates a new {@link JavaApplicationBuilder} to use for a tests in this class and/or sub-classes.
     *
     * @return the {@link JavaApplicationBuilder}
     */
    public abstract Platform getPlatform();


    /**
     * Ensure we can build and destroy a {@link CoherenceCluster} containing storage-enabled
     * {@link CoherenceCacheServer}s.
     */
    @Test
    public void shouldBuildStorageEnabledCluster()
    {
        final int             CLUSTER_SIZE   = 3;

        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().useLocalHostMode().setClusterPort(clusterPort)
                .setClusterName("Storage-Only");

        CoherenceClusterBuilder builder = new CoherenceClusterBuilder();

        builder.addSchema("DCS", schema, CLUSTER_SIZE, getPlatform());

        try (CoherenceCluster cluster = builder.realize(new SystemApplicationConsole()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }


    /**
     * Ensure we can build and destroy a {@link com.oracle.tools.runtime.coherence.Cluster}
     * of storage enabled members with a proxy server.
     */
    @Test
    public void shouldBuildStorageAndProxyCluster()
    {
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        CoherenceCacheServerSchema storageSchema =
            new CoherenceCacheServerSchema().setClusterPort(clusterPort).setStorageEnabled(true)
                .setCacheConfigURI("test-cache-config.xml").useLocalHostMode().setClusterName("Storage-Proxy");

        CoherenceCacheServerSchema extendSchema =
            new CoherenceCacheServerSchema().setStorageEnabled(false).setClusterPort(clusterPort)
                .setClusterName("Storage-Proxy").setCacheConfigURI("test-extend-proxy-config.xml")
                .setSystemProperty("coherence.extend.port",
                                   availablePorts).useLocalHostMode();

        CoherenceClusterBuilder builder = new CoherenceClusterBuilder();

        builder.addSchema("storage", storageSchema, 2, getPlatform());
        builder.addSchema("extend", extendSchema, 1, getPlatform());

        try (CoherenceCluster cluster = builder.realize(new SystemApplicationConsole()))
        {
            // ensure the cluster size is as expected
            assertThat(invoking(cluster).getClusterSize(), is(3));

            // ensure the member id's are different
            HashSet<Integer> memberIds = new HashSet<Integer>();

            for (CoherenceClusterMember member : cluster)
            {
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
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }


    /**
     * Ensure that we can build a cluster using WKA.
     */
    @Test
    public void shouldBuilderWKABasedStorageCluster()
    {
        Capture<Integer> wkaPort     = new Capture<Integer>(LocalPlatform.getInstance().getAvailablePorts());
        Capture<Integer> clusterPort = new Capture<Integer>(LocalPlatform.getInstance().getAvailablePorts());
        String           localHost   = Constants.getLocalHost();

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setStorageEnabled(true).setWellKnownAddress(localHost)
                .setClusterName("WKA").setWellKnownAddressPort(wkaPort).setLocalHostAddress(localHost)
                .setLocalHostPort(wkaPort).setClusterPort(clusterPort);

        SystemApplicationConsole console            = new SystemApplicationConsole();
        int                      desiredClusterSize = 4;
        CoherenceClusterBuilder  clusterBuilder     = new CoherenceClusterBuilder();

        clusterBuilder.addSchema("storage", schema, desiredClusterSize, getPlatform());

        try (CoherenceCluster cluster = clusterBuilder.realize(console))
        {
            assertThat(invoking(cluster).getClusterSize(), is(desiredClusterSize));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }


    /**
     * Ensure we perform a rolling restart of a {@link CoherenceCluster}
     */
    @Test
    public void shouldPerformRollingRestartOfCluster()
    {
        final int             CLUSTER_SIZE   = 4;

        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().useLocalHostMode().setClusterPort(clusterPort).setClusterName("Rolling");

        ApplicationConsole      console  = new SystemApplicationConsole();
        Platform                platform = getPlatform();

        CoherenceClusterBuilder builder  = new CoherenceClusterBuilder();

        builder.addSchema("DCS", schema, CLUSTER_SIZE, platform);

        try (CoherenceCluster cluster = builder.realize(console))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // construct the action to restart a cluster member (iff the DistributedCache is NODE_SAFE)
            RestartCoherenceClusterMemberAction restartAction = new RestartCoherenceClusterMemberAction("DCS",
                                                                                                        schema,
                                                                                                        console,
                                                                                                        new Predicate<CoherenceClusterMember>()
            {
                @Override
                public boolean evaluate(CoherenceClusterMember member)
                {
                    ServiceStatus status = member.getServiceStatus("DistributedCache");

                    return status == ServiceStatus.NODE_SAFE;
                }
            },
                                                                                                        platform);

            // let's perpetually restart a cluster member
            PerpetualAction<CoherenceClusterMember, CoherenceCluster> perpetualAction =
                new PerpetualAction<CoherenceClusterMember, CoherenceCluster>(restartAction);

            InteractiveActionExecutor<CoherenceClusterMember, CoherenceCluster> executor =
                new InteractiveActionExecutor<CoherenceClusterMember, CoherenceCluster>(cluster,
                                                                                        perpetualAction);

            executor.executeNext();
            executor.executeNext();
            executor.executeNext();

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }


    /**
     * Ensure that we can create and use a NamedCache via a CoherenceCacheServer.
     */
    @Test
    public void shouldAccessNamedCache()
    {
        final int             CLUSTER_SIZE   = 3;

        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().useLocalHostMode().setClusterPort(clusterPort).setClusterName("Access");

        CoherenceClusterBuilder builder = new CoherenceClusterBuilder();

        builder.addSchema("DCS", schema, CLUSTER_SIZE, getPlatform());

        try (CoherenceCluster cluster = builder.realize(new SystemApplicationConsole()))
        {
            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            NamedCache namedCache = cluster.getCache("dist-example");

            assertThat(namedCache.size(), is(0));
            namedCache.put("key", "hello");

            assertThat(namedCache.size(), is(1));
            assertThat(namedCache.get("key"), is("hello"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
