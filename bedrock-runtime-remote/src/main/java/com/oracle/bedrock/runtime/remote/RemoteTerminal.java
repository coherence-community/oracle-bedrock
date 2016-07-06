/*
 * File: RemoteTerminal.java
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

package com.oracle.bedrock.runtime.remote;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.Platform;

import java.util.List;
import java.util.Properties;

/**
 * An internal mechanism for interacting with a {@link RemotePlatform}, including
 * launching {@link RemoteApplicationProcess}es and creating remote directories.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
@Internal
public interface RemoteTerminal
{
    /**
     * Launches an {@link Application}, represented as a {@link RemoteApplicationProcess},
     * according to the information provided by the {@link Launchable} and specified {@link OptionsByType}.
     *
     * @param launchable        the {@link Launchable} defining how to launch the {@link Application}
     * @param applicationClass  the {@link Class} of the {@link Application} being launched
     * @param optionsByType     the {@link OptionsByType} to use when launching the {@link Application}
     *
     * @return an {@link RemoteApplicationProcess} representing the launched {@link Application}
     *
     * @throws RuntimeException when a problem occurs while starting the application
     */
    RemoteApplicationProcess launch(Launchable                   launchable,
                                    Class<? extends Application> applicationClass,
                                    OptionsByType                optionsByType);


    /**
     * Ensure that the specified directory exists on the {@link RemotePlatform}.
     *
     * @param directoryName  the directory to create
     * @param optionsByType  the {@link OptionsByType}
     */
    void makeDirectories(String        directoryName,
                         OptionsByType optionsByType);


    /**
     * A callback interface defining how an {@link Application} should be launched remotely using a
     * {@link RemoteTerminal}.
     */
    interface Launchable
    {
        /**
         * Obtains the command to launch an {@link Application} using a {@link RemoteTerminal}.
         *
         * @param platform       the {@link Platform}
         * @param optionsByType  the {@link OptionsByType}
         *
         * @return  the command
         */
        String getCommandToExecute(Platform      platform,
                                   OptionsByType optionsByType);


        /**
         * Obtains the command line arguments when launching an {@link Application} using a {@link RemoteTerminal}.
         *
         * @param platform       the {@link Platform}
         * @param optionsByType  the {@link OptionsByType}
         *
         * @return  the command line arguments to use when launching the {@link Application}
         */
        List<String> getCommandLineArguments(Platform      platform,
                                             OptionsByType optionsByType);


        /**
         * Obtains the environment variables to use when launching an {@link Application} using a {@link RemoteTerminal}.
         *
         * @param platform  the {@link Platform}
         * @param optionsByType   the {@link OptionsByType}
         *
         * @return  a {@link Properties} representing the required remote environment variables
         */
        Properties getEnvironmentVariables(Platform      platform,
                                           OptionsByType optionsByType);
    }
}
