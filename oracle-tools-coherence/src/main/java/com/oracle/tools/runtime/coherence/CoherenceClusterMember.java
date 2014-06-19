/*
 * File: CoherenceClusterMember.java
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

import com.oracle.tools.runtime.java.JavaApplication;

import com.tangosol.net.NamedCache;

import com.tangosol.util.UID;

import java.util.Set;

/**
 * A specialized {@link JavaApplication} that represents an Oracle Coherence Cluster Member
 * at runtime.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface CoherenceClusterMember extends JavaApplication
{
    /**
     * Obtains the number of members in the cluster in which the member represented
     * by this {@link CoherenceClusterMember} belongs.
     *
     * @return the number of members in the cluster
     */
    public int getClusterSize();


    /**
     * Obtains the local member id for the {@link CoherenceClusterMember}.
     *
     * @return the local member id
     */
    public int getLocalMemberId();


    /**
     * Obtains the local member {@link UID} for the {@link CoherenceClusterMember}.
     *
     * @return the local member {@link UID}
     */
    public UID getLocalMemberUID();


    /**
     * Obtains the member {@link UID}s for the {@link Cluster} in which the {@link CoherenceClusterMember} is operating.
     *
     * @return  a {@link Set} of {@link UID}, one for each {@link CoherenceClusterMember}
     */
    public Set<UID> getClusterMemberUIDs();


    /**
     * Obtains the role name for the local member.
     *
     * @return the role name
     */
    public String getRoleName();


    /**
     * Obtains the site name for the local member.
     *
     * @return the site name
     */
    public String getSiteName();


    /**
     * Obtains the cluster name for the local member.
     *
     * @return the site name
     */
    public String getClusterName();


    /**
     * Obtains a proxy of the specified {@link NamedCache} available in the
     * {@link CoherenceClusterMember}.
     *
     * @param cacheName  the name of the {@link NamedCache}
     *
     * @return  a proxy to the {@link NamedCache}
     */
    public NamedCache getCache(String cacheName);


    /**
     * Determines if a specified service is being run by the {@link CoherenceClusterMember}.
     *
     * @param serviceName  the name of the service
     *
     * @return <code>true</code> if the service is running, <code>false</code> otherwise
     */
    public boolean isServiceRunning(String serviceName);


    /**
     * Determines the status of a service being run by the {@link CoherenceClusterMember}.
     *
     * @param serviceName  the name of the service
     *
     * @return the {@link ServiceStatus}
     */
    public ServiceStatus getServiceStatus(String serviceName);
}
