/*
 * File: RemoteShell.java
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

import com.oracle.tools.Options;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import java.io.File;

/**
 * A {@link RemoteShell} is a wrapper around various types
 * of shell running on a remote platform.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface RemoteShell<A extends Application,S extends ApplicationSchema<A>,
        E extends RemoteApplicationEnvironment>
{
    /**
     * Realizes an instance of an {@link Application}.
     *
     * @param applicationSchema  the {@link ApplicationSchema} to use for realizing
     *                           the {@link RemoteApplicationProcess}
     * @param applicationName    the name of the application
     * @param platform           an optional {@link Platform} on which the {@link RemoteApplicationProcess}
     *                           will be realized
     * @param environment        the {@link RemoteApplicationEnvironment} for the application
     * @param workingDirectory the working directory for the process
     * @param options            the {@link Options} to use when realizing the {@link RemoteApplicationProcess}
     *
     * @return an {@link RemoteApplicationProcess} representing the application realized by
     *         the {@link RemoteShell}
     *
     * @throws RuntimeException when a problem occurs while starting the application
     */
    RemoteApplicationProcess realize(S         applicationSchema,
                                     String    applicationName,
                                     Platform  platform,
                                     E         environment,
                                     String    workingDirectory,
                                     Options   options);

    /**
     * Ensure that the specified directory exists on the remote platform.
     *
     * @param directoryName the directory to create
     */
    void makeDirectories(String directoryName, Options options);
}
