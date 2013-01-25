/*
 * File: DeferredAtomicBoolean.java
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

package com.oracle.tools.deferred.atomic;

import com.oracle.tools.deferred.Deferred;
import com.oracle.tools.deferred.ObjectNotAvailableException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link DeferredAtomicBoolean} is a {@link Deferred} representation of an
 * {@link AtomicBoolean}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredAtomicBoolean implements Deferred<Boolean>
{
    /**
     * The atomic value being deferred.
     */
    private AtomicBoolean m_atomic;


    /**
     * Constructs a {@link DeferredAtomicBoolean} representation of an
     * {@link AtomicBoolean}.
     *
     * @param atomic  the atomic to be deferred
     */
    public DeferredAtomicBoolean(AtomicBoolean atomic)
    {
        m_atomic = atomic;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean get() throws ObjectNotAvailableException
    {
        return m_atomic.get();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Boolean> getDeferredClass()
    {
        return Boolean.class;
    }
}
