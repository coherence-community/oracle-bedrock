/*
 * File: NotAvailable.java
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
 * A {@link NotAvailable} is a specialized {@link Deferred} that
 * always returns <code>null</code> when attempting to call {@link #get()}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NotAvailable<T> implements Deferred<T>
{
    /**
     * The class of the {@link Deferred} object.
     */
    private Class<T> m_deferredClass;


    /**
     * Constructor for a {@link NotAvailable} reference.
     *
     * @param deferredClass
     */
    public NotAvailable(Class<T> deferredClass)
    {
        m_deferredClass = deferredClass;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws ObjectNotAvailableException
    {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getDeferredClass()
    {
        return m_deferredClass;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("NotAvailable<%s>", getDeferredClass());
    }
}
