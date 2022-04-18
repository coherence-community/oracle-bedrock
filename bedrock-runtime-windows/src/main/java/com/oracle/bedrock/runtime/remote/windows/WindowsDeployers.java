/*
 * File: WindowsDeployers.java
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

package com.oracle.bedrock.runtime.remote.windows;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.remote.http.HttpDeployer;
import com.oracle.bedrock.runtime.remote.windows.http.PowerShellHttpDeployer;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.options.FileShareDeployer;

/**
 * Helps methods to create Windows-specific {@link Deployer}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsDeployers
{
    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use PowerShell Invoke-WebRequest to retrieve
     * artifacts.
     *
     * @param options the {@link Option}s controlling the deployer
     *
     * @return a new {@link HttpDeployer}
     */
    public static HttpDeployer powerShellHttp(Option... options)
    {
        return new PowerShellHttpDeployer(options);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use PowerShell Invoke-WebRequest to retrieve
     * artifacts.
     *
     * @param localShareName   the local share name
     * @param remoteShareName  the remote share name
     * @param options          the {@link Option}s controlling the deployer
     *
     * @return a new {@link FileShareDeployer}
     */
    public static FileShareDeployer fileShare(String    localShareName,
                                              String    remoteShareName,
                                              Option... options)
    {
        return new WindowsFileShareDeployer(localShareName, remoteShareName, options);
    }
}
