/*
 * File: JavaApplicationRuntime.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.ApplicationRuntime;

import java.util.Properties;

/**
 * An <strong>internal</strong> interface providing important information
 * concerning the runtime of {@link JavaApplication}.
 * <p>
 * Each {@link JavaApplication} has an instance of an {@link JavaApplicationRuntime}
 * allowing the {@link JavaApplication} to access and interact with the environment
 * in which it is running.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <P>  the type of {@link JavaApplicationProcess} used to represent
 *             the underlying {@link JavaApplication}
 */
public interface JavaApplicationRuntime<P extends JavaApplicationProcess> extends ApplicationRuntime<P>
{
    /**
     * Obtains the system properties that where used to establish
     * the {@link JavaApplication} when it was started.
     *
     * @return  the system properties
     */
    public Properties getSystemProperties();


    /**
     * Obtains the port this Java Virtual Machine is listening on for remote debug
     * connections or <= 0 if remote debugging is disabled.
     *
     * @return  the remote debugging port
     */
    public int getRemoteDebuggingPort();
}
