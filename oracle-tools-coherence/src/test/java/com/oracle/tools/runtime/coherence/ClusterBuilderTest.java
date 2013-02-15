/*
 * File: ClusterBuilderTest.java
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

import com.oracle.tools.runtime.java.VirtualizedJavaApplicationBuilder;
import com.oracle.tools.runtime.java.virtualization.Virtualization;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import junit.framework.Assert;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test the {@link ClusterBuilder} class.
 * <p>
 * Copyright (c) 2010. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClusterBuilderTest extends AbstractTest
{
    /**
     * A test to determine if we can create and destroy {@link Cluster}s.
     *
     * @throws Exception
     */
    @Test
    public void testClusterBuilding() throws Exception
    {
        final int             CLUSTER_SIZE = 5;

        AvailablePortIterator portIterator = Virtualization.getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setEnvironmentVariables(PropertiesBuilder.fromCurrentEnvironmentVariables())
                .setSingleServerMode().setClusterPort(portIterator.next()).setJMXPort(portIterator)
                .setJMXManagementMode(JMXManagementMode.LOCAL_ONLY);

        Cluster cluster = null;

        try
        {
            ClusterBuilder builder = new ClusterBuilder();

            builder.addBuilder(new VirtualizedJavaApplicationBuilder<ClusterMember, ClusterMemberSchema>(),
                               schema,
                               "DCCF",
                               CLUSTER_SIZE);

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
}
