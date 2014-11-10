/*
 * File: ContainerBasedJavaApplicationBuilderTest.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.options.Diagnostics;

import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.console.AbstractPipedApplicationConsole;
import com.oracle.tools.runtime.console.PipedApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Functional Tests for {@link ContainerBasedJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class ContainerBasedJavaApplicationBuilderTest extends AbstractJavaApplicationBuilderTest
{
    @Override
    public JavaApplicationBuilder<JavaApplication> newJavaApplicationBuilder()
    {
        return new ContainerBasedJavaApplicationBuilder<JavaApplication>();
    }


    @Override
    public Platform getPlatform()
    {
        return JavaVirtualMachine.getInstance();
    }


    /**
     * Ensure that the {@link #newJavaApplicationBuilder()} method is producing
     * the expected type of builder.
     */
    @Test
    public void shouldBeCorrectJavaApplicationBuilder()
    {
        assertThat(newJavaApplicationBuilder(), is(instanceOf(ContainerBasedJavaApplicationBuilder.class)));
    }


    /**
     * Ensure that the system properties, including their mutation, is isolated
     * between applications and from the test itself.
     */
    @Test
    public void shouldIsolateApplicationSystemProperties() throws IOException
    {
        String propertyName = "message";
        String firstValue   = "gudday";
        String secondValue  = "bonjour";

        SimpleJavaApplicationSchema firstSchema =
            new SimpleJavaApplicationSchema(SystemPropertyMutatingApplication.class.getName())
                .addArguments(propertyName,
                              firstValue);

        try (PipedApplicationConsole firstConsole =
            new PipedApplicationConsole(AbstractPipedApplicationConsole.DEFAULT_PIPE_SIZE,
                                        false);
            JavaApplication firstApplication = getPlatform().realize("test-1",
                                                                     firstSchema,
                                                                     firstConsole,
                                                                     Diagnostics.enabled()))
        {
            BufferedReader firstOutput = firstConsole.getOutputReader();

            // assert that the property is not set in the first application
            assertThat(firstOutput.readLine(), is("Existing: null"));

            // assert that the property is now set in the first application
            assertThat(firstOutput.readLine(), is("Now: " + firstValue));

            SimpleJavaApplicationSchema secondSchema =
                new SimpleJavaApplicationSchema(SystemPropertyMutatingApplication.class.getName())
                    .addArguments(propertyName,
                                  secondValue);

            try (PipedApplicationConsole secondConsole =
                new PipedApplicationConsole(AbstractPipedApplicationConsole.DEFAULT_PIPE_SIZE,
                                            false);
                JavaApplication secondApplication = getPlatform().realize("test-2",
                                                                          secondSchema,
                                                                          secondConsole,
                                                                          Diagnostics.enabled()))
            {
                BufferedReader secondOutput = secondConsole.getOutputReader();

                // assert that the property is not set in the second application,
                // even though it is now set in the first application
                assertThat(secondOutput.readLine(), is("Existing: null"));

                // assert that the property is now set in the second application
                assertThat(secondOutput.readLine(), is("Now: " + secondValue));

                // now send some input to terminate the second application
                secondConsole.getInputWriter().println();

                secondApplication.waitFor();
            }

            // now send some input to terminate the first application
            firstConsole.getInputWriter().println();

            firstApplication.waitFor();
        }

        // assert that the property remains unset in this test
        assertThat(System.getProperty(propertyName), is(nullValue()));
    }


    /**
     * Ensure that a {@link RuntimeException} caused by a {@link ClassNotFoundException} is
     * thrown when a class doesn't exist.
     */
    @Test
    public void shouldThrowClassNotFoundExceptionWhenApplicationDoesntExist()
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema("ThisClassDoesntExist");

        try (JavaApplication application = getPlatform().realize("test",
                                                                 schema,
                                                                 new SystemApplicationConsole(),
                                                                 Diagnostics.enabled()))
        {
            // nothing to do here
        }
        catch (RuntimeException e)
        {
            assertThat(e.getCause(), instanceOf(ClassNotFoundException.class));
        }
    }


    /**
     * Ensure that a {@link RuntimeException} thrown by a {@link JavaApplication} is re-thrown.
     */
    @Test
    public void shouldThrowExceptionWhenApplicationFailsToStart()
    {
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(FaultyApplication.class.getName());

        try (JavaApplication application = getPlatform().realize("test",
                                                                 schema,
                                                                 new SystemApplicationConsole(),
                                                                 Diagnostics.enabled()))
        {
            // wait for the application to terminate
            application.waitFor();

            fail("We should have seen a RuntimeException");
        }
        catch (RuntimeException e)
        {
            // we're all good!
        }
    }
}
