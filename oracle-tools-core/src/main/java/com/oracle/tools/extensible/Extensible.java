/*
 * File: Extensible.java
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

package com.oracle.tools.extensible;

/**
 * {@link Extensible}s are types that may be evolve or be dynamically extended
 * by adding new features during their lifetime.
 * <p>
 * Specifically, {@link Extensible}s allow applications to dynamically “add” new features
 * and “remove” existing features, together with their associated state during runtime.
 * <p>
 * For example:  Say we have a Person class.  During the lifetime of a Person we may like to add a
 * feature “Married”, that includes it’s own state and behavior.   Later we may want to remove it.
 * In this situation the Person class would benefit from being “Extensible”, with the Married concept,
 * also a class, being a “Feature”.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @see Feature
 *
 * @author Brian Oliver
 * @author Aleks Seovic
 */
public interface Extensible
{
    /**
     * Determines if an {@link Extensible} currently has and supports the specified type of feature.
     *
     * @return <code>true</code> if the {@link Extensible} supports the specified feature,
     *         <code>false</code> otherwise
     */
    <T> boolean has(Class<T> featureClass);


    /**
     * Obtains a reference to the current implementation of the specified type of feature on
     * the {@link Extensible}.
     *
     * @return the implementation of the specified feature class or <code>null</code> if the
     *         feature is not supported
     */
    <T> T get(Class<T> featureClass);


    /**
     * Dynamically adds the specified type of feature and implementation to the
     * {@link Extensible}, replacing the existing implementation of the specified type if present
     * and returning the previous implementation.  Returns <code>null</code> if the feature is
     * currently not supported by the {@link Extensible}.
     * <p>
     * Should the feature implement {@link Feature}, the {@link Feature#onAddingTo(Extensible)}
     * method is called using this {@link Extensible} as the parameter prior to returning.
     *
     * @param featureClass  the {@link Class} to be used to identify the feature
     * @param feature       the implementation of the feature
     *
     * @return the previous implementation of the featured or <code>null</code> if the feature
     *         was not defined
     *
     * @throws UnsupportedOperationException  if the feature can't be added to the {@link Extensible}
     *                                        for some reason (eg: it already implements it)
     */
    <T> T add(Class<T> featureClass,
              T        feature) throws UnsupportedOperationException;


    /**
     * Dynamically adds the specified feature to the {@link Extensible},
     * replacing the existing implementation of the same type if present and returning
     * the previous implementation.  Returns <code>null</code> if the feature is currently not supported
     * by the {@link Extensible}.
     * <p>
     * Should the feature implement {@link Feature}, the {@link Feature#onAddingTo(Extensible)}
     * method is called using this {@link Extensible} as the parameter prior to returning.
     *
     * @param feature  the implementation of the feature
     *
     * @return the previous implementation of the featured or <code>null</code> if the feature
     *         was not defined
     *
     * @throws UnsupportedOperationException  if the feature can't be added to the {@link Extensible}
     *                                        for some reason (eg: it already implements it)
     */
    <T> T add(T feature) throws UnsupportedOperationException;


    /**
     * Dynamically removes the implementation of a feature with the specified type of
     * from the {@link Extensible}, returning the implementation if it was removed.  Returns
     * <code>null</code> if the type of feature is not supported.
     * <p>
     * Should the feature implement {@link Feature}, the {@link Feature#onRemovingFrom(Extensible)}
     * method is called using this {@link Extensible} as the parameter.
     *
     * @param featureClass  the type of feature to remove
     *
     * @return the feature implementation or <code>null</code> if the feature was not defined
     *
     * @throws UnsupportedOperationException  if the feature can't be removed from the {@link Extensible}
     *                                        for some reason
     */
    <T> T remove(Class<T> featureClass) throws UnsupportedOperationException;
}
