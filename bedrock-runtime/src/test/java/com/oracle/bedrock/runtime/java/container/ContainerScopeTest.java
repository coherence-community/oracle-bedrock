/*
 * File: ContainerScopeTest.java
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

package com.oracle.bedrock.runtime.java.container;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit Tests for {@link ContainerScope}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ContainerScopeTest
{
    /**
     * Ensure that the {@link java.util.Properties} provided to a {@link ContainerScope}
     * are in the {@link ContainerScope}.
     */
    @Test
    public void shouldConstructScopeWithSpecifiedProperties()
    {
        Properties properties = new Properties();

        properties.put("key-1", "value-1");
        properties.put("key-2", "value-2");

        ContainerScope scope = new ContainerScope("test", properties);

        assertThat(scope.getName(), is("test"));
        assertThat(scope.getProperties(), is(properties));
    }


    /**
     * Ensure that {@link java.util.Properties} provided to a {@link ContainerScope}
     * are copied, not simply referenced.
     */
    @Test
    public void shouldConstructScopeWithACopyOfProperties()
    {
        Properties properties = new Properties();

        properties.put("key-1", "value-1");
        properties.put("key-2", "value-2");

        ContainerScope scope = new ContainerScope("test", properties);

        assertThat(scope.getProperties(), is(not(sameInstance(properties))));
    }


    /**
     * Ensure that we can construct a {@link ContainerScope} using
     * <code>null</code> {@link java.util.Properties}.
     */
    @Test
    public void shouldConstructScopeWithNullProperties()
    {
        ContainerScope scope = new ContainerScope("test");

        assertThat(scope.getProperties().isEmpty(), is(true));
    }
}
