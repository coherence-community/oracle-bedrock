/*
 * File: AbstractExtensible.java
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

package com.oracle.bedrock.extensible;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract implementation of an {@link Extensible}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractExtensible implements Extensible
{
    /**
     * The map of feature implementations, keyed by identifying type.
     */
    private ConcurrentHashMap<Class, Object> features;


    /**
     * Constructs an {@link AbstractExtensible}.
     */
    public AbstractExtensible()
    {
        this.features = new ConcurrentHashMap<>();
    }


    @Override
    public <T> boolean has(Class<T> featureClass)
    {
        return featureClass != null && (featureClass.isInstance(this) || features.containsKey(featureClass));
    }


    @Override
    public <T> T get(Class<T> featureClass)
    {
        // if the required class is null then return null
        if (featureClass == null)
        {
            return null;
        }

        // check whether we have the feature
        T feature = (T) features.get(featureClass);

        // if we do not have the feature check whether this class is
        // an implementation of the required feature
        if (feature == null && featureClass.isInstance(this))
        {
            feature = (T) this;
        }

        return feature;
    }


    @Override
    public <T> T add(Class<T> featureClass,
                     T        feature)
    {
        if (featureClass == null)
        {
            return null;
        }
        else
        {
            if (featureClass.isInstance(this))
            {
                throw new UnsupportedOperationException("Can't add " + featureClass
                                                        + " as it is directly implemented by "
                                                        + this.getClass().getName());

            }
            else
            {
                if (feature instanceof Feature)
                {
                    ((Feature) feature).onAddingTo(this);
                }

                return (T) features.put(featureClass, feature);
            }
        }
    }


    @Override
    public <T> T add(T feature)
    {
        return feature == null ? null : add((Class<T>) feature.getClass(), feature);
    }


    @Override
    public <T> T remove(Class<T> featureClass)
    {
        if (featureClass == null)
        {
            return null;
        }
        else
        {
            if (featureClass.isInstance(this))
            {
                throw new UnsupportedOperationException("Can't remove " + featureClass
                                                        + " as it is directly implemented by "
                                                        + this.getClass().getName());
            }
            else
            {
                T feature = (T) features.remove(featureClass);

                if (feature instanceof Feature)
                {
                    ((Feature) feature).onRemovingFrom(this);
                }

                return feature;
            }
        }
    }


    /**
     * Obtains an {@link Iterable} over the Features implementing a specific class/interface.
     *
     * @param requiredClass  the required {@link Class} a feature must implement
     *
     * @param <T>  the type of the required {@link Class}
     *
     * @return  an {@link Iterable} over features implementing the required class/interface
     */
    protected <T> Iterable<T> getInstancesOf(Class<T> requiredClass)
    {
        if (requiredClass == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            LinkedList<T> result = new LinkedList<>();

            features.values().forEach(
                feature -> {
                    if (requiredClass.isInstance(feature))
                    {
                        result.add((T) feature);
                    }
                });

            return result;
        }
    }


    /**
     * Removes all of the features from the {@link Extensible}.
     */
    protected void removeAllFeatures()
    {
        for (Map.Entry<Class, Object> entry : features.entrySet())
        {
            remove(entry.getKey());
        }
    }
}
