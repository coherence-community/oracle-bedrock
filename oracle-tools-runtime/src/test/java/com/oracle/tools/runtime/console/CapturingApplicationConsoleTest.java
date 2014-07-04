/*
 * File: CapturingApplicationConsoleTest.java
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

import java.io.PrintWriter;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link CapturingApplicationConsole}.
 *
 * @author Jonathan Knight
 */
public class CapturingApplicationConsoleTest
{
    @Test
    public void shouldCaptureStdOut() throws Exception
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole(5, false);

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SimpleApp.class.getCanonicalName()).setArgument("1").setArgument("2")
                .setArgument("3").setArgument("4").setArgument("5");

        SimpleJavaApplication app = LocalPlatform.getInstance().realize(schema, "App", console);

        app.waitFor();
        app.close();

        console.close();

        assertThat(console.m_outThread.isAlive(), is(false));

        LinkedList<String> lines = console.getCapturedOutputLines();

        assertThat(lines.size(), is(5));
        assertThat(lines.poll(), is("Out: 2"));
        assertThat(lines.poll(), is("Out: 3"));
        assertThat(lines.poll(), is("Out: 4"));
        assertThat(lines.poll(), is("Out: 5"));
        assertThat(lines.poll(), is("(terminated)"));
    }


    @Test
    public void shouldCaptureStdErr() throws Exception
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole(5, false);

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SimpleApp.class.getCanonicalName()).setArgument("1").setArgument("2")
                .setArgument("3").setArgument("4").setArgument("5");

        SimpleJavaApplication app = LocalPlatform.getInstance().realize(schema, "App", console);

        app.waitFor();
        app.close();

        console.close();

        assertThat(console.m_errThread.isAlive(), is(false));

        LinkedList<String> lines = console.getCapturedErrorLines();

        assertThat(lines.size(), is(5));
        assertThat(lines.poll(), is("Err: 2"));
        assertThat(lines.poll(), is("Err: 3"));
        assertThat(lines.poll(), is("Err: 4"));
        assertThat(lines.poll(), is("Err: 5"));
        assertThat(lines.poll(), is("(terminated)"));
    }


    @Test
    public void shouldBeAbleToResetLinesList() throws Exception
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole(5, false);
        PrintWriter                 stdIn   = console.getInputWriter();

        SimpleJavaApplicationSchema schema  = new SimpleJavaApplicationSchema(EchoApp.class.getCanonicalName());

        SimpleJavaApplication       app     = LocalPlatform.getInstance().realize(schema, "App", console);

        try
        {
            LinkedList<String> lines = console.getCapturedOutputLines();

            stdIn.println("Foo");
            assertEventually(lines, 1);
            assertThat(lines, contains("Echo: Foo"));
            stdIn.println("Bar");
            assertEventually(lines, 2);
            assertThat(lines, contains("Echo: Foo", "Echo: Bar"));

            lines.clear();

            assertThat(lines.isEmpty(), is(true));
            stdIn.println("Foo2");
            assertEventually(lines, 1);
            assertThat(lines, contains("Echo: Foo2"));
        }
        finally
        {
            app.close();
        }
    }

    /**
     *
     */
    @Test
    public void shouldBeAbleToWaitForOutput() throws Exception
    {

    }


    /**
     * A hacky assert eventually as we cannot use the Eventually class in the Test Support module.
     */
    public static void assertEventually(LinkedList<String> lines,
                                        int                expected)
    {
        long start = System.currentTimeMillis();

        while (true)
        {
            long end = System.currentTimeMillis();

            if (end - start > 30000)
            {
                assertThat(lines.size(), is(expected));
                break;
            }
            else
            {
                try
                {
                    assertThat(lines.size(), is(expected));
                    break;
                }
                catch (Throwable t)
                {
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e1)
                    {
                        // ignored
                    }
                }
            }
        }
    }
}
