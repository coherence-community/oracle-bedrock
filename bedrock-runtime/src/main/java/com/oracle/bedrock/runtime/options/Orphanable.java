/*
 * File: Orphanable.java
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
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.Application;

/**
 * An {@link Option} to define the if {@link Application}s created
 * by Bedrock are orphanable in that they may stay running after
 * the launching process has terminated.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Orphanable implements Option
{
    /**
     * Is an {@link Application} orphanable?
     */
    private boolean orphanable;


    /**
     * Privately construct a {@link Orphanable}
     *
     * @param orphanable  is an {@link Application} orphanable?
     */
    private Orphanable(boolean orphanable)
    {
        this.orphanable = orphanable;
    }


    /**
     * Determines if an {@link Application} is {@link Orphanable}.
     *
     * @return  <code>true</code> if {@link Orphanable}
     */
    public boolean isOrphanable()
    {
        return orphanable;
    }


    @Override
    public String toString()
    {
        return "Orphanable{" + orphanable + "}";
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof Orphanable))
        {
            return false;
        }

        Orphanable that = (Orphanable) other;

        if (orphanable != that.orphanable)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return (orphanable ? 1 : 0);
    }


    /**
     * Constructs a {@link Orphanable} being enabled.
     *
     * @return  an enabled {@link Orphanable}
     */
    public static Orphanable enabled()
    {
        return new Orphanable(true);
    }


    /**
     * Constructs a {@link Orphanable} being disabled, usually the default
     *
     * @return  a disabled {@link Orphanable}
     */
    @Options.Default
    public static Orphanable disabled()
    {
        return new Orphanable(false);
    }


    /**
     * Constructs a {@link Orphanable}.
     *
     * @param  enabled  is an {@link Application} orphanable?
     *
     * @return  a {@link Orphanable}
     */
    public static Orphanable enabled(boolean enabled)
    {
        return new Orphanable(enabled);
    }
}
