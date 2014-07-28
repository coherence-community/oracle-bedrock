/*
 * File: Existing.java
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
 * A {@link Existing} is a specialized {@link Deferred} implementation
 * that is based on a well-known and already established object, that is
 * guaranteed to be available when {@link #get()} is called.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Existing<T> implements Deferred<T>
{
    /**
     * The object to return on {@link #get()}.
     */
    private T object;


    /**
     * Construct a {@link Existing} given a specified object.
     *
     * @param object  the {@link Object} (can't be <code>null</code>)
     */
    public Existing(T object)
    {
        this.object = object;
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        return object;
    }


    @Override
    public Class<T> getDeferredClass()
    {
        return (Class<T>) object.getClass();
    }


    @Override
    public String toString()
    {
        return String.format("Existing<%s>{%s}", getDeferredClass(), get());
    }
}
