/*
 * File: PlatformPublicHostNamePropertyTest.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PlatformPublicHostNameProperty.
 *
 * @author Jonathan Knight
 */
public class PlatformPublicHostNamePropertyTest
{
    /**
     * PlatformPublicHostNameProperty's toString() method
     * should be the host name of the Platform's public address.
     */
    @Test
    public void shouldReturnPlatformAddress() throws Exception
    {
        Platform    platform = mock(Platform.class);
        InetAddress address  = InetAddress.getByName("192.10.10.1");

        when(platform.getPublicInetAddress()).thenReturn(address);

        PlatformPublicHostNameProperty property = new PlatformPublicHostNameProperty();

        property.setPlatform(platform);

        assertThat(property.toString(), is(address.getHostName()));
    }

    /**
     * PlatformPublicHostNameProperty's toString() method
     * should be localhost if Platform is null.
     */
    @Test
    public void shouldReturnLocalhost() throws Exception
    {
        PlatformPublicHostNameProperty property = new PlatformPublicHostNameProperty();

        property.setPlatform(null);

        assertThat(property.toString(), is(LocalPlatform.getInstance().getHostName()));
    }
}
