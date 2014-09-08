/*
 * File: SimpleApplicationBuilderTest.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.runtime.console.PipedApplicationConsole;

import com.oracle.tools.runtime.options.Diagnostics;
import com.oracle.tools.runtime.options.ErrorStreamRedirection;

import org.junit.Test;

import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;

/**
 * Functional tests for {@link SimpleApplicationBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class SimpleApplicationBuilderTest extends AbstractTest
{
    /**
     * Ensure that we can run a simple application (a JVM in this case).
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        SimpleApplicationSchema schema =
            new SimpleApplicationSchema("java").addOption(ErrorStreamRedirection.enabled());

        schema.addArgument("-help");

        SimpleApplicationBuilder builder     = new SimpleApplicationBuilder();
        PipedApplicationConsole  console     = new PipedApplicationConsole();
        Platform                 platform    = LocalPlatform.getInstance();

        SimpleApplication        application = builder.realize(schema,
                                                               "java",
                                                               console,
                                                               platform,
                                                               Diagnostics.enabled());

        String                   stdout      = console.getOutputReader().readLine();

        assertThat(stdout, containsString("Usage: java [-options] class [args...]"));

        application.close();
    }


    /**
     * Ensure that we can run a simple application (a JVM in this case).
     *
     * @throws Exception
     */
    @Test
    public void shouldInvokeLifecycleInterceptor() throws Exception
    {
        SimpleApplicationSchema schema =
            new SimpleApplicationSchema("java").addOption(ErrorStreamRedirection.enabled());

        LifecycleEventInterceptor<SimpleApplication> interceptor = Mockito.mock(LifecycleEventInterceptor.class);

        schema.addLifecycleInterceptor(interceptor);

        schema.addArgument("-help");

        SimpleApplicationBuilder builder  = new SimpleApplicationBuilder();
        PipedApplicationConsole  console  = new PipedApplicationConsole();
        Platform                 platform = LocalPlatform.getInstance();

        try (SimpleApplication application = builder.realize(schema, "java", console, platform, Diagnostics.enabled()))
        {
            Mockito.verify(interceptor, Mockito.times(1)).onEvent(Mockito.any(LifecycleEvent.class));

            String stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("Usage: java [-options] class [args...]"));

            application.waitFor();

            application.close();

            Mockito.verify(interceptor, Mockito.times(2)).onEvent(Mockito.any(LifecycleEvent.class));

            int exitCode = application.exitValue();

            assertThat(exitCode, is(0));
        }
    }
}
