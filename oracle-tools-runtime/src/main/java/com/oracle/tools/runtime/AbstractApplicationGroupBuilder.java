/*
 * File: AbstractApplicationGroupBuilder.java
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

import com.oracle.tools.runtime.console.NullApplicationConsole;
import com.oracle.tools.util.Quadruple;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * An {@link AbstractApplicationGroupBuilder} is a base implementation of an {@link ApplicationGroupBuilder}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Deprecated
public abstract class AbstractApplicationGroupBuilder<A extends Application, S extends ApplicationSchema<A>,
                                                      B extends ApplicationBuilder<A>,
                                                      G extends ApplicationGroup<A>>
    extends AbstractAssemblyBuilder<A, G> implements ApplicationGroupBuilder<A, S, B, G>
{
    /**
     * The map of {@link ApplicationBuilder}s to create applications, keyed by application prefix name.
     */
    protected LinkedHashMap<String, Quadruple<ApplicationBuilder<A>, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder>> m_builders;

    /**
     * Construct an {@link AbstractApplicationGroupBuilder}.
     */
    public AbstractApplicationGroupBuilder()
    {
        super();

        m_builders = new LinkedHashMap<String,
                                       Quadruple<ApplicationBuilder<A>, ApplicationSchema<A>, Integer,
                                                 ApplicationConsoleBuilder>>();
    }

    public <T extends A, S extends ApplicationSchema<T>,
            B extends ApplicationBuilder<T>> void addBuilder(B      applicationBuilder,
                                                             S      applicationSchema,
                                                             String applicationNamePrefix,
                                                             int    count)
    {
        addBuilder(applicationBuilder, applicationSchema, applicationNamePrefix, count, null);
    }


    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>,
            B extends ApplicationBuilder<T>> void addBuilder(B                         applicationBuilder,
                                                             S                         applicationSchema,
                                                             String                    applicationNamePrefix,
                                                             int                       count,
                                                             ApplicationConsoleBuilder consoleBuilder)
    {
        m_builders.put(applicationNamePrefix,
                       new Quadruple(applicationBuilder, applicationSchema, count, consoleBuilder));
    }

    @Override
    public G realize(ApplicationConsoleBuilder overridingConsoleBuilder)
    {
        // build a list of applications
        LinkedList<A> applications = new LinkedList<A>();

        realizeApplicationFromSchema(applications, overridingConsoleBuilder);

        for (String prefix : m_builders.keySet())
        {
            Quadruple<ApplicationBuilder<A>, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder> quadruple =
                m_builders.get(prefix);
            ApplicationBuilder<A>     builder            = quadruple.getA();
            ApplicationSchema<A>      schema             = quadruple.getB();
            int                       cRequiredInstances = quadruple.getC();
            ApplicationConsoleBuilder consoleBuilder     = quadruple.getD();

            // override the application defined console builder when this method is provided with one
            if (overridingConsoleBuilder != null)
            {
                consoleBuilder = overridingConsoleBuilder;
            }

            for (int i = 1; i <= cRequiredInstances; i++)
            {
                String             applicationName = String.format("%s-%d", prefix, i);

                ApplicationConsole console = consoleBuilder == null ? null : consoleBuilder.realize(applicationName);

                if (console == null)
                {
                    console = new NullApplicationConsole();
                }

                applications.add(builder.realize(schema, applicationName, console));
            }
        }

        return createApplicationGroup(applications);
    }
}
