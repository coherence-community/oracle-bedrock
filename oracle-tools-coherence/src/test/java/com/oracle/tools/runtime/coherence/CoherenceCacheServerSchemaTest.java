/*
 * File: CoherenceCacheServerSchemaTest.java
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
 * Unit tests for {@link CoherenceClusterMemberSchema}s.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CoherenceCacheServerSchemaTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultClassName() throws Exception
    {
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.getApplicationClassName(), is(CoherenceCacheServerSchema.DEFAULT_CACHE_SERVER_CLASSNAME));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedClassName() throws Exception
    {
        String                     className = "com.oracle.Test";
        CoherenceCacheServerSchema schema    = new CoherenceCacheServerSchema(className);

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
        String                     className = "com.oracle.Test";
        String                     classPath = "one.jar";
        CoherenceCacheServerSchema schema    = new CoherenceCacheServerSchema(className, classPath);

        assertThat(schema.getApplicationClassName(), is(className));
        assertThat(schema.getClassPath().toString(), is(classPath));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetCacheConfigURI() throws Exception
    {
        String                     value  = "TestValue";
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setCacheConfigURI(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_CACHECONFIG),
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
        String                     value  = "TestValue";
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setClusterName(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_CLUSTER_NAME),
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
        int                        value  = 1234;
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setClusterPort(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_CLUSTER_PORT),
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
        AvailablePortIterator      value  = new AvailablePortIterator();
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setClusterPort(value), is(sameInstance(schema)));
        assertThat((AvailablePortIterator) schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_CLUSTER_PORT),
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
        String                     value  = "test";
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setLocalHostAddress(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_LOCALHOST_ADDRESS),
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
        int                        value  = 12345;
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setLogLevel(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_LOG_LEVEL),
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
        int                        value  = 12345;
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setMulticastTTL(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_MULTICAST_TTL),
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
        String                     value  = "test";
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setPofConfigURI(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_POF_CONFIG),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setPofEnabled(true), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_POF_ENABLED),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setPofEnabled(false), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_POF_ENABLED),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setRemoteJMXManagement(true), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_MANAGEMENT_REMOTE),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setRemoteJMXManagement(false), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_MANAGEMENT_REMOTE),
                   is((Object) false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetHostToLocalhostInUseLocalHostMode() throws Exception
    {
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.useLocalHostMode(), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_LOCALHOST_ADDRESS),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.useLocalHostMode(), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_MULTICAST_TTL),
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
        String                     value  = "test";
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setSiteName(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_SITE_NAME),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setStorageEnabled(true), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_DISTRIBUTED_LOCALSTORAGE),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setStorageEnabled(false), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_DISTRIBUTED_LOCALSTORAGE),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setTCMPEnabled(true), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_TCMP_ENABLED),
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
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setTCMPEnabled(false), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties().getProperty(CoherenceCacheServerSchema.PROPERTY_TCMP_ENABLED),
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
        String                     value  = "test";
        CoherenceCacheServerSchema schema = new CoherenceCacheServerSchema();

        assertThat(schema.setWellKnownAddress(value), is(sameInstance(schema)));
        assertThat(schema.getSystemProperties()
            .getProperty(CoherenceCacheServerSchema.PROPERTY_WELL_KNOWN_ADDRESS),
                   is((Object) value));
    }
}
