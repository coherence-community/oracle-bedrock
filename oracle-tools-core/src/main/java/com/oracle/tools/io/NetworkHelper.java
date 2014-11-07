/*
 * File: NetworkHelper.java
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

package com.oracle.tools.io;

import com.oracle.tools.predicate.Predicate;

import static com.oracle.tools.predicate.Predicates.always;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.util.Enumeration;

/**
 * Common Network utilities.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NetworkHelper
{
    /**
     * The {@link Predicate} for the LOOPBACK {@link InetAddress}.
     */
    public static final Predicate<InetAddress> LOOPBACK_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            return address != null && address.isLoopbackAddress();
        }

        @Override
        public String toString()
        {
            return "LOOPBACK";
        }
    };

    /**
     * The {@link Predicate} for the NON_LOOPBACK {@link InetAddress}.
     */
    public static final Predicate<InetAddress> NON_LOOPBACK_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            return address != null &&!address.isLoopbackAddress();
        }

        @Override
        public String toString()
        {
            return "NON-LOOPBACK";
        }
    };

    /**
     * The {@link Predicate} for the IPv4 {@link InetAddress}.
     */
    public static final Predicate<InetAddress> IPv4_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            return address instanceof Inet4Address;
        }

        @Override
        public String toString()
        {
            return "IPv4-ADDRESS";
        }
    };

    /**
     * The {@link Predicate} for the IPv6 {@link InetAddress}.
     */
    public static final Predicate<InetAddress> IPv6_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            return address instanceof Inet6Address;
        }

        @Override
        public String toString()
        {
            return "IPv6-ADDRESS";
        }
    };

    /**
     * The {@link Predicate} for the Link Local {@link InetAddress}.
     */
    public static final Predicate<InetAddress> LINK_LOCAL_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            return address != null && address.isLinkLocalAddress();
        }

        @Override
        public String toString()
        {
            return "LINK-LOCAL-ADDRESS";
        }
    };

    /**
     * The {@link Predicate} for any local {@link InetAddress}.
     */
    public static final Predicate<InetAddress> ANY_LOCAL_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            return address != null && address.isAnyLocalAddress();
        }

        @Override
        public String toString()
        {
            return "ANY-LOCAL-ADDRESS";
        }
    };

    /**
     * The {@link Predicate} for the DEFAULT {@link InetAddress}, based on the
     * defined system properties java.net.preferIPv4Stack and java.net.preferIPv6Addresses.
     */
    public static final Predicate<InetAddress> DEFAULT_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean evaluate(InetAddress address)
        {
            Predicate<InetAddress> predicate;

            if (Boolean.getBoolean("java.net.preferIPv4Stack"))
            {
                predicate = NetworkHelper.IPv4_ADDRESS;
            }
            else if (Boolean.getBoolean("java.net.preferIPv6Addresses"))
            {
                predicate = NetworkHelper.IPv6_ADDRESS;
            }
            else
            {
                predicate = always();
            }

            return predicate.evaluate(address);
        }
    };


    /**
     * Acquires the first {@link InetAddress} (of the machine on which this code is executing)
     * that matches the specified {@link Predicate}.
     *
     * @return an {@link InetAddress} or <code>null</code> if no matching {@link InetAddress}
     *
     * @throws SocketException
     */
    public static InetAddress getInetAddress(Predicate<InetAddress> predicate) throws SocketException
    {
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements())
        {
            NetworkInterface i = (NetworkInterface) interfaces.nextElement();

            for (Enumeration addresses = i.getInetAddresses(); addresses.hasMoreElements(); )
            {
                InetAddress address = (InetAddress) addresses.nextElement();

                if (predicate.evaluate(address))
                {
                    return address;
                }
            }
        }

        return null;
    }
}
