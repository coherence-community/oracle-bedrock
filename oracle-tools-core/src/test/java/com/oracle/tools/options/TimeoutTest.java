/*
 * File: TimeoutTest.java
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

package com.oracle.tools.options;

import com.oracle.tools.util.Duration;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

/**
 * Unit Tests for {@link Timeout}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class TimeoutTest
{
    /**
     * Ensure that we can convert the default {@link Timeout} can be converted into milliseconds.
     */
    @Test
    public void shouldConvertTimeouts()
    {
        Timeout  timeout  = Timeout.autoDetect();
        Duration duration = timeout.getDuration();

        assertThat(duration.to(TimeUnit.MILLISECONDS), is(60000L));
    }


    /**
     * Ensure that we can create {@link Timeout}s from {@link String}s.
     */
    @Test
    public void shouldCreateTimeoutsFromStrings()
    {
        Timeout timeout;

        timeout = Timeout.after("42");
        assertThat(timeout.to(TimeUnit.MILLISECONDS), is(42L));

        timeout = Timeout.after("42ms");
        assertThat(timeout.to(TimeUnit.MILLISECONDS), is(42L));

        timeout = Timeout.after("42s");
        assertThat(timeout.to(TimeUnit.SECONDS), is(42L));

        timeout = Timeout.after("42m");
        assertThat(timeout.to(TimeUnit.MINUTES), is(42L));

        timeout = Timeout.after("42h");
        assertThat(timeout.to(TimeUnit.HOURS), is(42L));
    }
}
