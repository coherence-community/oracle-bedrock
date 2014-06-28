/*
 * File: InfrastructureTest.java
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

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Infrastructure class
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class InfrastructureTest
{
    private Platform         platform1;
    private ClosablePlatform platform2;
    private Platform         platform3;
    private ClosablePlatform platform4;


    @Before
    public void setup()
    {
        platform1 = mock(Platform.class, "Platform1");
        platform2 = mock(ClosablePlatform.class, "Platform2");
        platform3 = mock(Platform.class, "Platform3");
        platform4 = mock(ClosablePlatform.class, "Platform4");

        when(platform1.getName()).thenReturn("P-1");
        when(platform2.getName()).thenReturn("P-2");
        when(platform3.getName()).thenReturn("P-3");
        when(platform4.getName()).thenReturn("P-4");
    }


    @Test
    public void shouldContainPlatforms() throws Exception
    {
        Map<String, Platform> platforms = new HashMap<String, Platform>();

        platforms.put("P-1", platform1);
        platforms.put("P-2", platform2);

        Infrastructure<Platform> infrastructure = new Infrastructure<Platform>(platforms);

        assertThat(infrastructure.size(), is(2));
        assertThat(infrastructure.getPlatform("P-1"), is(platform1));
        assertThat(infrastructure.getPlatform("P-2"), is((Platform) platform2));
    }


    @Test
    public void shouldAddPlatforms() throws Exception
    {
        Map<String, Platform> platforms = new HashMap<String, Platform>();

        platforms.put("P-1", platform1);
        platforms.put("P-2", platform2);

        Infrastructure<Platform> infrastructure = new Infrastructure<Platform>(platforms);

        infrastructure.addPlatform(platform3);
        infrastructure.addPlatform(platform4);

        assertThat(infrastructure.size(), is(4));
        assertThat(infrastructure.getPlatform("P-1"), is(platform1));
        assertThat(infrastructure.getPlatform("P-2"), CoreMatchers.<Platform>is(platform2));
        assertThat(infrastructure.getPlatform("P-3"), is(platform3));
        assertThat(infrastructure.getPlatform("P-4"), CoreMatchers.<Platform>is(platform4));
    }


    @Test
    public void shouldCloseAllCloseablePlatforms() throws Exception
    {
        Infrastructure<Platform> infrastructure = new Infrastructure<Platform>();

        infrastructure.addPlatform(platform1);
        infrastructure.addPlatform(platform2);
        infrastructure.addPlatform(platform3);
        infrastructure.addPlatform(platform4);

        assertThat(infrastructure.size(), is(4));

        infrastructure.close();

        assertThat(infrastructure.size(), is(0));

        verify(platform2).close();
        verify(platform4).close();
    }


    @Test
    public void shouldCloseSelectedPlatforms() throws Exception
    {
        Infrastructure<Platform> infrastructure = new Infrastructure<Platform>();

        infrastructure.addPlatform(platform1);
        infrastructure.addPlatform(platform2);
        infrastructure.addPlatform(platform3);
        infrastructure.addPlatform(platform4);

        assertThat(infrastructure.size(), is(4));

        infrastructure.closePlatform("P-1");
        infrastructure.closePlatform("P-2");

        assertThat(infrastructure.size(), is(2));
        assertThat(infrastructure.getPlatform("P-3"), is(platform3));
        assertThat(infrastructure.getPlatform("P-4"), CoreMatchers.<Platform>is(platform4));

        verify(platform2).close();
    }


    @Test
    public void shouldIterateOverPlatforms() throws Exception
    {
        Infrastructure<Platform> infrastructure = new Infrastructure<Platform>();

        infrastructure.addPlatform(platform1);
        infrastructure.addPlatform(platform2);
        infrastructure.addPlatform(platform3);
        infrastructure.addPlatform(platform4);

        assertThat(infrastructure, containsInAnyOrder(platform1, platform2, platform3, platform4));
    }


    /**
     * An interface to test closing {@link Closeable} {@link Platform}s.
     */
    public static interface ClosablePlatform extends Platform, Closeable
    {
    }
}
