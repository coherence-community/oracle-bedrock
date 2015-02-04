/*
 * File: AbstractVagrantPlatformSchemaTest.java
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

package com.oracle.tools.runtime.virtual.vagrant;

import com.oracle.tools.runtime.virtual.CloseAction;

import org.junit.ClassRule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractVagrantPlatformSchemaTest
{
    /**
     * Field description
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    /**
     *
     */
    @Test
    public void shouldHaveCorrectName() throws Exception
    {
        AbstractVagrantPlatformSchema schema = new AbstractVagrantPlatformSchemaStub("Test", true, null);

        assertThat(schema.getName(), is("Test"));
    }


    /**
     *
     */
    @Test
    public void shouldHaveCorrectCloseAction() throws Exception
    {
        AbstractVagrantPlatformSchema schema = new AbstractVagrantPlatformSchemaStub("Test", true, null);

        assertThat(schema.getCloseAction(), is(CloseAction.PowerButton));
    }


    /**
     *
     */
    @Test
    public void shouldHaveCorrectWorkingDirectory() throws Exception
    {
        File                          dir        = temporaryFolder.newFolder();
        AbstractVagrantPlatformSchema schema     = new AbstractVagrantPlatformSchemaStub("Test", true, dir);
        File                          workingDir = schema.ensureWorkingDirectory("Test");

        assertThat(workingDir, is(new File(dir, "Test")));
        assertThat(workingDir.exists(), is(true));
    }


    /**
     *
     */
    @Test
    public void shouldSetWorkingDirectoryToCurrentDirectory() throws Exception
    {
        AbstractVagrantPlatformSchema schema     = new AbstractVagrantPlatformSchemaStub("Test", true, null);
        File                          workingDir = schema.ensureWorkingDirectory("Test");

        assertThat(workingDir, is(new File("./Test")));
        assertThat(workingDir.exists(), is(true));
    }


    /**
     *
     */
    @Test
    public void shouldRealizeVagrantPlatform() throws Exception
    {
        File                          dir      = temporaryFolder.newFolder();
        AbstractVagrantPlatformSchema stub     = new AbstractVagrantPlatformSchemaStub("Test", true, dir);
        AbstractVagrantPlatformSchema schema   = spy(stub);
        VagrantPlatform               platform = mock(VagrantPlatform.class);

        when(schema.realize(anyString(), any(File.class))).thenReturn(platform);

        VagrantPlatform result = schema.realize("Foo");

        assertThat(result, is(sameInstance(platform)));

        File expected = new File(new File(dir, "Foo"), "Vagrantfile");

        verify(schema).realize(eq("Foo"), eq(expected));
    }


    /**
     *
     */
    @Test
    public void shouldInstantiateVagrantPlatform() throws Exception
    {
        File                          file     = temporaryFolder.newFile();
        AbstractVagrantPlatformSchema schema   = new AbstractVagrantPlatformSchemaStub("Test", true, null);
        VagrantPlatform               platform = schema.instantiatePlatform("Foo", file, "Bar", 22, CloseAction.None);

        assertThat(platform, is(notNullValue()));
        assertThat(platform.getName(), is("Foo"));
        assertThat(platform.getOptions().get(CloseAction.class), is(CloseAction.None));
        assertThat(platform.getVagrantFile(), is(file));
        assertThat(platform.getPublicHostName(), is("Bar"));
    }


    private static class AbstractVagrantPlatformSchemaStub extends AbstractVagrantPlatformSchema
    {
        private AbstractVagrantPlatformSchemaStub(String  name,
                                                  boolean isSingleton,
                                                  File    workingDirectory)
        {
            super(name, isSingleton, workingDirectory);
        }


        @Override
        protected VagrantPlatform realize(String name,
                                          File   vagrantFile) throws IOException
        {
            return null;
        }
    }
}
