/*
 * File: SocketBasedRemoteExecutor.java
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

import com.oracle.tools.runtime.concurrent.AbstractControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutorListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;

import com.oracle.tools.util.CompletionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.Constructor;

import java.net.InetAddress;
import java.net.Socket;

import java.util.HashMap;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Socket}-based implementation of a {@link com.oracle.tools.runtime.concurrent.RemoteExecutor}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SocketBasedRemoteExecutor extends AbstractControllableRemoteExecutor
{
    /**
     * The unique identity of the {@link SocketBasedRemoteExecutor}.
     */
    private int executorId;

    /**
     * The {@link Socket} over which {@link Callable}s will be sent and accepted.
     */
    private Socket socket;

    /**
     * The {@link java.io.ObjectOutputStream} to the {@link Socket}.
     * <p/>
     * When this is <code>null</code> the {@link SocketBasedRemoteExecutor} is not connected.
     */
    private ObjectOutputStream output;

    /**
     * The {@link java.io.ObjectInputStream} from the {@link Socket}.
     * <p/>
     * When this is <code>null</code> the {@link SocketBasedRemoteExecutor} is not connected.
     */
    private ObjectInputStream input;

    /**
     * The {@link ExecutorService} for executing {@link Callable}s asynchronously.
     */
    private ExecutorService executorService;

    /**
     * The {@link ExecutorService} for executing received {@link Operation}s asynchronously.
     */
    private ExecutorService requestExecutorService;

    /**
     * The {@link Thread} to read {@link Callable}s from the {@link Socket}.
     * <p/>
     * When this is <code>null</code> the {@link SocketBasedRemoteExecutor} is not connected.
     */
    private Thread requestAcceptorThread;

    /**
     * A flag to indicate if the {@link SocketBasedRemoteExecutor} {@link ObjectInputStream}
     * is readable.
     */
    private AtomicBoolean isReadable;

    /**
     * A flag to indicate if the {@link SocketBasedRemoteExecutor} {@link ObjectOutputStream}
     * is writable.
     */
    private AtomicBoolean isWritable;

    /**
     * The defined protocol (of {@link Operation} types) that the
     * {@link SocketBasedRemoteExecutor} can process.
     */
    private HashMap<String, Class<? extends Operation>> protocol;

    /**
     * The pending {@link CompletionListener}s to be notified of responses.
     */
    private ConcurrentHashMap<Long, CompletionListener<?>> pendingListeners;

    /**
     * The next available sequence number for a callable sent from
     * this {@link SocketBasedRemoteExecutor}.
     */
    private AtomicLong nextSequenceNumber;


    /**
     * Constructs a {@link SocketBasedRemoteExecutor} to submit and accept {@link Callable}s.
     *
     * @param socket  the {@link Socket} over which {@link Callable}s
     *                will be submit and accepted
     *
     * @throws IOException when the {@link SocketBasedRemoteExecutor} can't connect
     *                     using the {@link Socket}
     */
    public SocketBasedRemoteExecutor(int    executorId,
                                     Socket socket) throws IOException
    {
        this.executorId                 = executorId;
        this.socket                     = socket;
        this.output                     = new ObjectOutputStream(socket.getOutputStream());
        this.input                      = new ObjectInputStream(socket.getInputStream());
        this.executorService            = Executors.newSingleThreadExecutor();
        this.requestExecutorService     = Executors.newCachedThreadPool();
        this.requestAcceptorThread      = null;
        this.isReadable                 = new AtomicBoolean(true);
        this.isWritable                 = new AtomicBoolean(true);
        this.protocol                   = new HashMap<String, Class<? extends Operation>>();
        this.pendingListeners           = new ConcurrentHashMap<Long, CompletionListener<?>>();
        this.nextSequenceNumber         = new AtomicLong(0);

        // we'll always attempt to reuse addresses
        this.socket.setReuseAddress(true);

        // establish the operations that are part of the protocol
        protocol.put("CALLABLE", CallableOperation.class);
        protocol.put("RESPONSE", ResponseOperation.class);
        protocol.put("RUNNABLE", RunnableOperation.class);
    }


    /**
     * Obtains the identity of the {@link SocketBasedRemoteExecutor}.
     *
     * @return  the identity of the {@link SocketBasedRemoteExecutor}
     */
    public int getExecutorId()
    {
        return executorId;

    }


    /**
     * Obtains the {@link InetAddress} of the {@link Socket}.
     *
     * @return  the {@link InetAddress} of the {@link Socket}
     */
    public InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }


    /**
     * Obtains the {@link InetAddress} port number for the {@link Socket}.
     *
     * @return  the {@link InetAddress} port number
     */
    public int getPort()
    {
        return socket.getPort();
    }


    /**
     * Opens the {@link SocketBasedRemoteExecutor} to accept and submit {@link Callable}s.
     */
    public synchronized void open()
    {
        if (!isOpen())
        {
            setOpen(true);

            requestAcceptorThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (isReadable.get() && isWritable.get())
                    {
                        try
                        {
                            // read the operation to perform
                            String operationType = input.readUTF();

                            // read the allocated sequence number for the operation
                            long sequence = input.readLong();

                            // read the serialized operation from the stream
                            int    length = input.readInt();
                            byte[] bytes  = new byte[length];

                            input.readFully(bytes, 0, length);

                            // attempt to instantiate, deserialize and schedule the operation for execution
                            try
                            {
                                ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
                                ObjectInputStream    stream = new ObjectInputStream(buffer);

                                // instantiate the operation and initialize its state
                                Class<? extends Operation> operationClass = protocol.get(operationType);

                                Constructor<? extends SocketBasedRemoteExecutor.Operation> constructor =
                                    operationClass.getConstructor(SocketBasedRemoteExecutor.class);

                                Operation operation = constructor.newInstance(SocketBasedRemoteExecutor.this);

                                operation.read(stream);

                                // submit the operation for asynchronous execution
                                requestExecutorService.submit(new Executor(sequence, operation));
                            }
                            catch (Exception e)
                            {
                                // when we can't execute the operation we notify the sender of the exception
                                executorService.submit(new Sender(sequence, new ResponseOperation(e)));
                            }
                        }
                        catch (Exception e)
                        {
                            // the stream has become corrupted or was closed
                            // (either way there's nothing else we can read or do)
                            isReadable.set(false);
                        }
                    }

                    close();
                }
            });

            requestAcceptorThread.setName("RemoteExecutor:RequestAcceptor");
            requestAcceptorThread.setDaemon(true);
            requestAcceptorThread.start();

            for (RemoteExecutorListener listener : getListeners())
            {
                try
                {
                    listener.onOpened(this);
                }
                catch (Exception e)
                {
                    // we ignore exceptions thrown by listeners
                }
            }
        }
    }


    @Override
    protected void onClose()
    {
        // no longer accept any more requests
        isReadable.set(false);

        // gracefully shutdown the executor services
        executorService.shutdown();
        requestExecutorService.shutdown();

        // no longer write any more responses
        isWritable.set(false);

        // close the input and output streams
        try
        {
            input.close();
        }
        catch (IOException e)
        {
            // don't care
        }
        finally
        {
            input = null;
        }

        try
        {
            output.close();
        }
        catch (IOException e)
        {
            // don't care
        }
        finally
        {
            output = null;
        }

        // close the socket
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            // don't care
        }
        finally
        {
            socket = null;
        }

        // raise IllegalStateExceptions for any remaining listeners
        for (CompletionListener listener : pendingListeners.values())
        {
            try
            {
                listener.onException(new IllegalStateException("RemoteExecutor is closed"));
            }
            catch (Exception e)
            {
                // we ignore exceptions that the listener throws
            }
        }

        pendingListeners.clear();
    }


    @Override
    public <T> void submit(RemoteCallable<T>     callable,
                           CompletionListener<T> listener) throws IllegalStateException
    {
        if (isOpen())
        {
            long    sequence           = nextSequenceNumber.getAndIncrement();
            boolean isResponseRequired = listener != null;

            if (isResponseRequired)
            {
                pendingListeners.put(sequence, listener);
            }

            CallableOperation operation = new CallableOperation(isResponseRequired, callable);

            executorService.submit(new Sender(sequence, operation));
        }
        else
        {
            throw new IllegalStateException("RemoteExecutor is closed");
        }
    }


    @Override
    public void submit(RemoteRunnable runnable) throws IllegalStateException
    {
        if (isOpen())
        {
            long              sequence  = nextSequenceNumber.getAndIncrement();
            RunnableOperation operation = new RunnableOperation(runnable);

            executorService.submit(new Sender(sequence, operation));
        }
        else
        {
            throw new IllegalStateException("RemoteExecutor is closed");
        }
    }


    /**
     * An {@link Operation} to be executed in-order by a {@link SocketBasedRemoteExecutor}.
     */
    interface Operation
    {
        /**
         * Obtains the unique type of {@link Operation}.
         *
         * @return the type of {@link Operation}
         */
        public String getType();


        /**
         * Writes the {@link Operation} state to the specified {@link ObjectOutputStream},
         * so that it may later be read from an {@link ObjectInputStream} and executed.
         *
         * @param output  the {@link ObjectOutputStream}
         *
         * @throws IOException  should the write fail
         */
        public void write(ObjectOutputStream output) throws IOException;


        /**
         * Reads the {@link Operation} state from the specified {@link ObjectInputStream}.
         *
         * @param input  the {@link ObjectInputStream}
         *
         * @throws IOException  should the read fail
         */
        public void read(ObjectInputStream input) throws IOException;


        /**
         * Executes the {@link Operation}.
         *
         * @param sequence         the sequence number of the {@link Operation}
         *
         * @return Operation  the {@link Operation} to be sent back to the initiator of this
         *                    {@link Operation} (if null, nothing is returned)
         */
        public Operation execute(long sequence);
    }


    /**
     * An {@link Operation} to send and execute a {@link java.util.concurrent.Callable}.
     */
    class CallableOperation implements Operation
    {
        /**
         * Is a response required for the {@link java.util.concurrent.Callable}?
         */
        private boolean isResponseRequired;

        /**
         * The {@link java.util.concurrent.Callable} to eventually execute.
         */
        private Callable<?> callable;


        /**
         * Constructs a {@link com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutor.CallableOperation}
         * (required for construction)
         */
        public CallableOperation()
        {
        }


        /**
         * Constructs a {@link com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutor.CallableOperation}
         *
         * @param isResponseRequired
         * @param callable
         *
         * @throws NullPointerException      should the {@link Callable} be <code>null</code>
         * @throws IllegalArgumentException  should the {@link Callable} be an anonymous inner class
         */
        public CallableOperation(boolean     isResponseRequired,
                                 Callable<?> callable)
        {
            if (callable == null)
            {
                throw new NullPointerException("Callable can't be null");
            }
            else if (callable.getClass().isAnonymousClass())
            {
                throw new IllegalArgumentException("Callable can't be an anonymous inner-class");
            }
            else
            {
                this.isResponseRequired = isResponseRequired;
                this.callable           = callable;
            }
        }


        @Override
        public String getType()
        {
            return "CALLABLE";
        }


        @Override
        public Operation execute(long sequence)
        {
            Operation operation = null;

            try
            {
                // execute the Callable
                Object result = callable.call();

                if (isResponseRequired)
                {
                    operation = new ResponseOperation(result);
                }
            }
            catch (Exception e)
            {
                if (isResponseRequired)
                {
                    operation = new ResponseOperation(e);
                }
            }

            return operation;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            isResponseRequired = input.readBoolean();

            try
            {
                // read the callable or the name of the callable class
                Object object = input.readObject();

                if (object instanceof String)
                {
                    String   className     = (String) object;
                    Class<?> callableClass = Class.forName(className);

                    callable = (Callable) callableClass.newInstance();
                }
                else
                {
                    callable = (Callable) object;
                }
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException(e);
            }
            catch (InstantiationException e)
            {
                throw new IOException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new IOException(e);
            }
        }


        @Override
        public void write(ObjectOutputStream output) throws IOException
        {
            output.writeBoolean(isResponseRequired);

            // serialize the Callable (if it is!)
            if (callable instanceof Serializable)
            {
                output.writeObject(callable);
            }
            else
            {
                output.writeObject(callable.getClass().getName());
            }
        }
    }


    /**
     * Asynchronously executes an {@link Operation} that was
     * received by the {@link SocketBasedRemoteExecutor}.
     */
    class Executor implements Runnable
    {
        private long      sequence;
        private Operation operation;


        /**
         * Constructs an {@link Executor}.
         *
         * @param operation
         */
        public Executor(long      sequence,
                        Operation operation)
        {
            this.sequence  = sequence;
            this.operation = operation;
        }


        @Override
        public void run()
        {
            // execute the operation
            Operation resultingOperation = operation.execute(sequence);

            // when there's a result, asynchronously send it back
            if (resultingOperation != null)
            {
                executorService.submit(new Sender(sequence, resultingOperation));
            }
        }
    }


    /**
     * An {@link Operation} to send and deliver a response.
     */
    class ResponseOperation implements Operation
    {
        /**
         * The response.
         */
        private Object response;


        /**
         * Constructs a {@link com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutor.ResponseOperation}
         * (required for construction)
         */
        public ResponseOperation()
        {
        }


        /**
         * Constructs a {@link com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutor.ResponseOperation}
         *
         * @param response  the response
         */
        public ResponseOperation(Object response)
        {
            this.response = response;
        }


        @Override
        public String getType()
        {
            return "RESPONSE";
        }


        @Override
        public Operation execute(long sequence)
        {
            CompletionListener listener = pendingListeners.remove(sequence);

            if (listener != null)
            {
                try
                {
                    if (response instanceof Exception)
                    {
                        listener.onException((Exception) response);
                    }
                    else
                    {
                        listener.onCompletion(response);
                    }
                }
                catch (Exception e)
                {
                    // we ignore any exceptions that the listener may throw
                }
            }

            return null;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            try
            {
                response = input.readObject();
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException(e);
            }
        }


        @Override
        public void write(ObjectOutputStream output) throws IOException
        {
            output.writeObject(response);
        }


        @Override
        public String toString()
        {
            return "ResponseOperation{response=" + response + "}";
        }
    }


    /**
     * An {@link Operation} to send and execute a {@link java.lang.Runnable}.
     */
    class RunnableOperation implements Operation
    {
        /**
         * The {@link java.lang.Runnable} to eventually execute.
         */
        private Runnable runnable;


        /**
         * Constructs a {@link RunnableOperation}
         * (required for serialization)
         */
        public RunnableOperation()
        {
        }


        /**
         * Constructs a {@link RunnableOperation}
         *
         * @param runnable  the {@link Runnable} to run remotely
         *
         * @throws NullPointerException      should the {@link Runnable} be <code>null</code>
         * @throws IllegalArgumentException  should the {@link Runnable} be an anonymous inner class
         */
        public RunnableOperation(Runnable runnable)
        {
            if (runnable == null)
            {
                throw new NullPointerException("Runnable can't be null");
            }
            else if (runnable.getClass().isAnonymousClass())
            {
                throw new IllegalArgumentException("Runnable can't be an anonymous inner-class");
            }
            else
            {
                this.runnable = runnable;
            }
        }


        @Override
        public String getType()
        {
            return "RUNNABLE";
        }


        @Override
        public Operation execute(long sequence)
        {
            try
            {
                runnable.run();
            }
            catch (Exception e)
            {
                // SKIP: do nothing if there is an exception
            }

            return null;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            try
            {
                // read the callable or the name of the callable class
                Object object = input.readObject();

                if (object instanceof String)
                {
                    String className = (String) object;

                    runnable = (Runnable) Class.forName(className).newInstance();
                }
                else
                {
                    runnable = (Runnable) object;
                }
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException(e);
            }
            catch (InstantiationException e)
            {
                throw new IOException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new IOException(e);
            }
        }


        @Override
        public void write(ObjectOutputStream output) throws IOException
        {
            // serialize the Runnable (if it is!)
            if (runnable instanceof Serializable)
            {
                output.writeObject(runnable);
            }
            else
            {
                output.writeObject(runnable.getClass().getName());
            }
        }
    }


    /**
     * Asynchronously sends an {@link Operation} over the
     * {@link ObjectOutputStream} for the {@link SocketBasedRemoteExecutor}.
     */
    class Sender implements Runnable
    {
        /**
         * The sequence number of the {@link Operation}.
         */
        private long sequence;

        /**
         * The {@link Operation} to send over the {@link ObjectOutputStream}.
         */
        private Operation operation;


        /**
         * Constructs an {@link Sender}.
         *
         * @param sequence   the sequence number of the {@link Operation}
         * @param operation  the {@link Operation}
         */
        public Sender(long      sequence,
                      Operation operation)
        {
            this.sequence  = sequence;
            this.operation = operation;
        }


        @Override
        public void run()
        {
            try
            {
                // create a temporary stream in which to serialize the operation
                // (so we can't corrupt the actual output stream if an operation fails to serialize)
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);    // 4k
                ObjectOutputStream    stream = new ObjectOutputStream(buffer);

                // serialize the operation and send the operation over the stream
                // (assume we must send the operation)
                boolean sendTemporaryStream;

                try
                {
                    // attempt to write the operation to the temporary stream
                    // (this may fail for numerous reasons,
                    // but typically because of serialization issues)
                    operation.write(stream);

                    // let's send the stream as we successfully serialized the operation!
                    sendTemporaryStream = true;
                }
                catch (NotSerializableException e)
                {
                    // determine if a "local" listener was provided with the operation
                    CompletionListener<?> listener = pendingListeners.remove(sequence);

                    if (listener == null)
                    {
                        // when there's no "local" listener, we assume we must send a response
                        sendTemporaryStream = true;

                        // while we failed to serialize the operation, that doesn't mean
                        // we should fail silently.  send the result as an exception to
                        // let the original caller know.
                        buffer.reset();
                        stream    = new ObjectOutputStream(buffer);

                        operation = new ResponseOperation(e);
                        operation.write(stream);
                    }
                    else
                    {
                        // when there's a "local" listener, we assume we don't need to
                        // send a response to the original caller
                        sendTemporaryStream = false;

                        // notify the "local" listener of the exception
                        listener.onException(e);
                    }
                }

                if (sendTemporaryStream)
                {
                    // we're done writing (to the buffer)
                    stream.flush();

                    // serialize the operation type
                    // (to the actual output stream)
                    output.writeUTF(operation.getType());

                    // serialize the operation sequence number (for responses)
                    output.writeLong(sequence);

                    // now send the buffer (to the actual output stream)
                    byte[] array = buffer.toByteArray();

                    output.writeInt(array.length);
                    output.write(array, 0, array.length);

                    // ensure the buffer is flushed so that the server can read it
                    output.flush();
                }
            }
            catch (IOException e)
            {
                System.err.println("Failed to send Operation [" + operation + "], Sequence #" + sequence);
                e.printStackTrace();
            }
        }
    }
}
