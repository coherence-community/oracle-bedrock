/*
 * File: DelegatingPropertiesTest.java
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

package com.oracle.tools.runtime.java.virtualization;

import com.oracle.tools.junit.AbstractTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.assertThat;

import java.util.Properties;

/**
 * Unit tests for {@link DelegatingProperties}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class DelegatingPropertiesTest
{
    /**
     * Ensure that when we start and stop {@link Virtualization} that the
     * {@link System#getProperties()} is replaced as expected with the
     * physical system.
     */
    @Test
    public void testStartAndStoppingVirtualizationOfSystemProperties()
    {
        Virtualization.stop();

        Properties systemProperties = System.getProperties();

        Virtualization.start();
        assertThat(System.getProperties(), is(instanceOf(DelegatingProperties.class)));

        Virtualization.stop();
        assertThat(System.getProperties(), is(sameInstance(systemProperties)));

        assertThat(System.getProperties(), is(sameInstance(Virtualization.getPhysicalSystem().getProperties())));
    }


    /**
     * Ensure that {@link System#getProperties()} when not virtualized
     * returns the physical system properties.
     */
    @Test
    public void testVirtualizationOfSystemProperties()
    {
        VirtualizedSystem system     = Virtualization.getPhysicalSystem();

        Properties        properties = system.getProperties();

        assertThat(properties, is(sameInstance(System.getProperties())));
    }


    /**
     * Ensure that we can clear virtualized {@link System#getProperties()}
     * and it doesn't effect the physical {@link System#getProperties()}.
     */
    @Test
    public void testClearingVirtualizedSystemProperties()
    {
        VirtualizedSystem physicalSystem = Virtualization.getPhysicalSystem();
        int               propertyCount  = physicalSystem.getProperties().size();

        Virtualization.start();

        VirtualizedSystem virtualSystem = Virtualization.getSystem();
        Properties        properties    = virtualSystem.getProperties();

        properties.clear();

        assertThat(properties.size(), is(0));

        Virtualization.stop();

        assertThat(physicalSystem.getProperties().size(), is(propertyCount));
        assertThat(System.getProperties().size(), is(propertyCount));
    }


    /**
     * Ensure that we can define and access new system properties in
     * a {@link VirtualizedSystem}.
     *
     * @throws Exception
     */
    @Test
    public void testAccessingVirtualizedSystemProperties() throws Exception
    {
        System.getProperties().remove("key-1");

        assertThat(System.getProperties().containsKey("key-1"), is(false));

        Properties properties = new Properties();

        properties.setProperty("key-1", "value-1");

        VirtualizedSystem virtualizedSystem = new VirtualizedSystem("test", Virtualization.getPhysicalSystem());

        virtualizedSystem.getProperties().putAll(properties);

        try
        {
            Virtualization.associateThreadWith(virtualizedSystem);

            assertThat(properties.containsKey("key-1"), is(true));
            assertThat((String) properties.get("key-1"), is("value-1"));
        }
        finally
        {
            Virtualization.dissociateThread();

            assertThat(System.getProperties().containsKey("key-1"), is(false));
        }
    }


    /**
     * Ensure that properties defined in a {@link VirtualizedSystem} hide
     * those defined in the physical system.
     *
     * @throws Exception
     */
    @Test
    public void testVirtualizedSystemPropertiesHidePhysicalSystemProperties() throws Exception
    {
        System.setProperty("key-1", "value-1");

        Virtualization.start();

        VirtualizedSystem virtualizedSystem = new VirtualizedSystem("test", Virtualization.getPhysicalSystem());

        virtualizedSystem.getProperties().setProperty("key-1", "value-2");

        try
        {
            Virtualization.associateThreadWith(virtualizedSystem);

            assertThat(System.getProperties().containsKey("key-1"), is(true));
            assertThat(System.getProperty("key-1"), is("value-2"));
        }
        finally
        {
            Virtualization.dissociateThread();
            Virtualization.stop();

            assertThat(System.getProperty("key-1"), is("value-1"));
        }
    }
}
