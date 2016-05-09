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

package com.oracle.bedrock.runtime.console;

import com.oracle.bedrock.deferred.DeferredHelper;
import com.oracle.bedrock.predicate.Predicates;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Console;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.Queue;

import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link CapturingApplicationConsole}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CapturingApplicationConsoleTest
{
    @Test
    public void shouldCaptureStdOut() throws Exception
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole(5, false);

        try (JavaApplication application = LocalPlatform.get().launch(JavaApplication.class,
                                                                      ClassName.of(SimpleApp.class),
                                                                      Arguments.of("1", "2", "3", "4", "5"),
                                                                      Console.of(console)))
        {
            application.waitFor();
        }

        console.close();

        assertThat(console.stdoutThread.isAlive(), is(false));

        Queue<String> lines = console.getCapturedOutputLines();

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

        try (JavaApplication application = LocalPlatform.get().launch(JavaApplication.class,
                                                                      ClassName.of(SimpleApp.class),
                                                                      Arguments.of("1", "2", "3", "4", "5"),
                                                                      Console.of(console)))
        {
            application.waitFor();
        }

        console.close();

        assertThat(console.stderrThread.isAlive(), is(false));

        Queue<String> lines = console.getCapturedErrorLines();

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

        try (JavaApplication application = LocalPlatform.get().launch(JavaApplication.class,
                                                                      ClassName.of(EchoApp.class),
                                                                      Console.of(console)))
        {
            Queue<String> lines = console.getCapturedOutputLines();

            stdIn.println("Foo");

            DeferredHelper.ensure(eventually(invoking(lines).size()), Predicates.is(1));

            assertThat(lines, contains("Echo: Foo"));

            stdIn.println("Bar");

            DeferredHelper.ensure(eventually(invoking(lines).size()), Predicates.is(2));

            assertThat(lines, contains("Echo: Foo", "Echo: Bar"));

            lines.clear();

            assertThat(lines.isEmpty(), is(true));
            stdIn.println("Foo2");

            DeferredHelper.ensure(eventually(invoking(lines).size()), Predicates.is(1));

            assertThat(lines, contains("Echo: Foo2"));
        }
    }
}
