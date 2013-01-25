/*
 * File: ApplicationGroupBuilder.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

import java.io.IOException;

/**
 * An {@link ApplicationGroupBuilder} is a builder for {@link ApplicationGroup}s.
 * That is, collections of related {@link Application}s.
 *
 * @param <A>  the type of {@link Application}s that will be built by the
 *             {@link ApplicationGroupBuilder}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface ApplicationGroupBuilder<A extends Application<A>, S extends ApplicationSchema<A, S>,
                                         B extends ApplicationBuilder<A, S>, G extends ApplicationGroup<A>>
{
    /**
     * Adds a {@link ApplicationBuilder} to the {@link ApplicationGroupBuilder}
     * that will be used to realize a type of {@link Application} when the
     * {@link ApplicationGroup} is realized with {@link #realize(ApplicationConsole)}.
     *
     * @param builder             the {@link ApplicationBuilder} for the
     *                            {@link Application}s
     * @param schema              the {@link ApplicationSchema} from which to
     *                            realize/configure the {@link Application}s
     * @param sPrefix             the {@link Application} name prefix for each
     *                            of the realized {@link Application}
     * @param cRequiredInstances  the number of instances of the
     *                            {@link Application} that should be realized for
     *                            the {@link ApplicationGroup} when
     *                            {@link #realize(ApplicationConsole)} is called
     */
    public void addBuilder(B builder,
                           S schema,
                           String sPrefix,
                           int cRequiredInstances);


    /**
     * Realizes an instance of an {@link ApplicationGroup}.
     *
     * @param console           The {@link ApplicationConsole} that will be used for I/O by the
     *                          {@link Application}s realized in the {@link ApplicationGroup}.
     *                          This may be <code>null</code> if not required.
     *
     * @return An {@link ApplicationGroup} representing the collection of realized {@link Application}s.
     *
     * @throws IOException Thrown if a problem occurs while realizing the application
     */
    public G realize(ApplicationConsole console) throws IOException;


    /**
     * Realizes an instance of an {@link ApplicationGroup} (without a console).
     *
     * @return An {@link ApplicationGroup} representing the collection of realized {@link Application}s.
     *
     * @throws IOException Thrown if a problem occurs while realizing the application
     */
    public G realize() throws IOException;
}
