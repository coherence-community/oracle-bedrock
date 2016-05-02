/*
 * File: Cached.java
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

import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link Cached} object is a specialized {@link Deferred} that holds
 * a reference to (ie: caches) an object that was successfully returned by
 * another {@link Deferred}.
 * <p>
 * ie: A {@link Cached} is a {@link Deferred} adapter.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Cached<T> implements Deferred<T>
{
    /**
     * The {@link Deferred} that will provide us with an object to cache.
     */
    private Deferred<T> deferred;

    /**
     * The last successfully resolved object from the {@link #deferred}.
     */
    private AtomicReference<T> object;


    /**
     * Constructs a {@link Cached} for the specified {@link Deferred}.
     *
     * @param deferred  the {@link Deferred}
     */
    public Cached(Deferred<T> deferred)
    {
        this.deferred = deferred;
        this.object   = null;
    }


    /**
     * Obtains the adapted {@link Deferred}.
     *
     * @return  the adapted {@link Deferred}
     */
    public Deferred<T> getDeferred()
    {
        return deferred;
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        if (object == null)
        {
            synchronized (this)
            {
                if (object == null)
                {
                    try
                    {
                        T deferred = this.deferred.get();

                        object = new AtomicReference<T>(deferred);
                    }
                    catch (UnavailableException e)
                    {
                        throw e;
                    }
                    catch (RuntimeException e)
                    {
                        throw new TemporarilyUnavailableException(this, e);
                    }
                }
            }
        }

        return object.get();
    }


    @Override
    public Class<T> getDeferredClass()
    {
        return deferred.getDeferredClass();
    }


    /**
     * Release the currently cached object.
     *
     * @return  the currently cached object
     */
    public synchronized T release()
    {
        T object = this.object == null ? null : this.object.get();

        this.object = null;

        return object;
    }


    @Override
    public String toString()
    {
        return String.format("Cached{%s}", getDeferredClass());
    }
}
