/*
 * File: NetworkHelperTest.java
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

package com.oracle.bedrock.runtime.network;

import com.oracle.bedrock.io.NetworkHelper;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

import java.net.InetAddress;

import java.util.List;

/**
 * Functional tests for the {@link NetworkHelper}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NetworkHelperIT
{
    /**
     * Ensure that there are bindable addresses
     */
    @Test
    public void shouldLocateBindableAddresses()
    {
        List<InetAddress> addresses = NetworkHelper.getInetAddresses(NetworkHelper.BINDABLE_ADDRESS);

        assertThat(addresses.size(), is(greaterThan(0)));

        for (InetAddress address : addresses)
        {
            System.out.println("Bindable Address: " + address);
        }
    }
}
