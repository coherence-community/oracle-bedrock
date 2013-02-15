/*
 * File: ClusterMemberBuilderTest.java
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
import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.coherence.ClusterMemberSchema.JMXManagementMode;

import com.oracle.tools.runtime.java.ExternalJavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.virtualization.Virtualization;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import junit.framework.Assert;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredAssert.assertThat;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.is;

/**
 * Unit tests for building {@link ClusterMember}s.
 * <p>
 * Copyright (c) 2010. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClusterMemberBuilderTest extends AbstractTest
{
    /**
     * A test to determine if we can connect to the Coherence JMX infrastructure.
     *
     * @throws Exception
     */
    @Test
    public void testJMXConnection() throws Exception
    {
        AvailablePortIterator portIterator = Virtualization.getAvailablePorts();

        ClusterMemberSchema schema =
            new ClusterMemberSchema().setEnvironmentVariables(PropertiesBuilder.fromCurrentEnvironmentVariables())
                .setSingleServerMode().setClusterPort(portIterator).setJMXPort(portIterator)
                .setJMXManagementMode(JMXManagementMode.LOCAL_ONLY).setRoleName("test-role").setSiteName("test-site");

        ClusterMember member = null;

        try
        {
            JavaApplicationBuilder<ClusterMember, ClusterMemberSchema> builder =
                new ExternalJavaApplicationBuilder<ClusterMember, ClusterMemberSchema>();

            member = builder.realize(schema, "TEST");

            assertThat(eventually(invoking(member).getClusterSize()), is(1));

            if (member instanceof Application)
            {
                long pid = ((Application<?>) member).getPid();

                Assert.assertTrue(pid > 0);
            }

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
