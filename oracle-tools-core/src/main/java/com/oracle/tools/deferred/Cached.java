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

package com.oracle.tools.deferred;

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
    private Deferred<T> m_deferred;

    /**
     * The last successfully resolved object from the {@link #m_deferred}.
     */
    private T m_object;


    /**
     * Constructs a {@link Cached} for the specified {@link Deferred}.
     *
     * @param deferred  the {@link Deferred}
     */
    public Cached(Deferred<T> deferred)
    {
        m_deferred = deferred;
        m_object   = null;
    }


    /**
     * Obtains the adapted {@link Deferred}.
     *
     * @return  the adapted {@link Deferred}
     */
    public Deferred<T> getDeferred()
    {
        return m_deferred;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws ObjectNotAvailableException
    {
        if (m_object == null)
        {
            synchronized (this)
            {
                if (m_object == null)
                {
                    m_object = m_deferred.get();
                }
            }
        }

        return m_object;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getDeferredClass()
    {
        return m_deferred.getDeferredClass();
    }


    /**
     * Release the currently cached object.
     *
     * @return  the currently cached object or <code>null</code> if nothing
     *          is currently cached
     */
    public synchronized T release()
    {
        T object = m_object;

        m_object = null;

        return object;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("Cached{%s}", getDeferredClass());
    }
}
