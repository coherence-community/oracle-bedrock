/*
 * File: ContainerMBeanServerBuilderTest.java
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

package com.oracle.tools.runtime.java.container;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.runtime.LocalPlatform;

import com.oracle.tools.runtime.java.features.JmxFeature;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import java.util.Collections;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;

/**
 * Functional Tests for the {@link ContainerMBeanServerBuilder}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class ContainerMBeanServerBuilderTest extends AbstractTest
{
    /**
     * Ensure that we can start and connect locally and remotely to a
     * contained {@link MBeanServer} for a specific domain.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldStartMBeanServerAndConnectLocallyAndRemotely() throws Exception
    {
        MBeanServer        mBeanServer        = null;
        JMXConnectorServer jmxConnectorServer = null;
        JMXConnector       jmxConnector       = null;

        try
        {
            // IMPORTANT: We have to set the rmi server hostname to the platform
            // here as it defaults to using the "hostname", which when on VPN
            // is often incorrect (ie: the real IP address instead of the routable
            // VPN address).
            System.setProperty("java.rmi.server.hostname", LocalPlatform.get().getAddress().getHostAddress());

            // we're going to be performing jmx container
            System.setProperty("javax.management.builder.initial",
                               DelegatingMBeanServerBuilder.class.getCanonicalName());
            System.setProperty(JmxFeature.SUN_MANAGEMENT_JMXREMOTE, "true");

            String domain = "Test";

            mBeanServer = MBeanServerFactory.createMBeanServer(domain);
            assertThat(mBeanServer.getDefaultDomain(), is(domain));

            Dummy      dummyMBean = new Dummy();
            ObjectName objectName = new ObjectName(domain + ":type=Dummy");

            mBeanServer.registerMBean(dummyMBean, objectName);

            MBeanInfo info = mBeanServer.getMBeanInfo(objectName);

            assertThat(info.getClassName(), is(Dummy.class.getCanonicalName()));

            dummyMBean.setAttributeOne(2);
            assertThat((Integer) mBeanServer.getAttribute(objectName, "AttributeOne"), is(2));

            ContainerScope scope = Container.getContainerScope();

            ContainerMBeanServerBuilder builder = scope == null
                                                  ? Container.getDefaultScope().getMBeanServerBuilder()
                                                  : scope.getMBeanServerBuilder();

            jmxConnectorServer = builder.getJMXConnectorServer(mBeanServer);

            jmxConnector       = jmxConnectorServer.toJMXConnector(Collections.EMPTY_MAP);
            jmxConnector.connect();

            MBeanServerConnection remoteMBeanServer = jmxConnector.getMBeanServerConnection();

            info = remoteMBeanServer.getMBeanInfo(objectName);
            assertThat(info.getClassName(), is(Dummy.class.getCanonicalName()));

            dummyMBean.setAttributeOne(19);
            assertThat((Integer) remoteMBeanServer.getAttribute(objectName, "AttributeOne"), is(19));
        }
        finally
        {
            if (jmxConnector != null)
            {
                jmxConnector.close();
            }

            if (jmxConnectorServer != null)
            {
                try
                {
                    jmxConnectorServer.stop();
                }
                catch (Throwable t)
                {
                    // ignored
                }
            }

            if (mBeanServer != null)
            {
                try
                {
                    MBeanServerFactory.releaseMBeanServer(mBeanServer);
                }
                catch (Throwable t)
                {
                    // ignored
                }
            }
        }

    }
}
