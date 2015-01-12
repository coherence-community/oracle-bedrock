/*
 * File: AbstractJavaApplicationBuilderTest.java
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

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.DummyApp;
import com.oracle.tools.runtime.DummyClassPathApp;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;

import com.oracle.tools.runtime.console.PipedApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.util.FutureCompletionListener;

import org.junit.Test;

import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;

import java.util.UUID;

import java.util.concurrent.ExecutionException;

/**
 * Functional Tests for {@link JavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public abstract class AbstractJavaApplicationBuilderTest extends AbstractTest
{
    /**
     * Creates a new {@link JavaApplicationBuilder} to use for a tests in this
     * class and/or sub-classes.
     *
     * @return the {@link JavaApplicationBuilder}
     */
    public abstract JavaApplicationBuilder<JavaApplication> newJavaApplicationBuilder();


    /**
     * Obtains the {@link Platform} on which to realize applications.
     *
     * @return  the {@link Platform} on which to realize applications
     */
    public abstract Platform getPlatform();


    /**
     * Ensure that we can start and terminate a {@link JavaApplication}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyApp.class.getCanonicalName()).addArguments("arg1",
                                                                                            "arg2").setSystemProperty("test.prop.1", "value.1")
                                                                                                .setSystemProperty("test.prop.2", "value.2");

        schema.setPreferIPv4(true);

        JavaApplicationBuilder<JavaApplication> builder  = newJavaApplicationBuilder();
        Platform                                platform = getPlatform();

        PipedApplicationConsole                 console  = new PipedApplicationConsole();

        try (JavaApplication application = builder.realize(schema,
                                                           "java-app",
                                                           console,
                                                           platform,
                                                           Diagnostics.enabled()))
        {
            String stdout = console.getOutputReader().readLine();

            assertThat(stdout.startsWith("[java-app:"), is(true));
            assertThat(stdout, containsString("arg1,arg2"));

            stdout = console.getOutputReader().readLine();

            assertThat(stdout, containsString("test.prop.1=value.1"));
        }
    }


    /**
     * Ensure that we can start and terminate an external Java-based
     * Application with a customized ClassPath.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplicationWithRestrictedClasspath() throws Exception
    {
        ClassPath   knownJarClassPath = ClassPath.ofResource("asm-license.txt");
        Class<Mock> knownClass        = Mock.class;

        ClassPath   path1             = ClassPath.ofClass(DummyClassPathApp.class);
        ClassPath   path2             = ClassPath.ofClass(StringHelper.class);
        ClassPath   classPath         = new ClassPath(knownJarClassPath, path1, path2);

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyClassPathApp.class.getCanonicalName()).setClassPath(classPath)
                .addArgument(knownClass.getCanonicalName());

        schema.setPreferIPv4(true);

        JavaApplicationBuilder<JavaApplication> builder  = newJavaApplicationBuilder();
        Platform                                platform = getPlatform();

        PipedApplicationConsole                 console  = new PipedApplicationConsole();

        try (JavaApplication application = builder.realize(schema,
                                                           "java-app",
                                                           console,
                                                           platform,
                                                           Diagnostics.enabled()))
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
     * Ensure that we can create applications that can have {@link java.util.concurrent.Callable}s
     * submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable()
    {
        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        schema.setSystemProperty("uuid", uuid);

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        try (JavaApplication application = builder.realize(schema, "application", console))
        {
            Eventually.assertThat(application, new GetSystemProperty("uuid"), is(uuid));

        }
    }


    /**
     * Ensure that we can use a {@link RemoteCallableStaticMethod} with a {@link JavaApplication}.
     */
    @Test
    public void shouldCallRemoteStaticMethodsInAnApplication() throws InterruptedException, ExecutionException
    {
        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(TesterApplication.class.getName());

        schema.setPreferIPv4(true);

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        try (JavaApplication application = builder.realize(schema, "application", console))
        {
            RemoteCallable<Integer> callable =
                new RemoteCallableStaticMethod<Integer>(TesterApplication.class.getName(),
                                                        "getMeaningOfLife");

            FutureCompletionListener<Integer> future = new FutureCompletionListener<Integer>();

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
        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(TesterApplication.class.getName());

        schema.setPreferIPv4(true);

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        try (JavaApplication application = builder.realize(schema, "application", console))
        {
            Tester tester = application.getProxyFor(Tester.class, new TesterProducer(), null);

            tester.doNothing();

            assertThat(tester.getMeaningOfLife(), is("42"));
            assertThat(tester.identity(42), is(42));

            application.waitFor();
        }
    }


    /**
     * Ensure that we can wait for applications to terminate.
     */
    @Test(timeout = 10000)
    public void shouldWaitFor()
    {
        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        schema.setPreferIPv4(true);

        // we'll wait at most 5 seconds in the application
        schema.addArgument("5");

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        try (JavaApplication application = builder.realize(schema, "sleeping", console))
        {
            application.waitFor();
        }
    }


    /**
     * Ensure that we can define a system property in the application
     * (and it won't be defined in this process).
     */
    @Test
    public void shouldDefineSystemPropertyInApplication()
    {
        // the custom property name and value
        String propertyName  = "custom.property";
        String propertyValue = "gudday";

        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        schema.setPreferIPv4(true);
        schema.setSystemProperty(propertyName, propertyValue);

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        try (JavaApplication application = builder.realize(schema, "sleeping", console))
        {
            // assert that the custom property is defined in the application
            Eventually.assertThat(application, new GetSystemProperty(propertyName), is(propertyValue));

            // assert that the custom property is not defined in the test
            assertThat(System.getProperty(propertyName), is(nullValue()));
        }
    }
}
