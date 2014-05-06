/*
 * File: DeferredPredicate.java
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

import com.oracle.tools.predicate.Predicate;

/**
 * A {@link DeferredPredicate} is a {@link Deferred} that represents an attempt to
 * evaluate and satisfy a {@link Predicate} with a given value (also a {@link Deferred}
 * at some point in the future.
 * <p>
 * A {@link DeferredPredicate} will only return <code>true</code> when the
 * {@link Predicate} is satisfied using the provided value, otherwise
 * <code>InstanceUnavailableException</code> will be thrown.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredPredicate<T> implements Deferred<Boolean>
{
    /**
     * The {@link Predicate} to use for matching.
     */
    private Predicate<? super T> predicate;

    /**
     * The {@link Deferred} value to be used for evaluating the
     * {@link Predicate}.
     */
    private Deferred<T> deferred;

    /**
     * The last resolved value use when attempting the match.
     * <p>
     * This is useful for reporting why a {@link DeferredPredicate} may have failed.
     */
    private T lastMatchedValue;


    /**
     * Constructs {@link DeferredPredicate} for the specified {@link Deferred}
     * and {@link Predicate}.
     *
     * @param deferred   the {@link Deferred} value to match with the {@link Predicate}
     * @param predicate  the {@link Predicate}
     */
    public DeferredPredicate(Deferred<T>          deferred,
                             Predicate<? super T> predicate)
    {
        this.deferred         = deferred;
        this.predicate        = predicate;
        this.lastMatchedValue = null;
    }


    /**
     * Constructs a {@link DeferredPredicate} for value with the
     * specified {@link Predicate}.
     *
     * @param value      the value to use supplied to the {@link Predicate}
     * @param predicate  the {@link Predicate}
     */
    public DeferredPredicate(T                    value,
                             Predicate<? super T> predicate)
    {
        if (value instanceof Deferred)
        {
            this.deferred = (Deferred) value;
        }
        else
        {
            this.deferred = new Existing<T>(value);
        }

        this.predicate        = predicate;
        this.lastMatchedValue = null;
    }


    /**
     * Obtains the {@link Deferred} that provides the underlying value
     * to evaluate with the {@link Predicate}.
     *
     * @return  the {@link Deferred}
     */
    public Deferred<T> getDeferred()
    {
        return deferred;
    }


    /**
     * Obtains the {@link Predicate} to evaluate.
     *
     * @return the {@link Predicate}
     */
    public Predicate<? super T> getPredicate()
    {
        return predicate;
    }


    /**
     * Obtains the value used when attempting to perform a match.
     * This is useful for determining why a match may have failed.
     *
     * @return  the last value (obtained from {@link #getDeferred()}) that
     *          was used for matching
     */
    public T getLastUsedMatchValue()
    {
        return lastMatchedValue;
    }


    @Override
    public Boolean get() throws UnresolvableInstanceException, InstanceUnavailableException
    {
        try
        {
            lastMatchedValue = deferred.get();

            if (predicate.evaluate(lastMatchedValue))
            {
                return true;
            }
            else
            {
                throw new InstanceUnavailableException(this);
            }
        }
        catch (InstanceUnavailableException e)
        {
            throw e;
        }
        catch (UnresolvableInstanceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new UnresolvableInstanceException(this, e);
        }
    }


    @Override
    public Class<Boolean> getDeferredClass()
    {
        return Boolean.class;
    }


    @Override
    public String toString()
    {
        return String.format("DeferredPredicate{deferred=%s, predicate=%s, lastUsedMatchValue=%s}",
                             deferred,
                             predicate,
                             lastMatchedValue);
    }
}
