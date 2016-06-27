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

package com.oracle.bedrock.io;

import com.oracle.bedrock.predicate.Predicates;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

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
        public boolean test(InetAddress address)
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
        public boolean test(InetAddress address)
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
        public boolean test(InetAddress address)
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
        public boolean test(InetAddress address)
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
        public boolean test(InetAddress address)
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
        public boolean test(InetAddress address)
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
        public boolean test(InetAddress address)
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
                predicate = Predicates.always();
            }

            return predicate.test(address);
        }
    };

    /**
     * The {@link Predicate} to test if it is possible to bind to an {@link InetAddress}.
     */
    public static final Predicate<InetAddress> BINDABLE_ADDRESS = new Predicate<InetAddress>()
    {
        @Override
        public boolean test(InetAddress address)
        {
            try (ServerSocket serverSocket = new ServerSocket(0, 1, address);
                DatagramSocket datagramSocket = new DatagramSocket(0, address))
            {
                return true;
            }
            catch (IOException e)
            {
                return false;
            }
        }

        @Override
        public String toString()
        {
            return "BINDABLE_ADDRESS";
        }
    };


    /**
     * Acquires the first {@link InetAddress} (of the machine on which this code is executing)
     * that matches the specified {@link Predicate}.
     *
     * @param predicate  the {@link InetAddress} {@link Predicate}
     *
     * @return an {@link InetAddress} or <code>null</code> if no matching {@link InetAddress}
     *
     * @throws SocketException  when an {@link InetAddress} is not available
     */
    public static InetAddress getInetAddress(Predicate<? super InetAddress> predicate) throws SocketException
    {
        for (NetworkInterface networkInterface : getNetworkInterfaces(Predicates.<NetworkInterface>always()))
        {
            for (Enumeration addresses = networkInterface.getInetAddresses(); addresses.hasMoreElements(); )
            {
                InetAddress address = (InetAddress) addresses.nextElement();

                if (predicate.test(address))
                {
                    return address;
                }
            }
        }

        return null;
    }


    /**
     * Obtains the list of {@link InetAddress}es from those specified that satisfy a {@link Predicate}.
     *
     * @param predicate  the {@link InetAddress} {@link Predicate}
     *
     * @return a {@link List} of {@link InetAddress} matching the {@link Predicate} or an empty
     *         list if no matching {@link InetAddress} can be found
     */
    public static List<InetAddress> getInetAddresses(Iterable<InetAddress>          addresses,
                                                     Predicate<? super InetAddress> predicate)
    {
        LinkedList<InetAddress> addressList = new LinkedList<>();

        for (InetAddress address : addresses)
        {
            try
            {
                if (predicate.test(address))
                {
                    addressList.add(address);
                }
            }
            catch (Exception e)
            {
                // ignore the address if an exception occurred
            }
        }

        return addressList;
    }


    /**
     * Obtains the list of {@link InetAddress}es (of the machine on which this code is executing)
     * that matches the specified {@link Predicate}.
     *
     * @param predicate  the {@link InetAddress} {@link Predicate}
     *
     * @return a {@link List} of {@link InetAddress} matching the {@link Predicate} or an empty
     *         list if no matching {@link InetAddress} can be found
     */
    public static List<InetAddress> getInetAddresses(Predicate<? super InetAddress> predicate)
    {
        LinkedList<InetAddress> addressList = new LinkedList<>();

        for (NetworkInterface networkInterface : getNetworkInterfaces(Predicates.<NetworkInterface>always()))
        {
            for (Enumeration addresses = networkInterface.getInetAddresses(); addresses.hasMoreElements(); )
            {
                InetAddress address = (InetAddress) addresses.nextElement();

                try
                {
                    if (predicate.test(address))
                    {
                        addressList.add(address);
                    }
                }
                catch (Exception e)
                {
                    // ignore the address if an exception occurred
                }
            }
        }

        return addressList;
    }


    /**
     * Obtains the list of {@link NetworkInterface}s (of the machine on which this code is executing),
     * ordered by {@link NetworkInterface#getIndex()}, that satisfy the specified {@link Predicate}.
     *
     * @param predicate  the {@link NetworkInterface} {@link Predicate}
     *
     * @return  a list of {@link NetworkInterface}s
     */
    public static List<NetworkInterface> getNetworkInterfaces(Predicate<? super NetworkInterface> predicate)
    {
        ArrayList<NetworkInterface> networkInterfaces = new ArrayList<>();

        try
        {
            // create the list of filtered network interfaces
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

            while (enumeration.hasMoreElements())
            {
                NetworkInterface networkInterface = enumeration.nextElement();

                if (predicate.test(networkInterface))
                {
                    networkInterfaces.add(networkInterface);
                }
            }

            // sort the network interfaces by index
            Collections.sort(networkInterfaces,
                             new Comparator<NetworkInterface>()
                             {
                                 @Override
                                 public int compare(NetworkInterface networkInterface1,
                                                    NetworkInterface networkInterface2)
                                 {
                                     return networkInterface1.getIndex() - networkInterface2.getIndex();
                                 }
                             });
        }
        catch (SocketException e)
        {
            // nothing to do when we've had an exception
        }

        return networkInterfaces;
    }


    /**
     * Obtains an {@link InetAddress} that is feasibly a reachable, bindable, non-loopback, site-local network address of the
     * localhost, especially in cases where a localhost is multi-homed with numerous virtual network adapters, including
     * loop-back.
     * <p>
     * This method will consult all of the network interfaces defined by the localhost to determine the
     * {@link InetAddress} that is feasibly a locally reachable network address.  When a localhost has numerous network
     * interfaces, this method will return the first available and active site-local {@link InetAddress}, like 192.168.x.x
     * or 10.10.x.x.  Should one not be available, this method will attempt to return the first non-loop-back address.
     * Should both of these approaches fail, the {@link InetAddress#getLocalHost()} will returned and failing that
     * not being available {@link InetAddress#getLoopbackAddress()} will be returned.
     *
     * @return the feasible {@link InetAddress}
     */
    public static InetAddress getFeasibleLocalHost()
    {
        try
        {
            InetAddress potentialInetAddress = null;

            // consider each of the NetworkInterfaces
            for (NetworkInterface networkInterface : getNetworkInterfaces(Predicates.<NetworkInterface>always()))
            {
                // consider each of the InetAddresses defined by the NetworkInterface
                for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    inetAddresses.hasMoreElements(); )
                {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // ensure that we can bind to the address
                    if (BINDABLE_ADDRESS.test(inetAddress))
                    {
                        // prefer non-loopback addresses
                        if (!inetAddress.isLoopbackAddress())
                        {
                            // prefer site-local addresses
                            if (inetAddress.isSiteLocalAddress())
                            {
                                // found a non-loopback site-local address!
                                return inetAddress;
                            }
                            else if (potentialInetAddress == null)
                            {
                                // discovered a non-loopback but it's not site-local address (remember it just in case!)
                                potentialInetAddress = inetAddress;
                            }
                        }
                    }
                }
            }

            if (potentialInetAddress == null)
            {
                // we didn't discover a site-local or other non-loopback address, so we'll ask Java for the localhost
                potentialInetAddress = InetAddress.getLocalHost();
            }

            if (potentialInetAddress == null)
            {
                // the localhost is undefined as well, so let's fall-back to loop-back
                potentialInetAddress = InetAddress.getLoopbackAddress();
            }

            return potentialInetAddress;
        }
        catch (Exception e)
        {
            // TODO: log once that the address can't be discovered (we're probably without a network)
            return InetAddress.getLoopbackAddress();
        }
    }


    /**
     * Obtains the wildcard address of the host.
     *
     * @return  the {@link InetAddress} representing the wildcard address
     */
    public static InetAddress getWildcardAddress()
    {
        return new InetSocketAddress(0).getAddress();
    }
}
