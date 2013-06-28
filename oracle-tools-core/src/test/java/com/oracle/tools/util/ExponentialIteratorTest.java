/*
 * File: FibonacciIteratorTest.java
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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit Tests for {@link ExponentialIterator}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ExponentialIteratorTest
{
    /**
     * Ensure {@link ExponentialIterator} generated the first few
     * correct exponential numbers.
     */
    @Test
    public void shouldGenerateExponentialSequence()
    {
        ExponentialIterator iterator = new ExponentialIterator(0, 100);

        assertThat(iterator.next(), is(0L));
        assertThat(iterator.next(), is(1L));
        assertThat(iterator.next(), is(2L));
        assertThat(iterator.next(), is(4L));
        assertThat(iterator.next(), is(8L));
        assertThat(iterator.next(), is(16L));
        assertThat(iterator.next(), is(32L));
        assertThat(iterator.next(), is(64L));
        assertThat(iterator.next(), is(128L));
    }
}
