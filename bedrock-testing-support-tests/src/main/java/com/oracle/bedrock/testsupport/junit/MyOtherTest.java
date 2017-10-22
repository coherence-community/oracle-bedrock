/*
 * File: MyOtherTest.java
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

package com.oracle.bedrock.testsupport.junit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;

/**
 * A class of dummy unit tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class MyOtherTest
{
    @Before
    public void setup() throws Exception
    {
    }

    @Test
    public void shouldPass() throws Exception
    {
    }

    @Test
    public void shouldFail() throws Exception
    {
        fail("Failed!");
    }

    @Test
    public void shouldSkip() throws Exception
    {
        Assume.assumeThat(true, is(false));
    }

    @Test
    @Ignore(value = "This is ignored")
    public void shouldIgnore() throws Exception
    {
    }

}
