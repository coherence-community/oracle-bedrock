/*
 * File: Predicates.java
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

import java.util.Collection;
import java.util.function.Predicate;

/**
 * A set of helper methods for creating {@link Predicate}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Predicates
{
    public static <T> Predicate<T> allOf(Predicate<T>... predicates)
    {
        return new AllOf<T>(predicates);
    }


    public static <T> Predicate<T> always()
    {
        return new Always();
    }


    public static <T> Predicate<Collection<T>> contains(T value)
    {
        return new Contains<T>(value);
    }


    public static <T> Predicate<Collection<T>> doesNotContain(T value)
    {
        return isNot(new Contains<T>(value));
    }


    public static <T> Predicate<T> equalTo(T value)
    {
        return new EqualTo<T>(value);
    }


    public static <T extends Comparable<T>> Predicate<T> greaterThan(T value)
    {
        return new GreaterThan<T>(value);
    }


    public static <T> Predicate<T> is(Predicate<T> predicate)
    {
        return new Is<T>(predicate);
    }


    public static <T> Predicate<T> is(T value)
    {
        return new Is<T>(new EqualTo<T>(value));
    }


    public static <T> Predicate<T> isNot(Predicate<T> predicate)
    {
        return new IsNot<T>(predicate);
    }


    public static <T> Predicate<T> isNot(T value)
    {
        return new IsNot<T>(new EqualTo<T>(value));
    }


    public static <T> Predicate<T> isNull()
    {
        return new IsNull();
    }


    public static <T extends Comparable<T>> Predicate<T> lessThan(T value)
    {
        return new LessThan<T>(value);
    }


    public static <T> Predicate<T> never()
    {
        return new Never();
    }
}
