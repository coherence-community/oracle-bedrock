/*
 * File: LocalPlatformApplicationTest.java
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

import com.oracle.tools.options.Decoration;
import com.oracle.tools.options.Diagnostics;

import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.console.PipedApplicationConsole;

import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.runtime.options.ErrorStreamRedirection;
import com.oracle.tools.runtime.options.Executable;

import org.junit.Test;

import org.mockito.Mockito;

import static org.hamcrest.core.Is.is;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;

/**
 * Functional tests for {@link Application}s on a {@link LocalPlatform}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class LocalPlatformApplicationTest extends AbstractTest
{
    /**
     * Ensure that we can run an {@link Application} (a JVM in this case).
     *
     * @throws Exception
     */
    @Test
    public void shouldLaunchApplication() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        try (Application application = LocalPlatform.get().launch(Application.class,
                                                                  Executable.named("java"),
                                                                  Argument.of("-help"),
                                                                  DisplayName.of("java"),
                                                                  ErrorStreamRedirection.enabled(),
                                                                  Diagnostics.enabled(),
                                                                  Console.of(console)))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("Usage: java [-options] class [args...]"));
        }
    }


    /**
     * Ensure that we can run an {@link Application} (a JVM in this case).
     *
     * @throws Exception
     */
    @Test
    public void shouldLaunchExecutable() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        try (Application application = LocalPlatform.get().launch("java",
                                                                  Argument.of("-help"),
                                                                  DisplayName.of("java"),
                                                                  ErrorStreamRedirection.enabled(),
                                                                  Diagnostics.enabled(),
                                                                  Console.of(console)))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("Usage: java [-options] class [args...]"));
        }
    }


    /**
     * Ensure that we can run an {@link Application} (a JVM in this case) and
     * {@link ApplicationListener}s are notified.
     *
     * @throws Exception
     */
    @Test
    public void shouldInvokeApplicationListeners() throws Exception
    {
        ApplicationListener<Application> listener = Mockito.mock(ApplicationListener.class);

        PipedApplicationConsole          console  = new PipedApplicationConsole();

        try (Application application = LocalPlatform.get().launch(Application.class,
                                                                  Executable.named("java"),
                                                                  Argument.of("-help"),
                                                                  Diagnostics.enabled(),
                                                                  ErrorStreamRedirection.enabled(),
                                                                  Console.of(console),
                                                                  Decoration.of(listener),
                                                                  DisplayName.of("java")))
        {
            Mockito.verify(listener, Mockito.times(1)).onRealized(Mockito.same(application));

            String stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("Usage: java [-options] class [args...]"));

            application.waitFor();

            application.close();

            Mockito.verify(listener, Mockito.times(1)).onClosing(Mockito.same(application));
            Mockito.verify(listener, Mockito.times(1)).onClosed(Mockito.same(application));

            int exitCode = application.exitValue();

            assertThat(exitCode, is(0));
        }
    }
}
