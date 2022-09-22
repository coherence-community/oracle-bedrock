/*
 * File: SystemApplicationConsoleTest.java
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

import com.oracle.bedrock.runtime.java.container.Scope;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Knight
 */
public class SystemApplicationConsoleTest
{
    /**
     * A {@link SystemApplicationConsole} should always return false
     * from {@link SystemApplicationConsole#isDiagnosticsEnabled()}
     */
    @Test
    public void shouldBeDiagnosticEnabled() throws Exception
    {
        SystemApplicationConsole console = new SystemApplicationConsole();

        assertThat(console.isDiagnosticsEnabled(), is(true));
    }


    /**
     * Make sure that if the PrintWriter wrapping System.out
     * is closed that System.out is not closed.
     */
    @Test
    public void shouldNotCloseSystemOutIfOutputWriterClosed()
    {
        Scope       scope  = mock(Scope.class);
        PrintStream stdOut = new PrintStreamStub(new ByteArrayOutputStream());
        PrintStream stdErr = new PrintStreamStub(new ByteArrayOutputStream());
        InputStream stdIn  = new ByteArrayInputStream(new byte[0]);

        when(scope.getStandardOutput()).thenReturn(stdOut);
        when(scope.getStandardError()).thenReturn(stdErr);
        when(scope.getStandardInput()).thenReturn(stdIn);

        SystemApplicationConsole console = new SystemApplicationConsole();

        console.init(scope);

        console.getOutputWriter().close();
    }


    /**
     * Make sure that if the PrintWriter wrapping System.err
     * is closed that System.err is not closed.
     */
    @Test
    public void shouldNotCloseSystemErrIfErrorWriterClosed()
    {
        Scope       scope  = mock(Scope.class);
        PrintStream stdOut = new PrintStreamStub(new ByteArrayOutputStream());
        PrintStream stdErr = new PrintStreamStub(new ByteArrayOutputStream());
        InputStream stdIn  = mock(InputStream.class, "In");

        when(scope.getStandardOutput()).thenReturn(stdOut);
        when(scope.getStandardError()).thenReturn(stdErr);
        when(scope.getStandardInput()).thenReturn(stdIn);

        SystemApplicationConsole console = new SystemApplicationConsole();

        console.init(scope);

        console.getErrorWriter().close();
    }

    static class PrintStreamStub
            extends PrintStream
    {
        public PrintStreamStub(OutputStream out)
        {
            super(out);
        }

    @Override
    public void close()
        {
        fail("Close should not be called");
        }
    }
}
