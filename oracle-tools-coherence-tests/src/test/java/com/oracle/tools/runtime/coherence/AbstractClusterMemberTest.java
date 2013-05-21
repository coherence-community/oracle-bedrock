/*
 * File: AbstractClusterMemberTest.java
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

import com.oracle.tools.runtime.Application;

import com.oracle.tools.runtime.coherence.ClusterMemberSchema.JMXManagementMode;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.NativeJavaApplicationBuilder;
import com.oracle.tools.runtime.java.container.Container;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import junit.framework.Assert;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.is;

/**
 * Functional Tests for {@link com.oracle.tools.runtime.coherence.ClusterMember}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractClusterMemberTest extends AbstractTest
{
    /**
     * Creates a new {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
     * to use for a tests in this class and/or sub-classes.
     *
     * @return the {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
     */
    public abstract JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> newJavaApplicationBuilder();


    /**
     * Ensure we can start and connect to the Coherence JMX infrastructure.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartJMXConnection() throws Exception
    {
        AvailablePortIterator portIterator = Container.getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setEnvironmentInherited(true).setClusterPort(portIterator).setJMXSupport(true)
                .setSingleServerMode().setJMXPort(portIterator).setJMXManagementMode(JMXManagementMode.LOCAL_ONLY)
                .setRoleName("test-role").setSiteName("test-site");

        ClusterMember member = null;

        try
        {
            JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> builder = newJavaApplicationBuilder();

            member = builder.realize(schema, "TEST", new SystemApplicationConsole());

            assertThat(eventually(invoking(member).getClusterSize()), is(1));

            Assert.assertEquals("test-role", member.getRoleName());
            Assert.assertEquals("test-site", member.getSiteName());
        }
        finally
        {
            if (member != null)
            {
                member.destroy();
            }
        }
    }


    /**
     * Ensure that we can start and stop Coherence Members on the same port
     * continuously (quickly) and there is only ever one member.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartStopMultipleTimes() throws Exception
    {
        AvailablePortIterator portIterator = Container.getAvailablePorts();

        int                   clusterPort  = portIterator.next();
        int                   jmxPort      = portIterator.next();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setEnvironmentInherited(true).setClusterPort(portIterator).setJMXSupport(true)
                .setSingleServerMode().setJMXPort(portIterator).setJMXManagementMode(JMXManagementMode.LOCAL_ONLY)
                .setRoleName("test-role").setSiteName("test-site").setDiagnosticsEnabled(true);

        ClusterMember                                              member  = null;

        JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> builder = newJavaApplicationBuilder();

        for (int i = 1; i <= 10; i++)
        {
            try
            {
                System.out.println("Building Instance: " + i);
                member = builder.realize(schema, "TEST");

                assertThat(eventually(invoking(member).getClusterSize()), is(1));

                Assert.assertEquals("test-role", member.getRoleName());
                Assert.assertEquals("test-site", member.getSiteName());
            }
            finally
            {
                if (member != null)
                {
                    member.destroy();
                }
            }
        }
    }
}
