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

package com.oracle.bedrock.runtime.java.container;

import com.oracle.bedrock.runtime.LocalPlatform;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit Tests for {@link DelegatingProperties}.
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
     * Ensure that when we start and stop the {@link Container}
     * that then isolates the {@link System#getProperties()}.
     */
    @Test
    public void shouldIsolateSystemPropertiesWhenContainerIsRunning()
    {
        Container.stop();

        Properties systemProperties = System.getProperties();

        Container.start();
        assertThat(System.getProperties(), is(instanceOf(DelegatingProperties.class)));

        Container.stop();
        assertThat(System.getProperties(), is(sameInstance(systemProperties)));

        assertThat(System.getProperties(), is(sameInstance(Container.getPlatformScope().getProperties())));
    }


    /**
     * Ensure that {@link System#getProperties()} aren't isolated when running
     * outside of a container.
     */
    @Test
    public void shouldNotIsolateSystemProperties()
    {
        Scope      scope      = Container.getPlatformScope();

        Properties properties = scope.getProperties();

        assertThat(properties, is(sameInstance(System.getProperties())));
    }


    /**
     * Ensure that we can clear scoped {@link System#getProperties()}
     * and it doesn't effect the platform {@link System#getProperties()}.
     */
    @Test
    public void shouldClearIsolatedSystemProperties()
    {
        Scope platformScope = Container.getPlatformScope();
        int   propertyCount = platformScope.getProperties().size();

        Container.start();

        Scope      scope      = Container.getDefaultScope();
        Properties properties = scope.getProperties();

        properties.clear();

        assertThat(properties.size(), is(0));

        Container.stop();

        assertThat(platformScope.getProperties().size(), is(propertyCount));
        assertThat(System.getProperties().size(), is(propertyCount));
    }


    /**
     * Ensure that we can define and access new system properties in
     * a {@link Scope}.
     *
     * @throws Exception
     */
    @Test
    public void shouldIsolateSystemPropertyUpdateAndAccess() throws Exception
    {
        System.getProperties().remove("key-1");

        assertThat(System.getProperties().containsKey("key-1"), is(false));

        Properties properties = new Properties();

        properties.setProperty("key-1", "value-1");

        PlatformScope platformScope = Container.getPlatformScope();

        ContainerScope scope = new ContainerScope("test",
                                                  platformScope.getProperties(),
                                                  LocalPlatform.get().getAvailablePorts(),
                                                  null,
                                                  false,
                                                  Container.PIPE_BUFFER_SIZE_BYTES);

        scope.getProperties().putAll(properties);

        try
        {
            Container.associateThreadWith(scope);

            assertThat(properties.containsKey("key-1"), is(true));
            assertThat((String) properties.get("key-1"), is("value-1"));
        }
        finally
        {
            Container.dissociateThread();

            assertThat(System.getProperties().containsKey("key-1"), is(false));
        }
    }


    /**
     * Ensure that properties defined in a {@link Scope}
     * hide those defined by the platform.
     *
     * @throws Exception
     */
    @Test
    public void shouldIsolateAndHideSystemProperties() throws Exception
    {
        System.setProperty("key-1", "value-1");

        Container.start();

        PlatformScope platformScope = Container.getPlatformScope();

        ContainerScope scope = new ContainerScope("test",
                                                  platformScope.getProperties(),
                                                  LocalPlatform.get().getAvailablePorts(),
                                                  null,
                                                  false,
                                                  Container.PIPE_BUFFER_SIZE_BYTES);

        scope.getProperties().setProperty("key-1", "value-2");

        try
        {
            Container.associateThreadWith(scope);

            assertThat(System.getProperties().containsKey("key-1"), is(true));
            assertThat(System.getProperty("key-1"), is("value-2"));
        }
        finally
        {
            Container.dissociateThread();
            Container.stop();

            assertThat(System.getProperty("key-1"), is("value-1"));
        }
    }
}
