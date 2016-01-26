/*
 * File: ExpressionEvaluatorTests.java
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

package com.oracle.tools.lang;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;

/**
 * Tests for {@link ExpressionEvaluator}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ExpressionEvaluatorTests
{
    /**
     * Ensure that we can create an implementation of {@link ExpressionEvaluator}.
     */
    @Test
    public void shouldInstantiateExpressionEvaluator()
    {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        assertThat(evaluator, is(not(nullValue())));
    }


    /**
     * Ensure that we can evaluate an expression using {@link ExpressionEvaluator}.
     */
    @Test
    public void shouldEvaluateExpression()
    {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        evaluator.defineVariable("one", 1L);

        System.out.println(evaluator.evaluate("${one}-${one}-${one + one}-fred.exec", String.class));
    }


    /**
     * Ensure that we define and resolve a variable containing periods.
     */
    @Test
    public void shouldDefineAndEvaluateAVariableContainingPeriods()
    {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();

        evaluator.defineVariable("one.two.three", 1L);
        evaluator.defineVariable("one.two.four", 2L);

        assertThat(evaluator.evaluate("${one.two.three}", Long.class), is(1L));
        assertThat(evaluator.evaluate("${one.two.four}", Long.class), is(2L));
        assertThat(evaluator.evaluate("${one.two.three + one.two.four}", Long.class), is(3L));
    }
}
