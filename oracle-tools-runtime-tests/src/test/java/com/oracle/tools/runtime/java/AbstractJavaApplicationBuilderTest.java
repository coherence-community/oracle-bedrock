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

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.DummyApp;
import com.oracle.tools.runtime.DummyClassPathApp;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.callable.GetSystemProperty;
import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;

import com.oracle.tools.runtime.console.PipedApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.util.FutureCompletionListener;

import org.junit.Test;

import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;

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
     * Obtains the internal Application Process Id for the specified Application
     * (without using the application.getId() method)
     *
     * @param application  the application
     *
     * @return the process id
     */
    public long getProcessIdFor(JavaApplication application)
    {
        return ((AbstractJavaApplication) application).getJavaProcess().getId();
    }


    /**
     * Ensure that we can start and terminate a {@link JavaApplication}.
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyApp.class.getCanonicalName()).setArgument("arg1").setArgument("arg2")
                .setSystemProperty("test.prop.1",
                                   "value.1").setSystemProperty("test.prop.2", "value.2").setDiagnosticsEnabled(true);

        JavaApplicationBuilder<JavaApplication> builder     = newJavaApplicationBuilder();

        PipedApplicationConsole                 console     = new PipedApplicationConsole();
        JavaApplication                         application = builder.realize(schema, "java-app", console);

        String                                  stdout      = console.getOutputReader().readLine();

        assertThat(stdout.startsWith("[java-app:"), is(true));
        assertThat(stdout, containsString("arg1,arg2"));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString("test.prop.1=value.1"));

        assertThat(application.getId(), is(getProcessIdFor(application)));

        application.close();
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
                .addArgument(knownClass.getCanonicalName()).setDiagnosticsEnabled(true);

        JavaApplicationBuilder<JavaApplication> builder     = newJavaApplicationBuilder();

        PipedApplicationConsole                 console     = new PipedApplicationConsole();
        JavaApplication                         application = builder.realize(schema, "java-app", console);

        String                                  stdout      = console.getOutputReader().readLine();

        assertThat(stdout, containsString(knownJarClassPath.iterator().next()));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString(path1.iterator().next()));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString(path2.iterator().next()));

        assertThat(application.getId(), is(getProcessIdFor(application)));

        application.close();
    }


    /**
     * Ensure that we can create applications that can have {@link java.util.concurrent.Callable}s
     * submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable() throws InterruptedException
    {
        SimpleJavaApplication application = null;

        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // set a System-Property for the SleepingApplication (we'll request it back)
        String uuid = UUID.randomUUID().toString();

        schema.setSystemProperty("uuid", uuid);

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        application = builder.realize(schema, "application", console);

        Eventually.assertThat(application, new GetSystemProperty("uuid"), is(uuid));

        application.close();
    }


    /**
     * Ensure that we can use a {@link RemoteCallableStaticMethod} with a {@link JavaApplication}.
     */
    @Test
    public void shouldCallRemoteStaticMethodsInAnApplication() throws InterruptedException, ExecutionException
    {
        SimpleJavaApplication application = null;

        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(TesterApplication.class.getName());

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        application = builder.realize(schema, "application", console);

        RemoteCallable<Integer> callable = new RemoteCallableStaticMethod<Integer>(TesterApplication.class.getName(),
                                                                                   "getMeaningOfLife");

        FutureCompletionListener<Integer> future = new FutureCompletionListener<Integer>();

        application.submit(callable, future);

        assertThat(future.get(), is(42));

        application.waitFor();

        application.close();
    }


    /**
     * Ensure that we can create a local proxy of a {@link Tester} in an {@link JavaApplication}.
     */
    @Test
    public void shouldProxyTesterInApplication() throws InterruptedException
    {
        SimpleJavaApplication application = null;

        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(TesterApplication.class.getName());

        JavaApplicationBuilder<JavaApplication> builder = newJavaApplicationBuilder();

        ApplicationConsole                      console = new SystemApplicationConsole();

        application = builder.realize(schema, "application", console);

        Tester tester = application.getProxyFor(Tester.class, new TesterProducer(), null);

        tester.doNothing();

        assertThat(tester.getMeaningOfLife(), is("42"));
        assertThat(tester.identity(42), is(42));

        application.waitFor();

        application.close();
    }


    /**
     * Ensure that we can wait for applications to terminate.
     */
    @Test(timeout = 10000)
    public void shouldWaitFor() throws InterruptedException
    {
        // define and start the SleepingApplication
        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

        // we'll wait at most 5 seconds in the application
        schema.setArgument("5");

        JavaApplicationBuilder<JavaApplication> builder     = newJavaApplicationBuilder();

        ApplicationConsole                      console     = new SystemApplicationConsole();

        SimpleJavaApplication                   application = builder.realize(schema, "sleeping", console);

        application.waitFor();

        application.close();
    }
}
