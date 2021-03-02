/*
 * File: ServiceStatus.java
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

package com.oracle.bedrock.runtime.coherence;

/**
 * Defines valid statuses a {@link CoherenceClusterMember} Service may be in.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public enum ServiceStatus
{
    /**
     * The service is endangered.  Any loss will cause data/computation loss.
     */
    ENDANGERED,

    /**
     * The service is machine-safe.  Any machine may be safely shutdown without loss.
     */
    MACHINE_SAFE,

    /**
     * The service is node-safe.  Any node may be safely shutdown without loss.
     */
    NODE_SAFE,

    /**
     * The service has been orphaned.  Data/computational services have been lost.
     */
    ORPHANED,

    /**
     * The service is rack-safe.  Any rack may be safely shutdown without loss.
     */
    RACK_SAFE,

    /**
     * The service is site-safe.  Any site may be safely shutdown without loss.
     */
    SITE_SAFE,

    /**
     * The service is running (but no other information is available).
     */
    RUNNING,

    /**
     * The service is not running.
     */
    STOPPED,

    /**
     * The service is running, but the actual status is undefined / unknown.
     */
    UNKNOWN
}
