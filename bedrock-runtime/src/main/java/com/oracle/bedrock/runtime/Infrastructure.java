/*
 * File: Infrastructure.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.options.PlatformPredicate;

/**
 * {@link Infrastructure} represents and provides a mechanism to acquire {@link Platform}s
 * satisfying zero or more {@link Option}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public interface Infrastructure
{
    /**
     * Acquire the {@link Platform} that satisfies the specified {@link Option}s.
     * <p>
     * Typically {@link Infrastructure} implementations will look for the {@link PlatformPredicate}
     * {@link Option} in order to determine, match and provide a suitable {@link Platform},
     * however this behavior ultimately dependent on the type of {@link Infrastructure}.
     *
     * @param options  the {@link Option}s
     *
     * @return  the {@link Platform} or <code>null</code> if the {@link Infrastructure}
     *          can't provide a {@link Platform} satisfying the {@link Option}s
     *
     * @see PlatformPredicate
     */
    Platform getPlatform(Option... options);


    /**
     * Obtains {@link Infrastructure} represents the {@link LocalPlatform}.
     * <p>
     * All calls to {@link #getPlatform(Option...)} will attempt to match against
     * the {@link LocalPlatform}.
     *
     * @return  {@link Infrastructure} representing the {@link LocalPlatform}
     */
    static Infrastructure local()
    {
        return (options -> {

            // obtain the PlatformPredicate (just in case one has been provided)
                    Options platformOptions = Options.from(options);

                    // assume using the LocalPlatform if there's no predicate
                    PlatformPredicate predicate = platformOptions.getOrDefault(PlatformPredicate.class,
                                                                               PlatformPredicate.isLocal());

                    // only return the LocalPlatform if the predicate matches
                    return predicate.test(LocalPlatform.get()) ? LocalPlatform.get() : null;
                });
    }
}
