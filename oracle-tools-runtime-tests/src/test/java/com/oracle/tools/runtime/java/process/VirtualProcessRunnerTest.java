/*
 * File: VirtualProcessRunnerTest.java
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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit and Functional Tests for the VirtualProcessRunner.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VirtualProcessRunnerTest extends AbstractTest
{
    /**
     * Method description
     */
    @Before
    public void clearProcessRunnableStub()
    {
        VirtualProcessRunnableStub.argsUsed = null;
        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = null;
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCallStaticMethod() throws Exception
    {
        VirtualizedSystemClassLoader classLoader = new VirtualizedSystemClassLoader();
        String                       className   = VirtualProcessRunnableStub.class.getCanonicalName();
        String                       methodName  = VirtualProcessRunnableStub.METHOD_STATIC_START;
        Object                       result      = new Object();
        List<String>                 args        = Arrays.asList("1", "2");

        VirtualProcessMethodInvoker  runner = new VirtualProcessMethodInvoker(classLoader, className, methodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = result;
        runner.run();

        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(methodName)));
        assertThat(runner.isFinished(), is(true));
        assertThat(runner.<Object>getResult(), sameInstance(result));
        assertThat(runner.getInstance(), CoreMatchers.nullValue());
        assertThat(runner.getError(), CoreMatchers.nullValue());
        assertThat(VirtualProcessRunnableStub.argsUsed, is(arrayContaining("1", "2")));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCallStaticNoArgsMethod() throws Exception
    {
        VirtualizedSystemClassLoader classLoader = new VirtualizedSystemClassLoader();
        String                       className   = VirtualProcessRunnableStub.class.getCanonicalName();
        String                       methodName  = VirtualProcessRunnableStub.METHOD_STATIC_START_NO_ARGS;
        Object                       result      = new Object();
        List<String>                 args        = Arrays.asList("1", "2");

        VirtualProcessMethodInvoker  runner = new VirtualProcessMethodInvoker(classLoader, className, methodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = result;
        runner.run();

        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(methodName)));
        assertThat(runner.isFinished(), is(true));
        assertThat(runner.<Object>getResult(), sameInstance(result));
        assertThat(runner.getInstance(), CoreMatchers.nullValue());
        assertThat(runner.getError(), CoreMatchers.nullValue());
        assertThat(VirtualProcessRunnableStub.argsUsed, is(nullValue()));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCallNonStaticMethod() throws Exception
    {
        VirtualizedSystemClassLoader classLoader = new VirtualizedSystemClassLoader();
        String                       className   = VirtualProcessRunnableStub.class.getCanonicalName();
        String                       methodName  = VirtualProcessRunnableStub.METHOD_START;
        Object                       result      = new Object();
        List<String>                 args        = Arrays.asList("3", "4");

        VirtualProcessMethodInvoker  runner = new VirtualProcessMethodInvoker(classLoader, className, methodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = result;
        runner.run();

        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(methodName)));
        assertThat(runner.<Object>getResult(), sameInstance(result));
        assertThat(runner.getInstance(), instanceOf(VirtualProcessRunnableStub.class));
        assertThat(runner.getError(), CoreMatchers.nullValue());
        assertThat(VirtualProcessRunnableStub.argsUsed, is(arrayContaining("3", "4")));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCallNonStaticNoArgsMethod() throws Exception
    {
        VirtualizedSystemClassLoader classLoader = new VirtualizedSystemClassLoader();
        String                       className   = VirtualProcessRunnableStub.class.getCanonicalName();
        String                       methodName  = VirtualProcessRunnableStub.METHOD_START_NO_ARGS;
        Object                       result      = new Object();
        List<String>                 args        = Arrays.asList("3", "4");

        VirtualProcessMethodInvoker  runner = new VirtualProcessMethodInvoker(classLoader, className, methodName, args);

        VirtualProcessRunnableStub.methodsCalled.clear();
        VirtualProcessRunnableStub.result = result;
        runner.run();

        assertThat(VirtualProcessRunnableStub.methodsCalled, is(Collections.singletonList(methodName)));
        assertThat(runner.<Object>getResult(), sameInstance(result));
        assertThat(runner.getInstance(), instanceOf(VirtualProcessRunnableStub.class));
        assertThat(runner.getError(), CoreMatchers.nullValue());
        assertThat(VirtualProcessRunnableStub.argsUsed, is(nullValue()));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedInstanceToCallNonStaticMethod() throws Exception
    {
        VirtualizedSystemClassLoader classLoader = new VirtualizedSystemClassLoader();
        String                       className   = VirtualProcessRunnableStub.class.getCanonicalName();
        String                       methodName  = VirtualProcessRunnableStub.METHOD_START;
        Object                       result      = new Object();
        List<String>                 args        = Arrays.asList("A", "B");

        VirtualProcessRunnableStub   instance    = new VirtualProcessRunnableStub();

        VirtualProcessRunnableStub.result = result;

        VirtualProcessMethodInvoker runner = new VirtualProcessMethodInvoker(classLoader,
                                                                             className,
                                                                             methodName,
                                                                             args,
                                                                             instance);

        runner.run();

        assertThat(runner.<Object>getResult(), sameInstance(result));
        assertThat((VirtualProcessRunnableStub) runner.getInstance(), sameInstance(instance));
        assertThat(runner.getError(), CoreMatchers.nullValue());
        assertThat(VirtualProcessRunnableStub.methodsCalled,
                   is(Arrays.asList(VirtualProcessRunnableStub.METHOD_START)));
        assertThat(VirtualProcessRunnableStub.argsUsed, is(arrayContaining("A", "B")));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetErrorIfExceptionThrown() throws Exception
    {
        VirtualizedSystemClassLoader classLoader = new VirtualizedSystemClassLoader();
        String                       className   = "No_Such_Class";
        String                       methodName  = "No_Such_Method";
        List<String>                 args        = Arrays.asList();

        VirtualProcessMethodInvoker  runner = new VirtualProcessMethodInvoker(classLoader, className, methodName, args);

        runner.run();

        assertThat(runner.getError(), instanceOf(RuntimeException.class));
        assertThat(runner.getError().getCause(), instanceOf(ClassNotFoundException.class));
    }
}
