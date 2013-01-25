/*
 * File: Future.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link Future} is a {@link Deferred} representation of a standard Java
 * {@link java.util.concurrent.Future}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Future<T> implements Deferred<T>
{
    /**
     * The {@link java.util.concurrent.Future} to represent as a {@link Deferred}.
     */
    private java.util.concurrent.Future<T> m_future;

    /**
     * The {@link Class} of the result of the {@link java.util.concurrent.Future}.
     */
    private Class<T> m_clzOfResult;


    /**
     * Constructs a {@link Future} based on a Java {@link java.util.concurrent.Future}.
     *
     * @param future  the Java {@link java.util.concurrent.Future}
     */
    public Future(Class<T> clzOfResult,
                  java.util.concurrent.Future<T> future)
    {
        m_clzOfResult = clzOfResult;
        m_future      = future;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws ObjectNotAvailableException
    {
        try
        {
            return m_future.get(0, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            throw new ObjectNotAvailableException(this, e);
        }
        catch (ExecutionException e)
        {
            throw new ObjectNotAvailableException(this, e);
        }
        catch (TimeoutException e)
        {
            throw new ObjectNotAvailableException(this, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getDeferredClass()
    {
        return m_clzOfResult;
    }
}
