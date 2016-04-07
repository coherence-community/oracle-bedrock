/*
 * File: ClassPathTest.java
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

import com.oracle.tools.Option;

import com.oracle.tools.io.FileHelper;

import com.oracle.tools.runtime.options.PlatformSeparators;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import java.net.URL;

/**
 * Unit Tests for {@link ClassPath}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClassPathTest
{
    /**
     * Ensure that we can create an empty ClassPath.
     */
    @Test
    public void shouldCreateEmptyClassPath()
    {
        ClassPath classPath = new ClassPath();

        assertThat(classPath.toString(), is(""));
        Assert.assertTrue(classPath.isEmpty());
        assertThat(classPath.size(), is(0));
    }


    /**
     * Ensure that we can copy an empty ClassPath.
     */
    @Test
    public void shouldCopyEmptyClassPath()
    {
        ClassPath classPath = new ClassPath((String) null);

        assertThat(classPath.toString(), is(""));
        Assert.assertTrue(classPath.isEmpty());

        classPath = new ClassPath(new ClassPath());
        assertThat(classPath.toString(), is(""));
        Assert.assertTrue(classPath.isEmpty());
        assertThat(classPath.size(), is(0));
    }


    /**
     * Ensure that we can copy a ClassPath.
     */
    @Test
    public void shouldCopyClassPath()
    {
        ClassPath classPath     = new ClassPath("simple.jar");

        ClassPath copyClassPath = new ClassPath(classPath);

        Assert.assertEquals(classPath, copyClassPath);
        Assert.assertNotSame(classPath, copyClassPath);
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath a single dot.
     */
    @Test
    public void shouldCreateDotClassPath()
    {
        String    path      = ".";
        ClassPath classPath = new ClassPath(path);

        assertThat(classPath.toString(), is(path + File.separator));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath using a wildcard *.
     */
    @Test
    public void shouldCreateWildCardClassPath()
    {
        String    path      = "*";
        ClassPath classPath = new ClassPath(path);

        assertThat(classPath.toString(), is(path));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath a single File separator.
     */
    @Test
    public void shouldCreateRootClassPath()
    {
        String    path      = File.separator;
        ClassPath classPath = new ClassPath(path);

        assertThat(classPath.toString(), is(path));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath containing a simple jar.
     */
    @Test
    public void shouldCreateSingleFileClassPath()
    {
        String    path      = "simple.jar";
        ClassPath classPath = new ClassPath(path);

        assertThat(classPath.toString(), is(path));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath containing a multiple jars.
     */
    @Test
    public void shouldCreateMultipleFileClassPath()
    {
        String    path      = "one.jar" + File.pathSeparator + "two.jar";
        ClassPath classPath = new ClassPath(path);

        assertThat(classPath.toString(), is(path));
        Assert.assertTrue(classPath.contains("one.jar"));
        Assert.assertTrue(classPath.contains("two.jar"));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(2));
    }


    /**
     * Ensure that we can create a ClassPath containing an absolute path.
     */
    @Test
    public void shouldCreateSingleAbsolutePath()
    {
        String    path      = File.separator + "one" + File.separator + "two";
        ClassPath classPath = new ClassPath(path);

        assertThat(classPath.toString(), is(path + File.separator));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath containing multiple absolute paths.
     */
    @Test
    public void shouldCreateMultipleAbsolutePaths()
    {
        String    path1     = File.separator + "one" + File.separator + "two";
        String    path2     = "three" + File.separator + "four";
        ClassPath classPath = new ClassPath(path1 + File.pathSeparator + path2);

        assertThat(classPath.toString(), is(path1 + File.separator + File.pathSeparator + path2 + File.separator));
        Assert.assertTrue(classPath.contains(path1));
        Assert.assertTrue(classPath.contains(path2));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(2));
    }


    /**
     * Ensure that we can create a ClassPath containing a simple jar
     * (with a terminating path separator).
     */
    @Test
    public void shouldCreateSingleFileWithAdditionalPathSeparator()
    {
        String    path      = "simple.jar";
        ClassPath classPath = new ClassPath(path + File.pathSeparator);

        assertThat(classPath.toString(), is(path));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can create a ClassPath containing an absolute path
     * (with a terminating path separator).
     */
    @Test
    public void shouldCreateSingleAbsolutePathWithAdditionalPathSeparator()
    {
        String    path      = File.separator + "one" + File.separator + "two";
        ClassPath classPath = new ClassPath(path + File.pathSeparator);

        assertThat(classPath.toString(), is(path + File.separator));
        Assert.assertTrue(classPath.contains(path));
        Assert.assertFalse(classPath.isEmpty());
        assertThat(classPath.size(), is(1));
    }


    /**
     * Ensure that we can both find and access a path (via a URL) using
     * a path containing a white space.
     */
    @Test
    public void shouldCorrectlyEncodePathsContainingWhiteSpace()
    {
        File temporaryFolder = null;

        try
        {
            // create a temporary folder that has a space in the path
            temporaryFolder = FileHelper.createTemporaryFolder("Temporary Folder");

            // create a ClassPath for the temporary folder
            ClassPath classPath = ClassPath.ofFile(temporaryFolder.getAbsoluteFile());

            // grab the path as a URL
            URL url = classPath.getURLs()[0];

            // ensure that the URL does not contain a space
            Assert.assertFalse(url.toString().contains(" "));
        }
        catch (Exception e)
        {
            Assert.fail("Failed to create a temporary folder containing a space");
        }
        finally
        {
            if (!FileHelper.recursiveDelete(temporaryFolder))
            {
                throw new RuntimeException("Failed to clean up the temporary folder: [" + temporaryFolder
                                           + "].  You should probably clean this up manually");
            }
        }
    }


    /**
     * Ensure that {@link ClassPath#toString(Option...)} uses the {@link PlatformSeparators}.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseSpecifiedSeparator() throws Exception
    {
        PlatformSeparators separators = mock(PlatformSeparators.class);
        ClassPath          path       = new ClassPath("foo.jar", "bar.jar");

        when(separators.getPathSeparator()).thenReturn("$");

        assertThat(path.toString(separators), is("foo.jar" + separators.getPathSeparator() + "bar.jar"));
    }


    /**
     * Ensure that {@link ClassPath#toString(Option...)} uses default {@link PlatformSeparators}.
     *
     * @throws Exception
     */
    @Test
    public void shouldUseDefaultSeparators() throws Exception
    {
        PlatformSeparators separators = PlatformSeparators.autoDetect();
        ClassPath          path       = new ClassPath("foo.jar", "bar.jar");

        assertThat(path.toString(separators), is("foo.jar" + separators.getPathSeparator() + "bar.jar"));
    }


    /**
     * Ensure that {@link ClassPath#toString(Option...)} uses the {@link ClassPathModifier}s.
     *
     * @throws Exception
     */
    @Test
    public void shouldApplyModifier() throws Exception
    {
        ClassPathModifier  modifier   = mock(ClassPathModifier.class, "Mod");
        PlatformSeparators separators = mock(PlatformSeparators.class, "Sep");
        ClassPath          path       = new ClassPath("foo.jar", "bar.jar");

        when(separators.getPathSeparator()).thenReturn("$");
        when(modifier.modify(anyString())).thenReturn("modified-path");

        String pathString = path.toString(separators, modifier);

        assertThat(pathString, is("modified-path"));
        verify(modifier).modify("foo.jar$bar.jar");
    }


    /**
     * Ensure that the {@link ClassPath} of an inner class/type can be resolved.
     *
     * @throws Exception
     */
    @Test
    public void shouldResolveClassPathOfInnerClass() throws Exception
    {
        ClassPath classPath = ClassPath.ofClass(JavaApplication.MetaClass.class);
    }
}
