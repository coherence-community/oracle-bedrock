/*
 * File: AssemblyBuilder.java
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

import java.io.IOException;

/**
 * A builder of {@link Assembly}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application}s that will be in a realized {@link Assembly}
 * @param <G>  the type of {@link Assembly}s that will be realized by the {@link AssemblyBuilder}
 */
public interface AssemblyBuilder<A extends Application, G extends Assembly<A>>
{
    /**
     * Adds an {@link ApplicationBuilder} to the {@link AssemblyBuilder}
     * that will be used to realize a type of {@link Application} when the
     * {@link Assembly} is realized.
     * <p>
     * Multiple calls to this method is permitted, this allowing an {@link Assembly}
     * to contain different types of {@link Application}s.
     * <p>
     * By default no {@link ApplicationConsole} will be used for the realized
     * {@link Application}s, unless one is specified when realizing the {@link Assembly}.
     *
     * @param applicationBuilder     the {@link ApplicationBuilder} for the {@link Application}s
     * @param applicationSchema      the {@link ApplicationSchema} from which to
     *                               realize/configure the {@link Application}s
     * @param applicationNamePrefix  the {@link Application} name prefix for each
     *                               of the realized {@link Application}
     * @param count                  the number of instances of the {@link Application} that should be realized for
     *                               the {@link Assembly} when {@link #realize(ApplicationConsole)} is called
     */
    public <T extends A, S extends ApplicationSchema<T>,
            B extends ApplicationBuilder<T>> void addBuilder(B      applicationBuilder,
                                                             S      applicationSchema,
                                                             String applicationNamePrefix,
                                                             int    count);


    /**
     * Adds an {@link ApplicationBuilder} to the {@link AssemblyBuilder}
     * that will be used to realize a type of {@link Application} when the
     * {@link Assembly} is realized.
     * <p>
     * Multiple calls to this method is permitted, this allowing an {@link Assembly}
     * to contain different types of {@link Application}s.
     * <p>
     * By default a new {@link ApplicationConsole} provided by the {@link ApplicationConsoleBuilder}
     * will used for each {@link Application} realized when creating the {@link Assembly}.
     *
     * @param applicationBuilder     the {@link ApplicationBuilder} for the {@link Application}s
     * @param applicationSchema      the {@link ApplicationSchema} from which to
     *                               realize/configure the {@link Application}s
     * @param applicationNamePrefix  the {@link Application} name prefix for each
     *                               of the realized {@link Application}
     * @param count                  the number of instances of the {@link Application} that should be realized for
     *                               the {@link Assembly} when {@link #realize(ApplicationConsole)} is called
     * @param consoleBuilder         the {@link ApplicationConsoleBuilder} to be used to provide
     *                               {@link ApplicationConsole}s for realized {@link Application}s.
     */
    public <T extends A, S extends ApplicationSchema<T>,
            B extends ApplicationBuilder<T>> void addBuilder(B                         applicationBuilder,
                                                             S                         applicationSchema,
                                                             String                    applicationNamePrefix,
                                                             int                       count,
                                                             ApplicationConsoleBuilder consoleBuilder);


    /**
     * Realizes an instance of an {@link Assembly}.
     *
     * @param overridingConsole  the {@link ApplicationConsole} that will be used for I/O by all of the
     *                           {@link Application}s realized in the {@link Assembly}, including
     *                           those that had a specific {@link ApplicationConsoleBuilder} specified for
     *                           them using {@link #addBuilder(ApplicationBuilder, ApplicationSchema, String, int, ApplicationConsoleBuilder)}
     *                           When this is <code>null</code> the defined {@link ApplicationConsole}
     *                           will be used for each {@link Application} in the {@link Assembly}
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws IOException Thrown if a problem occurs while realizing the application
     */
    public G realize(ApplicationConsole overridingConsole) throws IOException;


    /**
     * Realizes an instance of an {@link Assembly}.
     *
     * @param overridingConsoleBuilder  the {@link ApplicationConsoleBuilder} that will be used to create
     *                                  {@link ApplicationConsole}s for each of the realized {@link Application}s
     *                                  in the {@link Assembly}, overriding those that had a specific
     *                                  {@link ApplicationConsoleBuilder} specified for them using
     *                                  {@link #addBuilder(ApplicationBuilder, ApplicationSchema, String, int, ApplicationConsoleBuilder)}
     *                                  When this is <code>null</code> the defined {@link ApplicationConsole}
     *                                  will be used for each {@link Application} in the {@link Assembly}
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws IOException Thrown if a problem occurs while realizing the application
     */
    public G realize(ApplicationConsoleBuilder overridingConsoleBuilder) throws IOException;


    /**
     * Realizes an instance of an {@link Assembly} consisting of the {@link Application}s
     * defined by the {@link AssemblyBuilder}, using the {@link ApplicationConsole}s
     * that were defined for them.
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws IOException Thrown if a problem occurs while realizing the application
     */
    public G realize() throws IOException;
}
