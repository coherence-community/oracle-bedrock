/*
 * File: IsServiceStorageEnabled.java
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
import com.oracle.bedrock.util.Trilean;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.Service;

/**
 * A {@link RemoteCallable} to remotely determine if a named service is storage enabled,
 * returning a {@link Trilean} value of {@link Trilean#TRUE} if storage is enabled,
 * {@link Trilean#FALSE} if storage is disabled and {@link Trilean#UNKNOWN} if the service
 * is unknown or doesn't support enabling/disabling storage.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class IsServiceStorageEnabled implements RemoteCallable<Trilean>
{
    /**
     * The name of the service.
     */
    private String serviceName;


    /**
     * Constructs an {@link IsServiceStorageEnabled}
     *
     * @param serviceName  the name of the service
     */
    public IsServiceStorageEnabled(String serviceName)
    {
        this.serviceName = serviceName;
    }


    @Override
    public Trilean call() throws Exception
    {
        com.tangosol.net.Cluster cluster = CacheFactory.getCluster();
        Service                  service = cluster == null ? null : cluster.getService(serviceName);

        return service == null
               ||!(service instanceof DistributedCacheService) ? Trilean.UNKNOWN
               : Trilean.of(((DistributedCacheService) service).isLocalStorageEnabled());
    }
}
