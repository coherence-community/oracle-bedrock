/*
 * File: AbstractApplicationLauncher.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.options.DisplayName;

/**
 * An abstract implementation of an {@link ApplicationLauncher}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractApplicationLauncher<A extends Application, P extends Platform>
    implements ApplicationLauncher<A, P>
{
    /**
     * The {@link Platform} on which the {@link Application} will be launched
     */
    protected P platform;


    /**
     * Constructs an {@link AbstractApplicationLauncher}.
     *
     * @param platform the {@link Platform} on which an {@link Application} will be launched
     */
    public AbstractApplicationLauncher(P platform)
    {
        this.platform = platform;
    }


    /**
     * Obtain the {@link DisplayName} for the {@link Application}, defaulting to something sensible
     * based on the provided {@link Options} if not defined.
     *
     * @param options  the {@link Options}
     *
     * @return a {@link DisplayName}
     */
    protected DisplayName getDisplayName(Options options)
    {
        return options.get(DisplayName.class);
    }
}
