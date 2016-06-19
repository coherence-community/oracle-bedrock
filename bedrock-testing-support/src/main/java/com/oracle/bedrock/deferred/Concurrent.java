/*
 * File: Concurrent.java
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

/**
 * Supporting interfaces for concurrent assertion.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @see Concurrently
 *
 * @author Brian Oliver
 */
public class Concurrent
{
    /**
     * A mechanism to check the status and control the evaluation of an assertion, typically
     * occurring on another {@link Thread}.
     */
    interface Assertion extends AutoCloseable
    {
        /**
         * Checks the current state of the {@link Assertion}.
         *
         * @throws AssertionError  if the {@link Assertion} has failed
         */
        void check() throws AssertionError;


        /**
         * Stops evaluation of the {@link Assertion}.
         */
        @Override
        void close();


        /**
         * Determines if the {@link Assertion} has been closed.
         *
         * @return  <code>true></code> when the {@link Assertion} is closed and not longer being evaluated
         *          <code>false</code> otherwise
         */
        boolean isClosed();


        /**
         * Ensures that a {@link Throwable} doesn't contain any suppressed {@link AssertionError}s and
         * if it does, re-throws the first encountered.
         *
         * @param throwable  the {@link Throwable} to examine
         *
         * @throws AssertionError the first suppressed {@link AssertionError} encountered
         */
        static void check(Throwable throwable) throws AssertionError
        {
            if (throwable != null)
            {
                for (int i = 0; i < throwable.getSuppressed().length; i++)
                {
                    if (throwable.getSuppressed()[i] instanceof AssertionError)
                    {
                        throw(AssertionError) throwable.getSuppressed()[i];
                    }
                }
            }
        }
    }
}
