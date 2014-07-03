/*
 * File: FluentApplicationSchema.java
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

import java.util.List;

/**
 * An {@link ApplicationSchema} extension defining {@link ApplicationSchema} specific
 * fluent-methods.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application} that can be configured by the {@link FluentApplicationSchema}
 * @param <S>  the type of {@link FluentApplicationSchema} that will be returned from fluent methods
 */
public interface FluentApplicationSchema<A extends Application, S extends FluentApplicationSchema<A, S>>
    extends ApplicationSchema<A>
{
    /**
     * Obtains the {@link LifecycleEventInterceptor}s that the {@link FluentApplicationSchema} will
     * attach to {@link Application}s produced by the said {@link FluentApplicationSchema}.
     * <p>
     * {@link LifecycleEventInterceptor}s are typically used by {@link Application}s
     * and {@link ApplicationBuilder}s to raise {@link LifecycleEvent}s pertaining
     * to the life-cycle of an {@link Application}.
     *
     * @return  the {@link LifecycleEventInterceptor}s
     */
    public Iterable<LifecycleEventInterceptor<? super A>> getLifecycleInterceptors();


    /**
     * Adds an {@link LifecycleEventInterceptor} to the {@link FluentApplicationSchema}
     * those of which will be executed when certain {@link LifecycleEvent}s
     * occur on {@link Application}s created with the {@link ApplicationSchema}.
     *
     * @param interceptor  the {@link LifecycleEventInterceptor}
     *
     * @return  the {@link FluentApplicationSchema}
     */
    public S addLifecycleInterceptor(LifecycleEventInterceptor<? super A> interceptor);


    /**
     * Adds an additional argument to use when starting the {@link Application}.
     *
     * @param argument  the additional argument for the {@link Application}
     */
    public S addArgument(String argument);


    /**
     * Adds multiple arguments to use when starting the {@link Application}.
     *
     * @param arguments  the additional arguments for the {@link Application}
     */
    public S addArguments(String... arguments);


    /**
     * Adds multiple arguments to use when starting the {@link Application}.
     *
     * @param arguments  the additional arguments for the {@link Application}
     */
    public S addArguments(List<String> arguments);


    /**
     * Sets (and replaces) the arguments to use when starting the {@link Application}.
     *
     * @param arguments  the arguments for the {@link Application}
     */
    public S setArguments(String... arguments);


    /**
     * Sets (and replaces) the arguments to use when starting the {@link Application}.
     *
     * @param arguments  the arguments for the {@link Application}
     */
    public S setArguments(List<String> arguments);
}
