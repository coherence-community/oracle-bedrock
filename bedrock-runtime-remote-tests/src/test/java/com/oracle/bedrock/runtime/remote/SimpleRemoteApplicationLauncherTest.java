/*
 * File: SimpleRemoteApplicationLauncherTest.java
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

import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.remote.options.CustomDeployment;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Functional tests for {@link SimpleRemoteApplicationLauncher}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleRemoteApplicationLauncherTest extends AbstractRemoteTest
{
    /**
     * Ensure that we can launch deploy a test file.
     */
    @Test
    public void shouldLaunchSimpleApplicationRemotely() throws Exception
    {
        RemotePlatform platform    = getRemotePlatform();

        try (Application application = platform.launch("ls", Argument.of("-la"), Console.system()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }


    /**
     * Ensure that we can launch deploy a test file.
     */
    @Test
    public void shouldDeployAndLaunchSimpleApplicationRemotely() throws Exception
    {
        RemotePlatform platform    = getRemotePlatform();

        URL            testFileURL = Thread.currentThread().getContextClassLoader().getResource("test.txt");
        File           testFile    = new File(testFileURL.toURI().getPath());

        try (Application application = platform.launch("ls",
                                                       Argument.of("-la"),
                                                       SystemApplicationConsole.builder(),
                                                       CustomDeployment.including(new DeploymentArtifact(testFile))))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }
}
