/*
 * File: LaunchLogging.java
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

package com.oracle.bedrock.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;

/**
 * An {@link Option} to define enabled Bedrock launch logging.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LaunchLogging implements Option
{
    /**
     * Is launch logging output enabled?
     */
    private boolean enabled;


    /**
     * Construct a {@link LaunchLogging}
     *
     * @param enabled  is launch logging enabled
     */
    protected LaunchLogging(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if {@link LaunchLogging} is enabled.
     *
     * @return  <code>true</code> if {@link LaunchLogging} is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public String toString()
    {
        return "LaunchLogging{" + (enabled ? "enabled" : "disabled") + "}";
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof LaunchLogging))
        {
            return false;
        }

        LaunchLogging that = (LaunchLogging) other;

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
     * Constructs a {@link LaunchLogging} being enabled.
     *
     * @return  an enabled {@link LaunchLogging}
     */
    @OptionsByType.Default
    public static LaunchLogging enabled()
    {
        return new LaunchLogging(true);
    }


    /**
     * Constructs a {@link LaunchLogging} being disabled.
     *
     * @return  a disabled {@link LaunchLogging}
     */
    public static LaunchLogging disabled()
    {
        return new LaunchLogging(false);
    }


    /**
     * Constructs a {@link LaunchLogging}.
     *
     * @param  enabled  should launch logging be enabled?
     *
     * @return  a {@link LaunchLogging}
     */
    public static LaunchLogging enabled(boolean enabled)
    {
        return new LaunchLogging(enabled);
    }
}
