/*
 * File: PerpetualIterator.java
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

/**
 * An {@link Iterator} that perpetually returns the same value.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PerpetualIterator<T> implements Iterator<T>
{
    /**
     * The value to return for each {@link #next()} call.
     */
    private T value;


    /**
     * Constructs an {@link PerpetualIterator}.
     *
     * @param value  the value to return
     */
    public PerpetualIterator(T value)
    {
        this.value = value;
    }


    @Override
    public boolean hasNext()
    {
        return true;
    }


    @Override
    public T next()
    {
        return value;
    }


    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("PerpetualIterator's don't support removing values");
    }
}
