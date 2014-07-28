/*
 * File: DeferredAtomicLong.java
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

package com.oracle.tools.deferred.atomic;

import com.oracle.tools.deferred.Deferred;
import com.oracle.tools.deferred.PermanentlyUnavailableException;
import com.oracle.tools.deferred.TemporarilyUnavailableException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link DeferredAtomicLong} is a {@link Deferred} representation of an
 * {@link AtomicLong}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredAtomicLong implements Deferred<Long>
{
    /**
     * The atomic value being deferred.
     */
    private AtomicLong atomic;


    /**
     * Constructs a {@link DeferredAtomicLong} representation of an {@link AtomicLong}.
     *
     * @param atomic  the atomic to be deferred
     */
    public DeferredAtomicLong(AtomicLong atomic)
    {
        this.atomic = atomic;
    }


    @Override
    public Long get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        return atomic.get();
    }


    @Override
    public Class<Long> getDeferredClass()
    {
        return Long.class;
    }
}
