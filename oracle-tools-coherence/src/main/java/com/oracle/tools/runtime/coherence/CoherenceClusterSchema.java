/*
 * File: CoherenceClusterSchema.java
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

import java.util.Iterator;

/**
 * A schema encapsulating configuration and operational settings commonly
 * shared by applications that are part of an Oracle Coherence cluster.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface CoherenceClusterSchema extends CoherenceSchema
{
    /**
     * The tangosol.coherence.cluster property.
     */
    public static final String PROPERTY_CLUSTER_NAME = "tangosol.coherence.cluster";

    /**
     * The tangosol.coherence.clusterport property.
     */
    public static final String PROPERTY_CLUSTER_PORT = "tangosol.coherence.clusterport";

    /**
     * The tangosol.coherence.distributed.localstorage property.
     */
    public static final String PROPERTY_DISTRIBUTED_LOCALSTORAGE = "tangosol.coherence.distributed.localstorage";

    /**
     * The tangosol.coherence.site property.
     */
    public static final String PROPERTY_SITE_NAME = "tangosol.coherence.site";

    /**
     * The tangosol.coherence.ttl property.
     */
    public static final String PROPERTY_MULTICAST_TTL = "tangosol.coherence.ttl";

    /**
     * The tangosol.coherence.wka property.
     */
    public static final String PROPERTY_WELL_KNOWN_ADDRESS = "tangosol.coherence.wka";

    /**
     * The tangosol.coherence.wka.port property.
     */
    public static final String PROPERTY_WELL_KNOWN_ADDRESS_PORT = "tangosol.coherence.wka.port";


    /**
     * Obtains the Cluster Name for Oracle Coherence applications based on this {@link CoherenceClusterSchema}.
     *
     * @return  the cluster name (or null if not set)
     */
    public String getClusterName();


    /**
     * Obtains the multicast time-to-live for Oracle Coherence applications based on this {@link CoherenceClusterSchema}.
     *
     * @return  the multicast TTL (or -1 if not set)
     */
    public int getMulticastTTL();


    /**
     * Obtains the Site Name for Oracle Coherence applications based on this {@link CoherenceClusterSchema}.
     *
     * @return  the site name (or null if not set)
     */
    public String getSiteName();


    /**
     * Obtains the storage should be enabled for distributed caching services of Oracle Coherence applications based on
     * this {@link CoherenceClusterSchema}.
     *
     * @return  if storage is to be enabled
     */
    public boolean isStorageEnabled();


    /**
     * Obtains the Well-Known-Address (WKA) for Oracle Coherence applications based on this {@link CoherenceClusterSchema}.
     *
     * @return  the well-known-address (or null if not set)
     */
    public String getWellKnownAddress();
}
