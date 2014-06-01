/*
 * File: CoherenceSchema.java
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

/**
 * A schema encapsulating configuration and operational settings commonly
 * required by applications using Oracle Coherence.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface CoherenceSchema
{
    /**
     * The tangosol.coherence.cacheconfig property.
     */
    public static final String PROPERTY_CACHECONFIG = "tangosol.coherence.cacheconfig";

    /**
     * The tangosol.coherence.log property.
     */
    public static final String PROPERTY_LOG = "tangosol.coherence.log";

    /**
     * The tangosol.coherence.log.level property.
     */
    public static final String PROPERTY_LOG_LEVEL = "tangosol.coherence.log.level";

    /**
     * The tangosol.coherence.override property.
     */
    public static final String PROPERTY_OPERATIONAL_OVERRIDE = "tangosol.coherence.override";

    /**
     * The tangosol.pof.config property.
     */
    public static final String PROPERTY_POF_CONFIG = "tangosol.pof.config";

    /**
     * The tangosol.pof.enabled property.
     */
    public static final String PROPERTY_POF_ENABLED = "tangosol.pof.enabled";


    /**
     * Obtains the Cache Configuration URI for Oracle Coherence applications based on this {@link CoherenceSchema}.
     *
     * @return  the Cache Configuration URI (or null if not set)
     */
    public String getCacheConfigURI();


    /**
     * Obtains the log for Oracle Coherence applications based on this {@link CoherenceSchema}.
     *
     * @return  the log (or null if not set)
     */
    public String getLog();


    /**
     * Obtains the log level for Oracle Coherence applications based on this {@link CoherenceSchema}.
     *
     * @return  the log level (or -1 if not set)
     */
    public int getLogLevel();


    /**
     * Obtains the Operational Override URI for Oracle Coherence applications based on this {@link CoherenceSchema}.
     *
     * @return  the Operational Override URI (or null if not set)
     */
    public String getOperationalOverrideURI(String operationalOverrideURI);


    /**
     * Obtains POF Configuration URI for Oracle Coherence applications based on this {@link CoherenceSchema}.
     *
     * @return  the POF Configuration URI (or null if not set)
     */
    public String getPofConfigURI(String pofConfigURI);


    /**
     * Obtains if Portable-Object-Format serialization is enabled/disabled for Oracle Coherence applications
     * based on this {@link CoherenceSchema}.
     *
     * @return  <code>true</code> for POF enabled, <code>false</code> otherwise.
     */
    public boolean isPofEnabled();
}
