/*
 * File: DeferredCompletionListener.java
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

package com.oracle.tools.deferred.listener;

import com.oracle.tools.deferred.Deferred;
import com.oracle.tools.deferred.PermanentlyUnavailableException;
import com.oracle.tools.deferred.TemporarilyUnavailableException;

import com.oracle.tools.util.CompletionListener;

/**
 * A {@link Deferred} implementation of a {@link CompletionListener}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredCompletionListener<T> implements Deferred<T>, CompletionListener<T>
{
    /**
     * The expected {@link Class} of the result.
     */
    private Class<T> deferredClass;

    /**
     * The result as provided by the {@link CompletionListener}.
     * (may be null)
     */
    private T result;

    /**
     * The {@link Exception} as provided by the {@link CompletionListener}.
     * (null if no exception was raised)
     */
    private Exception exception;

    /**
     * A flag indicating if the {@link CompletionListener} has been
     * notified of a result or exception.
     */
    private boolean hasResult;


    /**
     * Constructs a {@link DeferredCompletionListener}.
     */
    public DeferredCompletionListener(Class<T> deferredClass)
    {
        this.deferredClass = deferredClass;
        this.result        = null;
        this.exception     = null;
        this.hasResult     = false;
    }


    @Override
    public void onCompletion(T result)
    {
        synchronized (this)
        {
            if (hasResult)
            {
                throw new IllegalStateException("Attempted to use a DeferredCompletionListener for more than one purpose");
            }
            else
            {
                hasResult      = true;

                this.result    = result;
                this.exception = null;
            }
        }
    }


    @Override
    public void onException(Exception exception)
    {
        synchronized (this)
        {
            if (hasResult)
            {
                throw new IllegalStateException("Attempted to use a DeferredCompletionListener for more than one purpose");
            }
            else
            {
                hasResult      = true;

                this.result    = null;
                this.exception = exception;
            }
        }
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        synchronized (this)
        {
            if (hasResult)
            {
                if (exception == null)
                {
                    return result;
                }
                else
                {
                    throw new PermanentlyUnavailableException(this, exception);
                }
            }
            else
            {
                throw new TemporarilyUnavailableException(this);
            }
        }
    }


    @Override
    public Class<T> getDeferredClass()
    {
        return deferredClass;
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("DeferredCompletionListener<" + deferredClass + ">{");
        builder.append(hasResult ? (exception == null ? result : exception) : "(no result)");
        builder.append("}");

        return builder.toString();
    }
}
