/*
 * File: AbstractAssemblyBuilder.java
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
import com.oracle.tools.runtime.console.SingletonApplicationConsoleBuilder;
import com.oracle.tools.util.Quadruple;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A base implementation of an {@link AssemblyBuilder}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application}s that will be in a realized {@link Assembly}
 * @param <G>  the type of {@link Assembly}s that will be realized by the {@link AssemblyBuilder}
 */
public abstract class AbstractAssemblyBuilder<A extends Application, G extends Assembly<A>>
    implements AssemblyBuilder<A, G>
{
    /**
     * The map of {@link ApplicationBuilder}s to create applications, keyed by application prefix name.
     */
    protected LinkedHashMap<String, Quadruple<Platform, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder>> m_schema;

    /**
     * Construct an {@link AbstractAssemblyBuilder}.
     */
    public AbstractAssemblyBuilder()
    {
        m_schema   = new LinkedHashMap<String,
                                       Quadruple<Platform, ApplicationSchema<A>, Integer,
                                                 ApplicationConsoleBuilder>>();
    }


    @Override
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(Platform platform,
                                                                             S        applicationSchema,
                                                                             String   applicationNamePrefix,
                                                                             int      count)
    {
        addApplication(platform, applicationSchema, applicationNamePrefix, count, null);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(Platform                  platform,
                                                                             S                         applicationSchema,
                                                                             String                    applicationNamePrefix,
                                                                             int                       count,
                                                                             ApplicationConsoleBuilder consoleBuilder)
    {
        m_schema.put(applicationNamePrefix,
                     new Quadruple(platform, applicationSchema, count, consoleBuilder));
    }


    @Override
    public G realize(ApplicationConsoleBuilder overridingConsoleBuilder)
    {
        // build a list of applications
        LinkedList<A> applications = new LinkedList<A>();

        realizeApplicationFromSchema(applications, overridingConsoleBuilder);

        return createApplicationGroup(applications);
    }


    public G realize(ApplicationConsole overridingConsole)
    {
        return realize(overridingConsole == null ? null : new SingletonApplicationConsoleBuilder(overridingConsole));
    }


    @Override
    public G realize() throws IOException
    {
        return realize((ApplicationConsoleBuilder) null);
    }


    /**
     * Realize the {@link Application}s from the {@link ApplicationSchema} and {@link Platform}s
     * previously added to this builder.
     *
     * @param applications              the list to add each realized {@link Application} to
     * @param overridingConsoleBuilder  the {@link ApplicationConsoleBuilder} to use to build {@link ApplicationConsole}s
     *                                  for each {@link Application}
     */
    protected void realizeApplicationFromSchema(LinkedList<A> applications, ApplicationConsoleBuilder overridingConsoleBuilder)
    {
        for (String prefix : m_schema.keySet())
        {
            Quadruple<Platform, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder> quadruple =
                    m_schema.get(prefix);
            Platform                  platform           = quadruple.getA();
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

                applications.add(platform.realize(schema, applicationName, console));
            }
        }
    }


    /**
     * Create an {@link Assembly} based on the specified collection of {@link Application}s.
     *
     * @param applications  The collection of {@link Application}s.
     *
     * @return An {@link Assembly} implementation.
     */
    abstract protected G createApplicationGroup(List<? extends A> applications);
}
