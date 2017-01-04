/*
 * File: Caching.java
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

package com.oracle.bedrock.runtime.concurrent.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Experimental;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;

/**
 * An {@link Option} to define how the results of {@link RemoteChannel} invocations
 * of {@link RemoteCallable}s are cached.
 * <p>
 * Applications may use this {@link Option} to specify if a specific {@link RemoteCallable}
 * result should be cached when calling {@link RemoteChannel#submit(RemoteCallable, Option...)}.
 * For example:
 * <code>
 *     // submit a callable for the first time and cache the results (using the default timeout)
 *     result = channel.submit(someCallable, Caching.enabled();
 *
 *     // resubmit the callable (this will use the cached result)
 *     result = channel.submit(someCallable, Caching.enabled();
 *
 *     // resubmit the callable (don't cache the result)
 *     result = channel.submit(someCallable, Caching.disabled();
 *
 *     // resubmit the callable (cache with a specific timeout)
 *     result = channel.submit(someCallable, Caching.enabled(Timeout.of);
 * </code>
 *
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Experimental
public class Caching implements Option
{
    /**
     * Is caching enabled?
     */
    private boolean enabled;

    /**
     * The {@link Caching} {@link Option}s.
     */
    private OptionsByType optionsByType;


    /**
     * Constructs a {@link Caching}.
     *
     * @param enabled  is caching enabled?
     * @param options  the {@link Option}s for {@link Caching}
     */
    private Caching(boolean   enabled,
                    Option... options)
    {
        this.enabled       = enabled;
        this.optionsByType = options == null || options.length == 0 ? OptionsByType.empty() : OptionsByType.of(options);
    }


    /**
     * Determines if {@link Caching} is enabled.
     *
     * @return <code>true</code> if {@link Caching} is enabled,
     *         <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains the {@link OptionsByType} specified for {@link Caching}.
     *
     * @return the {@link OptionsByType}
     */
    public OptionsByType getOptionsByType()
    {
        return optionsByType;
    }


    /**
     * Signifies that {@link Caching} is enabled.
     *
     * @param options  the {@link Option}s for {@link Caching}
     *
     * @return a {@link Caching}
     */
    public static Caching enabled(Option... options)
    {
        return new Caching(true, options);
    }


    /**
     * Signifies that {@link Caching} should be disabled (the default).
     *
     * @return a default {@link Caching}
     */
    @OptionsByType.Default
    public static Caching disabled()
    {
        return new Caching(false);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Caching))
        {
            return false;
        }

        Caching caching = (Caching) o;

        if (enabled != caching.enabled)
        {
            return false;
        }

        return optionsByType != null ? optionsByType.equals(caching.optionsByType) : caching.optionsByType == null;
    }


    @Override
    public int hashCode()
    {
        int result = (enabled ? 1 : 0);

        result = 31 * result + (optionsByType != null ? optionsByType.hashCode() : 0);

        return result;
    }
}
