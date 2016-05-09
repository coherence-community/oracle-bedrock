/*
 * File: CurlHttpDeployer.java
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
import com.oracle.bedrock.lang.StringHelper;
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
 * a curl process on the target platform to pull the artifacts
 * from the HTTP server.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CurlHttpDeployer extends HttpDeployer
{
    /**
     * Field description
     */
    public static final String DEFAULT_CURL = "curl";

    /**
     * The command to use to execute curl.
     */
    private final String curlCommand;


    /**
     * Create a {@link CurlHttpDeployer}.
     *
     * @param options the {@link Option}s controlling the deployer
     */
    public CurlHttpDeployer(Option... options)
    {
        this(DEFAULT_CURL);
    }


    /**
     * Create a {@link CurlHttpDeployer}.
     *
     * @param curlCommand the location of the curl executable
     * @param options     the {@link Option}s controlling the deployer
     */
    public CurlHttpDeployer(String    curlCommand,
                            Option... options)
    {
        super(options);
        this.curlCommand = curlCommand;
    }


    @Override
    protected void deployArtifact(URL      sourceURL,
                                  String   targetFileName,
                                  Platform platform)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = platform.launch(Application.class,
                                                       Executable.named(curlCommand),
                                                       Argument.of(sourceURL.toExternalForm()),
                                                       Argument.of("--create-dirs"),
                                                       Argument.of("-o"),
                                                       Argument.of(StringHelper.doubleQuoteIfNecessary(targetFileName)),
                                                       DisplayName.of("Deploy"),
                                                       Console.of(console)))
        {
            if (application.waitFor() != 0)
            {
                StringBuilder message =
                    new StringBuilder("Error deploying ").append(targetFileName).append(" - curl returned ")
                    .append(application.exitValue()).append("\n").append("curl output:");

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
