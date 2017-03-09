/*
 * File: ContainerClassLoaderTest.java
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

import classloader.child.DummyClass;
import classloader.parent.DummyParentLoadedClass;
import com.oracle.bedrock.runtime.java.ClassPath;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit Tests for {@link ContainerClassLoader}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class ContainerClassLoaderTest
{
    /**
     * Ensure that we can create ContainerClassLoader that uses
     * the System ClassPath.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultClassPath() throws Exception
    {
        ClassPath            systemClassPath = ClassPath.ofSystem();
        ContainerClassLoader loader          = ContainerClassLoader.newInstance("Test", null, System.getProperties());
        ClassPath            classPath       = loader.getClassPath();

        Assert.assertThat(classPath, is(systemClassPath));
    }


    /**
     * Ensure that we can create a ContainerClassLoader using a
     * custom ClassPath.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedClassPath() throws Exception
    {
        String               pathElement1    = File.separator + "test1.jar";
        String               pathElement2    = "test2.jar";
        ClassPath            classPath       = new ClassPath(pathElement1 + File.pathSeparator + pathElement2);

        ContainerClassLoader loader = ContainerClassLoader.newInstance("Test", classPath, System.getProperties());
        ClassPath            loaderClassPath = loader.getClassPath();

        assertTrue(classPath.contains(pathElement1));
        assertTrue(classPath.contains(pathElement2));
    }


    /**
     * Ensure that the ContainerClassLoader can load a Class.
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClass() throws Exception
    {
        ClassLoader loader = ContainerClassLoader.newInstance("Test", null, System.getProperties());
        Class<?>    result = loader.loadClass(DummyClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(DummyClass.class.getCanonicalName()));
        assertThat(result.equals(DummyClass.class), is(false));
    }


    /**
     * Ensure that the ContainerClassLoader can load a resource
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadResource() throws Exception
    {
        String      resourceName = "propertiesbuilder-test.properties";
        URL         expected     = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        ClassLoader loader       = ContainerClassLoader.newInstance("Test", null, System.getProperties());
        URL         resource     = loader.getResource(resourceName);

        assertThat(resource.toExternalForm(), is(expected.toExternalForm()));
    }


    /**
     * Ensure that the ContainerClassLoader won't load a Class
     * from a ClassPath that doesn't contain the said Class, but the said Class
     * is available (ie: Class scoping and isolation)
     *
     * @throws Exception
     */
    @Test(expected = ClassNotFoundException.class)
    public void shouldNotBeAbleToLoadClassNotOnClassPath() throws Exception
    {
        ClassPath   classPath = new ClassPath("test.jar");
        ClassLoader loader    = ContainerClassLoader.newInstance("Test", classPath, System.getProperties());

        loader.loadClass(DummyClass.class.getCanonicalName());
    }


    /**
     * Ensure that the ContainerClassLoader can load a Class from a
     * Jar.
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromJarInCustomClassPath() throws Exception
    {
        // NOTE: the Mockito class in the Mockito jar contains a LICENSE file
        ClassPath   classPath = ClassPath.ofResource("LICENSE");
        ClassLoader loader    = ContainerClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result    = loader.loadClass(Mockito.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(Mockito.class.getCanonicalName()));
        assertThat(result.equals(Mockito.class), is(false));
    }


    /**
     * Ensure that the ContainerClassLoader can load a Class from a
     * custom ClassPath (based on a Java Class).
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromDirectoryInCustomClassPath() throws Exception
    {
        ClassPath   classPath = ClassPath.ofClass(DummyClass.class);
        ClassLoader loader    = ContainerClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result    = loader.loadClass(DummyClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(DummyClass.class.getCanonicalName()));
        assertThat(result.equals(DummyClass.class), is(false));
    }


    /**
     * Ensure that the ContainerClassLoader can exclude packages when
     * loading classes.
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromParentClassLoaderIfPackageIsInExcludedList() throws Exception
    {
        ClassPath classPath = ClassPath.ofClass(DummyParentLoadedClass.class);

        System.setProperty(ContainerClassLoader.PROPERTY_EXCLUDED_PACKAGES, "classloader.parent");

        ClassLoader loader = ContainerClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result = loader.loadClass(DummyParentLoadedClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), not(sameInstance(loader)));
        assertThat(result.getCanonicalName(), is(DummyParentLoadedClass.class.getCanonicalName()));
        assertThat(result.equals(DummyParentLoadedClass.class), is(true));
    }


    /**
     * Ensure that the ContainerClassLoader inherits System Properties.
     *
     * @throws Exception
     */
    @Test
    public void shouldInheritSystemProperties() throws Exception
    {
        System.setProperty("test.property.1", "value.1");

        ContainerClassLoader loader = ContainerClassLoader.newInstance("Test", null, System.getProperties());

        Container.associateThreadWith(loader.getContainerScope());

        Class<?> result = loader.loadClass(DummyClass.class.getCanonicalName());
        Method   method = result.getDeclaredMethod("getProperty", String.class);

        assertThat((String) method.invoke(null, "test.property.1"), is("value.1"));

        Container.dissociateThread();
    }


    /**
     * Ensure that the ContainerClassLoader protects and isolates
     * System properties from changes with in the scope of the loader.
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

            ClassLoader loader = ContainerClassLoader.newInstance("Test", null, System.getProperties());

            Thread.currentThread().setContextClassLoader(loader);

            Container.start();

            Class<?> result    = loader.loadClass(DummyClass.class.getCanonicalName());
            Method   setMethod = result.getDeclaredMethod("setProperty", String.class, String.class);

            setMethod.invoke(null, key, "value.2");

            Method getMethod = result.getDeclaredMethod("getProperty", String.class);

            assertThat((String) getMethod.invoke(null, key), is("value.2"));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(saved);

            Container.stop();
        }

        assertThat(System.getProperty(key), is("value.1"));
    }


    /**
     * Ensure that we can correctly determine the Scope instance
     * from the ContainerClassLoader.
     *
     * @throws Exception
     */
    @Test
    public void shouldCorrectlyDetermineScopeFromThread() throws Exception
    {
        ContainerClassLoader loader = ContainerClassLoader.newInstance("Test", null, System.getProperties());

        Thread               thread = Thread.currentThread();
        ClassLoader          saved  = thread.getContextClassLoader();

        thread.setContextClassLoader(loader);

        try
        {
            assertThat(loader.getContainerScope(), sameInstance(Container.getContainerScope()));
        }
        finally
        {
            thread.setContextClassLoader(saved);
        }
    }
}
