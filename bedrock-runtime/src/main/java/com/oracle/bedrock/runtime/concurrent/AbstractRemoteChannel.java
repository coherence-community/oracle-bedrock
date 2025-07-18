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

package com.oracle.bedrock.runtime.concurrent;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.lang.ThreadFactories;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.concurrent.options.Caching;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.java.io.ClassLoaderAwareObjectInputStream;
import com.oracle.bedrock.util.Pair;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract implementation of a {@link RemoteChannel}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public abstract class AbstractRemoteChannel extends AbstractControllableRemoteChannel
{
    /**
    * The {@link Logger} for this class.
    */
    private static final Logger LOGGER = Logger.getLogger(AbstractRemoteChannel.class.getName());

    /**
     * The underlying {@link OutputStream} to use for sending requests and raising
     * events on the {@link RemoteChannel}.
     */
    private final OutputStream underlyingOutput;

    /**
     * The underlying {@link OutputStream} to use for receiving requests, responses
     * and events from the {@link RemoteChannel}.
     */
    private final InputStream underlyingInput;

    /**
     * The {@link ObjectOutputStream} from the {@link RemoteChannel}.
     * <p>
     * When this is <code>null</code> the {@link RemoteChannel} is not connected.
     */
    private ObjectOutputStream output;

    /**
     * The {@link ObjectInputStream} into the {@link RemoteChannel}.
     * <p>
     * When this is <code>null</code> the {@link RemoteChannel} is not connected.
     */
    private ObjectInputStream input;

    /**
     * The {@link ExecutorService} for executing tasks asynchronously in sequence
     * (a singled threaded worker queue).
     */
    private final ExecutorService sequentialExecutionService;

    /**
     * The {@link ExecutorService} for executing multiple tasks asynchronously and
     * concurrently (using multiple worker threads).
     */
    private final ExecutorService concurrentExecutionService;

    /**
     * The {@link Thread} to read {@link Callable}s from the {@link Socket}.
     * <p>
     * When this is <code>null</code> the {@link AbstractRemoteChannel} is not connected.
     */
    private Thread requestAcceptorThread;

    /**
     * A flag to indicate if the {@link AbstractRemoteChannel} {@link ObjectInputStream}
     * is readable.
     */
    private final AtomicBoolean isReadable;

    /**
     * A flag to indicate if the {@link AbstractRemoteChannel} {@link ObjectOutputStream}
     * is writable.
     */
    private final AtomicBoolean isWritable;

    /**
     * The defined protocol (of {@link Operation} types) that the
     * {@link AbstractRemoteChannel} can process.
     */
    @SuppressWarnings("rawtypes")
    private final HashMap<String, Class<? extends Operation>> protocol;

    /**
     * The pending {@link Operation}s that are waiting for responses,
     * indexed by sequence number.
     */
    private final ConcurrentHashMap<Long, Operation<?>> pendingOperations;

    /**
     * The next available sequence number for a callable sent from
     * this {@link AbstractRemoteChannel}.
     */
    private final AtomicLong nextSequenceNumber;

    /**
     * The result cache for {@link Callable}s submitted using a {@link Caching#enabled(Option...)}
     * option.
     * <p>
     * The cache is a map from the {@link RemoteCallable} to a {@link Pair}
     * consisting of the result and the {@link Instant} when result expires.
     */
    private final ConcurrentHashMap<Callable<?>, Pair<Object, Instant>> cache;

    /**
     * The {@link RemoteChannelSerializer} to use.
     */
    private RemoteChannelSerializer serializer;

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
        this(outputStream, inputStream, null);
    }

    /**
     * Constructs a {@link AbstractRemoteChannel} to submit and accept {@link Callable}s.
     *
     * @param outputStream  the {@link OutputStream} from the {@link RemoteChannel}
     * @param inputStream   the {@link InputStream} into the {@link RemoteChannel}
     *
     * @throws IOException when the {@link RemoteChannel} can't connect provided streams
     */
    public AbstractRemoteChannel(OutputStream outputStream,
                                 InputStream  inputStream,
                                 RemoteChannelSerializer serializer) throws IOException
    {
        // remember the underlying streams as we may have to interact with them later
        this.underlyingOutput = outputStream;
        this.underlyingInput  = inputStream;
        this.serializer       = serializer;

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
        this.pendingOperations          = new ConcurrentHashMap<>();
        this.nextSequenceNumber         = new AtomicLong(0);

        // establish the result cache for RemoteCallables
        this.cache = new ConcurrentHashMap<>();

        // establish the operations that are part of the protocol
        protocol.put("CALLABLE", CallableOperation.class);
        protocol.put("RESPONSE", ResponseOperation.class);
        protocol.put("RUNNABLE", RunnableOperation.class);
        protocol.put("EVENT", EventOperation.class);
    }

    public void setSerializer(RemoteChannelSerializer serializer)
    {
        this.serializer = serializer;
    }

