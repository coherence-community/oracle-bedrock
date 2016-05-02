/*
 * File: PlatformPredicate.java
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
import com.oracle.bedrock.runtime.Infrastructure;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;

import java.util.function.Predicate;

/**
 * An {@link Option} to specify a {@link Predicate} for matching a {@link Platform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Infrastructure
 */
public interface PlatformPredicate extends Predicate<Platform>, Option
{
    /**
     * Obtains a {@link PlatformPredicate} that matches an instance of the {@link LocalPlatform}.
     *
     * @return  a {@link PlatformPredicate}
     */
    @Options.Default
    static PlatformPredicate isLocal()
    {
        return platform -> platform instanceof LocalPlatform;
    }


    /**
     * Obtains a {@link PlatformPredicate} that matches any {@link Platform}.
     *
     * @return a {@link PlatformPredicate}
     */
    static PlatformPredicate any()
    {
        return platform -> true;
    }


    /**
     * Obtains a {@link PlatformPredicate} that never matches any {@link Platform}.
     *
     * @return a {@link PlatformPredicate}
     */
    static PlatformPredicate none()
    {
        return platform -> false;
    }


    /**
     * Obtains a {@link PlatformPredicate} for a {@link Platform} that satisfies a {@link Predicate}.
     *
     * @param predicate  the {@link Predicate} for a {@link Platform}
     *
     * @return a {@link PlatformPredicate}
     */
    static PlatformPredicate of(Predicate<? super Platform> predicate)
    {
        return platform -> predicate.test(platform);
    }


    /**
     * Obtains a {@link PlatformPredicate} to match the name of a {@link Platform}.
     *
     * @param regularExpression  the regular expression for matching the {@link Platform#getName()}
     *
     * @return a {@link PlatformPredicate}
     */
    static PlatformPredicate named(String regularExpression)
    {
        return platform -> platform.getName().matches(regularExpression);
    }


    /**
     * Obtains a {@link PlatformPredicate} that matches a specific {@link Platform}.
     *
     * @param platform  the {@link Platform}
     *
     * @return a {@link PlatformPredicate}
     */
    static PlatformPredicate is(Platform platform)
    {
        return p -> p.equals(platform);
    }
}
