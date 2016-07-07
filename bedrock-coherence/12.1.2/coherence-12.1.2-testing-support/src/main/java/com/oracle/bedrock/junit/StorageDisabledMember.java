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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.coherence.options.LocalStorage;
import com.oracle.bedrock.runtime.coherence.options.RoleName;
import com.tangosol.net.CacheFactory;
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
    public ConfigurableCacheFactory build(LocalPlatform                 platform,
                                          CoherenceClusterOrchestration orchestration,
                                          OptionsByType                 optionsByType)
    {
        // ----- establish the options for launching a local storage-disabled member -----
        optionsByType.add(RoleName.of("client"));
        optionsByType.add(LocalStorage.disabled());
        optionsByType.add(LocalHost.only());
        optionsByType.addIfAbsent(CacheConfig.of("coherence-cache-config.xml"));

        // ----- notify the Profiles that we're about to launch an application -----

        MetaClass<CoherenceClusterMember> metaClass = new CoherenceClusterMember.MetaClass();

        for (Profile profile : optionsByType.getInstancesOf(Profile.class))
        {
            profile.onLaunching(platform, metaClass, optionsByType);
        }

        // ----- create local system properties based on those defined by the launch options -----

        // modify the current system properties to include/override those in the schema
        com.oracle.bedrock.runtime.java.options.SystemProperties systemProperties =
            optionsByType.get(com.oracle.bedrock.runtime.java.options.SystemProperties.class);

        Properties properties = systemProperties.resolve(platform, optionsByType);

        for (String propertyName : properties.stringPropertyNames())
        {
            System.setProperty(propertyName, properties.getProperty(propertyName));
        }

        // create the session
        ConfigurableCacheFactory session =
            new ScopedCacheFactoryBuilder().getConfigurableCacheFactory(optionsByType.get(CacheConfig.class).getUri(),
                                                                        getClass().getClassLoader());

        // as this is a cluster member we have to join the cluster
        CacheFactory.ensureCluster();

        return session;
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
