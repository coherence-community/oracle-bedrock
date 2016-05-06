/*
 * File: WorkingDirectoryTest.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link WorkingDirectory} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WorkingDirectoryTest
{
    private Platform platform = LocalPlatform.get();
    private Options  options  = new Options();


    @Test
    public void shouldCreateAtSpecifiedFileLocation() throws Exception
    {
        File             file             = new File("/foo");

        WorkingDirectory workingDirectory = WorkingDirectory.at(file);

        assertThat(workingDirectory, is(notNullValue()));
        assertThat(workingDirectory.getValue(), is((Object) file));

        assertThat(workingDirectory.resolve(platform, options), is(file));
    }


    @Test
    public void shouldCreateAtSpecifiedFileLocationWithNullFile() throws Exception
    {
        File             file             = null;
        File             current          = new File(System.getProperty("user.dir"));

        WorkingDirectory workingDirectory = WorkingDirectory.at(file);

        assertThat(workingDirectory, is(notNullValue()));

        assertThat(workingDirectory.resolve(platform, options), is(current));
    }


    @Test
    public void shouldCreateAtSpecifiedStringLocation() throws Exception
    {
        File             file             = new File("/foo");

        WorkingDirectory workingDirectory = WorkingDirectory.at("/foo");

        assertThat(workingDirectory, is(notNullValue()));
        assertThat(workingDirectory.getValue(), is((Object) "/foo"));

        assertThat(workingDirectory.resolve(platform, options), is(file));
    }


    @Test
    public void shouldCreateAtSpecifiedNullLocationWithNull() throws Exception
    {
        Object           location         = null;
        File             current          = new File(System.getProperty("user.dir"));

        WorkingDirectory workingDirectory = WorkingDirectory.at(location);

        assertThat(workingDirectory, is(notNullValue()));

        assertThat(workingDirectory.resolve(platform, options), is(current));
    }


    @Test
    public void shouldCreateAsSubDirectory() throws Exception
    {
        File             file             = new File("/foo");

        WorkingDirectory workingDirectory = WorkingDirectory.subDirectoryOf(file);

        assertThat(workingDirectory, is(notNullValue()));

        File expected = new File(file, "myapp");

        assertThat(workingDirectory.resolve(platform, new Options(DisplayName.of("myapp"))), is(expected));
    }


    @Test
    public void shouldCreateAsSubDirectoryOfNullParent() throws Exception
    {
        File             current          = new File(System.getProperty("user.dir"));

        WorkingDirectory workingDirectory = WorkingDirectory.subDirectoryOf(null);

        assertThat(workingDirectory, is(notNullValue()));

        File expected = new File(current, "myapp");

        assertThat(workingDirectory.resolve(platform, new Options(DisplayName.of("myapp"))), is(expected));
    }


    @Test
    public void shouldCreateAtCurrentDirectory() throws Exception
    {
        File             file             = new File(System.getProperty("user.dir"));

        WorkingDirectory workingDirectory = WorkingDirectory.currentDirectory();

        assertThat(workingDirectory, is(notNullValue()));

        assertThat(workingDirectory.resolve(platform, options), is(file));
    }


    @Test
    public void shouldCreateAtTempDirectory() throws Exception
    {
        WorkingDirectory workingDirectory = WorkingDirectory.temporaryDirectory();

        assertThat(workingDirectory, is(notNullValue()));

        File file = workingDirectory.resolve(platform, options);

        assertThat(file, is(notNullValue()));
    }


    @Test
    public void shouldCreateAtExpression() throws Exception
    {
        WorkingDirectory workingDirectory = WorkingDirectory.at("/tmp/${platform.name}/${applicationName}");

        assertThat(workingDirectory, is(notNullValue()));

        File file = workingDirectory.resolve(platform, new Options(DisplayName.of("MyApp")));

        assertThat(file, is(new File("/tmp/Local/MyApp")));
    }


    @Test
    public void shouldCreateFromIterator() throws Exception
    {
        Iterator<?>      iterator         = Arrays.asList("/foo", "/bar").iterator();

        WorkingDirectory workingDirectory = WorkingDirectory.at(iterator);

        assertThat(workingDirectory, is(notNullValue()));

        File file1 = workingDirectory.resolve(platform, options);
        File file2 = workingDirectory.resolve(platform, options);

        assertThat(file1, is(new File("/foo")));
        assertThat(file2, is(new File("/bar")));
    }
}
