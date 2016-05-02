/*
 * File: StaticInfrastructure.java
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

import java.util.ArrayList;
import java.util.Optional;

/**
 * An {@link Infrastructure} representing a static collection of {@link Platform}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class StaticInfrastructure implements Infrastructure
{
    /**
     * The {@link Platform}s.
     */
    private ArrayList<Platform> platforms;


    /**
     * Constructs a {@link StaticInfrastructure} for the specified {@link Platform}s.
     *
     * @param platforms  the {@link Platform}s
     */
    StaticInfrastructure(Platform... platforms)
    {
        this.platforms = new ArrayList<>();

        if (platforms != null)
        {
            for (Platform platform : platforms)
            {
                this.platforms.add(platform);
            }
        }
    }


    /**
     * Obtains {@link StaticInfrastructure} representing the specified {@link Platform}s.
     *
     * @param platforms  the {@link Platform}s
     */
    public static Infrastructure of(Platform... platforms)
    {
        return new StaticInfrastructure(platforms);
    }


    @Override
    public Platform getPlatform(Option... options)
    {
        Options            platformOptions = Options.from(options);

        PlatformPredicate  predicate = platformOptions.getOrDefault(PlatformPredicate.class, PlatformPredicate.any());

        Optional<Platform> platform        = platforms.stream().filter(predicate).findFirst();

        return platform.isPresent() ? platform.get() : null;
    }
}
