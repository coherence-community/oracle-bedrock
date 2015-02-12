/*
 * File: AvailablePortIterator.java
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

package com.oracle.tools.runtime.network;

import java.io.IOException;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An {@link Iterator} implementation that lazily performs port scanning on a
 * specified address to determine what {@link ServerSocket} and {@link DatagramSocket}
 * ports are available.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class AvailablePortIterator implements Iterator<Integer>, Iterable<Integer>
{
    /**
     * The minimum port that can be used.
     */
    private final static int MINIMUM_PORT = 1;

    /**
     * The maximum port that can be used.
     */
    public final static int MAXIMUM_PORT = 65535;

    /**
     * The minimum number of ports allowed in the queue until we
     * start to find new ports.
     */
    private final static int LOW_PORT_THRESHOLD = 5;

    /**
     * The ideal number of ports in the queue.
     */
    private final static int IDEAL_AVAILABLE_PORTS = 10;

    /**
     * The {@link InetAddress} on which the port scanning is occurring.
     */
    private InetAddress inetAddress;

    /**
     * The start of the port range in which scanning will occur.
     */
    private int portRangeStart;

    /**
     * The end of the port range in which scanning will occur.
     */
    private int portRangeEnd;

    /**
     * A {@link Queue} of available {@link ServerSocket}s.
     * <p>
     * Connections to these sockets will remain open until they are requested using
     * {@link #next()}.
     */
    private Queue<ServerSocket> serverSockets;

    /**
     * A {@link Queue} of available {@link DatagramSocket}s
     * <p>
     * Connections to these sockets will remain open until they are requested using
     * {@link #next()}.
     */
    private Queue<DatagramSocket> datagramSockets;

    /**
     * The last port checked for availability
     */
    private int lastCheckedPort;


    /**
     * Constructs an {@link AvailablePortIterator}.
     * <p>
     * Defaults to using the wildcard address and full port range.
     *
     * @throws UnknownHostException  when the loop back address can not be determined
     */
    public AvailablePortIterator()
    {
        this(new InetSocketAddress(0).getAddress(), MINIMUM_PORT, MAXIMUM_PORT);
    }


    /**
     * Constructs an {@link AvailablePortIterator}
     * <p>
     * Defaults to using the wildcard address and a starting port.
     *
     * @param portRangeStart  the port at which to start scanning (inclusive)
     */
    public AvailablePortIterator(int portRangeStart)
    {
        this(new InetSocketAddress(0).getAddress(), portRangeStart, MAXIMUM_PORT);
    }


    /**
     * Constructs an {@link AvailablePortIterator}.
     * <p>
     * Defaults to using the wildcard address with the specified port range.
     *
     * @param portRangeStart  the port at which to start scanning (inclusive)
     * @param portRangeEnd    the port at which to destroy scanning (inclusive)
     */
    public AvailablePortIterator(int portRangeStart,
                                 int portRangeEnd)
    {
        this(new InetSocketAddress(0).getAddress(), portRangeStart, portRangeEnd);
    }


    /**
     * Constructs an {@link AvailablePortIterator}.
     *
     * @param inetAddress     the {@link InetAddress} on which to scan ports
     * @param portRangeStart  the port at which to start scanning (inclusive)
     * @param portRangeEnd    the port at which to destroy scanning (inclusive)
     */
    public AvailablePortIterator(InetAddress inetAddress,
                                 int         portRangeStart,
                                 int         portRangeEnd)
    {
        this.inetAddress     = inetAddress;
        this.portRangeStart  = portRangeStart;
        this.portRangeEnd    = portRangeEnd;
        this.serverSockets   = new ConcurrentLinkedQueue<ServerSocket>();
        this.datagramSockets = new ConcurrentLinkedQueue<DatagramSocket>();
        this.lastCheckedPort = portRangeStart - 1;

        acquireAvailablePorts(LOW_PORT_THRESHOLD, IDEAL_AVAILABLE_PORTS);
    }


    /**
     * Constructs an {@link AvailablePortIterator}.
     *
     * @param host            the host name or IP address on which to scan (as a String)
     * @param portRangeStart  the port at which to start scanning (inclusive)
     * @param portRangeEnd    the port at which to destroy scanning (inclusive)
     */
    public AvailablePortIterator(String host,
                                 int    portRangeStart,
                                 int    portRangeEnd) throws UnknownHostException
    {
        this(InetAddress.getByName(host), portRangeStart, portRangeEnd);
    }


    /**
     * Obtains the {@link InetAddress} on which port scanning is occurring, which
     * may be the wildcard address (0.0.0.0 or ::)
     *
     * @return an {@link InetAddress}
     */
    public InetAddress getInetAddress()
    {
        return inetAddress;
    }


    /**
     * Attempts to determine if the specified {@link ServerSocket} and
     * {@link DatagramSocket} port is available. If it is, a connection is made and
     * kept from being taken by other processes (or iterators).  When an available port is
     * requested (via a call to {@link #next()}) the sockets are closed and returned.
     *
     * @param port  the port to test
     *
     * @return <code>true</code> if the specified port was determined to be
     *         available and thus is now reserved for returning via {@link #next()}.
     */
    private boolean isPortAvailable(int port)
    {
        if (port < portRangeStart || port > portRangeEnd)
        {
            return false;
        }
        else
        {
            // attempt to bind to a ServerSocket on the specified port
            ServerSocket serverSocket = null;

            try
            {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(false);
                serverSocket.bind(new InetSocketAddress(inetAddress, port));
            }
            catch (IOException ioException)
            {
                if (serverSocket != null)
                {
                    try
                    {
                        serverSocket.close();
                    }
                    catch (IOException e)
                    {
                        // deliberately empty as failing here will have no effect on scanning for ports
                    }
                }

                // give up on this port as the ServerSocket isn't available
                return false;
            }

            // now attempt to bind to a DatagramSocket on the same port
            DatagramSocket datagramSocket = null;

            try
            {
                datagramSocket = new DatagramSocket(port, inetAddress);

                // success!  remember both the sockets for later retrieval
                datagramSockets.add(datagramSocket);
                serverSockets.add(serverSocket);

                return true;

            }
            catch (IOException ioException)
            {
                try
                {
                    // any failure to bind to the DatagramSocket means we have to relinquish the corresponding ServerSocket
                    serverSocket.close();

                    if (datagramSocket != null)
                    {
                        datagramSocket.close();
                    }
                }
                catch (IOException e)
                {
                    // deliberately empty as failing here will have no effect on scanning for ports
                }

                return false;
            }
        }
    }


    /**
     * Attempts to identify a specified number of the available ports.
     * Each available port is placed in a queue, ready for consumption by a
     * call to {@link #next()}.
     *
     * @param minimumThreshold  the minimum number of available ports in the queue
     *                          after which an attempt to find more is required
     * @param idealQueueSize    the ideal queue size to establish
     *
     * @return the number of available ports in the queue
     */
    private synchronized int acquireAvailablePorts(int minimumThreshold,
                                                   int idealQueueSize)
    {
        // we want at least the minimum specified number of sockets in the queue
        int count = serverSockets.size() < minimumThreshold
                    ? serverSockets.size() + idealQueueSize : serverSockets.size();

        while (serverSockets.size() < count && lastCheckedPort < portRangeEnd)
        {
            isPortAvailable(++lastCheckedPort);
        }

        return serverSockets.size();
    }


    @Override
    public Iterator<Integer> iterator()
    {
        return this;
    }


    @Override
    public boolean hasNext()
    {
        return acquireAvailablePorts(LOW_PORT_THRESHOLD, IDEAL_AVAILABLE_PORTS) > 0;
    }


    @Override
    public Integer next()
    {
        if (hasNext())
        {
            boolean reacquire;
            int     port;

            do
            {
                ServerSocket   serverSocket;
                DatagramSocket datagramSocket;

                synchronized (this)
                {
                    // ensure we've acquired at least one port
                    if (acquireAvailablePorts(LOW_PORT_THRESHOLD, IDEAL_AVAILABLE_PORTS) == 0)
                    {
                        throw new UnsupportedOperationException("Exhausted all available ports");
                    }
                    else
                    {
                        // grab an open ServerSocket
                        serverSocket = serverSockets.remove();

                        // grab the corresponding DatagramSocket
                        datagramSocket = datagramSockets.remove();
                    }
                }

                // determine the port from the server socket
                port = serverSocket.getLocalPort();

                // attempt to close the ports (as we're returning them)
                reacquire = false;

                try
                {
                    serverSocket.close();
                }
                catch (Exception e)
                {
                    // any failure to close means we should try another port
                    reacquire = true;
                }

                try
                {
                    datagramSocket.close();
                }
                catch (Exception e)
                {
                    // any failure to close means we should try another port
                    reacquire = true;
                }
            }
            while (reacquire);

            return port;
        }
        else
        {
            throw new NoSuchElementException("Attempted to iterate outside of the range of available ports");
        }
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("It's illegal to attempt to remove() a port from an AvailablePortIterator");
    }


    @Override
    public String toString()
    {
        ArrayList<ServerSocket> list    = new ArrayList<ServerSocket>(serverSockets);

        StringBuilder           builder = new StringBuilder("");

        for (ServerSocket socket : list)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }

            builder.append(socket.getLocalPort());
        }

        return "AvailablePortIterator{" + inetAddress + ":" + builder.toString() + "}";
    }
}
