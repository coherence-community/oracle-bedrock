/*
 * File: ApplicationStream.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Stream}-like interface for processing {@link Application}s in an {@link Assembly}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <T>  the type of {@link Application}
 */
public interface ApplicationStream<T extends Application>
{
    /**
     * Closes the {@link Application}s in the {@link ApplicationStream}.
     *
     * @param options  the {@link Option}s for closing the {@link Application}s
     */
    void close(Option... options);


    /**
     * Performs an action for each {@link Application} in the {@link ApplicationStream}.
     *
     * @param consumer  the {@link Application} consumer
     */
    void forEach(Consumer<? super T> consumer);


    /**
     * Returns the count of the number of {@link Application}s in the {@link ApplicationStream}.
     *
     * @return  the number of {@link Application}s in the {@link ApplicationStream}
     */
    long count();


    /**
     * Obtains an {@link ApplicationStream} consisting of {@link Application}s that satisfy the
     * specified {@link Predicate}.
     *
     * @param predicate  the {@link Application} predicate
     *
     * @return  an {@link ApplicationStream}
     */
    ApplicationStream<T> filter(Predicate<? super T> predicate);


    /**
     * Determines whether all of the {@link Application}s in the {@link ApplicationStream}
     * satisfy the specified {@link Predicate}.
     *
     * @param predicate  the {@link Application} predicate
     *
     * @return  <code>true</code> if the {@link Predicate} is satisfied for all of the {@link Application}s
     *          <code>false</code> otherwise
     */
    boolean allMatch(Predicate<? super T> predicate);


    /**
     * Determines whether any of the {@link Application}s in the {@link ApplicationStream}
     * satisfy the specified {@link Predicate}.
     *
     * @param predicate  the {@link Application} predicate
     *
     * @return  <code>true</code> if the {@link Predicate} is satisfied for any of the {@link Application}s
     *          <code>false</code> otherwise
     */
    boolean anyMatch(Predicate<? super T> predicate);


    /**
     * Determines whether none of the {@link Application}s in the {@link ApplicationStream}
     * satisfy the specified {@link Predicate}.
     *
     * @param predicate  the {@link Application} predicate
     *
     * @return  <code>true</code> if the {@link Predicate} is satisfied for none of the {@link Application}s
     *          <code>false</code> otherwise
     */
    boolean noneMatch(Predicate<? super T> predicate);


    /**
     * Returns an {@link ApplicationStream} limited to the maximum specified number of {@link Application}s
     * drawn in-order from this {@link ApplicationStream}.
     *
     * @param maximum  the maximum number of {@link Application}s in the returned {@link ApplicationStream}
     *
     * @return  an {@link ApplicationStream}
     */
    ApplicationStream<T> limit(int maximum);


    /**
     * Returns this {@link ApplicationStream} where by each {@link Application} has the provided
     * {@link Consumer} action performed against it prior to it being consumed from the resulting
     * {@link ApplicationStream}.
     *
     * @param consumer  the {@link Consumer}
     *
     * @return an {@link ApplicationStream}
     */
    ApplicationStream<T> peek(Consumer<? super T> consumer);


    /**
     * Performs a mutable reduction operation on the {@link Application}s of this {@link ApplicationStream}
     * using a {@link Collector}. A {@link Collector} encapsulates the functions used as arguments to
     * collect(Supplier, BiConsumer, BiConsumer), allowing for reuse of collection strategies and composition of
     * collect operations such as multiple-level grouping or partitioning.
     *
     * @param collector  the {@link Collector} describing the reduction
     * @param <R>        the type of the result
     * @param <A>        the intermediate accumulation of the {@link Collector}
     *
     * @return the result of the collection / reduction
     *
     * @see Collectors
     */
    <R, A> R collect(Collector<? super T, A, R> collector);


    /**
     * Performs a mutable reduction operation on the {@link Application}s of this {@link ApplicationStream}.
     *
     * @param supplier     the function that creates a new result container
     * @param accumulator  an associate, non-interfering, stateless function for incorporating an element into the result
     * @param combiner     an association, non-interfering, stateless function for combining two values
     * @param <R>          the type of the result
     *
     * @return the result of the collection / reduction
     *
     * @see Collectors
     */
    <R> R collect(Supplier<R>              supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R>         combiner);


    /**
     * Returns an {@link Optional} for any {@link Application} in the {@link ApplicationStream}.
     *
     * @return an {@link Optional} {@link Application}
     */
    Optional<T> findAny();


    /**
     * Returns the {@link Optional} first {@link Application} in the {@link ApplicationStream}.
     *
     * @return an {@link Optional} {@link Application}
     */
    Optional<T> findFirst();


    /**
     * Restarts the {@link Application}s in the {@link ApplicationStream} using the provided
     * {@link Option}s.
     *
     * @param options  the relaunch {@link Option}s
     */
    void relaunch(Option... options);


    /**
     * Returns an {@link ApplicationStream} consisting of the {@link Application}s in this
     * {@link ApplicationStream} but unordered in manner.  The resulting {@link ApplicationStream}
     * will consist of the same {@link Application}s but in a randomized order.
     *
     * @return  an unordered {@link ApplicationStream}
     */
    ApplicationStream<T> unordered();
}
