/*
 * File: ContainerBasedJavaApplicationProcessTest.java
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

package com.oracle.bedrock.runtime.java.container;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.junit.AbstractTest;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteCallableStaticMethod;
import com.oracle.bedrock.runtime.java.ContainerBasedJavaApplicationLauncher;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Functional Tests for {@link ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess}es.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class ContainerBasedJavaApplicationProcessTest extends AbstractTest
{
    /**
     * Setup to occur before each test method.
     */
    @Before
    public void onBeforeEachTest()
    {
        InvocationTracingApplication.initialize();
    }


    /**
     * Ensure that the standard application main method is called.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartStandardApplication() throws Exception
    {
        ContainerClassLoader classLoader = ContainerClassLoader.newInstance("shouldStartStandardApplication");

        String               className   = InvocationTracingApplication.class.getCanonicalName();
        String               methodName  = InvocationTracingApplication.METHOD_STATIC_MAIN;
        List<String>         arguments   = Arrays.asList("1", "2");

        try (ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess process =
            new ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess(classLoader,
                                                                                           new ContainerBasedJavaApplicationLauncher
                                                                                               .StandardController(className,
                                                                                                   arguments),
                                                                                           new Properties()))
        {
            process.start(OptionsByType.empty());
            process.waitFor();

            Iterator<InvocationTracingApplication.MethodInvocation> iterator =
                InvocationTracingApplication.getMethodInvocations().iterator();

            assertTrue(iterator.hasNext());

            InvocationTracingApplication.MethodInvocation methodInvocation = iterator.next();

            assertFalse(iterator.hasNext());

            assertThat(methodInvocation.getMethodName(), is(methodName));
            assertThat(methodInvocation.getArguments(), is(arrayContaining((Object) "1", (Object) "2")));
        }
    }


    /**
     * Ensure that the custom application start method is called.
     *
     * @throws Exception
     */
    @Test
    public void shouldStartCustomApplication() throws Exception
    {
        ContainerClassLoader             classLoader = ContainerClassLoader.newInstance("shouldStartCustomApplication");

        String                           className   = InvocationTracingApplication.class.getCanonicalName();
        String                           methodName  = InvocationTracingApplication.METHOD_STATIC_START;
        List<String>                     arguments   = Arrays.asList("1", "2", "3");
        RemoteCallableStaticMethod<Void> callable = new RemoteCallableStaticMethod<>(className, methodName, arguments);

        try (ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess process =
            new ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess(classLoader,
                                                                                           new ContainerBasedJavaApplicationLauncher
                                                                                               .CustomController(callable),
                                                                                           new Properties()))
        {
            process.start(OptionsByType.empty());
            process.waitFor();

            Iterator<InvocationTracingApplication.MethodInvocation> iterator =
                InvocationTracingApplication.getMethodInvocations().iterator();

            assertTrue(iterator.hasNext());

            InvocationTracingApplication.MethodInvocation methodInvocation = iterator.next();

            assertFalse(iterator.hasNext());

            assertThat(methodInvocation.getMethodName(), is(methodName));
            assertThat(methodInvocation.getArguments(), is(arrayContaining((Object) "1", (Object) "2", (Object) "3")));
        }
    }


    /**
     * Ensure that the custom application start method is called (without arguments)
     *
     * @throws Exception
     */
    @Test
    public void shouldStartCustomApplicationWithoutArguments() throws Exception
    {
        ContainerClassLoader classLoader =
            ContainerClassLoader.newInstance("shouldStartCustomApplicationWithoutArguments");

        String                           className  = InvocationTracingApplication.class.getCanonicalName();
        String                           methodName = InvocationTracingApplication.METHOD_STATIC_START_NO_ARGS;
        RemoteCallableStaticMethod<Void> callable   = new RemoteCallableStaticMethod<>(className, methodName);

        try (ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess process =
            new ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess(classLoader,
                                                                                           new ContainerBasedJavaApplicationLauncher
                                                                                               .CustomController(callable),
                                                                                           new Properties()))
        {
            process.start(OptionsByType.empty());
            process.waitFor();

            Iterator<InvocationTracingApplication.MethodInvocation> iterator =
                InvocationTracingApplication.getMethodInvocations().iterator();

            assertTrue(iterator.hasNext());

            InvocationTracingApplication.MethodInvocation methodInvocation = iterator.next();

            assertFalse(iterator.hasNext());

            assertThat(methodInvocation.getMethodName(), is(methodName));
            assertThat(methodInvocation.getArguments(), is(nullValue()));
        }
    }


    /**
     * Ensure that the custom application stop method is called (without arguments)
     *
     * @throws Exception
     */
    @Test
    public void shouldStopCustomApplicationWithoutArguments() throws Exception
    {
        ContainerClassLoader classLoader =
            ContainerClassLoader.newInstance("shouldStopCustomApplicationWithoutArguments");

        String                           className  = InvocationTracingApplication.class.getCanonicalName();
        String                           methodName = InvocationTracingApplication.METHOD_STATIC_STOP_NO_ARGS;
        RemoteCallableStaticMethod<Void> callable   = new RemoteCallableStaticMethod<>(className, methodName);

        try (ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess process =
            new ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess(classLoader,
                                                                                           new ContainerBasedJavaApplicationLauncher
                                                                                               .CustomController(null,
                                                                                                   callable),
                                                                                           new Properties()))
        {
            process.start(OptionsByType.empty());

            assertFalse(InvocationTracingApplication.getMethodInvocations().iterator().hasNext());

            process.close();

            Iterator<InvocationTracingApplication.MethodInvocation> iterator =
                InvocationTracingApplication.getMethodInvocations().iterator();

            assertTrue(iterator.hasNext());

            InvocationTracingApplication.MethodInvocation methodInvocation = iterator.next();

            assertThat(methodInvocation.getMethodName(), is(methodName));
            assertThat(methodInvocation.getArguments(), is(nullValue()));
        }
    }


    /**
     * Ensure that failing to start a custom application raises an exception
     *
     * @throws Exception
     */
    @Test
    public void shouldNotStartCustomApplicationWithoutArguments() throws Exception
    {
        ContainerClassLoader classLoader =
            ContainerClassLoader.newInstance("shouldNotStartCustomApplicationWithoutArguments");

        String                           className  = InvocationTracingApplication.class.getCanonicalName();
        String                           methodName = "method_does_not_exist";
        RemoteCallableStaticMethod<Void> callable   = new RemoteCallableStaticMethod<>(className, methodName);

        try (ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess process =
            new ContainerBasedJavaApplicationLauncher.ContainerBasedJavaApplicationProcess(classLoader,
                                                                                           new ContainerBasedJavaApplicationLauncher
                                                                                               .CustomController(callable),
                                                                                           new Properties()))
        {
            process.start(OptionsByType.empty());
            process.waitFor();

            fail("Should not have started the process");
        }
        catch (RuntimeException e)
        {
            assertThat(e.getCause(), is(instanceOf(NoSuchMethodException.class)));
        }
    }
}
