/*
 * File: ApplicationConsole.java
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

package com.oracle.tools.runtime;

import java.util.Formatter;

/**
 * An {@link ApplicationConsole} provides standard out and err output facilities
 * to an executing application.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface ApplicationConsole
{
    /**
     * A convenience method to write a formatted string to a console using
     * the specified format string and arguments.
     *
     * @param format  a format string as described in {@link Formatter} string
     *                syntax
     * @param args    arguments referenced by the format specifiers in the
     *                format string. If there are more arguments than format
     *                specifiers, the extra arguments are ignored.
     *                The number of arguments is variable and may be zero.
     */
    public void printf(String    format,
                       Object... args);
}
