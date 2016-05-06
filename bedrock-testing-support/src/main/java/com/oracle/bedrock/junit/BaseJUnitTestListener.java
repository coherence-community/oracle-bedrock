/*
 * File: BaseJUnitTestListener.java
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

package com.oracle.bedrock.junit;

/**
 * A base implementation of a {@link JUnitTestListener} that has
 * empty implementations of all methods. This class can then be
 * sub-classes by implementations that only want to implement a
 * few methods.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class BaseJUnitTestListener implements JUnitTestListener
{
    @Override
    public void junitStarted(Event event)
    {
    }


    @Override
    public void junitCompleted(Event event)
    {
    }


    @Override
    public void testRunStarted(Event event)
    {
    }


    @Override
    public void testRunFinished(Event event)
    {
    }


    @Override
    public void testClassStarted(Event event)
    {
    }


    @Override
    public void testClassFinished(Event event)
    {
    }


    @Override
    public void testStarted(Event event)
    {
    }


    @Override
    public void testSucceeded(Event event)
    {
    }


    @Override
    public void testIgnored(Event event)
    {
    }


    @Override
    public void testFailed(Event event)
    {
    }


    @Override
    public void testError(Event event)
    {
    }


    @Override
    public void testAssumptionFailure(Event event)
    {
    }
}
