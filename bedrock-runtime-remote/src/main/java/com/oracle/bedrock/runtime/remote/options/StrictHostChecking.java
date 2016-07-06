/*
 * File: StrictHostChecking.java
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

package com.oracle.bedrock.runtime.remote.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.remote.RemotePlatform;

/**
 * An {@link Option} to define the is strict host checking is required for
 * {@link RemotePlatform} SSH connections.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class StrictHostChecking implements Option
{
    /**
     * Is strict-host-checking enabled?
     */
    private boolean enabled;


    /**
     * Privately construct a {@link StrictHostChecking}
     *
     * @param enabled  is strict-host-checking enabled
     */
    private StrictHostChecking(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if {@link StrictHostChecking} is enabled.
     *
     * @return  <code>true</code> if {@link StrictHostChecking} is enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public String toString()
    {
        return "StrictHostChecking{" + (enabled ? "enabled" : "disabled") + "}";
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof StrictHostChecking))
        {
            return false;
        }

        StrictHostChecking that = (StrictHostChecking) other;

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
     * Constructs a {@link StrictHostChecking} being enabled.
     *
     * @return  an enabled {@link StrictHostChecking}
     */
    @OptionsByType.Default
    public static StrictHostChecking enabled()
    {
        return new StrictHostChecking(true);
    }


    /**
     * Constructs a {@link StrictHostChecking} being disabled.
     *
     * @return  a disabled {@link StrictHostChecking}
     */
    public static StrictHostChecking disabled()
    {
        return new StrictHostChecking(false);
    }


    /**
     * Constructs a {@link StrictHostChecking}.
     *
     * @param  enabled  should strict-host-checking be enabled?
     *
     * @return  a {@link StrictHostChecking}
     */
    public static StrictHostChecking enabled(boolean enabled)
    {
        return new StrictHostChecking(enabled);
    }
}
