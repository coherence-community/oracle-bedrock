/*
 * File: AvailablePortIteratorTest.java
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
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Functional tests for the {@link AvailablePortIterator}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class AvailablePortIteratorTest
{
    /**
     * Ensure that at least one port is available.
     */
    @Test
    public void shouldFindAvailablePort()
    {
        AvailablePortIterator iterator = new AvailablePortIterator(30000, 30030);

        Assert.assertThat(iterator.hasNext(), is(true));
    }


    /**
     * Ensure that at least one port is available on bindable addresses
     */
    @Test
    public void shouldFindAvailablePortOnBindableAddresses()
    {
        // acquire the list of bindable addresses
        List<InetAddress> addresses = NetworkHelper.getInetAddresses(NetworkHelper.BINDABLE_ADDRESS);

        // establish an available port iterator over those addresses
        AvailablePortIterator iterator = new AvailablePortIterator(30000, 30030, addresses);

        Assert.assertThat(iterator.hasNext(), is(true));
    }


    /**
     * Ensure that two {@link AvailablePortIterator}s don't find the
     * same ports.
     */
    @Test
    public void shouldFindDifferentAvailablePorts()
    {
        AvailablePortIterator availablePortIterator1 = new AvailablePortIterator(30000, 30100);
        AvailablePortIterator availablePortIterator2 = new AvailablePortIterator(30000, 30100);

        ArrayList<Integer>    availablePorts1        = new ArrayList<Integer>();

        for (int i = 0; i < 10; i++)
        {
            int port = availablePortIterator1.next();

            availablePorts1.add(port);
        }

        ArrayList<Integer> availablePorts2 = new ArrayList<Integer>();

        for (int i = 0; i < 10; i++)
        {
            int port = availablePortIterator2.next();

            availablePorts2.add(port);
        }

        MatcherAssert.assertThat(availablePorts1, not(equalTo(availablePorts2)));
    }
}
