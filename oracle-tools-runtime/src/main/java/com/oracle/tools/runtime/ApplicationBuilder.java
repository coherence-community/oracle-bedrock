/*
 * File: ApplicationBuilder.java
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

import com.oracle.tools.runtime.console.NullApplicationConsole;
import com.oracle.tools.runtime.console.SystemApplicationConsole;

import java.io.IOException;

/**
 * An {@link ApplicationBuilder} is responsible the creation of {@link Application}s based on {@link ApplicationSchema}s.
 *
 * @param <A>  the type of the {@link Application}s the {@link ApplicationBuilder} will realize
 * @param <S>  the type of the {@link ApplicationSchema} for the {@link Application}s
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface ApplicationBuilder<A extends Application<A>, S extends ApplicationSchema<A, S>>
{
    /**
     * Realizes an instance of an {@link Application}.
     *
     * @param schema           the {@link ApplicationSchema} to use for realizing the {@link Application}
     * @param applicationName  the name of the application
     * @param console          the {@link ApplicationConsole} that will be used for I/O by the
     *                         realized {@link Application}. This may be <code>null</code> if not required
     *
     * @return an {@link Application} representing the application realized by the {@link ApplicationBuilder}
     *
     * @throws IOException when a problem occurs while starting the application
     */
    public A realize(S schema,
                     String applicationName,
                     ApplicationConsole console) throws IOException;


    /**
     * Realizes an instance of an {@link Application} (using a {@link SystemApplicationConsole}).
     *
     * @param schema            the {@link ApplicationSchema} to use for realizing the {@link Application}
     * @param applicationName   the name of the application.
     *
     * @return an {@link Application} representing the application realized by the {@link ApplicationBuilder}
     *
     * @throws IOException when a problem occurs while starting the application
     */
    public A realize(S schema,
                     String applicationName) throws IOException;


    /**
     * Realizes an instance of an {@link Application} (without a name and using a {@link NullApplicationConsole}).
     *
     * @param schema  the {@link ApplicationSchema} to use for realizing the {@link Application}
     *
     * @return an {@link Application} representing the application realized by the {@link ApplicationBuilder}
     *
     * @throws IOException  when a problem occurs while starting the application
     */
    public A realize(S schema) throws IOException;
}
