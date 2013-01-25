/*
 * File: AbstractApplicationGroupTest.java
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

package com.oracle.tools.runtime;

import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

/**
 * Unit and Functional tests for {@link AbstractApplicationGroup}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractApplicationGroupTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldStoreApplicationsByName() throws Exception
    {
        Application       memberOne = mock(Application.class);
        Application       memberTwo = mock(Application.class);

        List<Application> members   = Arrays.asList(memberOne, memberTwo);

        when(memberOne.getName()).thenReturn("m1");
        when(memberTwo.getName()).thenReturn("m2");

        AbstractApplicationGroup group = new AbstractApplicationGroupStub(members);

        assertThat(group.getApplication("m1"), is(sameInstance(memberOne)));
        assertThat(group.getApplication("m2"), is(sameInstance(memberTwo)));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldIterateOverApplications() throws Exception
    {
        Application       memberOne = mock(Application.class);
        Application       memberTwo = mock(Application.class);

        List<Application> members   = Arrays.asList(memberOne, memberTwo);

        when(memberOne.getName()).thenReturn("m1");
        when(memberTwo.getName()).thenReturn("m2");

        AbstractApplicationGroup group = new AbstractApplicationGroupStub(members);

        assertThat((Iterable<Application>) group, containsInAnyOrder(memberOne, memberTwo));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldDestroyApplications() throws Exception
    {
        Application       memberOne = mock(Application.class);
        Application       memberTwo = mock(Application.class);

        List<Application> members   = Arrays.asList(memberOne, memberTwo);

        when(memberOne.getName()).thenReturn("m1");
        when(memberTwo.getName()).thenReturn("m2");

        AbstractApplicationGroup group = new AbstractApplicationGroupStub(members);

        group.destroy();

        verify(memberOne).destroy();
        verify(memberTwo).destroy();
    }


    private class AbstractApplicationGroupStub<A extends Application<A>> extends AbstractApplicationGroup<A>
    {
        private AbstractApplicationGroupStub(List<A> applications)
        {
            super(applications);
        }
    }
}
