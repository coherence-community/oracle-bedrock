/*
 * File: CoherenceClusterMemberSchema.java
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

import com.oracle.tools.runtime.java.JavaApplicationSchema;

/**
 * Defines a schema encapsulating configuration and operational settings
 * required by individual applications that are part of an Oracle Coherence cluster,
 * those of which are represented by {@link CoherenceClusterMember}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <A>  the type of {@link CoherenceClusterMember} that can be configured by the
 *             {@link CoherenceClusterMemberSchema}
 *
 * @author Brian Oliver
 */
public interface CoherenceClusterMemberSchema<A extends CoherenceClusterMember> extends JavaApplicationSchema<A>,
                                                                                        CoherenceClusterSchema
{
    /**
     * The com.tangosol.net.DefaultCacheServer classname.
     */
    public static final String DEFAULT_CACHE_SERVER_CLASSNAME = "com.tangosol.net.DefaultCacheServer";

    /**
     * The tangosol.coherence.localhost property.
     */
    public static final String PROPERTY_LOCALHOST_ADDRESS = "tangosol.coherence.localhost";

    /**
     * The tangosol.coherence.localport property.
     */
    public static final String PROPERTY_LOCALHOST_PORT = "tangosol.coherence.localport";

    /**
     * The tangosol.coherence.management property.
     */
    public static final String PROPERTY_MANAGEMENT_MODE = "tangosol.coherence.management";

    /**
     * The tangosol.coherence.management.remote property.
     */
    public static final String PROPERTY_MANAGEMENT_REMOTE = "tangosol.coherence.management.remote";

    /**
     * The tangosol.coherence.role property.
     */
    public static final String PROPERTY_ROLE_NAME = "tangosol.coherence.role";

    /**
     * The tangosol.coherence.machine property.
     */
    public static final String PROPERTY_MACHINE_NAME = "tangosol.coherence.machine";

    /**
     * The tangosol.coherence.tcmp.enabled property.
     */
    public static final String PROPERTY_TCMP_ENABLED = "tangosol.coherence.tcmp.enabled";


    /**
     * Obtains the localhost address for Oracle Coherence applications based on this {@link CoherenceClusterMemberSchema}.
     *
     * @return  the localhost address (or null if not set)
     */
    public String getLocalHostAddress();


    /**
     * Obtains the {@link JMXManagementMode} for Oracle Coherence applications based on this
     * {@link CoherenceClusterMemberSchema}.
     *
     * @return  the {@link JMXManagementMode} (or null if not set)
     */
    public JMXManagementMode getJMXManagementMode();


    /**
     * Obtains the role name for Oracle Coherence applications based on this {@link CoherenceClusterMemberSchema}.
     *
     * @return  the role name (or null if not set)
     */
    public String getRoleName();


    /**
     * Obtains the machine name for Oracle Coherence applications based on this {@link CoherenceClusterMemberSchema}.
     *
     * @return  the machine name (or null if not set)
     */
    public String getMachineName();

    /**
     * Obtains if remote JMX monitoring is enabled for Oracle Coherence applications based on this
     * {@link CoherenceClusterMemberSchema}.
     *
     * @return  if remote jmx management is enabled
     */
    public boolean isRemoteJMXManagement();


    /**
     * Obtains if TCMP (Tangosol Cluster Management Protocol) should be enabled for Oracle Coherence applications
     * based on this {@link CoherenceClusterMemberSchema}.
     *
     * @return  if TCMP is enabled
     */
    public boolean isTCMPEnabled();
}
