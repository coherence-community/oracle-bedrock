/*
 * File: JavaVirtualMachineTest.java
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

import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.management.RuntimeMXBean;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link JavaVirtualMachine}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class JavaVirtualMachineTest
{
    @Test
    public void shouldReturnHostName() throws Exception
    {
        Platform platform = JavaVirtualMachine.getInstance();

        assertThat(platform.getPrivateInetAddress(), is(LocalPlatform.getInstance().getPrivateInetAddress()));
    }


    @Test
    public void shouldReturnJavaApplicationBuilder()
    {
        JavaVirtualMachine platform = JavaVirtualMachine.getInstance();

        ApplicationBuilder builder  = platform.getApplicationBuilder(JavaApplication.class);

        assertThat(builder, instanceOf(ContainerBasedJavaApplicationBuilder.class));
    }


    @Test
    public void shouldReturnLocalApplicationBuilder()
    {
        JavaVirtualMachine platform = JavaVirtualMachine.getInstance();

        ApplicationBuilder builder  = platform.getApplicationBuilder(SimpleApplication.class);

        assertThat(builder, is(nullValue()));
    }


    @Test
    public void shouldSetDebugModeToFalseBasedOnProcessArguments() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.getInstance();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String>       args          = Arrays.asList("foo", "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        assertThat(jvm.isRunningWithDebugger(), is(false));
    }


    @Test
    public void shouldSetDebugModeToTrueBasedOnProcessArguments() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.getInstance();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String> args = Arrays.asList("foo",
                                          "-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:52429,suspend=y,server=n",
                                          "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        assertThat(jvm.isRunningWithDebugger(), is(true));
    }


    @Test
    public void shouldNotRunWithDebugMode() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.getInstance();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String>       args          = Arrays.asList("foo", "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        assertThat(jvm.shouldEnableRemoteDebugging(), is(false));
    }


    @Test
    public void shouldRunWithDebugMode() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.getInstance();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String> args = Arrays.asList("foo",
                                          "-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:52429,suspend=y,server=n",
                                          "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        assertThat(jvm.shouldEnableRemoteDebugging(), is(true));
    }


    @Test
    public void shouldNotRunWithDebugModeIfAutoDebugModeIsFalse() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.getInstance();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String> args = Arrays.asList("foo",
                                          "-agentlib:jdwp=transport=dt_socket,address=127.0.0.1:52429,suspend=y,server=n",
                                          "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        jvm.setAutoDebugEnabled(false);

        try
        {
            assertThat(jvm.shouldEnableRemoteDebugging(), is(false));
        }
        finally
        {
            jvm.setAutoDebugEnabled(true);
        }
    }


    /**
     * Mock out the {@link JavaVirtualMachine#INSTANCE} so we can
     * mock its methods
     */
    @Before
    public void mockJavaVirtualMachine() throws Exception
    {
        JavaVirtualMachineMockHelper.mockJavaVirtualMachine();
    }


    /**
     * Restore the {@link JavaVirtualMachine#INSTANCE}
     */
    @After
    public void restoreJavaVirtualMachine() throws Exception
    {
        JavaVirtualMachineMockHelper.restoreJavaVirtualMachine();
    }
}
