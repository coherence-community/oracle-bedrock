/*
 * File: ApplicationSchema.java
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

import java.io.File;

import java.util.List;

/**
 * An {@link ApplicationSchema} encapsulates {@link Platform} <strong>agnostic</strong>
 * configuration and operational settings required to realize an {@link Application}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application} that can be configured by the {@link ApplicationSchema}
 */
public interface ApplicationSchema<A extends Application>
{
    /**
     * Obtain the {@link Class} specifying the type of {@link Application}
     * that this {@link ApplicationSchema} defines.
     *
     * @return the {@link Class} specifying the type of {@link Application}
     *         that this {@link ApplicationSchema} defines
     */
    public Class<A> getApplicationClass();


    /**
     * Obtains the name of the application executable.
     *
     * @return the name of the executable to run
     */
    public String getExecutableName();


    /**
     * Obtains the {@link ApplicationListener}s that will be attached to
     * {@link Application}s produced by this {@link ApplicationSchema}.
     * <p>
     *
     * @return  the {@link ApplicationListener}s
     */
    public Iterable<ApplicationListener<? super A>> getApplicationListeners();


    /**
     * Obtains the default {@link Option}s defined on the {@link ApplicationSchema}.
     *
     * @return  the default {@link Option}s
     */
    public Options getOptions();


    /**
     * Obtains the {@link Options} to be used when realizing an {@link Application} on
     * the specified {@link Platform}.
     * <p>
     * Should there be no special {@link Options} for the specified {@link Platform},
     * this method will return the {@link Options} defined by {@link #getOptions()}.
     *
     * @param platform  the {@link Platform}
     *
     * @return  {@link Options} for the specified {@link Platform}
     */
    public Options getPlatformSpecificOptions(Platform platform);
}
