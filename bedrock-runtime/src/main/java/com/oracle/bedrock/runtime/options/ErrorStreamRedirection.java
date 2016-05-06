/*
 * File: ErrorStreamRedirection.java
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

/**
 * An {@link Option} to define if the standard error stream should be redirected
 * to the standard output.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ErrorStreamRedirection implements Option
{
    /**
     * Is stderr redirection enabled?
     */
    private boolean enabled;


    /**
     * Privately construct a {@link ErrorStreamRedirection}
     *
     * @param enabled  is stderr redirection enabled
     */
    private ErrorStreamRedirection(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if {@link ErrorStreamRedirection} is enabled.
     *
     * @return  <code>true</code> if {@link ErrorStreamRedirection} is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public String toString()
    {
        return "ErrorStreamRedirection{" + (enabled ? "enabled" : "disabled") + "}";
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof ErrorStreamRedirection))
        {
            return false;
        }

        ErrorStreamRedirection that = (ErrorStreamRedirection) other;

        if (enabled != that.enabled)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }


    /**
     * Constructs a {@link ErrorStreamRedirection} being enabled.
     *
     * @return  an enabled {@link ErrorStreamRedirection}
     */
    public static ErrorStreamRedirection enabled()
    {
        return new ErrorStreamRedirection(true);
    }


    /**
     * Constructs a {@link ErrorStreamRedirection} being disabled.
     *
     * @return  a disabled {@link ErrorStreamRedirection}
     */
    @Options.Default
    public static ErrorStreamRedirection disabled()
    {
        return new ErrorStreamRedirection(false);
    }


    /**
     * Constructs a {@link ErrorStreamRedirection}.
     *
     * @param  enabled  should stderr redirection be enabled?
     *
     * @return  a {@link ErrorStreamRedirection}
     */
    public static ErrorStreamRedirection enabled(boolean enabled)
    {
        return new ErrorStreamRedirection(enabled);
    }
}
