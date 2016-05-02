/*
 * File: MappingIterator.java
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
 * An {@link Iterator} that maps values from an underlying {@link Iterator}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class MappingIterator<X, Y> implements Iterator<Y>
{
    /**
     * The {@link Function} to map values.
     */
    private Function<X, Y> mapper;

    /**
     * The {@link Iterator} of values to map from.
     */
    private Iterator<X> iterator;


    /**
     * Constructs a {@link MappingIterator}.
     *
     * @param iterator  the {@link Iterator} of values to map from
     * @param mapper    the {@link Function} to map values to
     */
    public MappingIterator(Iterator<X>    iterator,
                           Function<X, Y> mapper)
    {
        this.iterator = iterator;
        this.mapper   = mapper;
    }


    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }


    @Override
    public Y next()
    {
        return mapper.map(iterator.next());
    }


    @Override
    public void remove()
    {
        iterator.remove();
    }


    /**
     * A function to map from one type of value to another.
     *
     * @param <X>  the type of value to map from
     * @param <Y>  the type of value to map to
     */
    public static interface Function<X, Y>
    {
        /**
         * Maps a value of type X to type Y.
         *
         * @param x  the value to map
         *
         * @return  the mapped value
         */
        public Y map(X x);
    }
}
