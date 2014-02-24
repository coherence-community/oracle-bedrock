/*
 * File: InteractiveActionExecutorTest.java
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

package com.oracle.tools.runtime.actions;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

/**
 * Unit Tests for {@link InteractiveActionExecutor}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class InteractiveActionExecutorTest
{
    /**
     * Ensure that an {@link InteractiveActionExecutor} won't execute
     * any {@link Action}s in an empty {@link Block}.
     */
    @Test
    public void shouldExecuteNothing()
    {
        Block                     block    = new Block();
        InteractiveActionExecutor executor = new InteractiveActionExecutor(null, block);

        assertThat(executor.executeNext(), is(false));
    }


    /**
     * Ensure that an {@link InteractiveActionExecutor} will execute
     * one {@link Action} in a {@link Block}.
     */
    @Test
    public void shouldExecuteOnce()
    {
        CountingAction            action   = new CountingAction();
        Block                     block    = new Block(action);
        InteractiveActionExecutor executor = new InteractiveActionExecutor(null, block);

        assertThat(executor.executeNext(), is(false));
        assertThat(action.getExecutionCount(), is(1));

        assertThat(executor.executeNext(), is(false));
        assertThat(action.getExecutionCount(), is(1));

        executor.executeAll();
        assertThat(action.getExecutionCount(), is(1));
    }


    /**
     * Ensure that an {@link InteractiveActionExecutor} will execute
     * two {@link Action}s in a {@link Block}.
     */
    @Test
    public void shouldExecuteTwice()
    {
        CountingAction            action   = new CountingAction();
        Block                     block    = new Block(action, action);
        InteractiveActionExecutor executor = new InteractiveActionExecutor(null, block);

        assertThat(executor.executeNext(), is(true));
        assertThat(action.getExecutionCount(), is(1));

        assertThat(executor.executeNext(), is(false));
        assertThat(action.getExecutionCount(), is(2));

        executor.executeAll();
        assertThat(action.getExecutionCount(), is(2));
    }


    /**
     * Ensure that an {@link InteractiveActionExecutor} will execute
     * an {@link Action} once from a {@link Block}.
     */
    @Test
    public void shouldExecuteAll()
    {
        CountingAction            action   = new CountingAction();
        Block                     block    = new Block(action, action);
        InteractiveActionExecutor executor = new InteractiveActionExecutor(null, block);

        executor.executeAll();
        assertThat(action.getExecutionCount(), is(2));

        assertThat(executor.executeNext(), is(false));
        assertThat(action.getExecutionCount(), is(2));
    }


    /**
     * Ensure that an {@link InteractiveActionExecutor} will execute
     * an {@link Action} a specified number of times.
     */
    @Test
    public void shouldExecuteNTimes()
    {
        int                       count            = 5;
        CountingAction            action           = new CountingAction();
        RepetitiveAction          repetitiveAction = new RepetitiveAction(action, count);
        InteractiveActionExecutor executor         = new InteractiveActionExecutor(null, repetitiveAction);

        executor.executeAll();
        assertThat(action.getExecutionCount(), is(count));

        assertThat(executor.executeNext(), is(false));
        assertThat(action.getExecutionCount(), is(count));
    }
}
