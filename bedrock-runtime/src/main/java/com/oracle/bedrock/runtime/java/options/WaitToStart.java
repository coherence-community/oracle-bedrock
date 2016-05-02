/*
 * File: WaitToStart.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

/**
 * Defines an {@link Option} defining if a {@link Platform} should wait for a
 * {@link JavaApplication} to start before returning control to a caller.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class WaitToStart implements Option
{
    /**
     * Should we wait for an application to start?
     */
    private boolean enabled;


    /**
     * Constructs a {@link WaitToStart} {@link Option}.
     *
     * @param enabled  when true, Bedrock will wait for an application to start,
     *                 otherwise it won't
     */
    private WaitToStart(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determine if Bedrock must wait for an application to start.
     *
     * @return  <code>true</code> when Bedrock must wait for an application to start,
     *          <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains a {@link WaitToStart} {@link Option} that is enabled.
     *
     * @return a {@link WaitToStart}
     */
    @Options.Default
    public static WaitToStart enabled()
    {
        return new WaitToStart(true);
    }


    /**
     * Obtains a {@link WaitToStart} {@link Option} that has a particular state.
     *
     * @param enabled  when true, Bedrock will wait for an application to start,
     *                 otherwise it won't
     *
     * @return a {@link WaitToStart}
     */
    public static WaitToStart of(boolean enabled)
    {
        return new WaitToStart(enabled);
    }


    /**
     * Obtains a {@link WaitToStart} {@link Option} that is disabled.
     *
     * @return a {@link WaitToStart}
     */
    public static WaitToStart disabled()
    {
        return new WaitToStart(false);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof WaitToStart))
        {
            return false;
        }

        WaitToStart that = (WaitToStart) o;

        return enabled == that.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
