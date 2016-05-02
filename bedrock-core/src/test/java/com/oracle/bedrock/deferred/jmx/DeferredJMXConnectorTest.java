/*
 * File: DeferredJMXConnectorTest.java
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

package com.oracle.bedrock.deferred.jmx;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link DeferredJMXConnector}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DeferredJMXConnectorTest
{
    /**
     * Ensure that the URL is set as expected.
     *
     * @throws Exception
     */
    @Test
    public void shouldSetURL() throws Exception
    {
        String               url      = "service:jmx:rmi:///jndi/rmi://localhost:40000/jmxrmi";

        DeferredJMXConnector deferred = new DeferredJMXConnector(url);

        assertThat(deferred.getJMXConnectionURL(), is(url));
    }


    /**
     * Ensure that a {@link JMXConnector} can get acquired.
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldGetConnector() throws Exception
    {
        String                                   url        = "service:jmx:rmi:///jndi/rmi://localhost:40000/jmxrmi";

        DeferredJMXConnector.JMXConnectorBuilder builder    = mock(DeferredJMXConnector.JMXConnectorBuilder.class);
        JMXConnector                             connector  = mock(JMXConnector.class);
        JMXServiceURL                            serviceURL = new JMXServiceURL(url);

        when(builder.realize(eq(serviceURL), anyMap())).thenReturn(connector);

        DeferredJMXConnector deferred = new DeferredJMXConnector(url, builder);

        assertThat(deferred.get(), is(sameInstance(connector)));
    }


    /**
     * Ensure that environment variables are passed correctly
     *
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldGetConnectorUsingEmptyCredentials() throws Exception
    {
        String                                   url        = "service:jmx:rmi:///jndi/rmi://localhost:40000/jmxrmi";

        DeferredJMXConnector.JMXConnectorBuilder builder    = mock(DeferredJMXConnector.JMXConnectorBuilder.class);
        JMXConnector                             connector  = mock(JMXConnector.class);
        JMXServiceURL                            serviceURL = new JMXServiceURL(url);

        when(builder.realize(eq(serviceURL), anyMap())).thenReturn(connector);

        DeferredJMXConnector deferred = new DeferredJMXConnector(url, builder);

        deferred.get();

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);

        verify(builder).realize(eq(serviceURL), captor.capture());

        Map env = captor.getValue();

        assertThat((String[]) env.get(JMXConnector.CREDENTIALS), is(arrayContaining("", "")));
    }
}
