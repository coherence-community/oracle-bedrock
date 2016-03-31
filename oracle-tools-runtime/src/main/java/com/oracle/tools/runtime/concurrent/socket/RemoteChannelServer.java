/*
 * File: RemoteChannelServer.java
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

package com.oracle.tools.runtime.concurrent.socket;

import com.oracle.tools.io.NetworkHelper;

import com.oracle.tools.predicate.Predicate;

import com.oracle.tools.runtime.concurrent.AbstractControllableRemoteChannel;
import com.oracle.tools.runtime.concurrent.ControllableRemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteChannel;
import com.oracle.tools.runtime.concurrent.RemoteEventStream;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.RemoteChannelListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;

import com.oracle.tools.util.CompletionListener;

import static com.oracle.tools.predicate.Predicates.allOf;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link ControllableRemoteChannel} that accepts and processes requests
 * from {@link RemoteChannelClient}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteChannelServer extends AbstractControllableRemoteChannel
{
    /**
     * The {@link ServerSocket} that will be used to accept {@link RemoteChannelClient}
     * connections and requests.
     * <p/>
     * When this is <code>null</code> the {@link RemoteChannelServer} is not open.
     */
    private ServerSocket serverSocket;

    /**
     * The {@link Thread} that will manage accepting {@link RemoteChannelClient}
     * connections.
     * <p/>
     * When this is <code>null</code> the {@link RemoteChannelServer} is not running.
     */
    private ServerThread serverThread;

    /**
     * The current {@link SocketBasedRemoteChannel}s managed by the {@link RemoteChannelServer}
     * (indexed by a executor id).
     */
    private ConcurrentHashMap<Integer, SocketBasedRemoteChannel> remoteExecutors;

    /**
     * Should the running {@link RemoteChannelServer} terminate as soon as possible?
     */
    private AtomicBoolean isTerminating;


    /**
     * Constructs a {@link RemoteChannelServer} that will accept
     * and process {@link Callable}s from {@link RemoteChannelClient}s.
     */
    public RemoteChannelServer()
    {
        this.serverSocket    = null;
        this.serverThread    = null;
        this.remoteExecutors = new ConcurrentHashMap<Integer, SocketBasedRemoteChannel>();
        this.isTerminating   = new AtomicBoolean(false);
    }


    /**
     * Opens and starts the {@link RemoteChannelServer}.
     * <p/>
     * Does nothing if the {@link RemoteChannelServer} is already open.
     *
     * @return the {@link java.net.InetAddress} on which the {@link RemoteChannelServer}
     *         is accepting requests from {@link RemoteChannelClient}s.
     */
    public synchronized InetAddress open() throws IOException
    {
        if (!isOpen())
        {
            serverSocket = new ServerSocket(0);    // use an ephemeral port
            serverSocket.setReuseAddress(true);

            serverThread = new ServerThread();

            serverThread.start();
            setOpen(true);
        }

        return getInetAddress(allOf(NetworkHelper.LOOPBACK_ADDRESS, NetworkHelper.DEFAULT_ADDRESS));
    }


    /**
     * Obtains the port on which the {@link RemoteChannelServer} is listening.
     *
     * @return the port
     */
    public synchronized int getPort()
    {
        if (serverSocket != null)
        {
            return serverSocket.getLocalPort();
        }
        else
        {
            throw new IllegalStateException("Server is closed");
        }
    }


    /**
     * Obtains the {@link InetAddress} on which the {@link RemoteChannelServer}
     * will accept connections (based on a specific {@link Predicate})
     *
     * @param predicate  the {@link Predicate} to filter {@link InetAddress}es
     *                   (or <code>null</code> indicating the default is acceptable)
     *
     * @return the {@link InetAddress}
     */
    public synchronized InetAddress getInetAddress(Predicate<InetAddress> predicate)
    {
        if (serverSocket != null)
        {
            try
            {
                predicate = predicate == null ? NetworkHelper.DEFAULT_ADDRESS : predicate;

                InetAddress inetAddress = NetworkHelper.getInetAddress(predicate);

                // when the specific inetAddess is not available, use the server socket
                return inetAddress == null ? serverSocket.getInetAddress() : inetAddress;
            }
            catch (SocketException e)
            {
                return serverSocket.getInetAddress();
            }
        }
        else
        {
            throw new IllegalStateException("Server is closed");
        }
    }


    @Override
    protected synchronized void onClose()
    {
        // we're now terminating
        isTerminating.set(true);

        for (SocketBasedRemoteChannel executor : remoteExecutors.values())
        {
            try
            {
                executor.close();
            }
            catch (Exception e)
            {
                // we don't care about exceptions for clients when we close
            }
        }

        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            // we don't care about exceptions as we close
        }
        finally
        {
            serverSocket = null;
        }
    }


    @Override
    public <T> void submit(RemoteCallable<T>     callable,
                           CompletionListener<T> listener) throws IllegalStateException
    {
        synchronized (this)
        {
            if (isOpen() &&!isTerminating.get())
            {
                int submissionCount = 0;

                for (SocketBasedRemoteChannel executor : remoteExecutors.values())
                {
                    executor.submit(callable, listener);
                    submissionCount++;
                }

                if (submissionCount == 0)
                {
                    throw new IllegalStateException("Failed to submit the request [" + callable
                                                    + "].  There are no RemoteExecutors connected");
                }
            }
            else
            {
                throw new IllegalStateException("Can't submit the request [" + callable
                                                + " as the RemoteExecutor is closing or is closed");
            }
        }
    }


    @Override
    public void submit(RemoteRunnable runnable) throws IllegalStateException
    {
        synchronized (this)
        {
            if (isOpen() &&!isTerminating.get())
            {
                int submissionCount = 0;

                for (SocketBasedRemoteChannel executor : remoteExecutors.values())
                {
                    executor.submit(runnable);
                    submissionCount++;
                }

                if (submissionCount == 0)
                {
                    throw new IllegalStateException("Failed to submit the request [" + runnable
                                                    + "].  There are no RemoteExecutors connected");
                }
            }
            else
            {
                throw new IllegalStateException("Can't submit the request [" + runnable
                                                + "] as the RemoteExecutor is closing or is closed");
            }
        }
    }


    @Override
    public RemoteEventStream ensureEventStream(String name)
    {
        synchronized (this)
        {
            if (isOpen() && !isTerminating.get())
            {
                RemoteChannel channel = serverThread.getRemoteChannel();

                return channel.ensureEventStream(name);
            }
            else
            {
                throw new IllegalStateException("Can't ensure event stream [" + name
                                                + "] as the RemoteExecutor is closing or is closed");
            }
        }
    }


    public void addRemoteEventStreamListener(String streamName, RemoteEventListener listener)
    {
        if (streamName == null)
        {
            throw new NullPointerException("The stream name cannot be null");
        }

        if (listener == null)
        {
            return;
        }

        synchronized (this)
        {
            if (isOpen() && !isTerminating.get())
            {
                serverThread.addRemoteEventStreamListener(streamName, listener);
            }
            else
            {
                throw new IllegalStateException("Can't add event stream listener to stream [" + streamName
                                                + "] as the RemoteExecutor is closing or is closed");
            }
        }
    }


    /**
     * Obtains the currently connected {@link SocketBasedRemoteChannel}s.
     *
     * @return an {@link Iterable} over the currently connected {@link SocketBasedRemoteChannel}s
     */
    public Iterable<SocketBasedRemoteChannel> getRemoteExecutors()
    {
        return remoteExecutors.values();
    }


    /**
     * The {@link Thread} used to execute remote chennel communication.
     */
    private class ServerThread extends Thread
    {
        /**
         * The {@link SocketBasedRemoteChannel} to use to communicate with the remote process.
         */
        private SocketBasedRemoteChannel remoteChannel;

        private final ConcurrentMap<String,List<RemoteEventListener>> preStartListeners;

        public final Object MONITOR = new Object();

        /**
         * Create a {@link ServerThread}
         */
        private ServerThread()
        {
            preStartListeners = new ConcurrentHashMap<>();
        }


        public void addRemoteEventStreamListener(String streamName, RemoteEventListener listener)
        {
            if (remoteChannel == null)
            {
                synchronized (MONITOR)
                {
                    if (!isTerminating.get())
                    {
                        if (remoteChannel != null)
                        {
                            remoteChannel.ensureEventStream(streamName).addEventListener(listener);
                        }
                        else
                        {
                            List<RemoteEventListener> list = preStartListeners.get(streamName);
                            if (list == null)
                            {
                                list = new ArrayList<>();

                                preStartListeners.put(streamName, list);
                            }

                            list.add(listener);
                        }
                    }
                }
            }
        }

        /**
         * Obtain the {@link RemoteChannel} being used for communication.
         *
         * @return  the {@link RemoteChannel} being used for communication
         */
        RemoteChannel getRemoteChannel()
        {
            return remoteChannel;
        }

        @Override
        public void run()
        {
            int channelId = 0;

            while (!isTerminating.get())
            {
                try
                {
                    Socket socket = serverSocket.accept();

                    synchronized (MONITOR)
                    {
                        remoteChannel = new SocketBasedRemoteChannel(++channelId, socket);

                        for (Map.Entry<String,List<RemoteEventListener>> entry : preStartListeners.entrySet())
                        {
                            String            streamName = entry.getKey();
                            RemoteEventStream stream     = remoteChannel.ensureEventStream(streamName);

                            for(RemoteEventListener listener : entry.getValue())
                            {
                                stream.addEventListener(listener);
                            }
                        }
                    }

                    // add the current listeners of the RemoteExecutorServer
                    // to the SocketBasedRemoteExecutor
                    for (RemoteChannelListener listener : getListeners())
                    {
                        remoteChannel.addListener(listener);
                    }

                    remoteExecutors.put(remoteChannel.getExecutorId(), remoteChannel);

                    remoteChannel.open();
                }
                catch (NullPointerException e)
                {
                    isTerminating.compareAndSet(false, true);
                }
                catch (IOException e)
                {
                    isTerminating.compareAndSet(false, true);
                }
            }
        }
    }
}
