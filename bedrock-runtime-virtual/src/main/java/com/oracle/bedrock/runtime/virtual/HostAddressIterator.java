/*
 * File: HostAddressIterator.java
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

package com.oracle.bedrock.runtime.virtual;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link Iterator} to use to provide IP addresses.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 * @author Jonathan Knight
 */
public class HostAddressIterator implements Iterator<String>
{
    /**
     * The first part address the iterator will start at (e.g. 192 in 192.168.15.100)
     */
    private int part1;

    /**
     * The second part address the iterator will start at (e.g. 168 in 192.168.15.100)
     */
    private int part2;

    /**
     * The third part address the iterator will start at (e.g. 15 in 192.168.15.100)
     */
    private int part3;

    /**
     * The fourth part address the iterator will start at (e.g. 100 in 192.168.15.100)
     */
    private int part4;


    /**
     * Create a {@link HostAddressIterator}.
     *
     * @param part1  the first part address the iterator will start at (e.g. 192 in 192.168.15.100)
     * @param part2  the second part address the iterator will start at (e.g. 168 in 192.168.15.100)
     * @param part3  the third part address the iterator will start at (e.g. 15 in 192.168.15.100)
     * @param part4  the fourth part address the iterator will start at (e.g. 100 in 192.168.15.100)
     */
    private HostAddressIterator(int part1, int part2, int part3, int part4)
    {
        this.part1 = part1;
        this.part2 = part2;
        this.part3 = part3;
        this.part4 = part4;
    }


    @Override
    public synchronized boolean hasNext()
    {
        return part4 <= 255;
    }


    @Override
    public synchronized String next()
    {
        return String.format("%d.%d.%d.%d", part1, part2, part3, part4++);
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }


    /**
     * Create a {@link HostAddressIterator} starting at the specified address
     *
     * @param startingAddress  the address the iterator will start at
     *
     * @return  a {@link HostAddressIterator} starting at the specified address
     */
    public static HostAddressIterator startingAt(String startingAddress)
    {
        if (startingAddress == null)
        {
            throw new IllegalArgumentException("Address cannot be null");
        }

        Pattern pattern = Pattern.compile("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                          "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                          "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                          "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

        Matcher matcher = pattern.matcher(startingAddress);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("Not a valid IP4 address (" + startingAddress + ")");
        }

        return startingAt(Integer.parseInt(matcher.group(1)),
                          Integer.parseInt(matcher.group(2)),
                          Integer.parseInt(matcher.group(3)),
                          Integer.parseInt(matcher.group(4)));
    }


    /**
     * Create a {@link HostAddressIterator} starting at the specified address
     *
     * @param part1  the first part address the iterator will start at (e.g. 192 in 192.168.15.100)
     * @param part2  the second part address the iterator will start at (e.g. 168 in 192.168.15.100)
     * @param part3  the third part address the iterator will start at (e.g. 15 in 192.168.15.100)
     * @param part4  the fourth part address the iterator will start at (e.g. 100 in 192.168.15.100)
     *
     * @return  a {@link HostAddressIterator} starting at the specified address
     */
    public static HostAddressIterator startingAt(int part1, int part2, int part3, int part4)
    {
        return new HostAddressIterator(part1, part2, part3, part4);
    }
}
