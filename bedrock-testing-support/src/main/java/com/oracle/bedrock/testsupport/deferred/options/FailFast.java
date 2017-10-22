/*
 * File: FailFast.java
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

package com.oracle.bedrock.testsupport.deferred.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.deferred.Concurrent;
import com.oracle.bedrock.testsupport.deferred.Concurrently;

/**
 * An {@link Option} indicating if a {@link Concurrently} created
 * assertion will fail-fast by interrupting the {@link Thread}
 * that requested the concurrent assertion.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class FailFast implements Option
{
    /**
     * Should a {@link Concurrent.Assertion} fail fast?
     */
    private final boolean enabled;


    /**
     * Constructs a {@link FailFast}.
     *
     * @param enabled  should a {@link Concurrent.Assertion} fail-fast?
     */
    private FailFast(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if failing fast is enabled
     *
     * @return <code>true</code> if fail-fast is enabled,
     *         <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains an enabled {@link FailFast}.
     *
     * @return a {@link FailFast}
     */
    public static FailFast enabled()
    {
        return new FailFast(true);
    }


    /**
     * Obtains a disabled {@link FailFast}.
     *
     * @return a {@link FailFast}
     */
    @OptionsByType.Default
    public static FailFast disabled()
    {
        return new FailFast(false);
    }


    @Override
    public String toString()
    {
        return "FailFast{" + enabled + '}';
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof FailFast))
        {
            return false;
        }

        FailFast failFast1 = (FailFast) o;

        return enabled == failFast1.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