/**
     * Get netstats information from OS.
     *
     * @return the netstat information as an {@link ArrayList}
     */
    public ArrayList<String> getNetStatsInfo()
        {
        ArrayList<String> asPortInfo = new ArrayList<>();

        try
            {
            String        sOS       = System.getProperty("os.name").toLowerCase();
            StringBuilder sbCommand = new StringBuilder().append("netstat ");

            if (sOS.contains("mac"))
                {
                sbCommand.append("-tanp tcp");
                }
            else if (sOS.contains("linux"))
                {
                sbCommand.append("-tanpve");
                }
            else if (sOS.contains("windows"))
                {
                sbCommand.append("-baonp tcp");
                }

            Process        process = Runtime.getRuntime().exec(sbCommand.toString());
            InputStream    in      = process.getInputStream();
            BufferedReader buffer  = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = buffer.readLine()) != null)
                {
                line += System.lineSeparator();
                asPortInfo.add(line);
                }

            return asPortInfo;
            }
        catch (Exception e)
            {
            LOGGER.warning(this.getClass().getName() + ".getNetStatsInfo: unexpected Exception: " + e.getLocalizedMessage());
            }

        return asPortInfo;
        }

    /**
     * Opens the {@link AbstractRemoteChannel} to accept and submit {@link Callable}s.
     */
    @SuppressWarnings("rawtypes")
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
                isReadable.set(false);
                setOpen(false);
                LOGGER.warning(this.getClass().getName() + ".open: unexpected IOException: " + e.getLocalizedMessage());
                LOGGER.info("netstats info: " + getNetStatsInfo());
                LOGGER.log(Level.FINE, "stack trace", e);
                return;
            }

            requestAcceptorThread = new Thread(() -> {
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

                            Constructor<? extends Operation> constructor =
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
                            sequentialExecutionService.submit(new Sender(sequence, new ResponseOperation<>(e)));
                        }
                    }
                    catch (Exception e)
                    {
                        // the stream has become corrupted or was closed
                        // (either way there's nothing else we can read or do)
                        isReadable.set(false);
                        LOGGER.log(Level.FINE, "termination of RemoteChannel:RequestAcceptor thread", e);
                    }
                }

                close();
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

        // gracefully shutdown the executor services
        concurrentExecutionService.shutdown();
        sequentialExecutionService.shutdown();

        // clear all of the event listeners
        eventListenersByStreamName.clear();

        // no longer write any more responses
        isWritable.set(false);

        // close the input and output streams
        try
        {
            if (input != null)
                {
                input.close();
                }
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
            if (output != null)
                {
                output.close();
                }
        }
        catch (IOException e)
        {
            // don't care
        }
        finally
        {
            output = null;
        }

        // raise IllegalStateExceptions for any remaining pending operations
        for (Operation<?> operation : pendingOperations.values())
        {
            try
            {
                operation.completeExceptionally(new IllegalStateException("RemoteChannel is closed"));
            }
            catch (Exception e)
            {
                // we ignore exceptions that the listener throws
            }
        }

        pendingOperations.clear();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
                                           Option...         options) throws IllegalStateException
    {
        if (isOpen())
        {
            OptionsByType optionsByType = OptionsByType.of(options);

            // determine if Caching is enabled for this submission
            Caching caching = optionsByType.get(Caching.class);

            if (caching.isEnabled())
            {
                // attempt to acquire the existing cache value that hasn't expired
                // (if it's expired replace it with null)
                Pair<Object, Instant> pair = cache.compute(callable,
                                                           (c, existing) -> existing == null
                                                                            || existing.getY()
                                                                            .isBefore(Instant.now()) ? null : existing);

                if (pair != null)
                {
                    CompletableFuture<T> future = new CompletableFuture<>();

                    future.complete((T) pair.getX());

                    return future;
                }
            }
            else
            {
                // ensure the cache is cleared for the current callable
                cache.remove(callable);
            }

            // by default we acknowledge when processed
            optionsByType.addIfAbsent(AcknowledgeWhen.PROCESSED);

            CallableOperation operation = new CallableOperation<>(callable, optionsByType);

            return sendOperation(operation, optionsByType);
        }
        else
        {
            throw new IllegalStateException("RemoteChannel is closed");
        }
    }


    @Override
    public CompletableFuture<Void> submit(RemoteRunnable runnable,
                                          Option...      options) throws IllegalStateException
    {
        if (isOpen())
        {
            OptionsByType optionsByType = OptionsByType.of(options);

            // by default we acknowledge when sent
            optionsByType.addIfAbsent(AcknowledgeWhen.SENT);

            RunnableOperation operation = new RunnableOperation(runnable, optionsByType);

            return sendOperation(operation, optionsByType);
        }
        else
        {
            throw new IllegalStateException("RemoteChannel is closed");
        }
    }


    @Override
    public CompletableFuture<Void> raise(RemoteEvent event,
                                         Option...   options)
    {
        if (isOpen())
        {
            OptionsByType optionsByType = OptionsByType.of(options);
            StreamName    streamName    = optionsByType.get(StreamName.class);

            // by default we acknowledge when sent
            optionsByType.addIfAbsent(AcknowledgeWhen.SENT);

            EventOperation operation = new EventOperation(streamName, event, optionsByType);

            return sendOperation(operation, optionsByType);
        }
        else
        {
            throw new IllegalStateException("RemoteChannel is closed");
        }
    }


    private <T> CompletableFuture<T> sendOperation(Operation<T>  operation,
                                                   OptionsByType optionsByType)
    {
        long   sequence = nextSequenceNumber.getAndIncrement();
        Sender sender   = new Sender(sequence, operation);

        if (optionsByType.get(AcknowledgeWhen.class) == AcknowledgeWhen.SENT)
        {
            return CompletableFuture.runAsync(sender, sequentialExecutionService).thenApply((_void) -> null);
        }
        else
        {
            pendingOperations.put(sequence, operation);

            sequentialExecutionService.submit(sender);

            return operation.getCompletableFuture();
        }
    }


    /**
     * An {@link Operation} to be executed in-order by a {@link AbstractRemoteChannel}.
     *
     * @param <T>  the result returned locally by remotely executing the operation.
     */
    interface Operation<T>
    {
        /**
         * Obtains the unique type of {@link Operation}.
         *
         * @return the type of {@link Operation}
         */
        String getType();


        /**
         * Writes the {@link Operation} state to the specified {@link ObjectOutputStream},
         * so that it may later be read from an {@link ObjectInputStream} and executed.
         *
         * @param output  the {@link ObjectOutputStream}
         *
         * @throws IOException  should the write fail
         */
        void write(ObjectOutputStream output) throws IOException;


        /**
         * Reads the {@link Operation} state from the specified {@link ObjectInputStream}.
         *
         * @param input  the {@link ObjectInputStream}
         *
         * @throws IOException  should the read fail
         */
        void read(ObjectInputStream input) throws IOException;


        /**
         * Executes the {@link Operation}.
         *
         * @param sequence  the sequence number of the {@link Operation}
         *
         * @return Operation  the {@link Operation} to be sent back to the initiator of this
         *                    {@link Operation} (if null, nothing is returned)
         */
        @SuppressWarnings("rawtypes")
        Operation execute(long sequence);


        /**
         * Obtains the {@link StreamName} on which the {@link Operation} should be processed
         * (in sequence) or <code>null</code> when the {@link Operation} has no requirement
         * for processing order.
         *
         * @return  the {@link StreamName}
         */
        StreamName getStreamName();


        /**
         * Notifies the {@link Operation} that it was completed with the specified result.
         *
         * @param result  the result
         */
        void complete(T result);


        /**
         * Notifies the {@link Operation} that is was completed exceptionally with the specified {@link Throwable}.
         *
         * @param throwable  the throwable
         */
        void completeExceptionally(Throwable throwable);


        /**
         * Obtains the {@link CompletableFuture} that can be used for acquiring
         * the result of the {@link Operation}, including the result and exception thrown.
         *
         * @return  the {@link CompletableFuture}
         */
        CompletableFuture<T> getCompletableFuture();
    }


    /**
     * An {@link Operation} to send and execute a {@link Callable}.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    class CallableOperation<T> implements Operation<T>
    {
        /**
         * The {@link CompletableFuture} for the result of the {@link Callable}.
         */
        private transient CompletableFuture<T> future;

        /**
         * The transient {@link OptionsByType} for the {@link Operation}.
         */
        private transient OptionsByType optionsByType;

        /**
         * Is a response required for the {@link Callable}?
         */
        private boolean isResponseRequired;

        /**
         * The {@link Callable} to eventually execute.
         */
        private Callable<T> callable;


        /**
         * Constructs a {@link AbstractRemoteChannel.CallableOperation}
         * (required for construction)
         */
        @SuppressWarnings("unused")
        public CallableOperation()
        {
        }


        /**
         * Constructs a {@link AbstractRemoteChannel.CallableOperation}
         *
         * @param callable       the {@link Callable} to execute remotely
         * @param optionsByType  the {@link OptionsByType} for the execution
         *
         * @throws NullPointerException      should the {@link Callable} be <code>null</code>
         * @throws IllegalArgumentException  should the {@link Callable} be an anonymous inner class
         */
        public CallableOperation(Callable<T>   callable,
                                 OptionsByType optionsByType)
        {
            Class<?> callableClass = callable == null ? null : callable.getClass();

            if (callableClass == null)
            {
                throw new NullPointerException("Callable can't be null");
            }
            else if (callableClass.isAnonymousClass())
            {
                throw new IllegalArgumentException("Callable can't be an anonymous inner-class");
            }
            else if (callableClass.isMemberClass() &&!Modifier.isStatic(callableClass.getModifiers()))
            {
                throw new IllegalArgumentException("Callable can't be an non-static inner-class");
            }
            else
            {
                this.isResponseRequired = optionsByType.get(AcknowledgeWhen.class) == AcknowledgeWhen.PROCESSED;
                this.callable           = callable;
                this.future             = new CompletableFuture<>();
                this.optionsByType      = optionsByType;
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
                // attempt to inject the RemoteChannel into the callable.
                AbstractRemoteChannel.this.injectInto(callable);

                // execute the Callable
                Object result = callable.call();

                if (isResponseRequired)
                {
                    operation = new ResponseOperation<>(result);
                }
            }
            catch (Throwable throwable)
            {
                if (isResponseRequired)
                {
                    operation = new ResponseOperation<>(throwable);
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
                Object object;
                if (serializer == null)
                {
                    object = input.readObject();
                }
                else
                {
                    int len = input.readInt();
                    object = serializer.deserialize(input.readNBytes(len));
                }

                if (object instanceof String)
                {
                    String   className     = (String) object;
                    Class<?> callableClass = Class.forName(className);

                    callable = (Callable) callableClass.getDeclaredConstructor().newInstance();
                }
                else
                {
                    callable = (Callable) object;
                }
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | NoSuchMethodException | InvocationTargetException e)
            {
                LOGGER.log(Level.SEVERE, e, () -> "Error reading CallableOperation");
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
                if (serializer == null)
                {
                    output.writeObject(callable);
                }
                else
                {
                    byte[] bytes = serializer.serialize(callable);
                    output.writeInt(bytes.length);
                    output.write(bytes);
                }
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


        @Override
        public void complete(T result)
        {
            // cache the result (if required)
            Caching caching = optionsByType.get(Caching.class);

            if (caching.isEnabled())
            {
                // determine the Caching Timeout
                Timeout timeout = caching.getOptionsByType().get(Timeout.class);

                // determine the Instant in the future when the result will timeout
                Instant instant = Instant.now().plusMillis(timeout.to(TimeUnit.MILLISECONDS));

                // cache the result for the callable
                cache.put(callable, new Pair<>(result, instant));
            }

            future.complete(result);
        }


        @Override
        public void completeExceptionally(Throwable throwable)
        {
            future.completeExceptionally(throwable);
        }


        @Override
        public CompletableFuture<T> getCompletableFuture()
        {
            return future;
        }
    }


    /**
     * An {@link Operation} to raise a {@link RemoteEvent}.
     */
    @SuppressWarnings("rawtypes")
    class EventOperation implements Operation<Void>
    {
        /**
         * The {@link CompletableFuture} for notifying of event delivery.
         */
        private transient CompletableFuture<Void> future;

        /**
         * The transient {@link OptionsByType} for the {@link Operation}.
         */
        private transient OptionsByType optionsByType;

        /**
         * The {@link StreamName} for the {@link RemoteEvent}
         */
        private StreamName streamName;

        /**
         * The {@link RemoteEvent} to eventually execute.
         */
        private RemoteEvent event;
        private boolean     isAckRequired;


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
         * @param streamName       the {@link StreamName} for the {@link RemoteEvent}
         * @param event            the {@link RemoteEvent} to fire remotely
         *
         * @throws NullPointerException      should the {@link RemoteEvent} be <code>null</code>
         * @throws IllegalArgumentException  should the {@link RemoteEvent} be an anonymous inner class
         */
        public EventOperation(StreamName    streamName,
                              RemoteEvent   event,
                              OptionsByType optionsByType)
        {
            if (streamName == null)
            {
                throw new NullPointerException("The streamName can't be null");
            }
            else if (event == null)
            {
                throw new NullPointerException("RemoteEvent can't be null");
            }
            else if (event.getClass().isAnonymousClass())
            {
                throw new IllegalArgumentException("RemoteEvent can't be an anonymous inner-class");
            }
            else
            {
                this.streamName    = streamName;
                this.isAckRequired = optionsByType.get(AcknowledgeWhen.class) == AcknowledgeWhen.PROCESSED;
                this.event         = event;
                this.future        = new CompletableFuture<>();
                this.optionsByType = optionsByType;
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

            return isAckRequired ? new ResponseOperation<>(null) : null;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            try
            {
                // read the streamName
                streamName = StreamName.of(input.readUTF());

                // read notification flag
                isAckRequired = input.readBoolean();

                // read the callable or the name of the callable class
                Object object;
                if (serializer == null)
                {
                    object = input.readObject();
                }
                else
                {
                    int len = input.readInt();
                    object = serializer.deserialize(input.readNBytes(len));
                }

            if (object instanceof String)
                {
                    String className = (String) object;

                    event = (RemoteEvent) Class.forName(className).getDeclaredConstructor().newInstance();
                }
                else
                {
                    event = (RemoteEvent) object;
                }
            }
            catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                    | InstantiationException | IllegalAccessException e)
            {
                throw new IOException(e);
            }
        }


        @Override
        public void write(ObjectOutputStream output) throws IOException
        {
            // serialize the stream name
            output.writeUTF(streamName.get());

            // serialize the notification flag
            output.writeBoolean(isAckRequired);

            // serialize the Runnable (if it is!)
            if (event instanceof Serializable)
            {
                if (serializer == null)
                {
                    output.writeObject(event);
                }
                else
                {
                    byte[] bytes = serializer.serialize(event);
                    output.writeInt(bytes.length);
                    output.write(bytes);
                }
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


        @Override
        public void complete(Void result)
        {
            future.complete(result);
        }


        @Override
        public void completeExceptionally(Throwable throwable)
        {
            future.completeExceptionally(throwable);
        }


        @Override
        public CompletableFuture<Void> getCompletableFuture()
        {
            return future;
        }
    }


    /**
     * Asynchronously executes an {@link Operation} that was
     * received by the {@link AbstractRemoteChannel}.
     */
    @SuppressWarnings("rawtypes")
    class Executor implements Runnable
    {
        /**
         * The sequence number for the {@link Operation} to execute.
         */
        private final long sequence;

        /**
         * The {@link Operation} to execute.
         */
        private final Operation operation;


        /**
         * Constructs an {@link Executor}.
         *
         * @param sequence   the sequence number of the {@link Operation}
         * @param operation  the {@link Operation} to execute
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    class ResponseOperation<T> implements Operation<T>
    {
        /**
         * The response.
         */
        private T response;


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
        public ResponseOperation(T response)
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
            Operation operation = pendingOperations.remove(sequence);

            if (operation != null)
            {
                try
                {
                    if (response instanceof Throwable)
                    {
                        operation.completeExceptionally((Throwable) response);
                    }
                    else
                    {
                        operation.complete(response);
                    }
                }
                catch (Throwable throwable)
                {
                    // we ignore any exceptions that the future may throw
                }
            }

            return null;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            try
            {
                if (serializer == null)
                {
                    response = (T) input.readObject();
                }
                else
                {
                    int len = input.readInt();
                    response = serializer.deserialize(input.readNBytes(len));
                }
            }
            catch (ClassNotFoundException e)
            {
                throw new IOException(e);
            }
        }


        @Override
        public void write(ObjectOutputStream output) throws IOException
        {
            System.err.println("****** JK: sending response " + response);
            if (serializer == null)
            {
                output.writeObject(response);
            }
            else
            {
                byte[] bytes = serializer.serialize(response);
                output.writeInt(bytes.length);
                output.write(bytes);
            }
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


        @Override
        public void complete(T result)
        {
            // nothing to do as ResponseOperations never get completed
        }


        @Override
        public void completeExceptionally(Throwable throwable)
        {
            // nothing to do as ResponseOperations don't throw exceptions
        }


        @Override
        public CompletableFuture<T> getCompletableFuture()
        {
            return null;
        }
    }


    /**
     * An {@link Operation} to send and execute a {@link Runnable}.
     */
    @SuppressWarnings("rawtypes")
    class RunnableOperation implements Operation<Void>
    {
        /**
         * The {@link CompletableFuture} for notifying of {@link Runnable} execution.
         */
        private transient CompletableFuture<Void> future;

        /**
         * The {@link Runnable} to eventually execute.
         */
        private Runnable runnable;

        /**
         * Is a response required for the {@link Runnable}?
         */
        private boolean isResponseRequired;


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
         * @param runnable       the {@link Runnable} to run remotely
         * @param optionsByType  the {@link OptionsByType} for the {@link Runnable} execution
         *
         * @throws NullPointerException      should the {@link Runnable} be <code>null</code>
         * @throws IllegalArgumentException  should the {@link Runnable} be an anonymous inner class
         */
        public RunnableOperation(Runnable      runnable,
                                 OptionsByType optionsByType)
        {
            Class<?> runnableClass = runnable == null ? null : runnable.getClass();

            if (runnableClass == null)
            {
                throw new NullPointerException("Runnable can't be null");
            }
            else if (runnableClass.isAnonymousClass())
            {
                throw new IllegalArgumentException("Runnable can't be an anonymous inner-class");
            }
            else if (runnableClass.isMemberClass() &&!Modifier.isStatic(runnableClass.getModifiers()))
            {
                throw new IllegalArgumentException("Runnable can't be an non-static inner-class");
            }
            else
            {
                this.runnable           = runnable;
                this.isResponseRequired = optionsByType.get(AcknowledgeWhen.class) == AcknowledgeWhen.PROCESSED;
                this.future             = new CompletableFuture<>();
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
            Operation response = null;

            try
            {
                // attempt to inject the RemoteChannel into the callable.
                AbstractRemoteChannel.this.injectInto(runnable);

                runnable.run();

                if (isResponseRequired)
                {
                    response = new ResponseOperation<>(null);
                }
            }
            catch (Throwable throwable)
            {
                if (isResponseRequired)
                {
                    response = new ResponseOperation<>(throwable);
                }
            }

            return response;
        }


        @Override
        public void read(ObjectInputStream input) throws IOException
        {
            isResponseRequired = input.readBoolean();

            try
            {
                // read the callable or the name of the callable class
                Object object;
                if (serializer == null)
                {
                    object = input.readObject();
                }
                else
                {
                    int len = input.readInt();
                    object = serializer.deserialize(input.readNBytes(len));
                }

                if (object instanceof String)
                {
                    String className = (String) object;

                    runnable = (Runnable) Class.forName(className).getDeclaredConstructor().newInstance();
                }
                else
                {
                    runnable = (Runnable) object;
                }
            }
            catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException
                    | IllegalAccessException | InstantiationException e)
            {
                LOGGER.log(Level.SEVERE, e, () -> "Error reading RunnableOperation");
                throw new IOException(e);
            }
        }


        @Override
        public void write(ObjectOutputStream output) throws IOException
        {
            output.writeBoolean(isResponseRequired);

            // serialize the Runnable (if it is!)
            if (runnable instanceof Serializable)
            {
                if (serializer == null)
                {
                    output.writeObject(runnable);
                }
                else
                {
                    byte[] bytes = serializer.serialize(runnable);
                    output.writeInt(bytes.length);
                    output.write(bytes);
                }
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


        @Override
        public void complete(Void result)
        {
            future.complete(result);
        }


        @Override
        public void completeExceptionally(Throwable throwable)
        {
            future.completeExceptionally(throwable);
        }


        @Override
        public CompletableFuture<Void> getCompletableFuture()
        {
            return future;
        }
    }


    /**
     * Asynchronously sends an {@link Operation} over the
     * {@link ObjectOutputStream} for the {@link AbstractRemoteChannel}.
     */
    @SuppressWarnings("rawtypes")
    class Sender implements Runnable
    {
        /**
         * The sequence number of the {@link Operation}.
         */
        private final long sequence;

        /**
         * The {@link Operation} to send over the {@link ObjectOutputStream}.
         */
        private final Operation operation;


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
                    LOGGER.log(Level.SEVERE, e, () -> "Sender: could not send " + operation);
                    // determine if the operation required acknowledgement (we can acknowledge failure here)
                    Operation operation = pendingOperations.remove(sequence);

                    if (operation == null)
                    {
                        // when the operation doesn't require acknowledgment, we assume we must send a response
                        sendTemporaryStream = true;

                        // while we failed to serialize the operation, that doesn't mean
                        // we should fail silently.  send the result as an exception to
                        // let the original caller know.
                        buffer.reset();
                        stream    = new ObjectOutputStream(buffer);

                        operation = new ResponseOperation<>(new Exception(e.getMessage()));
                        operation.write(stream);
                    }
                    else
                    {
                        // when there's a "local" future, we assume we don't need to
                        // send a response to the original caller
                        sendTemporaryStream = false;

                        // notify the operation of the exception
                        operation.completeExceptionally(e);
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
                LOGGER.log(Level.SEVERE, e, () -> "Caught exception sending operation " + operation);
            }
        }
    }
}
