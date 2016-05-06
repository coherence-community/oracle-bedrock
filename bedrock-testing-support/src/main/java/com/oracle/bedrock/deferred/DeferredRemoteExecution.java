/*
 * File: DeferredRemoteExecution.java
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

package com.oracle.bedrock.deferred;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

/**
 * A {@link Deferred} representing the result of a {@link Callable}
 * submitted to a {@link RemoteChannel}
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredRemoteExecution<T> implements Deferred<T>
{
    /**
     * The {@link RemoteChannel} to which the {@link Callable}
     * will be submitted.
     */
    private RemoteChannel remoteChannel;

    /**
     * The {@link RemoteCallable} to submit for execution.
     */
    private RemoteCallable<T> callable;

    /**
     * A flag indicating if the {@link Callable} has been submitted for execution.
     */
    private boolean hasSubmittedCallable;

    /**
     * A flag indicating if a result of an execution has arrived.
     */
    private boolean hasResult;

    /**
     * The result of an execution (may be null).
     */
    private T result;

    /**
     * The {@link Throwable} produced by an execution (null if no exception occurred)
     */
    private Throwable throwable;


    /**
     * Constructs a {@link DeferredRemoteExecution}
     *
     * @param remoteChannel  the {@link RemoteChannel} to be used for execution
     * @param callable        the {@link RemoteCallable} to execute
     */
    public DeferredRemoteExecution(RemoteChannel     remoteChannel,
                                   RemoteCallable<T> callable)
    {
        this.remoteChannel        = remoteChannel;
        this.callable             = callable;
        this.hasSubmittedCallable = false;
        this.hasResult            = false;
        this.result               = null;
        this.throwable            = null;
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        synchronized (this)
        {
            if (hasResult)
            {
                hasResult            = false;
                hasSubmittedCallable = false;

                if (throwable == null)
                {
                    return result;
                }
                else
                {
                    throw new TemporarilyUnavailableException(this, throwable);
                }
            }
            else
            {
                if (hasSubmittedCallable)
                {
                    throw new TemporarilyUnavailableException(this);
                }
                else
                {
                    hasSubmittedCallable = true;
                    hasResult            = false;
                    result               = null;
                    throwable            = null;

                    try
                    {
                        remoteChannel.submit(callable).handle((result, exception) -> {
                                synchronized (this)
                                {
                                    if (hasResult)
                                    {
                                        // TODO: serious issue if we've already got a result
                                    }
                                    else if (exception == null)
                                    {
                                        this.hasResult = true;
                                        this.result    = result;
                                        this.throwable = null;
                                    }
                                    else
                                    {
                                        this.hasResult = true;
                                        this.result    = null;
                                        this.throwable = exception;
                                    }
                                }

                                return null;
                            });
                    }
                    catch (Exception e)
                    {
                        throw new PermanentlyUnavailableException(this, e);
                    }

                    // we throw an instance unavailable exception immediately as we have to wait for the result
                    throw new TemporarilyUnavailableException(this);
                }
            }
        }
    }


    @Override
    public Class<T> getDeferredClass()
    {
        // determine the type based on the provided Callable instance
        for (Type type : callable.getClass().getGenericInterfaces())
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) type;

                if (parameterizedType.getRawType().equals(RemoteCallable.class))
                {
                    return (Class<T>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }

        throw new IllegalArgumentException("Could not determine the type of the specified Callable");
    }
}
