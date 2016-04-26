/*
 * File: MetaClass.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;

/**
 * Defines meta-information about the class of an {@link Application}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface MetaClass<A extends Application> extends Option
{
    /**
     * Obtains the concrete implementation {@link Class} for presenting {@link Application}s
     * launched on the specified {@link Platform} with the provided {@link Option}s.
     * <p>
     * This is the type of {@link Class} that will be returned by {@link Platform#launch(Class, Option...)}
     * for the {@link Application}.
     *
     * @param platform  the {@link Platform} on which the {@link Application} will be launched
     * @param options   the {@link Options} provided for launching the {@link Application}
     *
     * @return the {@link Class} of {@link Application}
     */
    Class<? extends A> getImplementationClass(Platform platform,
                                              Options  options);


    /**
     * Invoked by a {@link Platform} prior to an {@link Application} being launched, allowing
     * an implementation to override and customize the provided {@link Options}, arguments and properties.
     *
     * @param platform   the {@link Platform}
     * @param options    the {@link Options}
     */
    void onLaunching(Platform platform,
                     Options  options);


    /**
     * Invoked by a {@link Platform} prior to an {@link Application} being launched after
     * all other manipulation of the {@link Options} has taken place.
     * <p>
     * This is a final chance for the {@link MetaClass} to do an last-minute manipulation
     * of the {@link Options}.
     *
     * @param platform   the {@link Platform}
     * @param options    the final set of {@link Options} that will be used to launch
     *                   the {@link Application}.
     */
    void onFinalize(Platform platform, Options options);


    /**
     * Invoked by a {@link Platform} after an {@link Application} has been launched using the
     * specified {@link Options}, but before the {@link Application} is returned to the
     * {@link Thread} that requested the {@link Application}.
     *
     * @param platform      the {@link Platform}
     * @param application   the {@link Application}
     * @param options       the {@link Options}
     */
    void onLaunched(Platform    platform,
                    Application application,
                    Options     options);
}
