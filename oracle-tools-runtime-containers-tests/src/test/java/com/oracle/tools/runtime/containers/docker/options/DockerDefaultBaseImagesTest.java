/*
 * File: DockerDefaultBaseImagesTest.java
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

package com.oracle.tools.runtime.containers.docker.options;

import com.oracle.tools.runtime.Application;

import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.java.AbstractJavaApplication;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerDefaultBaseImagesTest
{
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullApplicationClass() throws Exception
    {
        new DockerDefaultBaseImages(null, "foo");
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullBaseImageName() throws Exception
    {
        new DockerDefaultBaseImages(Application.class, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowBlankBaseImageName() throws Exception
    {
        new DockerDefaultBaseImages(Application.class, " \t ");
    }


    @Test
    public void shouldBeImmutableEvenIfAddingSameClass() throws Exception
    {
        DockerDefaultBaseImages tree1 = new DockerDefaultBaseImages(SimpleApplication.class, "foo");
        DockerDefaultBaseImages tree2 = tree1.with(SimpleApplication.class, "bar");

        assertThat(tree1, is(not(sameInstance(tree2))));
        assertThat(tree1.getApplicationClass().equals(tree2.getApplicationClass()), is(true));
        assertThat(tree1.getBaseImage(SimpleApplication.class), is("foo"));
        assertThat(tree2.getBaseImage(SimpleApplication.class), is("bar"));
    }


    @Test
    public void shouldGetBaseImageIfDirectMatch() throws Exception
    {
        String                          image = "oraclelinux:7.1";
        DockerDefaultBaseImages tree  = new DockerDefaultBaseImages(Application.class, image);

        assertThat(tree.getBaseImage(Application.class), is(image));
    }


    @Test
    public void shouldGetBaseImageIfClassIsSubClassAndTreeIsEmpty() throws Exception
    {
        String                          image = "oraclelinux:7.1";
        DockerDefaultBaseImages tree  = new DockerDefaultBaseImages(Application.class, image);

        assertThat(tree.getBaseImage(SimpleApplication.class), is(image));
    }


    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToAddSuperClassTree() throws Exception
    {
        DockerDefaultBaseImages tree = new DockerDefaultBaseImages(SimpleApplication.class, "foo");

        tree.with(Application.class, "Oops");
    }


    @Test
    public void shouldGetBaseImageForSubClass() throws Exception
    {
        DockerDefaultBaseImages tree = new DockerDefaultBaseImages(Application.class, "foo")
                                                              .with(SimpleApplication.class, "Simple")
                                                              .with(JavaApplication.class, "Java");

        assertThat(tree.getBaseImage(Application.class), is("foo"));
        assertThat(tree.getBaseImage(SimpleApplication.class), is("Simple"));
        assertThat(tree.getBaseImage(JavaApplication.class), is("Java"));
    }



    @Test
    public void shouldGetBaseImageForDeepSubClass() throws Exception
    {
        DockerDefaultBaseImages tree = new DockerDefaultBaseImages(Application.class, "foo")
                                                             .with(SimpleApplication.class, "Simple")
                                                             .with(JavaApplication.class, "Java1")
                                                             .with(AbstractJavaApplication.class, "Java2")
                                                             .with(SimpleJavaApplication.class, "Java3");

        assertThat(tree.getBaseImage(Application.class), is("foo"));
        assertThat(tree.getBaseImage(SimpleApplication.class), is("Simple"));
        assertThat(tree.getBaseImage(JavaApplication.class), is("Java1"));
        assertThat(tree.getBaseImage(AbstractJavaApplication.class), is("Java2"));
        assertThat(tree.getBaseImage(SimpleJavaApplication.class), is("Java3"));
    }
}
