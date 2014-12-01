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

import org.hamcrest.Matchers;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.deferred;
import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.deferred.DeferredHelper.valueOf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.arrayWithSize;

import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
     * A simple enum for testing.
     */
    public static enum Status {ON,
                               OFF}


    /**
     * Ensure that we can create a {@link Deferred} for an {@link AtomicLong}.
     */
    @Test
    public void shouldDeferAnAtomicLong()
    {
        AtomicLong     a        = new AtomicLong(1);

        Deferred<Long> deferred = deferred(a);

        Assert.assertEquals(new Long(1), deferred.get());
    }


    /**
     * Ensure that we can create a {@link Deferred} for an {@link AtomicInteger}.
     */
    @Test
    public void shouldDeferAnAtomicInteger()
    {
        AtomicInteger     a        = new AtomicInteger(1);

        Deferred<Integer> deferred = deferred(a);

        Assert.assertEquals(new Integer(1), deferred.get());
    }


    /**
     * Ensure that we can create a {@link Deferred} for an {@link AtomicBoolean}.
     */
    @Test
    public void shouldDeferAnAtomicBoolean()
    {
        AtomicBoolean     a        = new AtomicBoolean(true);

        Deferred<Boolean> deferred = deferred(a);

        Assert.assertEquals(Boolean.TRUE, deferred.get());
    }


    /**
     * Ensure that we can create a {@link Deferred} representation of a lambda
     * expression using a {@link Callable}.
     */
    @Test
    public void shouldDeferALambdaUsingACallable()
    {
        Deferred<String> deferred = new DeferredCallable<>(() -> "Gudday", String.class);

        assertThat(deferred.getDeferredClass(), equalTo(String.class));
        assertThat(deferred.get(), is("Gudday"));
    }


    /**
     * Ensure that we can create a {@link Deferred} of a specific type
     * when specified.
     */
    @Test
    public void shouldCreateDeferredOfSpecificType()
    {
        Integer          integer        = new Integer(42);

        Deferred<Number> deferredNumber = eventually(invoking(integer, Number.class));

        Assert.assertThat(deferredNumber.getDeferredClass(), Matchers.instanceOf(Number.class.getClass()));
        Assert.assertThat(deferredNumber.get().intValue(), Matchers.is(42));
    }


    /**
     * Ensure that we can create a {@link Deferred} representing a {@link String}.
     */
    @Test
    public void shouldDeferAString()
    {
        StringContainer  container = new StringContainer("Gudday");

        Deferred<String> deferred  = eventually(invoking(container).getString());

        Assert.assertEquals("Gudday", deferred.get());
    }


    /**
     * Ensure that we can create a {@link Deferred} representing an array of {@link String}s.
     */
    @Test
    public void shouldDeferAStringArray()
    {
        StringContainer    container = new StringContainer("Gudday");

        Deferred<String[]> deferred  = eventually(invoking(container).getStrings());

        assertThat(deferred.get(), is(arrayWithSize(1)));
    }


    /**
     * Ensure that we can create a {@link Deferred} representing a {@link Enum}.
     */
    @Test
    public void shouldDeferAnEnum()
    {
        Status           s        = Status.ON;
        Deferred<Status> deferred = eventually(invoking(this).getStatus(s));

        assertThat(deferred.get(), Matchers.is(Status.ON));
    }


    /**
     * A simple method to return a {@link Status}
     *
     * @param status  the {@link Status} to return
     *
     * @return a {@link Status}
     */
    public Status getStatus(Status status)
    {
        return status;
    }


    /**
     * A simple container for a String.
     */
    public static class StringContainer
    {
        private String value;


        /**
         * Constructs an StringContainer.
         *
         * @param value  the String value
         */
        public StringContainer(String value)
        {
            this.value = value;
        }


        /**
         * Obtain the String value.
         *
         * @return  the String value
         */
        public String getString()
        {
            return value;
        }


        /**
         * Obtains the String value as an array.
         *
         * @return  the String value as an array
         */
        public String[] getStrings()
        {
            String[] result = new String[1];

            result[0] = value;

            return result;
        }
    }
}
