/*
 * File: FluentCoherenceSchema.java
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
 * A {@link CoherenceSchema} extension defining {@link CoherenceSchema} specific fluent-methods.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <S>  the type of {@link FluentCoherenceSchema} that will be returned from fluent methods
 */
public interface FluentCoherenceSchema<S extends FluentCoherenceSchema<S>> extends CoherenceSchema
{
    /**
     * Sets the Cache Configuration URI for Oracle Coherence applications based on this {@link FluentCoherenceSchema}.
     *
     * @param cacheConfigURI  the cache configuration URI
     *
     * @return  the {@link FluentCoherenceSchema}
     */
    public S setCacheConfigURI(String cacheConfigURI);


    /**
     * Sets the log for Oracle Coherence applications based on this {@link FluentCoherenceSchema}.
     * <p>
     * Log destinations typically include: "jdk", "log4j", "stdout",
     * "stderr" or a file name.
     *
     * @param destination  the destination for logs
     *
     * @return  the {@link FluentCoherenceSchema}
     */
    public S setLog(String destination);


    /**
     * Sets the log level for Oracle Coherence applications based on this {@link FluentCoherenceSchema}.
     *
     * @param level  the log level (typically between 0 and 9)
     *
     * @return  the {@link FluentCoherenceSchema}
     */
    public S setLogLevel(int level);


    /**
     * Sets the Operational Override URI for Oracle Coherence applications based on this {@link FluentCoherenceSchema}.
     *
     * @param operationalOverrideURI  the operational override URI
     *
     * @return  the {@link FluentCoherenceSchema}
     */
    public S setOperationalOverrideURI(String operationalOverrideURI);


    /**
     * Sets POF Configuration URI for Oracle Coherence applications based on this {@link FluentCoherenceSchema}.
     *
     * @param pofConfigURI  the POF configuration URI
     *
     * @return  the {@link FluentCoherenceSchema}
     */
    public S setPofConfigURI(String pofConfigURI);


    /**
     * Sets Portable-Object-Format serialization to be enabled/disabled for Oracle Coherence applications
     * based on this {@link FluentCoherenceSchema}.
     *
     * @param isEnabled  <code>true</code> for POF enabled, <code>false</code> otherwise.
     *
     * @return  the {@link FluentCoherenceSchema}
     */
    public S setPofEnabled(boolean isEnabled);
}
