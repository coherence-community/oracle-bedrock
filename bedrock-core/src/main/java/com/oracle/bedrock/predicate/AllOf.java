/*
 * File: AllOf.java
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

package com.oracle.bedrock.predicate;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A {@link Predicate} representing the lazy conjunction of
 * zero or more other {@link Predicate}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <T>  the type of the {@link Predicate} value
 *
 * @author Brian Oliver
 */
public class AllOf<T> implements Predicate<T>
{
    /**
     * The {@link Predicate}s that must hold.
     */
    private Predicate<? super T>[] predicates;


    /**
     * Constructor for the {@link AllOf} {@link Predicate}.
     *
     * @param predicates  the {@link Predicate}s to check
     */
    public AllOf(Predicate<? super T>... predicates)
    {
        this.predicates = predicates;
    }


    @Override
    public boolean test(T value)
    {
        if (predicates == null)
        {
            return false;
        }
        else
        {
            for (Predicate<? super T> predicate : predicates)
            {
                if (!predicate.test(value))
                {
                    return false;
                }
            }

            return true;
        }
    }


    @Override
    public String toString()
    {
        return "AllOf{" + Arrays.toString(predicates) + '}';
    }
}
