/*
 * File: ClusterMemberSchemaTest.java
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

import com.oracle.tools.runtime.network.AvailablePortIterator;
import com.oracle.tools.runtime.network.Constants;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ClusterMemberSchema}s.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ClusterMemberSchemaTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultClassName() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.getApplicationClassName(), is(ClusterMemberSchema.DEFAULT_CACHE_SERVER_CLASSNAME));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedClassName() throws Exception
    {
        String              className = "com.oracle.Test";
        ClusterMemberSchema schema    = new ClusterMemberSchema(className);

        assertThat(schema.getApplicationClassName(), is(className));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedClassNameAndClasspath() throws Exception
    {
        String              className = "com.oracle.Test";
        String              classPath = "one.jar";
        ClusterMemberSchema schema    = new ClusterMemberSchema(className, classPath);

        assertThat(schema.getApplicationClassName(), is(className));
        assertThat(schema.getClassPath(), is(classPath));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldHaveCorrectStartMethodWhenRunInProcess() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.getStartMethodName(), is(ClusterMemberSchema.DEFAULT_START_METHOD));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldHaveCorrectStopMethodWhenRunInProcess() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.getStopMethodName(), is(ClusterMemberSchema.DEFAULT_STOP_METHOD));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetCacheConfigURI() throws Exception
    {
        String              value  = "TestValue";
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setCacheConfigURI(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_CACHECONFIG),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetClusterName() throws Exception
    {
        String              value  = "TestValue";
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setClusterName(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_CLUSTER_NAME),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetClusterPort() throws Exception
    {
        int                 value  = 1234;
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setClusterPort(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_CLUSTER_PORT),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetClusterPortAvailablePortIterator() throws Exception
    {
        AvailablePortIterator value  = new AvailablePortIterator();
        ClusterMemberSchema   schema = new ClusterMemberSchema();

        assertThat(schema.setClusterPort(value), is(sameInstance(schema)));
        assertThat((AvailablePortIterator) schema.getSystemPropertiesBuilder()
            .getProperty(ClusterMemberSchema.PROPERTY_CLUSTER_PORT),
                   sameInstance(value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetLocalHostAddress() throws Exception
    {
        String              value  = "test";
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setLocalHostAddress(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_LOCALHOST_ADDRESS),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetLogLevel() throws Exception
    {
        int                 value  = 12345;
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setLogLevel(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_LOG_LEVEL),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetMulticastTTL() throws Exception
    {
        int                 value  = 12345;
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setMulticastTTL(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_MULTICAST_TTL),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetPofConfigURI() throws Exception
    {
        String              value  = "test";
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setPofConfigURI(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_POF_CONFIG),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetPofEnabledToTrue() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setPofEnabled(true), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_POF_ENABLED),
                   is((Object) true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetPofEnabledToFalse() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setPofEnabled(false), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_POF_ENABLED),
                   is((Object) false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetRemoteJmxManagementToTrue() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setRemoteJMXManagement(true), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_MANAGEMENT_REMOTE),
                   is((Object) true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetRemoteJmxManagementToFalse() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setRemoteJMXManagement(false), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_MANAGEMENT_REMOTE),
                   is((Object) false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetHostToLocalhostInSingleServerMode() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setSingleServerMode(), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_LOCALHOST_ADDRESS),
                   is((Object) Constants.getLocalHost()));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetMulticastTtlToZeroInSingleServerMode() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setSingleServerMode(), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_MULTICAST_TTL),
                   is((Object) 0));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetSiteName() throws Exception
    {
        String              value  = "test";
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setSiteName(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_SITE_NAME),
                   is((Object) value));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetStorageEnabledToTrue() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setStorageEnabled(true), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder()
            .getProperty(ClusterMemberSchema.PROPERTY_DISTRIBUTED_LOCALSTORAGE),
                   is((Object) true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetStorageEnabledToFalse() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setStorageEnabled(false), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder()
            .getProperty(ClusterMemberSchema.PROPERTY_DISTRIBUTED_LOCALSTORAGE),
                   is((Object) false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetTcmpEnabledToTrue() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setTCMPEnabled(true), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_TCMP_ENABLED),
                   is((Object) true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetTcmpEnabledToFalse() throws Exception
    {
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setTCMPEnabled(false), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_TCMP_ENABLED),
                   is((Object) false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetWellKnownAddress() throws Exception
    {
        String              value  = "test";
        ClusterMemberSchema schema = new ClusterMemberSchema();

        assertThat(schema.setWellKnownAddress(value), is(sameInstance(schema)));
        assertThat(schema.getSystemPropertiesBuilder().getProperty(ClusterMemberSchema.PROPERTY_WELL_KNOWN_ADDRESS),
                   is((Object) value));
    }
}
