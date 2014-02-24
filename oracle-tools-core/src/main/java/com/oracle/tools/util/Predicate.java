/*
 * File: Predicate.java
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

package com.oracle.tools.util;

/**
 * A {@link Predicate} represents a boolean function to be evaluated for a
 * specific type of value.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Predicate<T>
{
    /**
     * A {@link Predicate} that always returned true.
     */
    public static final Predicate ALWAYS = new Predicate()
    {
        @Override
        public boolean evaluate(Object value)
        {
            return true;
        }

        @Override
        public String toString()
        {
            return "ALWAYS";
        }
    };

    /**
     * A {@link Predicate} that never returns true.
     */
    public static final Predicate NEVER = new Predicate()
    {
        @Override
        public boolean evaluate(Object value)
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "NEVER";
        }
    };


    /**
     * Determines if the specified value satisfies the {@link Predicate}.
     *
     * @param value  the value to test
     *
     * @return <code>true</code> if the specified value satisfies the {@link Predicate},
     *         <code>false</code> otherwise
     */
    public boolean evaluate(T value);


    /**
     * A {@link Predicate} that determines if a value is equal to
     * that which is specified.
     *
     * @param <T>  the type of the value
     */
    public static class Is<T> implements Predicate<T>
    {
        /**
         * The value to compare.
         */
        private T value;


        /**
         * Constructs an {@link Is} {@link Predicate}.
         *
         * @param value  the required value to be equal
         */
        public Is(T value)
        {
            this.value = value;
        }


        @Override
        public boolean evaluate(T value)
        {
            return value == null && this.value == null || value != null && value.equals(this.value);
        }


        @Override
        public String toString()
        {
            return "Is{" + value + "}";
        }
    }
}
