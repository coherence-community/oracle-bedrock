/*
 * File: JvmOption.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Option;

import com.oracle.tools.runtime.java.JavaApplicationBuilder;

/**
 * An option that is used by a Java Virtual Machine.
 * <p>
 * This is an internal interface is used by {@link JavaApplicationBuilder}
 * implementations to identify {@link Option} implementations that are specific
 * for a Java Virtual Machine.
 * <p>
 * Implementations of this interface may optionally implement the Oracle Tools
 * {@link Option} interface, but it is not a requirement.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface JvmOption
{
    /**
     * Obtains the Java Virtual Machine option as a {@link String}.
     * eg: -Xms6m
     *
     * @return the Java Virtual Machine option
     */
    public String get();
}
