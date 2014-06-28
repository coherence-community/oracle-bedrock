/*
 * File: HostAddressIteratorTest.java
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

package com.oracle.tools.runtime.virtual;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HostAddressIteratorTest
{
    @Test
    public void shouldParseAddress() throws Exception
    {
        HostAddressIterator provider = new HostAddressIterator("192.168.1.200");

        assertThat(provider.hasNext(), is(true));
        assertThat(provider.next(), is("192.168.1.200"));
        assertThat(provider.next(), is("192.168.1.201"));
    }


    @Test
    public void shouldEndWhenLastPartIs255() throws Exception
    {
        HostAddressIterator provider = new HostAddressIterator("192.168.1.255");

        assertThat(provider.hasNext(), is(true));
        assertThat(provider.next(), is("192.168.1.255"));
        assertThat(provider.hasNext(), is(false));
    }


    @Test
    public void shouldFailToParseInvalidAddress() throws Exception
    {
        assertInvalidAddress(null);
        assertInvalidAddress("");
        assertInvalidAddress("foo");
        assertInvalidAddress("192.168.");
        assertInvalidAddress("256.168.1.200");
        assertInvalidAddress("192.256.1.200");
        assertInvalidAddress("192.168.256.200");
        assertInvalidAddress("192.168.1.256");
    }


    private void assertInvalidAddress(String address)
    {
        try
        {
            new HostAddressIterator(address);
            fail("Expected IllegalArgumentExeption for address " + address);
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }
}
