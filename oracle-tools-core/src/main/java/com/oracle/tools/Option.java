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

package com.oracle.tools;

/**
 * Encapsulates a strictly immutable configuration option.
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
     * A special type of {@link Option} that collects {@link Collectable}
     * {@link Option}s to produce a new {@link Collectable} {@link Option}.
     * <p>
     * {@link Collector}s must define an {@link Options.Default} annotation
     * on either a constructor, static attribute or static method to define the
     * mechanism for instantiating a new {@link Collector}.
     *
     * @param <C> the type of {@link Collectable}s that are collected
     * @param <T> the type of {@link Collector}, usually the concrete type implementing this
     *            interface, to permit fluent-style methods calls
     */
    interface Collector<C extends Collectable<T>, T extends Collector<C, T>> extends Option
    {
        /**
         * Collects the specified {@link Collectable} into a new {@link Collector},
         * possibly including {@link Collectable}s from this {@link Collector}.
         * <p>
         * It's important to return a new {@link Collector} to ensure the current
         * {@link Collector} {@link Option} is immutable.
         *
         * @param collectable  the {@link Collectable}
         *
         * @return a new {@link Collector}
         */
        T collect(C collectable);
    }

    /**
     * A special type of {@link Option} that are collected by {@link Collector}s.
     * <p>
     * {@link Collectable}s are never placed in {@link Options}, instead they are
     * collected into {@link Collector}s, which themselves are managed by {@link Options}.
     *
     * @param <T> the type of {@link Collector} in which the {@link Collectable}
     *            will be collected
     */
    interface Collectable<T extends Collector> extends Option
    {
        /**
         * Obtains the type of {@link Collector} into which this {@link Collectable} should be placed.
         *
         * @return  the type of {@link Collector}
         */
        Class<T> getCollectorClass();
    }
}
