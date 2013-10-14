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

package com.oracle.tools.deferred;

import com.oracle.tools.runtime.concurrent.RemoteExecutor;

import com.oracle.tools.util.CompletionListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.concurrent.Callable;

import javax.lang.model.type.UnknownTypeException;

/**
 * A {@link Deferred} representing the result of a {@link Callable}
 * submitted to a {@link RemoteExecutor}
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredRemoteExecution<T> implements Deferred<T>, CompletionListener<T>
{
    /**
     * The {@link RemoteExecutor} to which the {@link Callable}
     * will be submitted.
     */
    private RemoteExecutor remoteExecutor;

    /**
     * The {@link Callable} to submit for execution.
     */
    private Callable<T> callable;

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
     * The {@link Exception} produced by an execution (null if no exception occurred)
     */
    private Exception exception;


    /**
     * Constructs a {@link DeferredRemoteExecution}
     *
     * @param remoteExecutor  the {@link RemoteExecutor} to be used for execution
     * @param callable        the {@link Callable} to execute
     */
    public DeferredRemoteExecution(RemoteExecutor remoteExecutor,
                                   Callable<T>    callable)
    {
        this.remoteExecutor       = remoteExecutor;
        this.callable             = callable;
        this.hasSubmittedCallable = false;
        this.hasResult            = false;
        this.result               = null;
        this.exception            = null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompletion(T result)
    {
        synchronized (this)
        {
            if (hasResult)
            {
                // TODO: serious issue if we've already got a result
            }
            else
            {
                this.hasResult = true;
                this.result    = result;
                this.exception = null;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onException(Exception exception)
    {
        synchronized (this)
        {
            if (hasResult)
            {
                // TODO: serious issue if we've already got a result
            }
            else
            {
                this.hasResult = true;
                this.result    = null;
                this.exception = exception;
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws UnresolvableInstanceException, InstanceUnavailableException
    {
        synchronized (this)
        {
            if (hasResult)
            {
                hasResult = false;

                if (exception != null)
                {
                    throw new InstanceUnavailableException(this, exception);
                }
                else
                {
                    return result;
                }
            }
            else
            {
                if (hasSubmittedCallable)
                {
                    throw new InstanceUnavailableException(this);
                }
                else
                {
                    hasSubmittedCallable = true;
                    hasResult            = false;
                    result               = null;
                    exception            = null;

                    try
                    {
                        remoteExecutor.submit(callable, this);
                    }
                    catch (Exception e)
                    {
                        throw new UnresolvableInstanceException(this, e);
                    }

                    // we throw an instance unavailable exception immediately as we have to wait for the result
                    throw new InstanceUnavailableException(this);
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getDeferredClass()
    {
        // determine the type based on the provided Callable instance
        for (Type type : callable.getClass().getGenericInterfaces())
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) type;

                if (parameterizedType.getRawType().equals(Callable.class))
                {
                    return (Class<T>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }

        throw new IllegalArgumentException("Could not determine the type of the specified Callable");
    }
}
