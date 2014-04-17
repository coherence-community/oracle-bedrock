/*
 * File: AbstractRemoteApplicationBuilderTest.java
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

import com.oracle.tools.runtime.remote.java.RemoteJavaApplicationBuilder;

/**
 * Abstract functional test class for {@link RemoteJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractRemoteApplicationBuilderTest
{
    /**
     * Obtains the remote server host name that the tests will run against.
     * <p>
     * By default this will use the "oracletools.remote.hostname" system property
     * to determine the host name and if this is not defined, it will default to 127.0.0.1
     *
     * @return  the remote server host
     */
    public String getRemoteHostName()
    {
        return System.getProperty("oracletools.remote.hostname", "127.0.0.1");
    }


    /**
     * Obtains the user name that will be used for connecting and running tests
     * on the remote server.
     * <p>
     * By default this will use the "oracletools.remote.username" system property
     * to determine the user name and if this is not defined, it will default to the "user.name"
     * system property.
     *
     * @return  the remote server user name
     */
    public String getRemoteUserName()
    {
        return System.getProperty("oracletools.remote.username", System.getProperty("user.name"));
    }


    /**
     * Obtains the remote {@link Authentication} to use for connection to the remote server host.
     *
     * @return  the remote {@link Authentication}
     */
    public Authentication getRemoteAuthentication()
    {
        return SecureKeys.fromPrivateKeyFile(System.getProperty("oracletools.remote.privatekey.file",
                                                                "~/.ssh/127.0.0.1_dsa"));
    }
}
