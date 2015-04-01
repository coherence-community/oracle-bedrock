/*
 * File: StorageDisabledMember.java
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

package com.oracle.tools.junit;

import com.oracle.tools.runtime.LocalPlatform;

import com.oracle.tools.runtime.coherence.CoherenceCacheServerSchema;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.ScopedCacheFactoryBuilder;

import java.util.Properties;

/**
 * A {@link SessionBuilder} for Coherence Storage Disabled Members.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class StorageDisabledMember implements SessionBuilder
{
    @Override
    public ConfigurableCacheFactory realize(LocalPlatform                 platform,
                                            CoherenceClusterOrchestration orchestration,
                                            CoherenceCacheServerSchema    serverSchema)
    {
        // build a schema for a local storage-disabled member
        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema(serverSchema).setRoleName("client").setStorageEnabled(false);

        // obtain the cache configuration to use for the storage-disabled member
        String cacheConfigURI = serverSchema.getCacheConfigURI();

        if (cacheConfigURI == null || cacheConfigURI.trim().isEmpty())
        {
            cacheConfigURI = "coherence-cache-config.xml";
        }

        // set the current system properties with those of the schema
        Properties properties = schema.getSystemProperties(platform);

        for (String propertyName : properties.stringPropertyNames())
        {
            System.setProperty(propertyName, properties.getProperty(propertyName));
        }

        return new ScopedCacheFactoryBuilder().getConfigurableCacheFactory(cacheConfigURI, getClass().getClassLoader());
    }


    @Override
    public boolean equals(Object other)
    {
        return other instanceof StorageDisabledMember;
    }


    @Override
    public int hashCode()
    {
        return StorageDisabledMember.class.hashCode();
    }
}
