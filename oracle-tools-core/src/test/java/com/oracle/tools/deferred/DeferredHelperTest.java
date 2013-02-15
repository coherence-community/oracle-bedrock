/*
 * File: DeferredHelperTest.java
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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.oracle.tools.deferred.DeferredHelper.*;

/**
 * Unit tests for the {@link DeferredHelper}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredHelperTest
{
    /**
     * Ensure that we can create a {@link Deferred} for an {@link AtomicLong}.
     */
    @Test
    public void thatDeferringAnAtomicLongIsPossible()
    {
        AtomicLong     a        = new AtomicLong(1);

        Deferred<Long> deferred = deferred(a);

        Assert.assertEquals(new Long(1), deferred.get());
    }


    /**
     * Ensure that we can create a {@link Deferred} for an {@link AtomicInteger}.
     */
    @Test
    public void thatDeferringAnAtomicIntegerIsPossible()
    {
        AtomicInteger     a        = new AtomicInteger(1);

        Deferred<Integer> deferred = deferred(a);

        Assert.assertEquals(new Integer(1), deferred.get());
    }


    /**
     * Ensure that we can create a {@link Deferred} for an {@link AtomicBoolean}.
     */
    @Test
    public void thatDeferringAnAtomicBooleanIsPossible()
    {
        AtomicBoolean     a        = new AtomicBoolean(true);

        Deferred<Boolean> deferred = deferred(a);

        Assert.assertEquals(Boolean.TRUE, deferred.get());
    }


    /**
     * Ensure that we can use DeferredAssert with Strings.
     */
    @Test
    public void thatCanUseDeferredAssertWithString()
    {
        StringContainer  container = new StringContainer("Gudday");

        Deferred<String> defString = eventually(invoking(container).getString());

        Assert.assertEquals("Gudday", defString.get());
    }


    /**
     * A simple container for a String.
     */
    public static class StringContainer
    {
        private String m_String;


        /**
         * Constructs an StringContainer.
         *
         * @param string
         */
        public StringContainer(String string)
        {
            m_String = string;
        }


        /**
         * Obtain the String
         *
         * @return
         */
        public String getString()
        {
            return m_String;
        }
    }
}
