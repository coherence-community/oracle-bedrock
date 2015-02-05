/*
 * File: JSchBasedAuthentication.java
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

package com.oracle.tools.runtime.remote.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.oracle.tools.runtime.remote.Authentication;

/**
 * A specialized {@link com.oracle.tools.runtime.remote.Authentication} that defines {@link JSch} specific callbacks for
 * {@link com.oracle.tools.runtime.remote.Authentication}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface JSchBasedAuthentication extends Authentication
{
    /**
     * Configures the {@link JSch} framework for the type
     * of {@link Authentication}, prior to creating a {@link Session}.
     *
     * @param jsch  the {@link JSch} framework to configure
     */
    public void configureFramework(JSch jsch);


    /**
     * Configures the {@link JSch} {@link Session} for the type
     * of {@link Authentication}, prior to creating a connection.
     *
     * @param session  the {@link Session} to configure
     */
    public void configureSession(Session session);
}
