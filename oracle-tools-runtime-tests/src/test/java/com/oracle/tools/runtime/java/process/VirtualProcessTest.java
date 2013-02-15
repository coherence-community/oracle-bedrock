/*
 * File: VirtualProcessTest.java
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

package com.oracle.tools.runtime.java.process;

import com.oracle.tools.junit.AbstractTest;
import com.oracle.tools.runtime.java.virtualization.VirtualizedSystemClassLoader;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit and Functional Tests for {@link VirtualProcess}es.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VirtualProcessTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldStartProcess() throws Exception
    {
        final Class                        clazz           = VirtualProcessRunnableStub.class;
        final String                       className       = clazz.getCanonicalName();
        final String                       startMethodName = VirtualProcessRunnableStub.METHOD_STATIC_START;
        final String                       stopMethodName  = VirtualProcessRunnableStub.METHOD_STATIC_STOP;
        final List<String>                 args            = Arrays.asList();

        final VirtualizedSystemClassLoader classLoader     = mock(VirtualizedSystemClassLoader.class);

        when(classLoader.loadClass(className)).thenReturn(clazz);

        VirtualProcess process = new VirtualProcess(className, classLoader, startMethodName, stopMethodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        process.start();

        verify(classLoader).loadClass(className);
        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(startMethodName)));

        VirtualProcessMethodInvoker runnable = process.getStartRunnable();

        assertThat(runnable.getClassLoader(), sameInstance(classLoader));
        assertThat(runnable.getClassName(), is(className));
        assertThat(runnable.getMethodName(), is(startMethodName));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldStartProcessOnlyOnce() throws Exception
    {
        final Class                        clazz           = VirtualProcessRunnableStub.class;
        final String                       className       = clazz.getCanonicalName();
        final String                       startMethodName = VirtualProcessRunnableStub.METHOD_STATIC_START;
        final String                       stopMethodName  = VirtualProcessRunnableStub.METHOD_STATIC_STOP;
        final List<String>                 args            = Arrays.asList();

        final VirtualizedSystemClassLoader classLoader     = mock(VirtualizedSystemClassLoader.class);

        when(classLoader.loadClass(className)).thenReturn(clazz);

        VirtualProcess process = new VirtualProcess(className, classLoader, startMethodName, stopMethodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        process.start();

        VirtualProcessMethodInvoker runner = process.getStartRunnable();

        process.start();

        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(startMethodName)));
        assertThat(process.getStartRunnable(), sameInstance(runner));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldStopProcess() throws Exception
    {
        final Class                        clazz           = VirtualProcessRunnableStub.class;
        final String                       className       = clazz.getCanonicalName();
        final String                       startMethodName = VirtualProcessRunnableStub.METHOD_STATIC_START;
        final String                       stopMethodName  = VirtualProcessRunnableStub.METHOD_STATIC_STOP;
        final List<String>                 args            = Arrays.asList();

        final VirtualizedSystemClassLoader classLoader     = mock(VirtualizedSystemClassLoader.class);

        when(classLoader.loadClass(className)).thenReturn(clazz);

        VirtualProcess process = new VirtualProcess(className, classLoader, startMethodName, stopMethodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        process.destroy();

        verify(classLoader).loadClass(className);
        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(stopMethodName)));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldCallStopOnCorrectInstance() throws Exception
    {
        final Class                        clazz           = VirtualProcessRunnableStub.class;
        final String                       className       = clazz.getCanonicalName();
        final String                       startMethodName = VirtualProcessRunnableStub.METHOD_START;
        final String                       stopMethodName  = VirtualProcessRunnableStub.METHOD_STOP;
        final List<String>                 args            = Arrays.asList();

        final VirtualizedSystemClassLoader classLoader     = mock(VirtualizedSystemClassLoader.class);

        when(classLoader.loadClass(className)).thenReturn(clazz);

        VirtualProcessRunnableStub  stub   = mock(VirtualProcessRunnableStub.class);
        VirtualProcessMethodInvoker runner = mock(VirtualProcessMethodInvoker.class);

        when(runner.getInstance()).thenReturn(stub);

        VirtualProcess process = new VirtualProcess(className, classLoader, startMethodName, stopMethodName, args);

        process.setStartRunnable(runner);
        process.destroy();

        verify(stub).stop();
        verify(classLoader).loadClass(className);
        assertThat(process.getStartRunnable(), CoreMatchers.nullValue());
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldInvokeCorrectStaticMethodOnCorrectClass() throws Exception
    {
        final Class                        clazz       = VirtualProcessRunnableStub.class;
        final String                       className   = clazz.getCanonicalName();
        final String                       methodName  = VirtualProcessRunnableStub.METHOD_STATIC_START;

        final VirtualizedSystemClassLoader classLoader = mock(VirtualizedSystemClassLoader.class);

        when(classLoader.loadClass(className)).thenReturn(clazz);

        VirtualProcess process = new VirtualProcess(null, classLoader, null, null, null);

        Object         result  = new Object();

        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = result;

        Object invokeResult = process.invoke(className, methodName);

        verify(classLoader).loadClass(className);
        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(methodName)));
        assertThat(invokeResult, sameInstance(result));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldInvokeCorrectNonStaticMethodOnCorrectClass() throws Exception
    {
        final Class                        clazz       = VirtualProcessRunnableStub.class;
        final String                       className   = clazz.getCanonicalName();
        final String                       methodName  = VirtualProcessRunnableStub.METHOD_START;

        final VirtualizedSystemClassLoader classLoader = mock(VirtualizedSystemClassLoader.class);

        when(classLoader.loadClass(className)).thenReturn(clazz);

        VirtualProcess process = new VirtualProcess(null, classLoader, null, null, null);

        Object         result  = new Object();

        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = result;

        Object invokeResult = process.invoke(className, methodName);

        verify(classLoader).loadClass(className);
        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(methodName)));
        assertThat(invokeResult, sameInstance(result));
    }
}
