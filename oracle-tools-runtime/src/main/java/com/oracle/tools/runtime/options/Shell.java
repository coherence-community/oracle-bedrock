/*
 * File: Shell.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;

import com.oracle.tools.runtime.Platform;

/**
 * An {@link Option} to configure the type of {@link Shell} a {@link Platform} uses
 * or should use.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Shell implements Option
{
    /**
     * The type of {@link Shell}.
     */
    public static enum Type
    {
        /**
         * The {@link Shell} is based on Microsoft Windows.
         */
        WINDOWS,

        /**
         * The {@link Shell} is based on BASH.
         */
        BASH,

        /**
         * The {@link Shell} is based on SH.
         */
        SH,

        /**
         * The {@link Shell} is based on CSH.
         */
        CSH,

        /**
         * The {@link Shell} is based on TSCH.
         */
        TSCH,
    }


    /**
     * The type of the {@link Shell}, <code>null</code> if unknown.
     */
    private Type type;


    /**
     * Constructs a {@link Shell} {@link Option} with the specified {@link Type}.
     *
     * @param type  the {@link Type} of the {@link Shell}, or <code>null</code> if unknown.
     */
    private Shell(Type type)
    {
        this.type = type;
    }


    /**
     * Obtains the {@link Type} of the {@link Shell}.
     *
     * @return  the {@link Type}
     */
    public Type getType()
    {
        return type;
    }


    /**
     * Creates a {@link Shell} of the specified {@link Type}.
     *
     * @param type  the {@link Type} of the {@link Shell}
     *
     * @return a {@link Shell} of the specified {@link Type}
     */
    public static Shell is(Type type)
    {
        return new Shell(type);
    }


    /**
     * Creates a {@link Shell} of unknown {@link Type}.
     *
     * @return a {@link Shell}
     */
    public static Shell isUnknown()
    {
        return new Shell(null);
    }
}
