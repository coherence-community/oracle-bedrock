/*
 * File: GetAutoStartServiceNames.java
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

package com.oracle.bedrock.runtime.coherence.callables;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.tangosol.coherence.config.CacheConfig;
import com.tangosol.coherence.config.scheme.AbstractCompositeScheme;
import com.tangosol.coherence.config.scheme.ServiceScheme;
import com.tangosol.internal.net.ConfigurableCacheFactorySession;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Coherence;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import com.tangosol.run.xml.XmlElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link RemoteCallable} to determine the names of auto-start cluster-based services.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class GetAutoStartServiceNames implements RemoteCallable<Set<String>>
{
    @Override
    public Set<String> call() throws Exception
    {
        List<ConfigurableCacheFactory> ccfList = new ArrayList<>();

        Coherence.getInstances()
                .stream()
                .flatMap(c -> c.getConfiguration().getSessionConfigurations().values().stream())
                .map(SessionConfiguration::getName)
                .map(name -> Coherence.findSession(name).orElse(null))
                .filter(Objects::nonNull)
                .filter(s -> s instanceof ConfigurableCacheFactorySession)
                .map(s -> ((ConfigurableCacheFactorySession) s).getConfigurableCacheFactory())
                .forEach(ccfList::add);

        if (ccfList.isEmpty())
        {
            ccfList.add(CacheFactory.getConfigurableCacheFactory());
        }

        return ccfList.stream()
                      .flatMap(this::getServiceNames)
                      .collect(Collectors.toSet());
    }


    private Stream<String> getServiceNames(ConfigurableCacheFactory configurableCacheFactory)
    {
        if (configurableCacheFactory instanceof DefaultConfigurableCacheFactory)
        {
            DefaultConfigurableCacheFactory cacheFactory = (DefaultConfigurableCacheFactory) configurableCacheFactory;

            // obtain the XmlElements representing the service scheme configurations
            XmlElement              xmlCacheConfig = cacheFactory.getConfig();

            Map<String, XmlElement> serviceSchemes = cacheFactory.collectServiceSchemes(xmlCacheConfig);

            HashSet<String>         serviceNames   = new HashSet<>();

            for (String serviceName : serviceSchemes.keySet())
            {
                XmlElement xmlServiceScheme = serviceSchemes.get(serviceName);

                boolean    isAutoStart      = xmlServiceScheme.getSafeElement("autostart").getBoolean(false);

                if (isAutoStart)
                {
                    serviceNames.add(serviceName);
                }
            }

            return serviceNames.stream();
        }
        else if (configurableCacheFactory instanceof ExtensibleConfigurableCacheFactory)
        {
            ExtensibleConfigurableCacheFactory cacheFactory =
                (ExtensibleConfigurableCacheFactory) configurableCacheFactory;

            CacheConfig cacheConfig = cacheFactory.getCacheConfig();

            if (cacheConfig == null)
            {
                throw new RuntimeException("Failed to determine the CacheConfig for the ExtensibleConfigurableCacheFactory");
            }
            else
            {
                LinkedHashSet<String> serviceNames = new LinkedHashSet<>();

                for (ServiceScheme serviceScheme : cacheConfig.getServiceSchemeRegistry())
                {
                    if (serviceScheme.isAutoStart())
                    {
                        if (serviceScheme instanceof AbstractCompositeScheme)
                        {
                            serviceScheme = ((AbstractCompositeScheme) serviceScheme).getBackScheme();

                            if (isAutoStartable(serviceScheme))
                            {
                                serviceNames.add(serviceScheme.getScopedServiceName());
                            }
                        }
                        else
                        {
                            serviceNames.add(serviceScheme.getScopedServiceName());
                        }
                    }
                }

                return serviceNames.stream();
            }
        }
        else
        {
            throw new RuntimeException("The ConfigurableCacheFactory is neither a DefaultConfigurableCacheFactory or a ExtensibleConfigurableCacheFactory");
        }
    }


    /**
     * Determine if the specified {@link ServiceScheme} is auto-startable
     * (typically if it requires a cluster to operate).
     *
     * @param scheme  the {@link ServiceScheme}
     *
     * @return if the service scheme is auto-startable
     */
    public boolean isAutoStartable(ServiceScheme scheme)
    {
        return scheme.getServiceBuilder().isRunningClusterNeeded();
    }
}
