/*
 * File: WGetHttpDeployer.java
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

package com.oracle.bedrock.runtime.remote.http;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.Executable;

import java.net.URL;

/**
 * An implementation of {@link HttpDeployer} that runs
 * a wget process on the target platform to pull the artifacts
 * from the HTTP server.
 * <p>
 * NOTE: WGet does not create directories so the target directory
 * for any artifact being deployed must already exist.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WGetHttpDeployer extends HttpDeployer
{
    /**
     * Field description
     */
    public static final String DEFAULT_WGET = "wget";

    /**
     * The command to use to execute wget.
     */
    private final String wgetCommand;


    /**
     * Create a {@link WGetHttpDeployer}.
     *
     * @param options the {@link Option}s controlling the deployer
     */
    public WGetHttpDeployer(Option... options)
    {
        this(DEFAULT_WGET, options);
    }


    /**
     * Create a {@link WGetHttpDeployer}.
     *
     * @param wgetCommand the location of the wget command
     * @param options     the {@link Option}s controlling the deployer
     */
    public WGetHttpDeployer(String    wgetCommand,
                            Option... options)
    {
        super(options);
        this.wgetCommand = wgetCommand;
    }


    @Override
    protected void deployArtifact(URL      sourceURL,
                                  String   targetFileName,
                                  Platform platform)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = platform.launch(Application.class,
                                                       Executable.named(wgetCommand),
                                                       Argument.of("-O"),
                                                       Argument.of(targetFileName),
                                                       Argument.of(sourceURL.toExternalForm()),
                                                       DisplayName.of("Deploy"),
                                                       Console.of(console)))
        {
            int exitCode = application.waitFor();

            application.close();

            if (exitCode != 0)
            {
                StringBuilder message =
                    new StringBuilder("Error deploying ").append(targetFileName).append(" - wget returned ")
                    .append(exitCode).append("\n").append("wget output:");

                for (String line : console.getCapturedOutputLines())
                {
                    message.append(line);
                }

                for (String line : console.getCapturedErrorLines())
                {
                    message.append(line);
                }

                throw new RuntimeException(message.toString());
            }
        }

    }
}
