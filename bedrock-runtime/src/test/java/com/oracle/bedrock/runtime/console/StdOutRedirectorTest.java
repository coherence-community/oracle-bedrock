/*
 * File: AbstractApplicationOutputRedirectorTest.java
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

package com.oracle.bedrock.runtime.console;

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.console.StdOutRedirector;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Jonathan Knight
 */
public class StdOutRedirectorTest
{
    @Test
    public void shouldWriteNonConsoleDiagnosticOutput() throws Exception
    {
        InputStream        inputStream  = new ByteArrayInputStream("foo".getBytes());
        StringWriter       writer       = new StringWriter();
        PrintWriter        outputWriter = new PrintWriter(writer);
        ApplicationConsole console = mock(ApplicationConsole.class);

        when(console.getOutputWriter()).thenReturn(outputWriter);
        when(console.isDiagnosticsEnabled()).thenReturn(false);

        StdOutRedirector redirector = new StdOutRedirector()
        {
            @Override
            public synchronized void start()
            {
                run();
            }
        };

        redirector.start("TestApp", "X", inputStream, console, 1234, false);

        BufferedReader reader = new BufferedReader(new StringReader(writer.getBuffer().toString()));
        String         line1  = reader.readLine();

        assertThat(line1, is("foo"));

        String line2 = reader.readLine();

        assertThat(line2, is("(terminated)"));

        String line3 = reader.readLine();

        assertThat(line3, is(nullValue()));
    }


    @Test
    public void shouldWriteConsoleDiagnosticOutput() throws Exception
    {
        InputStream        inputStream  = new ByteArrayInputStream("foo".getBytes());
        StringWriter       writer       = new StringWriter();
        PrintWriter        outputWriter = new PrintWriter(writer);
        ApplicationConsole console = mock(ApplicationConsole.class);

        when(console.getOutputWriter()).thenReturn(outputWriter);
        when(console.isDiagnosticsEnabled()).thenReturn(true);

        StdOutRedirector redirector = new StdOutRedirector()
        {
            @Override
            public synchronized void start()
            {
                run();
            }
        };

        redirector.start("TestApp", "X", inputStream, console, 1234, false);

        BufferedReader reader = new BufferedReader(new StringReader(writer.getBuffer().toString()));
        String         line1  = reader.readLine();

        assertThat(line1, is("[TestApp:X:1234]    1: foo"));

        String line2 = reader.readLine();

        assertThat(line2, is("[TestApp:X:1234]    2: (terminated)"));

        String line3 = reader.readLine();

        assertThat(line3, is(nullValue()));
    }
}
