/*
 * File: JavaApplicationBuilderTest.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.runtime.ApplicationConsole;

import com.oracle.tools.runtime.java.process.ExternalProcessBuilder;
import com.oracle.tools.runtime.java.process.JavaProcessBuilder;
import com.oracle.tools.runtime.java.process.VirtualProcessBuilder;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

/**
 * Unit Tests for {@link JavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class JavaApplicationBuilderTest extends AbstractTest
{
    /**
     * Ensure that we can create an {@link ExternalProcessBuilder}
     * when using an {@link ExternalJavaApplicationBuilder}.
     *
     * @throws Exception
     */
    @Test
    public void testCreateExternalProcessBuilder() throws Exception
    {
        String                      applicationName = "Test Application";

        ApplicationConsole          console         = mock(ApplicationConsole.class);

        SimpleJavaApplicationSchema schema          = mock(SimpleJavaApplicationSchema.class);

        when(schema.getApplicationClassName()).thenReturn("com.oracle.Test");

        ExternalJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new ExternalJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        JavaProcessBuilder processBuilder = builder.createJavaProcessBuilder(schema, applicationName, console);

        assertThat(processBuilder, is(instanceOf(ExternalProcessBuilder.class)));
        assertThat(processBuilder.getApplicationClassName(), is("com.oracle.Test"));
    }


    /**
     * Ensure that we can create an {@link VirtualProcessBuilder}
     * when using an {@link VirtualizedJavaApplicationBuilder}.
     *
     * @throws Exception
     */
    @Test
    public void testCreateInternalProcessBuilder() throws Exception
    {
        String                      applicationName = "Test Application";

        ApplicationConsole          console         = mock(ApplicationConsole.class);

        SimpleJavaApplicationSchema schema          = mock(SimpleJavaApplicationSchema.class);

        when(schema.getApplicationClassName()).thenReturn("com.oracle.Test");

        List<String> arguments = Arrays.asList(new String[] {"arg-1", "arg-2"});

        when(schema.getArguments()).thenReturn(arguments);

        VirtualizedJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new VirtualizedJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        JavaProcessBuilder processBuilder = builder.createJavaProcessBuilder(schema, applicationName, console);

        processBuilder.addArguments(arguments);

        assertThat(processBuilder, is(instanceOf(VirtualProcessBuilder.class)));
        assertThat(processBuilder.getApplicationClassName(), is("com.oracle.Test"));
        assertThat(processBuilder.getArguments(), is(arguments));
    }


    /**
     * Ensure that the start and stop methods are propagated to {@link VirtualProcessBuilder}s.
     *
     * @throws Exception
     */
    @Test
    public void testCreateInternalProcessBuilderWithCorrectStartAndStopMethods() throws Exception
    {
        String                      applicationName = "Test Application";

        ApplicationConsole          console         = mock(ApplicationConsole.class);

        SimpleJavaApplicationSchema schema          = mock(SimpleJavaApplicationSchema.class);

        when(schema.getApplicationClassName()).thenReturn("com.oracle.Test");
        when(schema.getStartMethodName()).thenReturn("startMe");
        when(schema.getStopMethodName()).thenReturn("stopMe");

        VirtualizedJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new VirtualizedJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        JavaProcessBuilder processBuilder = builder.createJavaProcessBuilder(schema, applicationName, console);

        assertThat(processBuilder, is(instanceOf(VirtualProcessBuilder.class)));

        VirtualProcessBuilder internalBuilder = (VirtualProcessBuilder) processBuilder;

        assertThat(internalBuilder.getStartMethodName(), is("startMe"));
        assertThat(internalBuilder.getStopMethodName(), is("stopMe"));
    }

//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldClearEnvironmentByDefault() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//      Map<String, String>      environment    = mock(Map.class);
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//      when(processBuilder.getEnvironment()).thenReturn(environment);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema("com.oracle.Test");
//
//      builder.realize(schema, name, console);
//      verify(environment).clear();
//  }
//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldClearEnvironmentIfCloneEnvironmentIsFalse() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//      Map<String, String>      environment    = mock(Map.class);
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//      when(processBuilder.getEnvironment()).thenReturn(environment);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema("com.oracle.Test").setIsInherited(false);
//
//      builder.realize(schema, name, console);
//      verify(environment).clear();
//  }
//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldNotClearEnvironmentIfCloneEnvironmentIsTrue() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//      Map<String, String>      environment    = mock(Map.class);
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//      when(processBuilder.getEnvironment()).thenReturn(environment);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema("com.oracle.Test").setIsInherited(true);
//
//      builder.realize(schema, name, console);
//      verify(environment, never()).clear();
//  }
//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldAddEnvironmentVariables() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//      Map<String, String>      environment    = new HashMap<String, String>();
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//      when(processBuilder.getEnvironment()).thenReturn(environment);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema("com.oracle.Test");
//
//      schema.getEnvironmentVariablesBuilder().setProperty("env1", "value1").setProperty("env2", "value2");
//
//      builder.realize(schema, name, console);
//      assertThat(environment.get("env1"), is("value1"));
//      assertThat(environment.get("env2"), is("value2"));
//  }
//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldAddClasspathToEnvironmentVariables() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//      Map<String, String>      environment    = new HashMap<String, String>();
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//      when(processBuilder.getEnvironment()).thenReturn(environment);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema =
//          new SimpleJavaApplicationSchema("com.oracle.Test").setClassPath("oracle.jar;coherence.jar");
//
//      builder.realize(schema, name, console);
//      assertThat(environment.get("CLASSPATH"), is("oracle.jar;coherence.jar"));
//  }
//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldSetSystemProperties() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema =
//          new SimpleJavaApplicationSchema("com.oracle.Test").setSystemProperty("prop1",
//                                                                               "value1")
//                                                                                   .setSystemProperty("prop2", "value2");
//
//      builder.realize(schema, name, console);
//      assertThat(properties.size(), is(2));
//      assertThat(properties.getProperty("prop1"), is("value1"));
//      assertThat(properties.getProperty("prop2"), is("value2"));
//  }
//
//
//  /**
//   * Method description
//   *
//   * @throws Exception
//   */
//  @SuppressWarnings({"unchecked"})
//  @Test
//  public void shouldAddArguments() throws Exception
//  {
//      String                   name           = "Test";
//      ApplicationConsole       console        = mock(ApplicationConsole.class);
//      final JavaProcessBuilder processBuilder = mock(JavaProcessBuilder.class);
//      Process                  process        = mock(Process.class);
//      Properties               properties     = new Properties();
//
//      when(processBuilder.getSystemProperties()).thenReturn(properties);
//      when(processBuilder.realize()).thenReturn(process);
//
//      AbstractJavaApplicationBuilder builder = new AbstractJavaApplicationBuilderStubWithBuilders(processBuilder,
//                                                                                                  null);
//
//      SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema("com.oracle.Test");
//      List<String>                args   = schema.getArguments();
//
//      args.add("arg1");
//      args.add("arg2");
//
//      builder.realize(schema, name, console);
//      inOrder(processBuilder).verify(processBuilder).addArgument("arg1");
//      inOrder(processBuilder).verify(processBuilder).addArgument("arg2");
//  }
}
