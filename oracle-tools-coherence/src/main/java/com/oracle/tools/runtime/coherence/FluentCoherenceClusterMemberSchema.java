/*
 * File: FluentCoherenceClusterMemberSchema.java
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
 * A {@link CoherenceClusterSchema} extension defining {@link CoherenceClusterSchema} specific fluent-methods.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <S>  the type of {@link FluentCoherenceSchema} that will be returned from fluent methods
 */
public interface FluentCoherenceClusterMemberSchema<A extends CoherenceClusterMember,
                                                    S extends FluentCoherenceClusterMemberSchema<A, S>>
    extends FluentCoherenceClusterSchema<S>,
            CoherenceClusterMemberSchema<A>
{
    /**
     * Sets the localhost address for Oracle Coherence applications based on this {@link FluentCoherenceClusterMemberSchema}.
     *
     * @param address  the localhost address
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setLocalHostAddress(String address);


    /**
     * Sets the localhost port for Oracle Coherence applications based on this {@link FluentCoherenceClusterMemberSchema}.
     *
     * @param port  the localhost port
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setLocalHostPort(int port);


    /**
     * Sets the localhost port for Oracle Coherence applications based on this {@link FluentCoherenceClusterMemberSchema},
     * choosing one of those provided by the {@link Iterator} when an application is realized.
     *
     * @param ports  an {@link Iterator} over ports
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setLocalHostPort(Iterator<Integer> ports);


    /**
     * Sets the {@link JMXManagementMode} for Oracle Coherence applications based on this
     * {@link FluentCoherenceClusterMemberSchema}.
     *
     * @param mode  the {@link JMXManagementMode}
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setJMXManagementMode(JMXManagementMode mode);


    /**
     * Sets the role name for Oracle Coherence applications based on this {@link FluentCoherenceClusterMemberSchema}.
     *
     * @param name  the role name
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setRoleName(String name);


    /**
     * Sets if remote JMX monitoring is enabled for Oracle Coherence applications based on this
     * {@link FluentCoherenceClusterMemberSchema}.
     *
     * @param isEnabled  <code>true</code> if remote jmx management should be enabled
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setRemoteJMXManagement(boolean isEnabled);


    /**
     * Sets if TCMP (Tangosol Cluster Management Protocol) should be enabled for Oracle Coherence applications
     * based on this {@link FluentCoherenceClusterMemberSchema}.  When disabled applications will not be able to join
     * a Coherence Cluster.
     *
     * @param isTCMPEnabled  is TCMP enabled
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S setTCMPEnabled(boolean isTCMPEnabled);


    /**
     * Configures the {@link FluentCoherenceClusterMemberSchema} so that when a Oracle Coherence application
     * is realized it will be running in local-host only mode, meaning that other cluster members
     * must be on the same host.
     * <p>
     * This is typically used for running {@link ClusterMember}s locally.
     *
     * @return  the {@link FluentCoherenceClusterMemberSchema}
     */
    public S useLocalHostMode();
}
