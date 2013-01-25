/*
 * File: VirtualizedSystemTest.java
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

package com.oracle.tools.runtime.java.virtualization;

import com.oracle.tools.junit.AbstractTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import java.util.Properties;

/**
 * Unit and Functional Tests for {@link VirtualizedSystem}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VirtualizedSystemTest extends AbstractTest
{
    /**
     * Ensure that the {@link Properties} provided to a {@link VirtualizedSystem}
     * are in the {@link VirtualizedSystem}.
     */
    @Test
    public void testConstructionWithProperties()
    {
        Properties properties = new Properties();

        properties.put("key-1", "value-1");
        properties.put("key-2", "value-2");

        VirtualizedSystem system = new VirtualizedSystem("test", properties, System.out, System.err, null);

        assertThat(system.getName(), is("test"));
        assertThat(system.getProperties(), is(properties));
    }


    /**
     * Ensure that {@link Properties} provided to a {@link VirtualizedSystem}
     * are copied, not simply referenced.
     */
    @Test
    public void testConstructionIsACopy()
    {
        Properties properties = new Properties();

        properties.put("key-1", "value-1");
        properties.put("key-2", "value-2");

        VirtualizedSystem system = new VirtualizedSystem("test", properties, System.out, System.err, null);

        assertThat(system.getProperties(), is(not(sameInstance(properties))));
    }


    /**
     * Ensure that we can construct a {@link VirtualizedSystem} using
     * <code>null</code> {@link Properties}.
     */
    @Test
    public void testConstructionWithNullProperties()
    {
        VirtualizedSystem system = new VirtualizedSystem("test", null, System.out, System.err, null);

        assertThat(system.getProperties().isEmpty(), is(true));
    }
}
