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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.java.profiles.RemoteDebugging;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    /**
     * Test that remote debug mode is set to the value returned
     * from {@link JavaVirtualMachine#shouldEnableRemoteDebugging()}
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldDefaultDebugModeToFalseFromJVM() throws Exception
    {
        JavaVirtualMachine jvm = JavaVirtualMachine.get();

        when(jvm.shouldEnableRemoteDebugging()).thenReturn(false);

        RemoteDebugging remoteDebugging = RemoteDebugging.autoDetect();

        Assert.assertThat(remoteDebugging.isEnabled(), is(false));
        verify(jvm, atLeastOnce()).shouldEnableRemoteDebugging();
    }


    /**
     * Test that remote debug mode is set to the value returned
     * from {@link JavaVirtualMachine#shouldEnableRemoteDebugging()}
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldDefaultDebugModeToTrueFromJVM() throws Exception
    {
        JavaVirtualMachine jvm = JavaVirtualMachine.get();

        when(jvm.shouldEnableRemoteDebugging()).thenReturn(true);

        RemoteDebugging remoteDebugging = RemoteDebugging.autoDetect();

        Assert.assertThat(remoteDebugging.isEnabled(), is(true));
        verify(jvm, atLeastOnce()).shouldEnableRemoteDebugging();
    }


    @Test
    public void shouldReturnLocalPlatformAddress() throws Exception
    {
        Platform platform = JavaVirtualMachine.get();

        assertThat(platform.getAddress(), is(LocalPlatform.get().getAddress()));
    }


    @Test
    public void shouldSetDebugModeToFalseBasedOnProcessArguments() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.get();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String>       args          = Arrays.asList("foo", "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        assertThat(jvm.isRunningWithDebugger(), is(false));
    }


    @Test
    public void shouldSetDebugModeToTrueBasedOnProcessArguments() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.get();
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
        JavaVirtualMachine jvm           = JavaVirtualMachine.get();
        RuntimeMXBean      runtimeMXBean = mock(RuntimeMXBean.class);
        List<String>       args          = Arrays.asList("foo", "bar");

        when(jvm.getRuntimeMXBean()).thenReturn(runtimeMXBean);
        when(runtimeMXBean.getInputArguments()).thenReturn(args);

        assertThat(jvm.shouldEnableRemoteDebugging(), is(false));
    }


    @Test
    public void shouldRunWithDebugMode() throws Exception
    {
        JavaVirtualMachine jvm           = JavaVirtualMachine.get();
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
        JavaVirtualMachine jvm           = JavaVirtualMachine.get();
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
