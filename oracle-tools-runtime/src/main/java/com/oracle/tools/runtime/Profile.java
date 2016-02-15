/*
 * File: Profile.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

/**
 * Often implemented by {@link Option} classes, {@link Profile}s provide a mechanism to
 * intercept and dynamically modify the behavior of {@link Platform}s and {@link Application}s at runtime.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Profile
{
    /**
     * Invoked by a {@link Platform} prior to an {@link ApplicationSchema} being realized, allowing
     * an implementation to override and customize the provided {@link Options}, arguments and properties.
     *
     * @param platform   the {@link Platform}
     * @param schema     the {@link ApplicationSchema}
     * @param options    the {@link Options}
     */
    void onBeforeRealize(Platform          platform,
                         ApplicationSchema schema,
                         Options           options);


    /**
     * Invoked by a {@link Platform} after an {@link Application} has been realized using the
     * specified {@link Options}, but before the {@link Application} is returned to the
     * {@link Thread} that requested the {@link Application}.
     *
     * @param platform      the {@link Platform}
     * @param application   the {@link Application}
     * @param options       the {@link Options}
     */
    void onAfterRealize(Platform    platform,
                        Application application,
                        Options     options);


    /**
     * Invoked by an {@link Application} prior to it being closed for the specified
     * {@link Platform}, when it was launched with the provided {@link Options}.
     *
     * @param platform      the {@link Platform}
     * @param application  the {@link Application}
     * @param options      the {@link Options}
     */
    void onBeforeClose(Platform    platform,
                       Application application,
                       Options     options);
}
