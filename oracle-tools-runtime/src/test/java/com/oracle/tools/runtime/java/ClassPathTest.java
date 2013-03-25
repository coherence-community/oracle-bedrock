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
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import java.io.File;

/**
 * Unit tests for {@link ClassPath}s.
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

        Assert.assertThat(classPath.toString(), is(""));
    }


    /**
     * Ensure that we can create a ClassPath containing a simple jar.
     */
    @Test
    public void shouldCreateSingleFileClassPath()
    {
        ClassPath classPath = new ClassPath("simple.jar");

        Assert.assertThat(classPath.toString(), is("simple.jar"));
    }


    /**
     * Ensure that we can create a ClassPath containing a multiple jars.
     */
    @Test
    public void shouldCreateMultipleFileClassPath()
    {
        ClassPath classPath = new ClassPath("one.jar" + File.pathSeparator + "two.jar");

        Assert.assertThat(classPath.toString(), is("one.jar" + File.pathSeparator + "two.jar"));
    }


    /**
     * Ensure that we can create a ClassPath containing a simple jar
     * (containing white space).
     */
    @Test
    public void shouldCreateSingleFileClassPathWithWhiteSpace()
    {
        ClassPath classPath = new ClassPath("this is my simple.jar");

        Assert.assertThat(classPath.toString(), is("\"this is my simple.jar\""));
    }


    /**
     * Ensure that we can create a ClassPath containing an absolute path.
     */
    @Test
    public void shouldCreateSingleAbsolutePath()
    {
        ClassPath classPath = new ClassPath(File.separator + "one" + File.separator + "two");

        Assert.assertThat(classPath.toString(), is(File.separator + "one" + File.separator + "two" + File.separator));
    }


    /**
     * Ensure that we can create a ClassPath containing multiple absolute paths.
     */
    @Test
    public void shouldCreateMultipleAbsolutePaths()
    {
        ClassPath classPath = new ClassPath(File.separator + "one" + File.separator + "two" + File.pathSeparator
                                            + "three" + File.separator + "four");

        Assert.assertThat(classPath.toString(),
                          is(File.separator + "one" + File.separator + "two" + File.separator + File.pathSeparator
                             + "three" + File.separator + "four" + File.separator));
    }


    /**
     * Ensure that we can create a ClassPath containing an absolute path
     * (containing white space).
     */
    @Test
    public void shouldCreateSingleAbsolutePathWithWhiteSpace()
    {
        ClassPath classPath = new ClassPath(File.separator + "one two" + File.separator + "three four");

        Assert.assertThat(classPath.toString(),
                          is("\"" + File.separator + "one two" + File.separator + "three four" + File.separator
                             + "\""));
    }


    /**
     * Ensure that we can create a ClassPath containing a simple jar
     * (with a terminating path separator).
     */
    @Test
    public void shouldCreateSingleFileWithAdditionalPathSeparator()
    {
        ClassPath classPath = new ClassPath("simple.jar" + File.pathSeparator);

        Assert.assertThat(classPath.toString(), is("simple.jar"));
    }


    /**
     * Ensure that we can create a ClassPath containing an absolute path
     * (with a terminating path separator).
     */
    @Test
    public void shouldCreateSingleAbsolutePathWithAdditionalPathSeparator()
    {
        ClassPath classPath = new ClassPath(File.separator + "one" + File.separator + "two" + File.pathSeparator);

        Assert.assertThat(classPath.toString(), is(File.separator + "one" + File.separator + "two" + File.separator));
    }
}
