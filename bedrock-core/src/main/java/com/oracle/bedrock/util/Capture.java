/*
 * File: Capture.java
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

package com.oracle.bedrock.util;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A {@link Capture} is an {@link Iterator} that captures and returns a
 * single value from another underlying {@link Iterator}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Capture<T> implements Iterator<T>
{
    /**
     * The underlying wrapped {@link Iterator} supplier.
     */
    private final Supplier<Iterator<T>> supplier;

    /**
     * The underlying wrapped {@link Iterator}.
     */
    private volatile Iterator<T> iterator;

    /**
     * The captured value from the {@link Iterator}.
     */
    private volatile AtomicReference<T> capture;

    /**
     * Constructs a {@link Capture} with the specified
     * underlying (source) {@link Iterator}.
     *
     * @param iterator  the underlying {@link Iterator} providing
     *                  the {@link Capture} with its value.
     */
    public Capture(Iterator<T> iterator)
    {
    this(() -> iterator);
    }

    /**
     * Constructs a {@link Capture} with the specified
     * underlying (source) {@link Iterator}.
     *
     * @param supplier  the underlying {@link Iterator} supplier providing
     *                  the {@link Capture} with its value.
     */
    public Capture(Supplier<Iterator<T>> supplier)
    {
        this.supplier = supplier;
        this.capture  = null;
    }


    /**
     * Determine if the {@link Capture} has captured a value
     * from the underlying {@link Iterator}.
     *
     * @return  <code>true</code> if a value has been captured
     */
    public boolean hasValue()
    {
        return capture != null;
    }


    /**
     * Obtains the captured value.  Should a value not be captured,
     * {@link #next()} is invoked to capture a value.
     *
     * @return  the captured value
     */
    public T get()
    {
        return next();
    }


    @Override
    public boolean hasNext()
    {
        return hasValue() || ensureIterator().hasNext();
    }


    @Override
    public T next()
    {
        if (!hasValue())
        {
            capture = new AtomicReference<T>(ensureIterator().next());
        }

        return capture.get();
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Can't remove from a Capture");
    }


    private synchronized Iterator<T> ensureIterator()
    {
        if (iterator == null)
        {
            Iterator<T> it = supplier.get();
            if (it == null)
            {
                throw new IllegalStateException("The iterator supplier returned null");
            }
            iterator = it;
        }
        return iterator;
    }

    /**
     * Obtains a {@link Capture} based on a provided {@link Iterator}.
     *
     * @param supplier  the {@link Iterator} supplier
     * @param <T>       the type of the value captured
     *
     * @return the {@link Capture} of the {@link Iterator}
     */
    public static <T> Capture<T> of(Supplier<Iterator<T>> supplier)
    {
        return new Capture<>(supplier);
    }


    /**
     * Obtains a {@link Capture} based on a provided {@link Iterator}.
     *
     * @param iterator  the {@link Iterator}
     * @param <T>       the type of the value captured
     *
     * @return the {@link Capture} of the {@link Iterator}
     */
    public static <T> Capture<T> of(Iterator<T> iterator)
    {
        return new Capture<>(iterator);
    }
}
