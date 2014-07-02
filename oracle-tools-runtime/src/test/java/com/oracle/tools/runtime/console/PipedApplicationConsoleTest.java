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

package com.oracle.tools.runtime.console;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import org.junit.Test;

import java.io.BufferedReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link PipedApplicationConsole}
 *
 * @author Jonathan Knight
 */
public class PipedApplicationConsoleTest
{
    @Test
    public void shouldNotBePlainModeByDefault() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        assertThat(console.isPlainMode(), is(false));
    }


    @Test
    public void shouldNotBePlainMode() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole(PipedApplicationConsole.DEFAULT_PIPE_SIZE, false);

        assertThat(console.isPlainMode(), is(false));
    }


    @Test
    public void shouldBePlainMode() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole(PipedApplicationConsole.DEFAULT_PIPE_SIZE, true);

        assertThat(console.isPlainMode(), is(true));
    }


    @Test
    public void shouldBeAbleToReadPipedStdOutAfterConsoleClosed() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SimpleApp.class.getCanonicalName()).setArgument("foo").setArgument("bar");

        PipedApplicationConsole console = new PipedApplicationConsole(1024, true);

        SimpleJavaApplication   app     = LocalPlatform.getInstance().realize(schema, "App", console);

        BufferedReader          reader  = console.getOutputReader();

        app.waitFor();
        app.close();

        assertThat(reader.readLine(), is("Out: foo"));
        assertThat(reader.readLine(), is("Out: bar"));
        assertThat(reader.readLine(), is("(terminated)"));
        assertThat(reader.readLine(), is(nullValue()));
    }


    @Test
    public void shouldBeAbleToReadPipedStdErrAfterConsoleClosed() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SimpleApp.class.getCanonicalName()).setArgument("foo").setArgument("bar");

        PipedApplicationConsole console = new PipedApplicationConsole(1024, true);

        SimpleJavaApplication   app     = LocalPlatform.getInstance().realize(schema, "App", console);

        BufferedReader          reader  = console.getErrorReader();

        app.waitFor();
        app.close();

        assertThat(reader.readLine(), is("Err: foo"));
        assertThat(reader.readLine(), is("Err: bar"));
        assertThat(reader.readLine(), is("(terminated)"));
        assertThat(reader.readLine(), is(nullValue()));
    }
}
