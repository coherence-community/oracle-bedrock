/*
 * File: StabilityPredicate.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.predicate.Predicates;
import com.oracle.bedrock.runtime.Assembly;

import java.util.function.Predicate;

/**
 * An {@link Option} that defines a {@link Predicate} for testing the stability
 * of an {@link Assembly}.   This is typically used for ensuring an {@link Assembly}
 * reaches a certain condition after creation, expansion and/or relaunching.
 *
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Assembly}
 */
public class StabilityPredicate<A extends Assembly> implements Option
{
    /**
     * The {@link Predicate} to test the stability of an {@link Assembly}.
     */
    private Predicate<A> predicate;


    /**
     * Constructs a {@link StabilityPredicate} with the specified {@link Predicate}.
     *
     * @param predicate  the {@link Predicate}
     */
    private StabilityPredicate(Predicate<A> predicate)
    {
        this.predicate = predicate;
    }


    /**
     * Obtains a {@link StabilityPredicate} for the specified {@link Predicate}.
     *
     * @param predicate  the {@link Predicate}
     *
     * @param <A>  the type of {@link Assembly}
     *
     * @return a {@link StabilityPredicate}
     */
    public static <A extends Assembly> StabilityPredicate<A> of(Predicate<A> predicate)
    {
        return new StabilityPredicate<>(predicate);
    }


    /**
     * Obtains a {@link StabilityPredicate} that succeeds immediately without checking
     * any {@link Assembly} conditions.
     *
     * @param <A>  the type of the {@link Assembly}
     *
     * @return a {@link StabilityPredicate}
     */
    public static <A extends Assembly> StabilityPredicate<A> none()
    {
        return new StabilityPredicate<>(Predicates.always());
    }


    /**
     * Obtain the {@link Predicate} from a {@link StabilityPredicate}
     *
     * @return  the {@link Predicate}
     */
    public Predicate<A> get()
    {
        return predicate;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof StabilityPredicate))
        {
            return false;
        }

        StabilityPredicate<?> that = (StabilityPredicate<?>) o;

        return predicate != null ? predicate.equals(that.predicate) : that.predicate == null;

    }


    @Override
    public int hashCode()
    {
        return predicate != null ? predicate.hashCode() : 0;
    }
}
