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

import classloader.child.DummyClass;

import classloader.parent.DummyParentLoadedClass;

import com.oracle.tools.junit.AbstractTest;

import com.oracle.tools.runtime.java.ClassPath;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;

/**
 * Unit tests for the {@link VirtualizationClassLoader}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class VirtualizationClassLoaderTest
{
    /**
     * Ensure that we can create VirtualizationClassLoader that uses
     * the System ClassPath.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultClassPath() throws Exception
    {
        ClassPath                 systemClassPath = ClassPath.ofSystem();
        VirtualizationClassLoader loader = VirtualizationClassLoader.newInstance("Test", null, System.getProperties());
        ClassPath                 classPath       = loader.getClassPath();

        Assert.assertThat(classPath, is(systemClassPath));
    }


    /**
     * Ensure that we can create a VirtualizationClassLoader using a
     * custom ClassPath.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedClassPath() throws Exception
    {
        String    pathElement1 = File.separator + "test1.jar";
        String    pathElement2 = "test2.jar";
        ClassPath classPath    = new ClassPath(pathElement1 + File.pathSeparator + pathElement2);

        VirtualizationClassLoader loader = VirtualizationClassLoader.newInstance("Test",
                                                                                 classPath,
                                                                                 System.getProperties());
        ClassPath loaderClassPath = loader.getClassPath();

        assertTrue(classPath.contains(pathElement1));
        assertTrue(classPath.contains(pathElement2));
    }


    /**
     * Ensure that the VirtualizationClassLoader can load a Class.
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
     * Ensure that the VirtualizationClassLoader can load a resource
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
     * Ensure that the VirtualizationClassLoader won't load a Class
     * from a ClassPath that doesn't contain the said Class, but the said Class
     * is available (ie: Class isolation)
     *
     * @throws Exception
     */
    @Test(expected = ClassNotFoundException.class)
    public void shouldNotBeAbleToLoadClassNotOnClassPath() throws Exception
    {
        ClassPath   classPath = new ClassPath("test.jar");
        ClassLoader loader    = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());

        loader.loadClass(DummyClass.class.getCanonicalName());
    }


    /**
     * Ensure that the VirtualizationClassLoader can load a Class from a
     * Jar.
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromJarInCustomClassPath() throws Exception
    {
        // NOTE: the Mockito class in the Mockito jar contains the asm-license.txt
        ClassPath   classPath = ClassPath.ofResource("asm-license.txt");
        ClassLoader loader    = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result    = loader.loadClass(Mockito.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(Mockito.class.getCanonicalName()));
        assertThat(result.equals(Mockito.class), is(false));
    }


    /**
     * Ensure that the VirtualizationClassLoader can load a Class from a
     * custom ClassPath (based on a Java Class).
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromDirectoryInCustomClassPath() throws Exception
    {
        ClassPath   classPath = ClassPath.ofClass(DummyClass.class);
        ClassLoader loader    = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result    = loader.loadClass(DummyClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), sameInstance(loader));
        assertThat(result.getCanonicalName(), is(DummyClass.class.getCanonicalName()));
        assertThat(result.equals(DummyClass.class), is(false));
    }


    /**
     * Ensure that the VirtualizationClassLoader can exclude packages when
     * loading classes.
     *
     * @throws Exception
     */
    @Test
    public void shouldLoadClassFromParentClassLoaderIfPackageIsInExcludedList() throws Exception
    {
        ClassPath classPath = ClassPath.ofClass(DummyParentLoadedClass.class);

        System.setProperty(VirtualizationClassLoader.PROPERTY_EXCLUDED_PACKAGES, "classloader.parent");

        ClassLoader loader = VirtualizationClassLoader.newInstance("Test", classPath, System.getProperties());
        Class<?>    result = loader.loadClass(DummyParentLoadedClass.class.getCanonicalName());

        assertThat(result.getClassLoader(), not(sameInstance(loader)));
        assertThat(result.getCanonicalName(), is(DummyParentLoadedClass.class.getCanonicalName()));
        assertThat(result.equals(DummyParentLoadedClass.class), is(true));
    }


    /**
     * Ensure that the VirtualizationClassLoader inherits System Properties.
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
     * Ensure that the VirtualizationClassLoader protects and isolates
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
     * Ensure that we can correctly determine the VirtualizedSystem instance
     * from the VirtualizationClassLoader.
     *
     * @throws Exception
     */
    @Test
    public void shouldCorrectlyDetermineVirtualizedSystemFromThread() throws Exception
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
}
