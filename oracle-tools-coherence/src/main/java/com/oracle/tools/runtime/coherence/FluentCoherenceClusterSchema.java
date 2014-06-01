/*
 * File: FluentCoherenceClusterSchema.java
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
public interface FluentCoherenceClusterSchema<S extends FluentCoherenceClusterSchema<S>>
    extends FluentCoherenceSchema<S>,
            CoherenceClusterSchema
{
    /**
     * Sets the Cluster Name for Oracle Coherence applications based on this {@link FluentCoherenceClusterSchema}.
     *
     * @param name  the cluster name
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setClusterName(String name);


    /**
     * Sets the Cluster Port for Oracle Coherence applications based on this {@link FluentCoherenceClusterSchema}.
     *
     * @param port  the cluster port
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setClusterPort(int port);


    /**
     * Sets the Cluster Port for Oracle Coherence applications based on this {@link FluentCoherenceClusterSchema}, choosing
     * one of those provided by the {@link Iterator} when an application is realized.
     *
     * @param ports  an {@link Iterator} over ports to be used, one for each time an
     *               application is realized using this schema
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setClusterPort(Iterator<Integer> ports);


    /**
     * Sets the multicast time-to-live for Oracle Coherence applications based on this {@link FluentCoherenceClusterSchema}.
     *
     * @param ttl  the time-to-live
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setMulticastTTL(int ttl);


    /**
     * Sets the Site Name for Oracle Coherence applications based on this {@link FluentCoherenceClusterSchema}.
     *
     * @param name  the site name
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setSiteName(String name);


    /**
     * Sets if storage should be enabled for distributed caching services of Oracle Coherence applications based on
     * this {@link FluentCoherenceClusterSchema}.
     *
     * @param isStorageEnabled
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setStorageEnabled(boolean isStorageEnabled);


    /**
     * Sets the Well-Known-Address (WKA) for Oracle Coherence applications based on this {@link FluentCoherenceClusterSchema}.
     *
     * @param address  The address (as a {@link String}).
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setWellKnownAddress(String address);


    /**
     * Sets the Well-Known-Address (WKA) port for Oracle Coherence applications based on this
     * {@link FluentCoherenceClusterSchema}.
     *
     * @param port  the port
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setWellKnownAddressPort(int port);


    /**
     * Sets the Well-Known-Address (WKA) port for Oracle Coherence applications based on this
     * {@link FluentCoherenceClusterSchema}, choosing one of those provided by the {@link Iterator} when
     * an application is realized.
     *
     * @param ports  an {@link Iterator} over ports
     *
     * @return  the {@link FluentCoherenceClusterSchema}
     */
    public S setWellKnownAddressPort(Iterator<Integer> ports);
}
