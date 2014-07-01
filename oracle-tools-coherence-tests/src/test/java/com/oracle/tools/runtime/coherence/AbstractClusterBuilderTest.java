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
import com.oracle.tools.predicate.Predicate;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.actions.InteractiveActionExecutor;
import com.oracle.tools.runtime.actions.PerpetualAction;
import com.oracle.tools.runtime.coherence.actions.RestartClusterMemberAction;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.network.AvailablePortIterator;
import com.oracle.tools.runtime.network.Constants;
import com.oracle.tools.util.Capture;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.deferred.Eventually.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Functional Tests for the {@link com.oracle.tools.runtime.coherence.ClusterBuilder} class.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Deprecated
public abstract class AbstractClusterBuilderTest extends AbstractTest
{
    /**
     * Creates a new {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
     * to use for a tests in this class and/or sub-classes.
     *
     * @return the {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
     */
    public abstract JavaApplicationBuilder<ClusterMember> newJavaApplicationBuilder();


    /**
     * Ensure we can build and destroy a {@link com.oracle.tools.runtime.coherence.Cluster}
     * of storage enabled members.
     *
     * @throws Exception
     */
    @Test
    public void shouldBuildStorageEnabledCluster() throws Exception
    {
        final int             CLUSTER_SIZE   = 3;

        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        ClusterMemberSchema   schema         = new ClusterMemberSchema().useLocalHostMode().setClusterPort(clusterPort);

        Cluster               cluster        = null;

        try
        {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(newJavaApplicationBuilder(), schema, "DCS", CLUSTER_SIZE);

            cluster = builder.realize(new SystemApplicationConsole());

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));
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
                cluster.close();
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
        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        ClusterMemberSchema storageSchema =
            new ClusterMemberSchema().setClusterPort(clusterPort).setStorageEnabled(true)
                .setCacheConfigURI("test-cache-config.xml").useLocalHostMode();

        ClusterMemberSchema extendSchema =
            new ClusterMemberSchema().setStorageEnabled(false).setClusterPort(clusterPort)
                .setCacheConfigURI("test-extend-proxy-config.xml").setSystemProperty("coherence.extend.port",
                                                                                     availablePorts).useLocalHostMode();

        Cluster cluster = null;

        try
        {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(newJavaApplicationBuilder(), storageSchema, "storage", 2);
            builder.addBuilder(newJavaApplicationBuilder(), extendSchema, "extend", 1);

            cluster = builder.realize(new SystemApplicationConsole());

            // ensure the cluster size is as expected
            assertThat(invoking(cluster).getClusterSize(), is(3));

            // ensure the member id's are different
            HashSet<Integer> memberIds = new HashSet<Integer>();

            for (ClusterMember member : cluster)
            {
                memberIds.add(member.getLocalMemberId());
            }

            Assert.assertEquals(3, memberIds.size());

            ClusterMember extendMember = cluster.getApplication("extend-1");

            assertThat(invoking(extendMember).isServiceRunning("ExtendTcpProxyService"), is(true));

            for (ClusterMember storageMember : cluster.getApplications("storage"))
            {
                assertThat(invoking(storageMember).isServiceRunning("ExtendTcpProxyService"), is(false));
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
                cluster.close();
            }
        }
    }


    /**
     * Ensure that we can build a cluster using WKA.
     *
     * @throws Exception
     */
    @Test
    public void shouldBuilderWKABasedStorageCluster() throws Exception
    {
        Capture<Integer> wkaPort   = new Capture<Integer>(LocalPlatform.getInstance().getAvailablePorts());
        String           localHost = Constants.getLocalHost();

        ClusterMemberSchema memberSchema =
            new ClusterMemberSchema().setStorageEnabled(true).setWellKnownAddress(localHost)
                .setWellKnownAddressPort(wkaPort).setLocalHostAddress(localHost).setLocalHostPort(wkaPort);

        SystemApplicationConsole console            = new SystemApplicationConsole();

        Cluster                  cluster            = null;
        int                      desiredClusterSize = 3;

        try
        {
            ClusterBuilder clusterBuilder = new ClusterBuilder();

            clusterBuilder.addBuilder(newJavaApplicationBuilder(), memberSchema, "storage", desiredClusterSize);

            cluster = clusterBuilder.realize(console);

            assertThat(invoking(cluster).getClusterSize(), is(desiredClusterSize));
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
                cluster.close();
            }
        }
    }


    /**
     * Ensure we perform a rolling restart of a {@link Cluster}
     *
     * @throws Exception
     */
    @Test
    public void shouldPerformRollingRestartOfCluster() throws Exception
    {
        final int                             CLUSTER_SIZE   = 3;

        AvailablePortIterator                 availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>                      clusterPort    = new Capture<Integer>(availablePorts);

        ClusterMemberSchema schema = new ClusterMemberSchema().useLocalHostMode().setClusterPort(clusterPort);

        Cluster                               cluster        = null;
        ApplicationConsole                    console        = new SystemApplicationConsole();
        JavaApplicationBuilder<ClusterMember> memberBuilder  = newJavaApplicationBuilder();

        try
        {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(memberBuilder, schema, "DCS", CLUSTER_SIZE);

            cluster = builder.realize(console);

            assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE));

            // construct the action to restart a cluster member (iff the DistributedCache is NODE_SAFE)
            RestartClusterMemberAction restartAction = new RestartClusterMemberAction("DCS",
                                                                                      memberBuilder,
                                                                                      schema,
                                                                                      console,
                                                                                      new Predicate<ClusterMember>()
            {
                @Override
                public boolean evaluate(ClusterMember member)
                {
                    ServiceStatus status = member.getServiceStatus("DistributedCache");

                    return status == ServiceStatus.NODE_SAFE;
                }
            });

            // let's perpetually restart a cluster member
            PerpetualAction<ClusterMember, Cluster> perpetualAction = new PerpetualAction<ClusterMember,
                                                                                          Cluster>(restartAction);

            InteractiveActionExecutor<ClusterMember, Cluster> executor = new InteractiveActionExecutor<ClusterMember,
                                                                                                       Cluster>(cluster,
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
        finally
        {
            if (cluster != null)
            {
                cluster.close();
            }
        }
    }
}
