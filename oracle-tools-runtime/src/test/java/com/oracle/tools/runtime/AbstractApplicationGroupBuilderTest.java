/*
 * File: AbstractApplicationGroupBuilderTest.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.runtime.console.NullApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import org.junit.Test;

import org.mockito.Matchers;

import static org.hamcrest.Matchers.contains;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit Tests for {@link AbstractApplicationGroupBuilder}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class AbstractApplicationGroupBuilderTest
{
    /**
     * Ensure that an ApplicationGroupBuilder will track the Applications
     * built with it.
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldRealizeApplicationUsingBuilders() throws Exception
    {
        ApplicationBuilder builder1    = mock(ApplicationBuilder.class);
        ApplicationBuilder builder2    = mock(ApplicationBuilder.class);
        ApplicationSchema  schema1     = mock(ApplicationSchema.class);
        ApplicationSchema  schema2     = mock(ApplicationSchema.class);
        Application        application = mock(Application.class);

        when(builder1.realize(Matchers.<ApplicationSchema>any(), Matchers.<String>any(),
                              Matchers.<ApplicationConsole>any())).thenReturn(application);
        when(builder2.realize(Matchers.<ApplicationSchema>any(), Matchers.<String>any(),
                              Matchers.<ApplicationConsole>any())).thenReturn(application);

        AbstractApplicationGroupBuilder groupBuilder = new AbstractApplicationGroupBuilderStub();

        groupBuilder.addBuilder(builder1, schema1, "testA", 2);
        groupBuilder.addBuilder(builder2, schema2, "testB", 1);
        groupBuilder.realize();

        verify(builder1).realize(same(schema1), eq("testA-1"), isA(NullApplicationConsole.class));
        verify(builder1).realize(same(schema1), eq("testA-2"), isA(NullApplicationConsole.class));
        verify(builder2).realize(same(schema2), eq("testB-1"), isA(NullApplicationConsole.class));
    }


    /**
     * Ensure that an ApplicationGroupBuilder will track the Applications
     * built with it.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldCallCreateApplicationGroupWithApplications() throws Exception
    {
        ApplicationBuilder builder = mock(ApplicationBuilder.class);
        ApplicationSchema  schema  = mock(ApplicationSchema.class);
        Application        app1    = mock(Application.class);
        Application        app2    = mock(Application.class);

        when(builder.realize(Matchers.<ApplicationSchema>any(), Matchers.<String>any(),
                             Matchers.<ApplicationConsole>any())).thenReturn(app1,
                                                                             app2);

        AbstractApplicationGroupBuilderStub groupBuilder = new AbstractApplicationGroupBuilderStub();

        groupBuilder.addBuilder(builder, schema, "test", 2);
        groupBuilder.realize();

        assertThat(groupBuilder.applications, contains(app1, app2));
    }


    /**
     * Ensure an ApplicationGroupBuilder will pass in a shared ApplicationConsole.
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldPassConsoleToBuilders() throws Exception
    {
        ApplicationBuilder builder1    = mock(ApplicationBuilder.class);
        ApplicationSchema  schema1     = mock(ApplicationSchema.class);
        Application        application = mock(Application.class);

        when(builder1.realize(Matchers.<ApplicationSchema>any(), Matchers.<String>any(),
                              Matchers.<ApplicationConsole>any())).thenReturn(application);

        AbstractApplicationGroupBuilder groupBuilder = new AbstractApplicationGroupBuilderStub();

        groupBuilder.addBuilder(builder1, schema1, "testA", 1);

        SystemApplicationConsole console = new SystemApplicationConsole();

        groupBuilder.realize(console);

        verify(builder1).realize(same(schema1), eq("testA-1"), same(console));
    }


    private class AbstractApplicationGroupBuilderStub extends AbstractApplicationGroupBuilder
    {
        private List<Application> applications;


        @SuppressWarnings({"unchecked"})
        @Override
        protected ApplicationGroup createAssembly(List applications)
        {
            this.applications = applications;

            return null;
        }
    }
}
