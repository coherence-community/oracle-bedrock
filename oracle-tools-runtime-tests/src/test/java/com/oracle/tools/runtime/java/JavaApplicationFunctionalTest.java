/*
 * File: JavaApplicationFunctionalTest.java
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
import com.oracle.tools.runtime.CapturingApplicationConsole;
import com.oracle.tools.runtime.DummyApp;
import com.oracle.tools.runtime.DummyClassPathApp;
import com.oracle.tools.runtime.console.NullApplicationConsole;
import com.oracle.tools.runtime.java.virtualization.VirtualizationClassLoader;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for {@link JavaApplication}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JavaApplicationFunctionalTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplication() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyApp.class.getCanonicalName()).setArgument("arg1").setArgument("arg2")
                .setSystemProperty("test.prop.1",
                                   "value.1").setSystemProperty("test.prop.2", "value.2");

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new ExternalJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        CapturingApplicationConsole console     = new CapturingApplicationConsole();
        JavaApplication<?>          application = builder.realize(schema, "java-app", console);

        application.waitFor();

        int exitCode = application.exitValue();

        assertThat(exitCode, is(0));

        application.destroy();
        assertThat(console.getConsoleOutputLine("out", 0).startsWith("[java-app:out:"), is(true));
        assertThat(console.getConsoleOutputLine("out", 0), containsString("arg1,arg2"));
        assertThat(console.getConsoleOutputLine("out", 1), containsString("test.prop.1=value.1"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldRunApplicationInProcess() throws Exception
    {
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyApp.class.getCanonicalName()).setArgument("arg1").setArgument("arg2")
                .setSystemProperty("test.prop.1",
                                   "value.1").setSystemProperty("test.prop.2", "value.2");

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new VirtualizedJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        JavaApplication<?> application = builder.realize(schema, "java-app", NullApplicationConsole.INSTANCE);

        String[]           actualArgs  = (String[]) application.invoke(DummyApp.class.getCanonicalName(), "getArgs");

        assertThat(actualArgs[0], is("arg1"));
        assertThat(actualArgs[1], is("arg2"));

        Properties actualProperties = (Properties) application.invoke(DummyApp.class.getCanonicalName(),
                                                                      "getProperties");

        assertThat(actualProperties.getProperty("test.prop.1"), is("value.1"));
        assertThat(actualProperties.getProperty("test.prop.2"), is("value.2"));

        application.destroy();

        Boolean stopCalled = (Boolean) application.invoke(DummyApp.class.getCanonicalName(), "wasStopCalled");

        assertThat(stopCalled, is(true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldRunOutOfProcessApplicationWithRestrictedClasspath() throws Exception
    {
        String      knownJarClassPath = findKnownJarWithoutDependeniesOnClassPath();
        Class<Mock> knownClass        = Mock.class;

        String      path1             = findLocationOnClassPath(DummyClassPathApp.class);
        String      path2             = findLocationOnClassPath(VirtualizationClassLoader.class);
        String classPath              = knownJarClassPath + File.pathSeparator + path1 + File.pathSeparator + path2;

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyClassPathApp.class.getCanonicalName()).setClassPath(classPath)
                .setArgument(knownClass.getCanonicalName());

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new ExternalJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        CapturingApplicationConsole console     = new CapturingApplicationConsole();
        JavaApplication<?>          application = builder.realize(schema, "java-app", console);

        application.waitFor();
        assertThat(console.getConsoleOutputLine("out", 0), containsString(knownJarClassPath));
        assertThat(console.getConsoleOutputLine("out", 1), containsString(path1));
        assertThat(console.getConsoleOutputLine("out", 2), containsString(path2));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldRunInProcessApplicationWithRestrictedClasspath() throws Exception
    {
        String      knownJarClassPath = findKnownJarWithoutDependeniesOnClassPath();
        Class<Mock> knownClass        = Mock.class;

        String      path1             = findLocationOnClassPath(DummyClassPathApp.class);
        String      path2             = findLocationOnClassPath(VirtualizationClassLoader.class);
        String classPath              = knownJarClassPath + File.pathSeparator + path1 + File.pathSeparator + path2;

        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(DummyClassPathApp.class.getCanonicalName()).setClassPath(classPath)
                .setArgument(knownClass.getCanonicalName());

        JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new VirtualizedJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        CapturingApplicationConsole console     = new CapturingApplicationConsole();
        JavaApplication<?>          application = builder.realize(schema, "java-app", console);

        URL[] actualClassPath = (URL[]) application.invoke(DummyClassPathApp.class.getCanonicalName(), "getClassPath");

        assertThat(actualClassPath.length, is(3));
        assertThat(actualClassPath[0].toExternalForm(), is(new File(knownJarClassPath).toURI().toURL().toExternalForm()));    // .endsWith(knownJarClassPath), is(true));
        assertThat(actualClassPath[1].toExternalForm(), is(new File(path1).toURI().toURL().toExternalForm()));    // .endsWith(path1 + "/"), is(true));
        assertThat(actualClassPath[2].toExternalForm(), is(new File(path2).toURI().toURL().toExternalForm()));    // .endsWith(path2), is(true));
    }


    /**
     * Locates a known jar on the classpath that doesn't have any dependencies
     *
     * @return the location of the jar
     * @throws Exception if anything goes wrong
     */
    public String findKnownJarWithoutDependeniesOnClassPath() throws Exception
    {
        String resourceName = "asm-license.txt";

        return findLocationOnClassPath(resourceName);
    }


    /**
     * Finds the location of a Jar or directory on the classpath.
     * This is done by looking for the specified Class file.
     * Once the URL for this is located we can work out the Jar or directory location.
     *
     * @return The location of the Coherence Jar file.
     * @throws Exception if anything goes wrong
     */
    public String findLocationOnClassPath(Class<?> resource) throws Exception
    {
        return findLocationOnClassPath(resource.getCanonicalName().replace(".", "/") + ".class", resource.getClassLoader());
    }


    /**
     * Finds the location of a Jar or directory on the classpath.
     * This is done by looking for the specified resource.
     * Once the URL for this is located we can work out the Jar or directory location.
     *
     * @return The location of the Coherence Jar file.
     * @throws Exception if anything goes wrong
     */
    public String findLocationOnClassPath(String resourceName) throws Exception
    {
        return findLocationOnClassPath(resourceName, getClass().getClassLoader());
    }

    /**
     * Finds the location of a Jar or directory on the classpath.
     * This is done by looking for the specified resource.
     * Once the URL for this is located we can work out the Jar or directory location.
     *
     * @return The location of the Coherence Jar file.
     * @throws Exception if anything goes wrong
     */
    public String findLocationOnClassPath(String resourceName, ClassLoader classLoader) throws Exception
    {
        Enumeration<URL> resources = classLoader.getResources(resourceName);

        if (resources.hasMoreElements())
        {
            URL    url      = resources.nextElement();
            String location = URLDecoder.decode(url.toExternalForm(), "UTF-8");

            location = location.substring(0, location.length() - resourceName.length() - 1);

            if (location.startsWith("jar:"))
            {
                location = location.substring(4, location.length() - 1);
            }

            return new File(new URI(location)).getAbsolutePath();
        }
        else
        {
            throw new IllegalStateException("Cannot find Coherence Jar - unable to locate " + resourceName
                                            + " on Class Path");
        }
    }
}
