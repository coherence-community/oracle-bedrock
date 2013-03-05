/*
 * File: VirtualizationClassLoaderTest.java
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

package com.oracle.tools.runtime.java.virtualization;

import com.oracle.tools.DummyClass;
import com.oracle.tools.junit.AbstractTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link VirtualizationClassLoader}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class VirtualizationClassLoaderTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultClassPath() throws Exception
    {
        URL[]                     systemClasspathURLs = getDefaultClassPath();

        VirtualizationClassLoader loader = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());

        URL[]                     path = loader.getClassPath();

        for (int i = 0; i < path.length; i++)
        {
            assertThat(path[i], is(systemClasspathURLs[i]));
        }
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedClassPath() throws Exception
    {
        String pathElement1 = "/test1.jar";
        String pathElement2 = "/test2.jar";
        String classpath = pathElement1 + File.pathSeparator + pathElement2;

        VirtualizationClassLoader loader = VirtualizationClassLoader.newInstance("Test",
                                                                                 classpath,
                                                                                 System.getProperties());
        URL[] path = loader.getClassPath();

        assertThat(path[0].toExternalForm(), is(new File(pathElement1).toURI().toURL().toExternalForm()));
        assertThat(path[1].toExternalForm(), is(new File(pathElement2).toURI().toURL().toExternalForm()));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClass() throws Exception
    {
        ClassLoader loader = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());
        Class<?>    result = loader.loadClass(DummyClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(DummyClass.class.getCanonicalName()));
        assertThat(result.equals(DummyClass.class), is(false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadResource() throws Exception
    {
        String      resourceName = "propertiesbuilder-test.properties";
        URL         expected     = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        ClassLoader loader       = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());
        URL         resource     = loader.getResource(resourceName);

        assertThat(resource.toExternalForm(), is(expected.toExternalForm()));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test(expected = ClassNotFoundException.class)
    public void shouldNotBeAbleToLoadClassNotOnClassPath() throws Exception
    {
        String      classPath = "test.jar";
        ClassLoader loader    = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());

        loader.loadClass(DummyClass.class.getCanonicalName());
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromJarInCustomClassPath() throws Exception
    {
        String      classPath = findMockitoJarOnClassPath();
        ClassLoader loader    = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result    = loader.loadClass(Mockito.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(Mockito.class.getCanonicalName()));
        assertThat(result.equals(Mockito.class), is(false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromDirectoryInCustomClassPath() throws Exception
    {
        String      classPath = findLocationOnClassPath(DummyClass.class);

        ClassLoader loader    = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result    = loader.loadClass(DummyClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(DummyClass.class.getCanonicalName()));
        assertThat(result.equals(DummyClass.class), is(false));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromParentClassLoaderIfPackageIsInExcludedList() throws Exception
    {
        String classPath = findLocationOnClassPath(DummyClass.class);

        System.setProperty(VirtualizationClassLoader.PROPERTY_EXCLUDED_PACKAGES, "com.oracle.tools");

        ClassLoader loader = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result = loader.loadClass(DummyClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), not(sameInstance(loader)));
        assertThat(result.getCanonicalName(), is(DummyClass.class.getCanonicalName()));
        assertThat(result.equals(DummyClass.class), is(true));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldInheritSystemProperties() throws Exception
    {
        System.setProperty("test.property.1", "value.1");

        VirtualizationClassLoader loader = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());

        Virtualization.associateThreadWith(loader.getVirtualizedSystem());

        Class<?> result = loader.loadClass(DummyClass.class.getCanonicalName());
        Method   method = result.getDeclaredMethod("getProperty", String.class);

        assertThat((String) method.invoke(null, "test.property.1"), is("value.1"));

        Virtualization.dissociateThread();
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldIsolateSystemProperties() throws Exception
    {
        ClassLoader saved = Thread.currentThread().getContextClassLoader();

        String      key;

        try
        {
            key = "test.property.1";
            System.setProperty(key, "value.1");

            ClassLoader loader = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());

            Thread.currentThread().setContextClassLoader(loader);

            Virtualization.start();

            Class<?> result    = loader.loadClass(DummyClass.class.getCanonicalName());
            Method   setMethod = result.getDeclaredMethod("setProperty", String.class, String.class);

            setMethod.invoke(null, key, "value.2");

            Method getMethod = result.getDeclaredMethod("getProperty", String.class);

            assertThat((String) getMethod.invoke(null, key), is("value.2"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(saved);

            Virtualization.stop();
        }

        assertThat(System.getProperty(key), is("value.1"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void testCorrectlyDetermineVirtualizedSystemFromThread() throws Exception
    {
        VirtualizationClassLoader loader = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());

        Thread                    thread = Thread.currentThread();
        ClassLoader               saved  = thread.getContextClassLoader();

        thread.setContextClassLoader(loader);

        try
        {
            assertThat(loader.getVirtualizedSystem(), sameInstance(Virtualization.getVirtualizedSystem()));
        }
        finally
        {
            thread.setContextClassLoader(saved);
        }
    }


    private URL[] getDefaultClassPath() throws Exception
    {
        String   systemClassPath         = System.getProperty("java.class.path");
        String[] systemClassPathElements = systemClassPath.split(File.pathSeparator);
        URL[]    systemClasspathURLs     = new URL[systemClassPathElements.length];

        for (int i = 0; i < systemClassPathElements.length; i++)
        {
            String end = (systemClassPathElements[i].endsWith(".jar")) ? "" : File.separator;

            systemClasspathURLs[i] = new File(systemClassPathElements[i] + end).toURI().toURL();
        }

        return systemClasspathURLs;
    }


    /**
     * Finds the location of Mockito Jar file on the classpath.
     * This is done by looking for the resource asm-license.txt which is contained in
     * the Mockito jar. Once the URL for this is located we can work out the Jar location.
     *
     * @return The location of the Mockito Jar file.
     * @throws Exception if anything goes wrong
     */
    public String findMockitoJarOnClassPath() throws Exception
    {
        String resourceName = "asm-license.txt";

        return findLocationOnClassPath(resourceName);
    }


    /**
     * Finds the location of a Jar or directory on the classpath.
     * This is done by looking for the specified Class file.
     * Once the URL for this is located we can work out the Jar or directory location.
     *
     * @param resource the resource to use to locate the class path
     * @return The location of the Jar
     * @throws Exception if anything goes wrong
     */
    public String findLocationOnClassPath(Class<?> resource) throws Exception
    {
        return findLocationOnClassPath(resource.getCanonicalName().replace(".", File.separator) + ".class");
    }


    /**
     * Finds the location of a Jar or directory on the classpath.
     * This is done by looking for the specified resource.
     * Once the URL for this is located we can work out the Jar or directory location.
     *
     * @param resourceName the resource to use to locate the class path
     * @return The location of the Coherence Jar file.
     * @throws Exception if anything goes wrong
     */
    public String findLocationOnClassPath(String resourceName) throws Exception
    {
        Enumeration<URL> resources = getClass().getClassLoader().getResources(resourceName);

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
