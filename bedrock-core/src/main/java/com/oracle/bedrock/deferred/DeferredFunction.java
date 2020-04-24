/*
 * File: DeferredFunction.java
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

package com.oracle.bedrock.deferred;

import java.util.function.Function;

/**
 * A {@link Deferred} representation of the {@link Function}.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredFunction<T, R> implements Deferred<R>
{
    /**
     * The {@link Deferred} on which the {@link Function} is to be called
     */
    private Deferred<T> deferred;

    /**
     * The {@link Function} to be called.
     */
    private Function<T, R> function;


    /**
     * Constructs a {@link DeferredFunction}.
     *
     * @param deferred       the {@link Deferred}
     * @param function       the {@link Function}
     */
    public DeferredFunction(Deferred<T>    deferred,
                            Function<T, R> function)
    {
        this.deferred = deferred;
        this.function = function;
    }


    @Override
    public R get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        try
        {
            // attempt to get the deferred value
            T value = deferred.get();

            // call the function with the value
            return function.apply(value);
        }
        catch (TemporarilyUnavailableException e)
        {
            // we rethrow temporarily unavailable exceptions
            throw e;
        }
        catch (PermanentlyUnavailableException e)
        {
            // we rethrow permanently unavailable exceptions
            throw e;
        }
        catch (Exception e)
        {
            // we assume any other exception is just a temporary issue
            throw new TemporarilyUnavailableException(this, e);
        }
    }
}
