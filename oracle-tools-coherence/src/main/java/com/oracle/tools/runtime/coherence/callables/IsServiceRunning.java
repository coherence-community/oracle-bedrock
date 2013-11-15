/*
 * File: IsServiceRunning.java
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

import com.oracle.tools.runtime.java.container.Container;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Service;

import java.io.Serializable;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} to remotely determine if a named service is running
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class IsServiceRunning implements Callable<Boolean>, Serializable
{
    /**
     * The name of the service.
     */
    private String serviceName;


    /**
     * Constructs an {@link IsServiceRunning}
     *
     * @param serviceName  the name of the service
     */
    public IsServiceRunning(String serviceName)
    {
        this.serviceName = serviceName;
    }


    @Override
    public Boolean call() throws Exception
    {
        com.tangosol.net.Cluster cluster = CacheFactory.getCluster();
        Service                  service = cluster == null ? null : cluster.getService(serviceName);

        return service == null ? false : service.isRunning();
    }
}
