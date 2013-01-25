/*
 * File: SimpleApplicationFunctionalTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

import org.junit.Test;

import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;

/**
 * Unit and Functional tests for {@link SimpleApplication}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleApplicationFunctionalTest extends AbstractTest
{
    /**
     * Ensure that we can run a simple application (a JVM in this case).
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        SimpleApplicationSchema schema = new SimpleApplicationSchema("java");

        schema.addArgument("-help");

        SimpleApplicationBuilder    builder     = new SimpleApplicationBuilder();
        CapturingApplicationConsole console     = new CapturingApplicationConsole();
        SimpleApplication           application = builder.realize(schema, "java", console);

        application.waitFor();
        console.waitForLine(60000);
        application.destroy();

        int exitCode = application.exitValue();

        assertThat(exitCode, is(0));

        // combine the std out and std err outputs for line one
        // (this is because different versions of Java, namely 6 and 7+ use
        // different IO streams for console output)
        String result = console.getConsoleOutputLine("err", 0) + "\n" + console.getConsoleOutputLine("out", 0);

        assertThat(result, containsString("Usage: java [-options] class [args...]"));
    }


    /**
     * Ensure that we can run a simple application (a JVM in this case).
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldInvokeLifecycleInterceptor() throws Exception
    {
        SimpleApplicationSchema                      schema      = new SimpleApplicationSchema("java");

        LifecycleEventInterceptor<SimpleApplication> interceptor = Mockito.mock(LifecycleEventInterceptor.class);

        schema.addLifecycleInterceptor(interceptor);

        schema.addArgument("-help");

        SimpleApplicationBuilder    builder     = new SimpleApplicationBuilder();
        CapturingApplicationConsole console     = new CapturingApplicationConsole();
        SimpleApplication           application = builder.realize(schema, "java", console);

        Mockito.verify(interceptor, Mockito.times(1)).onEvent(Mockito.any(LifecycleEvent.class));

        application.waitFor();
        console.waitForLine(60000);
        application.destroy();

        Mockito.verify(interceptor, Mockito.times(2)).onEvent(Mockito.any(LifecycleEvent.class));

        int exitCode = application.exitValue();

        assertThat(exitCode, is(0));

        // combine the std out and std err outputs for line one
        // (this is because different versions of Java, namely 6 and 7+ use
        // different IO streams for console output)
        String result = console.getConsoleOutputLine("err", 0) + "\n" + console.getConsoleOutputLine("out", 0);

        assertThat(result, containsString("Usage: java [-options] class [args...]"));
    }
}
