/*
 * File: ExecutableTest.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for the {@link Executable} {@link Option}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 */
public class ExecutableTest
{
    /**
     * Ensure that we can set the name of the {@link Executable}.
     */
    @Test
    public void shouldSetExecutable()
    {
        Executable executable = Executable.named("ls");

        assertThat(executable.getName(), is("ls"));
    }


    /**
     * Ensure that a null {@link Executable} name is not permitted.
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNullExecutableName()
    {
        Executable executable = Executable.named(null);
    }
}
