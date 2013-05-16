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

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.runtime.DummyApp;
import com.oracle.tools.runtime.DummyClassPathApp;

import com.oracle.tools.runtime.console.PipedApplicationConsole;

import com.oracle.tools.runtime.java.container.ContainerClassLoader;

import org.junit.Test;

import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertThat;

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
    public abstract JavaApplicationBuilder<SimpleJavaApplication,
                                           SimpleJavaApplicationSchema> newJavaApplicationBuilder();


    /**
     * Obtains the internal Application Process Id for the specified Application
     * (without using the application.getId() method)
     *
     * @param application  the application
     *
     * @return the process id
     */
    public long getProcessIdFor(JavaApplication<?> application)
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

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            newJavaApplicationBuilder();

        PipedApplicationConsole console     = new PipedApplicationConsole();
        JavaApplication<?>      application = builder.realize(schema, "java-app", console);

        String                  stdout      = console.getOutputReader().readLine();

        assertThat(stdout.startsWith("[java-app:"), is(true));
        assertThat(stdout, containsString("arg1,arg2"));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString("test.prop.1=value.1"));

        assertThat(application.getId(), is(getProcessIdFor(application)));

        application.destroy();
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
        ClassPath   path2             = ClassPath.ofClass(ContainerClassLoader.class);
        ClassPath   classPath         = new ClassPath(knownJarClassPath, path1, path2);

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyClassPathApp.class.getCanonicalName()).setClassPath(classPath)
                .setArgument(knownClass.getCanonicalName()).setDiagnosticsEnabled(true);

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            newJavaApplicationBuilder();

        PipedApplicationConsole console     = new PipedApplicationConsole();
        JavaApplication<?>      application = builder.realize(schema, "java-app", console);

        String                  stdout      = console.getOutputReader().readLine();

        assertThat(stdout, containsString(knownJarClassPath.iterator().next()));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString(path1.iterator().next()));

        stdout = console.getOutputReader().readLine();

        assertThat(stdout, containsString(path2.iterator().next()));

        assertThat(application.getId(), is(getProcessIdFor(application)));

        application.destroy();
    }
}
