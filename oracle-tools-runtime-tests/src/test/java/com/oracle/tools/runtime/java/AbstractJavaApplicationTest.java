/*
 * File: AbstractJavaApplicationTest.java
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

import classloader.applications.SleepingApplication;
import classloader.applications.Tester;
import classloader.applications.TesterApplication;
import classloader.applications.TesterProducer;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.lang.StringHelper;

import com.oracle.tools.options.Diagnostics;

import com.oracle.tools.runtime.DummyApp;
import com.oracle.tools.runtime.DummyClassPathApp;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;

import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.console.PipedApplicationConsole;

import com.oracle.tools.runtime.java.options.ClassName;
import com.oracle.tools.runtime.java.options.IPv4Preferred;
import com.oracle.tools.runtime.java.options.SystemProperty;

import com.oracle.tools.runtime.java.profiles.RemoteDebugging;
import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.DisplayName;

import com.oracle.tools.util.FutureCompletionListener;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.nullValue;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.UUID;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Integration Tests for {@link JavaApplication} launched across various {@link Platform}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public abstract class AbstractJavaApplicationTest<P extends Platform> extends AbstractTest
{
    /**
     * Obtains the {@link Platform} {@link JavaApplication}s will be launched.
     *
     * @return  the {@link Platform}
     */
    public abstract P getPlatform();


    /**
     * Ensure that we can start and terminate a {@link JavaApplication}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        PipedApplicationConsole console = new PipedApplicationConsole();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                DisplayName.of("java-app"),
                                                                ClassName.of(DummyApp.class),
                                                                Arguments.of("arg1", "arg2"),
                                                                SystemProperty.of("test.prop.1", "value.1"),
                                                                SystemProperty.of("test.prop.2", "value.2"),
                                                                IPv4Preferred.yes(),
                                                                Diagnostics.enabled(),
                                                                Console.of(console)))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout.startsWith("[java-app:"), is(true));
            assertThat(stdout, containsString("arg1,arg2"));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("test.prop.1=value.1"));
        }
    }


    /**
     * Ensure that we can start and terminate a {@link JavaApplication} with a customized {@link ClassPath}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplicationWithRestrictedClasspath() throws Exception
    {
        ClassPath               knownJarClassPath = ClassPath.ofResource("asm-license.txt");
        Class<Mock>             knownClass        = Mock.class;

        ClassPath               path1             = ClassPath.ofClass(DummyClassPathApp.class);
        ClassPath               path2             = ClassPath.ofClass(StringHelper.class);
        ClassPath               classPath         = new ClassPath(knownJarClassPath, path1, path2);

        PipedApplicationConsole console           = new PipedApplicationConsole();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                DisplayName.of("java-app"),
                                                                ClassName.of(DummyClassPathApp.class),
                                                                Arguments.of(knownClass.getCanonicalName()),
                                                                classPath,
                                                                IPv4Preferred.yes(),
                                                                Diagnostics.enabled(),
                                                                Console.of(console)))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString(knownJarClassPath.iterator().next()));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString(path1.iterator().next()));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString(path2.iterator().next()));
        }
    }


    /**
     * Ensure that we can create {@link JavaApplication}s that can have {@link Callable}s
     * submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable()
    {
        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                SystemProperty.of("uuid", uuid)))
        {
            Eventually.assertThat(application, new GetSystemProperty("uuid"), is(uuid));
        }
    }


    /**
     * Ensure that a {@link Callable} that raises an exception is re-raised as an {@link ExecutionException}.
     */
    @Test
    public void shouldRaiseExecutionExceptionFromCallable()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class)))
        {
            application.submit(new ErrorThrowingCallable("Submission Failed"));

            fail();
        }
        catch (RuntimeException e)
        {
            Assert.assertThat(e.getCause().getCause().getMessage(), is("Submission Failed"));
        }
    }


    /**
     * Ensure that we can use a {@link RemoteCallableStaticMethod} with a {@link JavaApplication}.
     */
    @Test
    public void shouldCallRemoteStaticMethodsInAnApplication() throws InterruptedException, ExecutionException
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(TesterApplication.class),
                                                                IPv4Preferred.yes()))
        {
            RemoteCallable<Integer> callable = new RemoteCallableStaticMethod<>(TesterApplication.class.getName(),
                                                                                "getMeaningOfLife");

            FutureCompletionListener<Integer> future = new FutureCompletionListener<>();

            application.submit(callable, future);

            assertThat(future.get(), is(42));

            application.waitFor();
        }
    }


    /**
     * Ensure that we can create a local proxy of a {@link Tester} in an {@link JavaApplication}.
     */
    @Test
    public void shouldProxyTesterInApplication()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(TesterApplication.class),
                                                                IPv4Preferred.yes()))
        {
            Tester tester = application.getProxyFor(Tester.class, new TesterProducer(), null);

            tester.doNothing();

            assertThat(tester.getMeaningOfLife(), is("42"));
            assertThat(tester.identity(42), is(42));

            application.waitFor();
        }
    }


    /**
     * Ensure that we can wait for {@link JavaApplication}s to terminate.
     */
    @Test(timeout = 10000)
    public void shouldWaitFor()
    {
        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                Arguments.of("5"),
                                                                IPv4Preferred.yes()))
        {
            application.waitFor();
        }
    }


    /**
     * Ensure that we can define a {@link SystemProperty} in the {@link JavaApplication}
     * (and it won't be defined in this process).
     */
    @Test
    public void shouldDefineSystemPropertyInApplication()
    {
        // the custom property name and value
        String propertyName  = "custom.property";
        String propertyValue = "gudday";

        try (JavaApplication application = getPlatform().launch(JavaApplication.class,
                                                                ClassName.of(SleepingApplication.class),
                                                                SystemProperty.of(propertyName, propertyValue),
                                                                IPv4Preferred.yes()))
        {
            // assert that the custom property is defined in the application
            Eventually.assertThat(application, new GetSystemProperty(propertyName), is(propertyValue));

            // assert that the custom property is not defined in the test
            assertThat(System.getProperty(propertyName), is(nullValue()));
        }
    }
}
