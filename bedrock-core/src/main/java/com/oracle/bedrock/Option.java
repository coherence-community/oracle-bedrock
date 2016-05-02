/*
 * File: Option.java
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

package com.oracle.bedrock;

/**
 * Encapsulates a <b>strictly immutable</b> configuration option.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @see Options
 */
public interface Option
{
    /**
     * A special type of {@link Option} is collected by {@link Collector}s.
     * <p>
     * {@link Collectable}s are never managed by {@link Options} directly, instead they are
     * collected into {@link Collector}s, which themselves are managed by {@link Options}.
     */
    interface Collectable extends Option
    {
        /**
         * Obtains the type of {@link Collector} into which this {@link Collectable} should be placed.
         *
         * @return the type of {@link Collector}
         */
        Class<? extends Collector> getCollectorClass();
    }


    /**
     * A special type of {@link Option} that manages a collection
     * of {@link Collectable}s with in an {@link Options} instance.
     * <p>
     * {@link Collector} implementations must define an {@link Options.Default} annotation
     * on either a constructor, static attribute or static method to define the
     * mechanism for instantiating a new {@link Collector} when required by an {@link Options} instance.
     * <p>
     * Like regular {@link Option}s, {@link Collector}s are immutable.  Each mutation
     * of a {@link Collector}, by adding, removing or updating {@link Collectable}s, must
     * produce a new {@link Collector}.
     *
     * @param <C> the type of {@link Collectable}s that are collected
     * @param <T> this type of {@link Collector} being implemented to permit fluent-style method calls
     */
    interface Collector<C extends Collectable, T extends Collector<C, T>> extends Option, Iterable<C>
    {
        /**
         * Collects the specified {@link Collectable} into a new {@link Collector} instance,
         * that of which also contains the {@link Collectable}s of this {@link Collector}.
         *
         * @param collectable the {@link Collectable}
         *
         * @return a new {@link Collector} instance, containing the specified {@link Collectable}
         */
        T with(C collectable);


        /**
         * Dispenses the specified {@link Collectable} from this {@link Collector} by
         * creating a new {@link Collector} without the said {@link Collectable}.
         *
         * @param collectable the {@link Collectable}
         *
         * @return a new {@link Collector} instance, without the specified {@link Collector}
         */
        T without(C collectable);


        /**
         * Obtains all of the instance of the specified class that have been collected
         * by the {@link Collector}.
         *
         * @param requiredClass  the required class
         * @param <O>            the type of the required class
         *
         * @return  an {@link Iterable} over the {@link Collectable} instances of the required class
         */
        <O> Iterable<O> getInstancesOf(Class<O> requiredClass);
    }
}
