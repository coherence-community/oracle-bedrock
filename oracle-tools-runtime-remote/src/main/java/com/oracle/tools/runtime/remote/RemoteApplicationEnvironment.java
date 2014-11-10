/*
 * File: RemoteApplicationEnvironment.java
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

package com.oracle.tools.runtime.remote;

import com.oracle.tools.runtime.Application;

import java.io.Closeable;

import java.net.InetAddress;

import java.util.Properties;

/**
 * Defines environmental requirements for a remote {@link Application}.
 * <p>
 * This interface is used by {@link AbstractRemoteApplicationBuilder}s to
 * provide information during the {@link Application} realization process.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface RemoteApplicationEnvironment extends Closeable
{
    /**
     * Obtains the remote command to execute to launch the remote {@link Application}.
     *
     * @param localInetAddress  the local {@link InetAddress} that the remote
     *                          {@link Application} can use to connect back to the platform
     *                          that realized the {@link Application}
     *
     * @return  the remote command
     */
    public String getRemoteCommandToExecute(InetAddress localInetAddress);


    /**
     * Obtains the environment variables to use for the remote {@link Application}.
     *
     * @return  a {@link Properties} representing the required remote environment variables
     */
    public Properties getRemoteEnvironmentVariables();


    /**
     * Closes the {@link RemoteApplicationEnvironment}.
     */
    @Override
    public void close();
}
