/*
 * File: FutureCompletionListener.java
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

package com.oracle.tools.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link Future}-based implementation of a {@link CompletionListener}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class FutureCompletionListener<T> implements CompletionListener<T>, Future<T>
{
    /**
     * Has a result or exception been provided?
     */
    private boolean hasResult;

    /**
     * The result (may be null).
     */
    private T result;

    /**
     * The exception (null if there was no exception).
     */
    private Exception exception;


    /**
     * Constructs a {@link FutureCompletionListener}.
     */
    public FutureCompletionListener()
    {
        hasResult = false;
        result    = null;
        exception = null;
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
                // TODO: seriously in trouble now... this listener has already been used as a Future once before!
            }
            else
            {
                this.hasResult = true;
                this.result    = result;
                this.exception = null;

                notifyAll();
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
                // TODO: seriously in trouble now... this listener has already been used as a Future once before!
            }
            else
            {
                this.hasResult = true;
                this.result    = null;
                this.exception = exception;

                notifyAll();
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        // CompletionListeners can't be cancelled
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled()
    {
        // CompletionListeners can't be cancelled
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone()
    {
        synchronized (this)
        {
            return hasResult;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws InterruptedException, ExecutionException
    {
        synchronized (this)
        {
            if (!hasResult)
            {
                wait();
            }

            if (hasResult)
            {
                if (exception != null)
                {
                    throw new ExecutionException(exception);
                }
                else
                {
                    return result;
                }
            }
            else
            {
                throw new InterruptedException("Interrupted while waiting for a result");
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get(long     timeout,
                 TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        synchronized (this)
        {
            if (!hasResult)
            {
                unit.timedWait(this, timeout);
            }

            if (hasResult)
            {
                if (exception != null)
                {
                    throw new ExecutionException(exception);
                }
                else
                {
                    return result;
                }
            }
            else
            {
                throw new TimeoutException();
            }
        }
    }
}
