/*
 * File: PowerShellHttpDeployer.java
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

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.Option;

import com.oracle.bedrock.lang.StringHelper;

import com.oracle.bedrock.runtime.Application;

import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.runtime.options.WorkingDirectory;

import java.io.File;

import java.net.URL;

/**
 * An implementation of {@link PowerShellHttpDeployer} that runs
 * a PowerShell Invoke-WebRequest command on the target platform
 * to pull the artifacts from the HTTP server.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class PowerShellHttpDeployer extends HttpDeployer
{
    /**
     * Create a {@link PowerShellHttpDeployer}.
     *
     * @param options the {@link Option}s controlling the deployer
     */
    public PowerShellHttpDeployer(Option... options)
    {
        super(options);
    }


    @Override
    protected void deployArtifact(final URL      sourceURL,
                                  final String   targetFileName,
                                  final Platform platform)
    {
        int                         index        = targetFileName.lastIndexOf('\\');
        String                      parentFolder = index > 0 ? targetFileName.substring(0, index) : "C:\\";

        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = platform.launch(Application.class,
                                                       Executable.named("powershell"),
                                                       Argument.of("-Command"),
                                                       Argument.of("Invoke-WebRequest"),
                                                       Argument.of("-Uri"),
                                                       Argument.of(sourceURL.toExternalForm()),
                                                       Argument.of("-OutFile"),
                                                       Argument.of(StringHelper.doubleQuoteIfNecessary(targetFileName)),
                                                       WorkingDirectory.at(new File(parentFolder)),
                                                       DisplayName.of("Deploy"),
                                                       Console.of(console)))
        {
            int exitCode = application.waitFor();

            if (exitCode != 0)
            {
                StringBuilder message =
                    new StringBuilder("Error deploying ").append(targetFileName).append(" - PowerShell returned ")
                    .append(application.exitValue()).append("\n").append("Invoke-WebRequest output:");

                for (String line : console.getCapturedOutputLines())
                {
                    message.append('\n').append(line);
                }

                for (String line : console.getCapturedErrorLines())
                {
                    message.append('\n').append(line);
                }

                throw new RuntimeException(message.toString());
            }
        }
    }
}
