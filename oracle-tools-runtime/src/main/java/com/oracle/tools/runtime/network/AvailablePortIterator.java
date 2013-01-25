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
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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
 * An {@link AvailablePortIterator} is an {@link Iterator} implementation that
 * lazily performs a port scanning on a specified address to determine what
 * {@link ServerSocket} ports are available.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
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
    private final static int MAXIMUM_PORT = 65535;

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
    private InetAddress m_inetAddress;

    /**
     * The start of the port range in which scanning will occur.
     */
    private int m_portRangeStart;

    /**
     * The end of the port range in which scanning will occur.
     */
    private int m_portRangeEnd;

    /**
     * A {@link Queue} of available {@link ServerSocket} and {@link DatagramSocket}s.
     * <p>
     * Connections to these remain open until they are requested using
     * {@link #next()}.
     */
    private Queue<ServerSocket> m_availableSockets;

    /**
     * The last port checked for availability
     */
    private int m_lastCheckedPort;


    /**
     * Constructs an {@link AvailablePortIterator}.
     * <p>
     * Defaults to using the loop back address and full port range.
     *
     * @throws UnknownHostException  when the loop back address can not be determined
     */
    public AvailablePortIterator() throws UnknownHostException
    {
        this(InetAddress.getByName(Constants.LOCAL_HOST), MINIMUM_PORT, MAXIMUM_PORT);
    }


    /**
     * Constructs an {@link AvailablePortIterator}
     * <p>
     * Defaults to using the loop back address and starting port.
     *
     * @param portRangeStart  the port at which to start scanning (inclusive)
     *
     * @throws UnknownHostException  when the loop back address can not be determined
     */
    public AvailablePortIterator(int portRangeStart) throws UnknownHostException
    {
        this(InetAddress.getByName(Constants.LOCAL_HOST), portRangeStart, MAXIMUM_PORT);
    }


    /**
     * Constructs an {@link AvailablePortIterator}.
     * <p>
     * Defaults to using the loop back address but the specified port range.
     *
     * @param portRangeStart  the port at which to start scanning (inclusive)
     * @param portRangeEnd    the port at which to stop scanning (inclusive)
     *
     * @throws UnknownHostException  when the loop back address can not be determined
     */
    public AvailablePortIterator(int portRangeStart,
                                 int portRangeEnd) throws UnknownHostException
    {
        this(InetAddress.getByName(Constants.LOCAL_HOST), portRangeStart, portRangeEnd);
    }


    /**
     * Constructs an {@link AvailablePortIterator}.
     *
     * @param inetAddress     the {@link InetAddress} on which to scan ports (typically loopback or localhost)
     * @param portRangeStart  the port at which to start scanning (inclusive)
     * @param portRangeEnd    the port at which to stop scanning (inclusive)
     */
    public AvailablePortIterator(InetAddress inetAddress,
                                 int portRangeStart,
                                 int portRangeEnd)
    {
        m_inetAddress      = inetAddress;
        m_portRangeStart   = portRangeStart;
        m_portRangeEnd     = portRangeEnd;
        m_availableSockets = new ConcurrentLinkedQueue<ServerSocket>();
        m_lastCheckedPort  = portRangeStart - 1;

        acquireAvailablePorts(LOW_PORT_THRESHOLD, IDEAL_AVAILABLE_PORTS);
    }


    /**
     * Constructs an {@link AvailablePortIterator}.
     *
     * @param host            the host name or IP address on which to scan (as a String)
     * @param portRangeStart  the port at which to start scanning (inclusive)
     * @param portRangeEnd    the port at which to stop scanning (inclusive)
     */
    public AvailablePortIterator(String host,
                                 int portRangeStart,
                                 int portRangeEnd) throws UnknownHostException
    {
        this(InetAddress.getByName(host), portRangeStart, portRangeEnd);
    }


    /**
     * Obtains the {@link InetAddress} on which port scanning is occurring.
     *
     * @return an {@link InetAddress}
     */
    public InetAddress getInetAddress()
    {
        return m_inetAddress;
    }


    /**
     * Attempts to determine if the specified {@link ServerSocket} port is
     * available. If it is, a connection is made and kept ut from being taken
     * by other processes (or iterators).  When an available port is requested
     * (via a call to {@link #next()}) the socket is closed and returned.
     *
     * @param port  the port to test
     *
     * @return <code>true</code> if the specified port was determined to be
     *         available and thus is now reserved for returning via {@link #next()}.
     */
    private boolean isPortAvailable(int port)
    {
        if (port < m_portRangeStart || port > m_portRangeEnd)
        {
            return false;
        }
        else
        {
            // attempt to connect to both a server and datagram socket (we want both to be free)
            ServerSocket serverSocket = null;

            try
            {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(false);
                serverSocket.bind(new InetSocketAddress(m_inetAddress, port));

                m_availableSockets.add(serverSocket);

                return true;
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
        int count = m_availableSockets.size() < minimumThreshold
                    ? m_availableSockets.size() + idealQueueSize : m_availableSockets.size();

        while (m_availableSockets.size() < count && m_lastCheckedPort < m_portRangeEnd)
        {
            isPortAvailable(++m_lastCheckedPort);
        }

        return m_availableSockets.size();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Integer> iterator()
    {
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        return acquireAvailablePorts(LOW_PORT_THRESHOLD, IDEAL_AVAILABLE_PORTS) > 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Integer next()
    {
        if (hasNext())
        {
            boolean reacquire;
            int     port;

            do
            {
                ServerSocket socket;

                synchronized (this)
                {
                    // ensure we've acquired at least one port
                    if (acquireAvailablePorts(LOW_PORT_THRESHOLD, IDEAL_AVAILABLE_PORTS) == 0)
                    {
                        throw new UnsupportedOperationException("Exhausted all available ports");
                    }
                    else
                    {
                        // grab the open socket
                        socket = m_availableSockets.remove();
                    }
                }

                // determine the port from the server socket
                port = socket.getLocalPort();

                // attempt to close the ports (as we're returning them)
                reacquire = false;

                try
                {
                    socket.close();
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("It's illegal to attempt to remove() a port from an AvailablePortIterator");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        ArrayList<ServerSocket> list    = new ArrayList<ServerSocket>(m_availableSockets);

        StringBuilder           builder = new StringBuilder("");

        for (ServerSocket socket : list)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }

            builder.append(socket.getLocalPort());
        }

        return "AvailablePortIterator{" + builder.toString() + "}";
    }
}
