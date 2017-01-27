/*
 * File: ExtendClient.java
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

import com.oracle.bedrock.Bedrock;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.Clustering;
import com.oracle.bedrock.runtime.coherence.options.LocalStorage;
import com.oracle.bedrock.runtime.coherence.options.RoleName;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.table.Cell;
import com.oracle.bedrock.table.Table;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.ScopedCacheFactoryBuilder;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link SessionBuilder} for Coherence *Extend Clients.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ExtendClient implements SessionBuilder
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(ExtendClient.class.getName());

    /**
     * The Coherence Cache Configuration URI to use for the {@link ExtendClient}.
     */
    private String cacheConfigURI;


    /**
     * Creates an {@link ExtendClient} for the specified Cache Configuration URI.
     *
     * @param cacheConfigURI the cache configuration URI
     */
    public ExtendClient(String cacheConfigURI)
    {
        this.cacheConfigURI = cacheConfigURI;
    }


    @Override
    public ConfigurableCacheFactory build(LocalPlatform    platform,
                                          CoherenceCluster cluster,
                                          OptionsByType    optionsByType)
    {
        // ----- establish the diagnostics output table -----

        Table diagnosticsTable = new Table();

        diagnosticsTable.getOptions().add(Table.orderByColumn(0));

        // ----- establish the options for launching a local extend-based member -----

        optionsByType.add(RoleName.of("extend-client"));
        optionsByType.add(Clustering.disabled());
        optionsByType.add(LocalStorage.disabled());
        optionsByType.add(SystemProperty.of("coherence.profile", "thin"));
        optionsByType.add(SystemProperty.of("coherence.client", "remote"));
        optionsByType.add(CacheConfig.of(cacheConfigURI));

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

        Properties properties            = systemProperties.resolve(platform, optionsByType);

        Table      systemPropertiesTable = new Table();

        systemPropertiesTable.getOptions().add(Table.orderByColumn(0));
        systemPropertiesTable.getOptions().add(Cell.Separator.of(""));
        systemPropertiesTable.getOptions().add(Cell.DisplayNull.asEmptyString());

        for (String propertyName : properties.stringPropertyNames())
        {
            String propertyValue = properties.getProperty(propertyName);

            systemPropertiesTable.addRow(propertyName + (System.getProperties().containsKey(propertyName) ? "*" : ""),
                                         propertyValue);

            System.setProperty(propertyName, propertyValue.isEmpty() ? "" : propertyValue);
        }

        diagnosticsTable.addRow("System Properties", systemPropertiesTable.toString());

        // ----- output the diagnostics -----

        if (LOGGER.isLoggable(Level.INFO))
        {
            LOGGER.log(Level.INFO,
                       "Oracle Bedrock " + Bedrock.getVersion() + ": Starting *Extend Client...\n"
                       + "------------------------------------------------------------------------\n"
                       + diagnosticsTable.toString() + "\n"
                       + "------------------------------------------------------------------------\n");
        }

        // ----- establish the session -----                   

        // create the session
        ConfigurableCacheFactory session = new ScopedCacheFactoryBuilder().getConfigurableCacheFactory(cacheConfigURI,
                                                                                                       getClass().getClassLoader());

        return session;
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof ExtendClient))
        {
            return false;
        }

        ExtendClient that = (ExtendClient) other;

        return cacheConfigURI.equals(that.cacheConfigURI);

    }


    @Override
    public int hashCode()
    {
        return cacheConfigURI.hashCode();
    }
}
