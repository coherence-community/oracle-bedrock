/*
 * File: DeferredMatch.java
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

import org.hamcrest.Matcher;

/**
 * A {@link DeferredMatch} is a {@link Deferred} that represents
 * a Hamcrest {@link Matcher}, that of which must be satisfied for a
 * specified {@link Deferred} object at some point in the future.
 * <p>
 * A {@link DeferredMatch} will only return <code>true</code> when the match
 * is successful, otherwise <code>null</code> will be returned.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredMatch<T> implements Deferred<Boolean>
{
    /**
     * The {@link Deferred} that we want to match with a {@link Matcher}.
     */
    private Deferred<T> deferred;

    /**
     * The {@link Matcher} to use against the {@link Deferred}.
     */
    private Matcher<? super T> matcher;

    /**
     * The last {@link Deferred} value use when attempting the match.
     * <p>
     * This is useful for reporting why a {@link DeferredMatch} may have
     * failed.
     */
    private T lastUsedMatchValue;


    /**
     * Constructs a {@link DeferredMatch} for the {@link Deferred}
     * with the specified {@link Matcher}.
     *
     * @param deferred  the {@link Deferred} to match against
     * @param matcher   the {@link Matcher}
     */
    public DeferredMatch(Deferred<T>        deferred,
                         Matcher<? super T> matcher)
    {
        this.deferred           = deferred;
        this.matcher            = matcher;
        this.lastUsedMatchValue = null;
    }


    /**
     * Obtains the adapted {@link Deferred}.
     *
     * @return  the adapted {@link Deferred}
     */
    public Deferred<T> getDeferred()
    {
        return deferred;
    }


    /**
     * Obtains the {@link Matcher} to apply.
     *
     * @return the {@link Matcher}
     */
    public Matcher<? super T> getMatcher()
    {
        return matcher;
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
        return lastUsedMatchValue;
    }


    @Override
    public Boolean get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        try
        {
            lastUsedMatchValue = deferred.get();

            if (matcher.matches(lastUsedMatchValue))
            {
                return true;
            }
            else
            {
                throw new TemporarilyUnavailableException(this);
            }
        }
        catch (UnavailableException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PermanentlyUnavailableException(this, e);
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
        return String.format("DeferredMatch{deferred=%s, matcher=%s, lastUsedMatchValue=%s}",
                             deferred,
                             matcher,
                             lastUsedMatchValue);
    }
}
