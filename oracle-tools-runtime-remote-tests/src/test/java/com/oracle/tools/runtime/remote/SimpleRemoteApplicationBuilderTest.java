/*
 * File: SimpleRemoteApplicationBuilderTest.java
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

import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.options.EnvironmentVariables;

import com.oracle.tools.runtime.remote.options.CustomDeployment;
import com.oracle.tools.runtime.remote.options.StrictHostChecking;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.URL;

/**
 * Functional tests for {@link SimpleRemoteApplicationBuilder}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleRemoteApplicationBuilderTest extends AbstractRemoteApplicationBuilderTest
{
    /**
     * Ensure that we can launch deploy a test file.
     */
    @Test
    public void shouldLaunchSimpleApplicationRemotely() throws IOException, InterruptedException
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("ls -la");

        RemotePlatform platform = new RemotePlatform(InetAddress.getByName(getRemoteHostName()),
                                                     getRemoteUserName(),
                                                     getRemoteAuthentication());

        URL  testFileURL = Thread.currentThread().getContextClassLoader().getResource("test.txt");
        File testFile    = new File(testFileURL.getFile());

        try (SimpleApplication application = platform.realize("Java",
                                                              schema,
                                                              new SystemApplicationConsole(),
                                                              CustomDeployment
                                                                  .including(new DeploymentArtifact(testFile)),
                                                              StrictHostChecking.disabled()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }
}
