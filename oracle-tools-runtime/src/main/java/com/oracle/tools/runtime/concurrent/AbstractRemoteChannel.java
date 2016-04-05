/*
 * File: AbstractRemoteChannel.java
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

package com.oracle.tools.runtime.concurrent;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.lang.ThreadFactories;

import com.oracle.tools.runtime.concurrent.options.StreamName;

import com.oracle.tools.runtime.java.io.ClassLoaderAwareObjectInputStream;

import com.oracle.tools.util.CompletionListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.lang.reflect.Constructor;

import java.net.Socket;

import java.util.HashMap;
import java.util.Set;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An astract implementation of a {@link RemoteChannel}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractRemoteChannel extends AbstractControllableRemoteChannel
{
    /**
     * The underlying {@link OutputStream} to use for sending requests and raising
     * events on the {@link RemoteChannel}.
     */
    private OutputStream underlyingOutput;

    /**
     * The underlying {@link OutputStream} to use for receiving requests, responses
     * and events from the {@link RemoteChannel}.
     */
    private InputStream underlyingInput;

    /**
     * The {@link ObjectOutputStream} from the {@link RemoteChannel}.
     * <p/>
     * When this is <code>null</code> the {@link RemoteChannel} is not connected.
     */
    private ObjectOutputStream output;

    /**
     * The {@link ObjectInputStream} into the {@link RemoteChannel}.
     * <p/>
     * When this is <code>null</code> the {@link RemoteChannel} is not connected.
     */
    private ObjectInputStream input;

    /**
     * The {@link ExecutorService} for executing tasks asynchronously in sequence
     * (a singled threaded worker queue).
     */
    private ExecutorService sequentialExecutionService;

    /**
     * The {@link ExecutorService} for executing multiple tasks asynchronously and
     * concurrently (using multiple worker threads).
     */
    private ExecutorService concurrentExecutionService;

    /**
     * The {@link Thread} to read {@link Callable}s from the {@link Socket}.
     * <p/>
     * When this is <code>null</code> the {@link AbstractRemoteChannel} is not connected.
     */
    private Thread requestAcceptorThread;

    /**
     * A flag to indicate if the {@link AbstractRemoteChannel} {@link ObjectInputStream}
     * is readable.
     */
    private AtomicBoolean isReadable;

    /**
     * A flag to indicate if the {@link AbstractRemoteChannel} {@link ObjectOutputStream}
     * is writable.
     */
    private AtomicBoolean isWritable;

    /**
     * The defined protocol (of {@link Operation} types) that the
     * {@link AbstractRemoteChannel} can process.
     */
    private HashMap<String, Class<? extends Operation>> protocol;

    /**
     * The pending {@link CompletionListener}s to be notified of responses.
     */
    private ConcurrentHashMap<Long, CompletionListener<?>> pendingListeners;

    /**
     * The next available sequence number for a callable sent from
     * this {@link AbstractRemoteChannel}.
     */
    private AtomicLong nextSequenceNumber;


    /**
     * Constructs a {@link AbstractRemoteChannel} to submit and accept {@link Callable}s.
     *
     * @param outputStream  the {@link OutputStream} from the {@link RemoteChannel}
     * @param inputStream   the {@link InputStream} into the {@link RemoteChannel}
     *
     * @throws IOException when the {@link RemoteChannel} can't connect provided streams
     */
    public AbstractRemoteChannel(OutputStream outputStream,
                                 InputStream  inputStream) throws IOException
    {
        // remember the underlying streams as we may may have to interact with them later
        this.underlyingOutput = outputStream;
        this.underlyingInput  = inputStream;

        // establish the object output stream
        this.output = underlyingOutput instanceof ObjectOutputStream
                      ? (ObjectOutputStream) underlyingOutput : new ObjectOutputStream(underlyingOutput);

        // immediately flush to ensure that the object output stream headers are written
        // allowing connected streams to begin reading (avoid blocking).
        this.output.flush();

        this.sequentialExecutionService = Executors.newSingleThreadExecutor(ThreadFactories.usingDaemonThreads(true));
        this.concurrentExecutionService = Executors.newCachedThreadPool(ThreadFactories.usingDaemonThreads(true));
        this.requestAcceptorThread      = null;
        this.isReadable                 = new AtomicBoolean(true);
        this.isWritable                 = new AtomicBoolean(true);
        this.protocol                   = new HashMap<>();
        this.pendingListeners           = new ConcurrentHashMap<>();
        this.nextSequenceNumber         = new AtomicLong(0);

        // establish the operations that are part of the protocol
        protocol.put("CALLABLE", CallableOperation.class);
        protocol.put("RESPONSE", ResponseOperation.class);
        protocol.put("RUNNABLE", RunnableOperation.class);
        protocol.put("EVENT", EventOperation.class);
    }


    /**
     * Opens the {@link AbstractRemoteChannel} to accept and submit {@link Callable}s.
     */
    public synchronized void open()
    {
        if (!isOpen())
        {
            setOpen(true);

            // determine the ClassLoader to use for reading requests
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // establish the input stream to read requests
            try
            {
                this.input = new ClassLoaderAwareObjectInputStream(classLoader, underlyingInput);
            }
            catch (IOException e)
            {
                // TODO: log the exception

                isReadable.set(false);
            }

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
                                                                   ByteArrayInputStream buffer =
                                                                       new ByteArrayInputStream(bytes);
                                                                   ObjectInputStream stream =
                                                                       new ClassLoaderAwareObjectInputStream(classLoader,
                                                                                                             buffer);

                                                                   // instantiate the operation and initialize its state
                                                                   Class<? extends Operation> operationClass =
                                                                       protocol.get(operationType);

                                                                   Constructor<? extends AbstractRemoteChannel.Operation> constructor =
                                                                       operationClass.getConstructor(AbstractRemoteChannel.class);

                                                                   Operation operation =
                                                                       constructor.newInstance(AbstractRemoteChannel
                                                                           .this);

                                                                   operation.read(stream);

                                                                   // submit the operation for execution based on the
                                                                   // operational stream
                                                                   StreamName streamName = operation.getStreamName();

                                                                   if (streamName == null)
                                                                   {
                                                                       // when there's no stream name, execute the operation concurrently
                                                                       concurrentExecutionService.submit(new Executor(sequence,
                                                                                                                      operation));
                                                                   }
                                                                   else
                                                                   {
                                                                       // when there's stream name, execute the operation sequentially
                                                                       sequentialExecutionService.submit(new Executor(sequence,
                                                                                                                      operation));
                                                                   }

                                                               }
                                                               catch (Exception e)
                                                               {
                                                                   // when we can't execute the operation we notify the sender of the exception
                                                                   sequentialExecutionService.submit(new Sender(sequence,
                                                                                                                new ResponseOperation(e)));
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

            requestAcceptorThread.setName("RemoteChannel:RequestAcceptor");
            requestAcceptorThread.setDaemon(true);
            requestAcceptorThread.start();

            for (RemoteChannelListener listener : channelListeners)
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

        // clear all of the event listeners
        eventListenersByStreamName.clear();

        // gracefully shutdown the executor services
        concurrentExecutionService.shutdown();
        sequentialExecutionService.shutdown();

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

        // raise IllegalStateExceptions for any remaining listeners
        for (CompletionListener listener : pendingListeners.values())
        {
            try
            {
                listener.onException(new IllegalStateException("RemoteChannel is closed"));
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
                           CompletionListener<T> listener,
                           Option...             options) throws IllegalStateException
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

            sequentialExecutionService.submit(new Sender(sequence, operation));
        }
        else
        {
            throw new IllegalStateException("RemoteChannel is closed");
        }
    }


    @Override
    public void submit(RemoteRunnable runnable,
                       Option...      options) throws IllegalStateException
    {
        if (isOpen())
        {
            long              sequence  = nextSequenceNumber.getAndIncrement();
            RunnableOperation operation = new RunnableOperation(runnable);

            sequentialExecutionService.submit(new Sender(sequence, operation));
        }
        else
        {
            throw new IllegalStateException("RemoteChannel is closed");
        }
    }


    @Override
    public void raise(RemoteEvent event,
                      Option...   options)
    {
        if (isOpen())
        {
            Options        listenerOptions = Options.from(options);

            StreamName     streamName      = listenerOptions.get(StreamName.class);

            long           sequence        = nextSequenceNumber.getAndIncrement();
            EventOperation operation       = new EventOperation(streamName, event);

            sequentialExecutionService.submit(new Sender(sequence, operation));
        }
        else
        {
            throw new IllegalStateException("RemoteChannel is closed");
        }
    }


    /**
     * An {@link Operation} to be executed in-order by a {@link AbstractRemoteChannel}.
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


        /**
         * Obtains the {@link StreamName} on which the {@link Operation} should be processed
         * (in sequence) or <code>null</code> when the {@link Operation} has no requirement
         * for processing order.
         *
         * @return  the {@link StreamName}
         */
        public StreamName getStreamName();
    }


    /**
     * An {@link Operation} to send and execute a {@link Callable}.
     */
    class CallableOperation implements Operation
    {
        /**
         * Is a response required for the {@link Callable}?
         */
        private boolean isResponseRequired;

        /**
         * The {@link Callable} to eventually execute.
         */
        private Callable<?> callable;


        /**
         * Constructs a {@link AbstractRemoteChannel.CallableOperation}
         * (required for construction)
         */
        public CallableOperation()
        {
        }


        /**
         * Constructs a {@link AbstractRemoteChannel.CallableOperation}
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
            catch (Throwable throwable)
            {
                if (isResponseRequired)
                {
                    operation = new ResponseOperation(throwable);
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


        @Override
        public StreamName getStreamName()
        {
            return null;
        }
    }


    /**
     * An {@link Operation} to raise a {@link RemoteEvent}.
     */
    class EventOperation implements Operation
    {
        /**
         * The {@link StreamName} for the {@link RemoteEvent}
         */
        private StreamName streamName;

        /**
         * The {@link RemoteEvent} to eventually execute.
         */
        private RemoteEvent event;


        /**
         * Constructs an {@link EventOperation}
         * (required for serialization)
         */
        public EventOperation()
        {
        }


        /**
         * Constructs an {@link EventOperation}
         *
         * @param streamName  the {@link StreamName} for the {@link RemoteEvent}
         * @param event       the {@link RemoteEvent} to fire remotely
         *
         * @throws NullPointerException      should the {@link RemoteEvent} be <code>null</code>
         * @throws IllegalArgumentException  should the {@link RemoteEvent} be an anonymous inner class
         */
        public EventOperation(StreamName  streamName,
                              RemoteEvent event)
        {
            if (streamName == null)
            {
                throw new NullPointerException("The streamName can't be null");
            }

            this.streamName = streamName;

            if (event == null)
            {
                throw new NullPointerException("RemoteEvent can't be null");
            }
            else if (event.getClass().isAnonymousClass())
            {
                throw new IllegalArgumentException("RemoteEvent can't be an anonymous inner-class");
            }
            else
            {
                this.event = event;
            }
        }


        @Override
        public String getType()
        {
            return "EVENT";
        }


        @Override
        public Operation execute(long sequence)
        {
            Set<RemoteEventListener> eventListeners = eventListenersByStreamName.get(streamName);

            if (eventListeners != null)
            {
                eventListeners.forEach(
                    eventListener -> {
                        try
                        {
                            eventListener.onEvent(event);
                        }
                        catch (Throwable e)
                        {
                            e.printStackTrace();
                        }
                    });
            }

            return null;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            try
            {
                // read the streamName
                streamName = StreamName.of(input.readUTF());

                // read the callable or the name of the callable class
                Object object = input.readObject();

                if (object instanceof String)
                {
                    String className = (String) object;

                    event = (RemoteEvent) Class.forName(className).newInstance();
                }
                else
                {
                    event = (RemoteEvent) object;
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
            // serialize the stream name
            output.writeUTF(streamName.get());

            // serialize the Runnable (if it is!)
            if (event instanceof Serializable)
            {
                output.writeObject(event);
            }
            else
            {
                output.writeObject(event.getClass().getName());
            }
        }


        @Override
        public StreamName getStreamName()
        {
            return streamName;
        }
    }


    /**
     * Asynchronously executes an {@link Operation} that was
     * received by the {@link AbstractRemoteChannel}.
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
                sequentialExecutionService.submit(new Sender(sequence, resultingOperation));
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
         * Constructs a {@link AbstractRemoteChannel.ResponseOperation}
         * (required for construction)
         */
        public ResponseOperation()
        {
        }


        /**
         * Constructs a {@link AbstractRemoteChannel.ResponseOperation}
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
                    if (response instanceof Throwable)
                    {
                        listener.onException((Throwable) response);
                    }
                    else
                    {
                        listener.onCompletion(response);
                    }
                }
                catch (Throwable throwable)
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


        @Override
        public StreamName getStreamName()
        {
            return null;
        }
    }


    /**
     * An {@link Operation} to send and execute a {@link Runnable}.
     */
    class RunnableOperation implements Operation
    {
        /**
         * The {@link Runnable} to eventually execute.
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
            catch (Throwable throwable)
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


        @Override
        public StreamName getStreamName()
        {
            return null;
        }
    }


    /**
     * Asynchronously sends an {@link Operation} over the
     * {@link ObjectOutputStream} for the {@link AbstractRemoteChannel}.
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
                e.printStackTrace();
            }
        }
    }
}
