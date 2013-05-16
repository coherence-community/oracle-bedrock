/*
 * File: PipedApplicationConsoleTest.java
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

package com.oracle.tools.runtime.java.console;

import com.oracle.tools.runtime.console.PipedApplicationConsole;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import java.io.BufferedReader;

/**
 * Unit Tests for {@link com.oracle.tools.runtime.console.PipedApplicationConsole}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PipedApplicationConsoleTest
{
    /**
     * Ensure that we can read content sent to stdout.
     *
     * @throws Exception
     */
    @Test
    public void shouldPipeStdOut() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        console.getOutputWriter().println("Hello World");

        Assert.assertThat(console.getOutputReader().readLine(), is("Hello World"));
    }


    /**
     * Ensure that we can read content sent to stderr.
     *
     * @throws Exception
     */
    @Test
    public void shouldPipeStdErr() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        console.getErrorWriter().println("Hello World");

        Assert.assertThat(console.getErrorReader().readLine(), is("Hello World"));
    }


    /**
     * Ensure that we can read content sent to stdin.
     *
     * @throws Exception
     */
    @Test
    public void shouldPipeStdIn() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        console.getInputWriter().println("Hello World");

        BufferedReader reader = new BufferedReader(console.getInputReader());

        Assert.assertThat(reader.readLine(), is("Hello World"));
    }


    /**
     * Ensure that we close a console multiple times.
     *
     * @throws Exception
     */
    @Test
    public void shouldCloseMultipleTimes() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        console.close();
        console.close();
    }
}
