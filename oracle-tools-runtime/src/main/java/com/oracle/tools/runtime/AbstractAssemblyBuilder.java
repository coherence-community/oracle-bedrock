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

import com.oracle.tools.Option;

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
     * The map of required {@link Assembly} {@link Characteristics}, keyed by application prefix name.
     */
    protected LinkedHashMap<String, Characteristics<A>> characteristics;


    /**
     * Construct an {@link AbstractAssemblyBuilder}.
     */
    public AbstractAssemblyBuilder()
    {
        characteristics = new LinkedHashMap<String, Characteristics<A>>();
    }


    @Override
    public <T extends A, S extends ApplicationSchema<T>> void addSchema(String    applicationNamePrefix,
                                                                        S         applicationSchema,
                                                                        int       count,
                                                                        Platform  platform,
                                                                        Option... options)
    {
        addSchema(applicationNamePrefix, applicationSchema, count, null, platform, options);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addSchema(String                    applicationNamePrefix,
                                                                        S                         applicationSchema,
                                                                        int                       count,
                                                                        ApplicationConsoleBuilder consoleBuilder,
                                                                        Platform                  platform,
                                                                        Option...                 options)
    {
        characteristics.put(applicationNamePrefix,
                            new Characteristics<A>((ApplicationSchema<A>) applicationSchema,
                                                   count,
                                                   consoleBuilder,
                                                   platform,
                                                   options));
    }


    @Override
    public G realize(ApplicationConsoleBuilder overridingConsoleBuilder)
    {
        // build a list of applications
        LinkedList<A> applications = new LinkedList<A>();

        // establish the applications for the assembly
        realizeApplicationFromSchema(applications, overridingConsoleBuilder);

        // establish the assembly based on the applications
        G assembly = createAssembly(applications);

        // add the assembly as an option to all of the applications
        for (A application : applications)
        {
            application.getOptions().add(assembly);
        }

        return assembly;
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
    protected void realizeApplicationFromSchema(LinkedList<A>             applications,
                                                ApplicationConsoleBuilder overridingConsoleBuilder)
    {
        for (String prefix : characteristics.keySet())
        {
            Characteristics<A>        characteristic = characteristics.get(prefix);
            ApplicationSchema<A>      schema         = characteristic.getApplicationSchema();
            int                       instanceCount  = characteristic.getInstanceCount();
            ApplicationConsoleBuilder consoleBuilder = characteristic.getConsoleBuilder();
            Platform                  platform       = characteristic.getPlatform();
            Option[]                  options        = characteristic.getOptions();

            // override the application defined console builder when this method is provided with one
            if (overridingConsoleBuilder != null)
            {
                consoleBuilder = overridingConsoleBuilder;
            }

            for (int i = 1; i <= instanceCount; i++)
            {
                String             applicationName = String.format("%s-%d", prefix, i);

                ApplicationConsole console = consoleBuilder == null ? null : consoleBuilder.realize(applicationName);

                if (console == null)
                {
                    console = new NullApplicationConsole();
                }

                applications.add(platform.realize(applicationName, schema, console, options));
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
    abstract protected G createAssembly(List<? extends A> applications);


    /**
     * Encapsulates the characteristics for one or more specific types of {@link Application}
     * to be created as part of an {@link Assembly} when it is realized.
     *
     * @param <A>  the type of the {@link Application}
     */
    protected static class Characteristics<A extends Application>
    {
        /**
         * The {@link ApplicationSchema} for the {@link Application}s.
         */
        private ApplicationSchema<A> schema;

        /**
         * The number of {@link Application} instances to be created
         */
        private int count;

        /**
         * The {@link ApplicationConsoleBuilder} used to create {@link ApplicationConsole}s.
         */
        private ApplicationConsoleBuilder consoleBuilder;

        /**
         * The {@link Platform} to be used to realize the {@link Application}s.
         */
        private Platform platform;

        /**
         * The {@link Option}s for realizing the {@link Application}s.
         */
        private Option[] options;


        /**
         * Constructs an {@link Characteristics}.
         *
         * @param schema          the {@link ApplicationSchema} for the {@link Application}s
         * @param count           the number of {@link Application} instances to be created
         * @param consoleBuilder  the {@link ApplicationConsoleBuilder} used to create consoles
         * @param platform        the {@link Platform} used to create the {@link Application}s
         * @param options         the {@link Option}s for the {@link Platform}
         */
        public Characteristics(ApplicationSchema<A>      schema,
                               int                       count,
                               ApplicationConsoleBuilder consoleBuilder,
                               Platform                  platform,
                               Option...                 options)
        {
            this.schema         = schema;
            this.count          = count;
            this.consoleBuilder = consoleBuilder;
            this.platform       = platform;
            this.options        = options;
        }


        /**
         * Obtains the {@link ApplicationSchema}.
         *
         * @return  the {@link ApplicationSchema}
         */
        public ApplicationSchema<A> getApplicationSchema()
        {
            return schema;
        }


        /**
         * Obtains the number of {@link Application} instances to realize
         * in the {@link Assembly}.
         *
         * @return  the instance count
         */
        public int getInstanceCount()
        {
            return count;
        }


        /**
         * Obtains the {@link ApplicationConsoleBuilder} to use for realizing
         * {@link ApplicationConsole}s for each {@link Application}.
         *
         * @return  the {@link ApplicationConsoleBuilder}
         */
        public ApplicationConsoleBuilder getConsoleBuilder()
        {
            return consoleBuilder;
        }


        /**
         * Obtains the {@link Platform} on which to realize the {@link Application}s.
         *
         * @return  the {@link Platform}
         */
        public Platform getPlatform()
        {
            return platform;
        }


        /**
         * Obtains the {@link Option}s to use when realizing the {@link Application}s.
         *
         * @return  the {@link Option}s
         */
        public Option[] getOptions()
        {
            return options;
        }
    }
}
