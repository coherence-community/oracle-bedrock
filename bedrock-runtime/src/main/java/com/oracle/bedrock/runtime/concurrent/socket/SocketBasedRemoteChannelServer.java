/*
 * File: SocketBasedRemoteChannelServer.java
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

package com.oracle.bedrock.runtime.concurrent.socket;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.io.NetworkHelper;
import com.oracle.bedrock.runtime.concurrent.AbstractControllableRemoteChannel;
import com.oracle.bedrock.runtime.concurrent.AbstractRemoteChannel;
import com.oracle.bedrock.runtime.concurrent.ControllableRemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.oracle.bedrock.predicate.Predicates.allOf;

/**
 * A {@link ControllableRemoteChannel} that accepts and processes requests
 * from {@link SocketBasedRemoteChannelClient}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public class SocketBasedRemoteChannelServer extends AbstractControllableRemoteChannel
{
    /**
    * The {@link Logger} for this class.
    */
    private static final Logger LOGGER = Logger.getLogger(SocketBasedRemoteChannelServer.class.getName());

    /**
     * The {@link ServerSocket} that will be used to accept {@link SocketBasedRemoteChannelClient}
     * connections and requests.
     * <p>
     * When this is <code>null</code> the {@link SocketBasedRemoteChannelServer} is not open.
     */
    private ServerSocket serverSocket;

    /**
     * The {@link Thread} that will manage accepting {@link SocketBasedRemoteChannelClient}
     * connections.
     * <p>
     * When this is <code>null</code> the {@link SocketBasedRemoteChannelServer} is not running.
     */
    private ServerThread serverThread;

    /**
     * The current {@link SocketBasedRemoteChannel}s managed by the {@link SocketBasedRemoteChannelServer}
     * (indexed by a executor id).
     */
    private ConcurrentHashMap<Integer, SocketBasedRemoteChannel> remoteChannels;

    /**
     * Should the running {@link SocketBasedRemoteChannelServer} terminate as soon as possible?
     */
    private AtomicBoolean isTerminating;

    /**
     * The name for this server.
     */
    private String name;

    /**
     * The port that this server should bind to (or zero to use an ephemeral port).
     */
    private final int port;

    /**
     * Constructs a {@link SocketBasedRemoteChannelServer} that will accept
     * and process {@link Callable}s from {@link SocketBasedRemoteChannelClient}s.
     *
     * @param name  the name of this server
     */
    public SocketBasedRemoteChannelServer(String name)
    {
        this(name, 0);
    }

    /**
     * Constructs a {@link SocketBasedRemoteChannelServer} that will accept
     * and process {@link Callable}s from {@link SocketBasedRemoteChannelClient}s.
     *
     * @param name  the name of this server
     * @param port  the port to bind to
     */
    public SocketBasedRemoteChannelServer(String name, int port)
    {
        super();
        this.name           = name;
        this.port           = port;
        this.serverSocket   = null;
        this.serverThread   = null;
        this.remoteChannels = new ConcurrentHashMap<>();
        this.isTerminating  = new AtomicBoolean(false);
    }


    /**
     * Opens and starts the {@link SocketBasedRemoteChannelServer}.
     * <p>
     * Does nothing if the {@link SocketBasedRemoteChannelServer} is already open.
     *
     * @return the {@link java.net.InetAddress} on which the {@link SocketBasedRemoteChannelServer}
     *         is accepting requests from {@link SocketBasedRemoteChannelClient}s.
     *
     * @throws IOException  when a {@link ServerSocket} can't be established
     */
    public synchronized InetAddress open() throws IOException
    {
        if (!isOpen())
        {
            serverSocket = new ServerSocket(port);    // 0 to use an ephemeral port
            serverSocket.setReuseAddress(true);

            serverThread = new ServerThread();

            serverThread.start();
            setOpen(true);
        }

        return getInetAddress(allOf(NetworkHelper.LOOPBACK_ADDRESS, NetworkHelper.DEFAULT_ADDRESS));
    }


    /**
     * Obtains the port on which the {@link SocketBasedRemoteChannelServer} is listening.
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
     * Obtains the {@link InetAddress} on which the {@link SocketBasedRemoteChannelServer}
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

        for (SocketBasedRemoteChannel executor : remoteChannels.values())
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
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
                                           Option...         options) throws IllegalStateException
    {
        synchronized (this)
        {
            if (isOpen() &&!isTerminating.get())
            {
                List<CompletableFuture<T>> futures =
                    remoteChannels.values().stream().map((channel) -> channel.submit(callable))
                    .collect(Collectors.toList());

                if (futures.isEmpty())
                {
                    throw new IllegalStateException("Failed to submit the request [" + callable
                                                    + "].  There are no RemoteChannels connected");
                }

                return (CompletableFuture<T>) CompletableFuture.anyOf(futures.toArray(new CompletableFuture[futures.size()]));
            }
            else
            {
                throw new IllegalStateException("Can't submit the request [" + callable
                                                + " as the RemoteChannel is closing or is closed");
            }
        }
    }


    @Override
    public CompletableFuture<Void> submit(RemoteRunnable runnable,
                                          Option...      options) throws IllegalStateException
    {
        synchronized (this)
        {
            if (isOpen() &&!isTerminating.get())
            {
                List<CompletableFuture<?>> futures =
                    remoteChannels.values().stream().map((channel) -> channel.submit(runnable))
                    .collect(Collectors.toList());

                if (futures.isEmpty())
                {
                    throw new IllegalStateException("Failed to submit the request [" + runnable
                                                    + "].  There are no RemoteChannels connected");
                }

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
            }
            else
            {
                throw new IllegalStateException("Can't submit the request [" + runnable
                                                + "] as the RemoteChannel is closing or is closed");
            }
        }
    }


    @Override
    public void addListener(RemoteEventListener listener,
                            Option...           options)
    {
        super.addListener(listener, options);

        // now add the listener to all of the RemoteChannels
        remoteChannels.forEach((id, remoteChannel) -> remoteChannel.addListener(listener, options));
    }


    @Override
    public void removeListener(RemoteEventListener listener,
                               Option...           options)
    {
        super.removeListener(listener, options);

        // now remove the listener from all of the RemoteChannels
        remoteChannels.forEach((id, remoteChannel) -> remoteChannel.removeListener(listener, options));
    }


    @Override
    public CompletableFuture<Void> raise(RemoteEvent event,
                                         Option...   options)
    {
        if (isOpen())
        {
            List<CompletableFuture<?>> futures = remoteChannels.values().stream().map((channel) -> {
                                                         try
                                                         {
                                                             return channel.raise(event, options);
                                                         }
                                                         catch (Throwable e)
                                                         {
                                                             // we ignore exceptions when a RemoteChannel fails to raise (probably because it is closing)
                                                             return CompletableFuture.completedFuture(null);
                                                         }
                                                     }).collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        }

        return CompletableFuture.completedFuture(null);
    }


    /**
     * Obtains the currently connected {@link SocketBasedRemoteChannel}s.
     *
     * @return an {@link Iterable} over the currently connected {@link SocketBasedRemoteChannel}s
     */
    public Iterable<SocketBasedRemoteChannel> getRemoteChannels()
    {
        return remoteChannels.values();
    }


    /**
     * The {@link Thread} used to manage communication with a single {@link RemoteChannel}.
     */
    private class ServerThread extends Thread
    {
        /**
         * The {@link SocketBasedRemoteChannel} to use to communicate with the remote process.
         */
        private SocketBasedRemoteChannel remoteChannel;


        /**
         * Create a {@link ServerThread}
         */
        private ServerThread()
        {
            super("RemoteChannelServer-" + name);
        }


        @Override
        public void run()
        {
            int channelId = 0;

            while (!isTerminating.get())
            {
                // determine the next channel id
                int remoteChannelId = ++channelId;
                boolean connected = false;
                int attempts = 0;
                int maxAttempts = 5;

                while (!connected && attempts < maxAttempts)
                {
                    attempts++;
                    try
                    {
                        Socket                   socket  = serverSocket.accept();
                        SocketBasedRemoteChannel channel = new SocketBasedRemoteChannel(socket);

                        // add all of the RemoteChannelServer RemoteEventListeners to the RemoteChannel
                        eventListenersByStreamName.forEach((streamName,
                            listeners) -> listeners.forEach(listener -> channel.addListener(listener, streamName)));

                        // add all of the RemoteChannelServer ChannelListeners to the RemoteChannel
                        channelListeners.forEach(channel::addListener);

                        // open the channel to for communication
                        channel.open();

                        if (channel.isOpen())
                        {
                            // remember our the RemoteChannel
                            remoteChannels.put(remoteChannelId, channel);
                            remoteChannel = channel;
                            connected = true;
                        }
                        else
                        {
                            attempts--;
                            LOGGER.severe("SocketBasedRemoteChannelServer \"" + name + "\" Rejected connection on " + socket);
                        }
                    }
                    catch (Throwable e)
                    {
                        if (remoteChannel != null && remoteChannel.isOpen())
                        {
                        try
                            {
                            remoteChannel.onClose();
                            }
                        catch (Throwable t)
                            {
                            // ignored
                            }
                        }
                        remoteChannel = null;
                        remoteChannels.remove(remoteChannelId);
                    }
                }
                isTerminating.compareAndSet(false, !connected);
            }
        }
    }
}
