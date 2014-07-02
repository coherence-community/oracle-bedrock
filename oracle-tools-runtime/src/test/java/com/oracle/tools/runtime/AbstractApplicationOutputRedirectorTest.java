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

package com.oracle.tools.runtime;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Jonathan Knight
 */
public class AbstractApplicationOutputRedirectorTest
{
    @Test
    public void shouldWritePlainOutput() throws Exception
    {
        InputStream  inputStream  = new ByteArrayInputStream("foo".getBytes());
        StringWriter writer       = new StringWriter();
        PrintWriter  outputWriter = new PrintWriter(writer);

        AbstractApplication.OutputRedirector redirector = new AbstractApplication.OutputRedirector("TestApp",
                                                                                                   "X",
                                                                                                   inputStream,
                                                                                                   outputWriter,
                                                                                                   1234,
                                                                                                   false,
                                                                                                   true);

        redirector.run();

        BufferedReader reader = new BufferedReader(new StringReader(writer.getBuffer().toString()));
        String         line1  = reader.readLine();
        assertThat(line1, is("foo"));
        String         line2  = reader.readLine();
        assertThat(line2, is("(terminated)"));
        String         line3  = reader.readLine();
        assertThat(line3, is(nullValue()));
    }


    @Test
    public void shouldWriteDecoratedOutput() throws Exception
    {
        InputStream  inputStream  = new ByteArrayInputStream("foo".getBytes());
        StringWriter writer       = new StringWriter();
        PrintWriter  outputWriter = new PrintWriter(writer);

        AbstractApplication.OutputRedirector redirector = new AbstractApplication.OutputRedirector("TestApp",
                                                                                                   "X",
                                                                                                   inputStream,
                                                                                                   outputWriter,
                                                                                                   1234,
                                                                                                   false,
                                                                                                   false);

        redirector.run();

        BufferedReader reader = new BufferedReader(new StringReader(writer.getBuffer().toString()));
        String         line1  = reader.readLine();
        assertThat(line1, is("[TestApp:X:1234]    1: foo"));
        String         line2  = reader.readLine();
        assertThat(line2, is("[TestApp:X:1234]    2: (terminated)"));
        String         line3  = reader.readLine();
        assertThat(line3, is(nullValue()));
    }
}
