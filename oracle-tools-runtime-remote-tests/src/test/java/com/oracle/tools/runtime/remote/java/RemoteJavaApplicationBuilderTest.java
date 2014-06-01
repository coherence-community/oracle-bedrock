/*
 * File: RemoteJavaApplicationBuilderTest.java
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

package com.oracle.tools.runtime.remote.java;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationBuilderTest;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

/**
 * Functional tests for {@link RemoteJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteJavaApplicationBuilderTest extends AbstractRemoteApplicationBuilderTest
{
    /**
     * Ensure that we can launch Java remotely.
     */
    @Test
    public void shouldLaunchJavaApplicationRemotely() throws IOException, InterruptedException
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // sleep only for 5 seconds
        schema.setArgument("5");

        RemoteJavaApplicationBuilder<SimpleJavaApplication> builder =
            new RemoteJavaApplicationBuilder<SimpleJavaApplication>(getRemoteHostName(),
                                                                    getRemoteUserName(),
                                                                    getRemoteAuthentication());

        builder.setStrictHostChecking(false);
        builder.setAutoDeployEnabled(true);
        builder.setJavaHome(System.getProperty("java.home"));

        SimpleJavaApplication application = builder.realize(schema, "Java", new SystemApplicationConsole());

        assertThat(application.waitFor(), is(0));

        application.close();

        assertThat(application.exitValue(), is(0));
    }
}
