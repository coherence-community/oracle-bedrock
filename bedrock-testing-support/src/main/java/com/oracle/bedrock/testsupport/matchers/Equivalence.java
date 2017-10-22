/*
 * File: Equivalence.java
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

package com.oracle.bedrock.testsupport.matchers;

/**
 * A function to determine the equivalence of two objects of the same type.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Equivalence<T>
{
    /**
     * The standard {@link Object#equals(Object)} function.
     */
    public static final Equivalence EQUALS = new Equivalence<Object>()
    {
        @Override
        public boolean equals(Object x,
                              Object y)
        {
            return x == null && y == null || x != null && x.equals(y);
        }
    };

    /**
     * The {@link String#equalsIgnoreCase(String)} function.
     */
    public static final Equivalence<String> EQUALS_IGNORE_CASE = new Equivalence<String>()
    {
        @Override
        public boolean equals(String x,
                              String y)
        {
            return x == null && y == null || x.equalsIgnoreCase(y);
        }
    };


    /**
     * Determine if two values of the same type are equal.
     *
     * @param x  the first value
     * @param y  the second value
     *
     * @return <code>true</code> if x and y are equal
     */
    public boolean equals(T x,
                          T y);
}
