/*
 * File: RemoteExecutorServer.java
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

import com.oracle.tools.runtime.concurrent.AbstractControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutorListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;

import com.oracle.tools.util.CompletionListener;

import static com.oracle.tools.predicate.Predicates.allOf;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor} that accepts and processes requests
 * from {@link RemoteExecutorClient}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteExecutorServer extends AbstractControllableRemoteExecutor
{
    /**
     * The {@link ServerSocket} that will be used to accept {@link com.oracle.tools.runtime.concurrent.socket.RemoteExecutorClient}
     * connections and requests.
     * <p/>
     * When this is <code>null</code> the {@link RemoteExecutorServer} is not open.
     */
    private ServerSocket serverSocket;

    /**
     * The {@link Thread} that will manage accepting {@link com.oracle.tools.runtime.concurrent.socket.RemoteExecutorClient}
     * connections.
     * <p/>
     * When this is <code>null</code> the {@link RemoteExecutorServer} is not running.
     */
    private Thread serverThread;

    /**
     * The current {@link com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutor}s managed by the {@link RemoteExecutorServer}
     * (indexed by a executor id).
     */
    private ConcurrentHashMap<Integer, SocketBasedRemoteExecutor> remoteExecutors;

    /**
     * Should the running {@link RemoteExecutorServer} terminate as soon as possible?
     */
    private AtomicBoolean isTerminating;


    /**
     * Constructs a {@link RemoteExecutorServer} that will accept
     * and process {@link Callable}s from {@link com.oracle.tools.runtime.concurrent.socket.RemoteExecutorClient}s.
     */
    public RemoteExecutorServer()
    {
        this.serverSocket    = null;
        this.serverThread    = null;
        this.remoteExecutors = new ConcurrentHashMap<Integer, SocketBasedRemoteExecutor>();
        this.isTerminating   = new AtomicBoolean(false);
    }


    /**
     * Opens and starts the {@link RemoteExecutorServer}.
     * <p/>
     * Does nothing if the {@link RemoteExecutorServer} is already open.
     *
     * @return the {@link java.net.InetAddress} on which the {@link RemoteExecutorServer}
     *         is accepting requests from {@link com.oracle.tools.runtime.concurrent.socket.RemoteExecutorClient}s.
     */
    public synchronized InetAddress open() throws IOException
    {
        if (!isOpen())
        {
            serverSocket = new ServerSocket(0);    // use an ephemeral port
            serverSocket.setReuseAddress(true);

            serverThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    int channelId = 0;

                    while (!isTerminating.get())
                    {
                        try
                        {
                            Socket                    socket   = serverSocket.accept();
                            SocketBasedRemoteExecutor executor = new SocketBasedRemoteExecutor(++channelId, socket);

                            // add the current listeners of the RemoteExecutorServer
                            // to the SocketBasedRemoteExecutor
                            for (RemoteExecutorListener listener : getListeners())
                            {
                                executor.addListener(listener);
                            }

                            remoteExecutors.put(executor.getExecutorId(), executor);

                            executor.open();
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
            });

            serverThread.start();
            setOpen(true);
        }

        return getInetAddress(allOf(NetworkHelper.LOOPBACK_ADDRESS, NetworkHelper.DEFAULT_ADDRESS));
    }


    /**
     * Obtains the port on which the {@link RemoteExecutorServer} is listening.
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
     * Obtains the {@link InetAddress} on which the {@link RemoteExecutorServer}
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

                return NetworkHelper.getInetAddress(predicate);
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

        for (SocketBasedRemoteExecutor executor : remoteExecutors.values())
        {
            executor.close();
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

                for (SocketBasedRemoteExecutor executor : remoteExecutors.values())
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

                for (SocketBasedRemoteExecutor executor : remoteExecutors.values())
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
                                                + " as the RemoteExecutor is closing or is closed");
            }
        }
    }


    /**
     * Obtains the currently connected {@link SocketBasedRemoteExecutor}s.
     *
     * @return an {@link Iterable} over the currently connected {@link SocketBasedRemoteExecutor}s
     */
    public Iterable<SocketBasedRemoteExecutor> getRemoteExecutors()
    {
        return remoteExecutors.values();
    }
}
