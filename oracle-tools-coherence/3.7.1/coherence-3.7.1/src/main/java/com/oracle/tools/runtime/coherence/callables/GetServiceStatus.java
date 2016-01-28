/*
 * File: GetServiceStatus.java
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

package com.oracle.tools.runtime.coherence.callables;

import com.oracle.tools.runtime.coherence.ServiceStatus;

import com.oracle.tools.runtime.concurrent.RemoteCallable;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.PartitionedService;
import com.tangosol.coherence.component.util.safeService.safeCacheService.SafeDistributedCacheService;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Service;

/**
 * A {@link RemoteCallable} to remotely determine the status of a service.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class GetServiceStatus implements RemoteCallable<ServiceStatus>
{
    /**
     * The name of the service.
     */
    private String serviceName;


    /**
     * Constructs an {@link GetServiceStatus}
     *
     * @param serviceName  the name of the service
     */
    public GetServiceStatus(String serviceName)
    {
        this.serviceName = serviceName;
    }


    @Override
    public ServiceStatus call() throws Exception
    {
        com.tangosol.net.Cluster cluster = CacheFactory.getCluster();
        Service                  service = cluster == null ? null : cluster.getService(serviceName);

        if (service == null)
        {
            return null;
        }
        else if (service.isRunning())
        {
            // SafeDistributedCacheServices provide a lot more fidelity!
            if (service instanceof SafeDistributedCacheService)
            {
                SafeDistributedCacheService distributedCacheService = (SafeDistributedCacheService) service;
                PartitionedService partitionedService = (PartitionedService) distributedCacheService.getService();
                int                         backupStrength          = (Integer) partitionedService.getBackupStrength();

                switch (backupStrength)
                {
                case 0 :
                    return ServiceStatus.ORPHANED;

                case 1 :
                    return ServiceStatus.ENDANGERED;

                case 2 :
                    return ServiceStatus.NODE_SAFE;

                case 3 :
                    return ServiceStatus.MACHINE_SAFE;

                case 4 :
                    return ServiceStatus.RACK_SAFE;

                case 5 :
                    return ServiceStatus.SITE_SAFE;

                default :
                    return ServiceStatus.UNKNOWN;
                }
            }
            else
            {
                return ServiceStatus.RUNNING;
            }

        }
        else
        {
            return ServiceStatus.STOPPED;
        }
    }
}
