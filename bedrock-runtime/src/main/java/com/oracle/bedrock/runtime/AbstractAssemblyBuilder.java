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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.Discriminator;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A base implementation of an {@link AssemblyBuilder}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application}s that will be part of the {@link Assembly} when
 *             built by the {@link AssemblyBuilder}
 * @param <G>  the type of the {@link Assembly} built by the {@link AssemblyBuilder}
 */
@Internal
public abstract class AbstractAssemblyBuilder<A extends Application, G extends Assembly<A>>
    implements AssemblyBuilder<A, G>
{
    /**
     * The map of required {@link Assembly} {@link Characteristics}
     */
    protected LinkedList<Characteristics> characteristics;


    /**
     * Construct an {@link AbstractAssemblyBuilder}.
     */
    public AbstractAssemblyBuilder()
    {
        characteristics = new LinkedList<>();
    }


    @Override
    public void include(int                count,
                        Class<? extends A> applicationClass,
                        Option...          options)
    {
        characteristics.add(new Characteristics(count, applicationClass, options));
    }


    @Override
    public G build(Infrastructure infrastructure,
                   Option...      options)
    {
        // build a list of applications
        LinkedList<A> applications = new LinkedList<>();

        // establish the applications for the assembly
        launchApplications(applications, infrastructure, options);

        // establish the assembly based on the applications
        G assembly = createAssembly(applications, OptionsByType.of(options));

        return assembly;
    }


    /**
     * Launch the {@link Application}s based on the defined {@link Characteristics}.
     *
     * @param applications    the list to add each realized {@link Application} to
     * @param infrastructure  the {@link Infrastructure} on which to launch the {@link Application}s
     * @param options         the {@link Option}s for overriding any defined {@link Characteristics}
     */
    protected void launchApplications(LinkedList<A>  applications,
                                      Infrastructure infrastructure,
                                      Option...      options)
    {
        // attempt to launch the applications using the specified infrastructure
        try
        {
            for (Characteristics characteristic : characteristics)
            {
                int                instanceCount    = characteristic.getCount();
                Class<? extends A> applicationClass = characteristic.getApplicationClass();

                for (int i = 1; i <= instanceCount; i++)
                {
                    // establish a new set of launch options for each application
                    OptionsByType launchOptions = OptionsByType.of(characteristic.getOptions()).addAll(options);

                    // include a discriminator for the application being launched
                    launchOptions.add(Discriminator.of(i));

                    // ensure there's at least a system console
                    launchOptions.addIfAbsent(Console.system());

                    // finalize the launching options
                    Option[] launchingOptions = launchOptions.asArray();

                    // determine the platform
                    Platform platform = infrastructure.getPlatform(launchingOptions);

                    // launch the application
                    A application = platform.launch(applicationClass, launchingOptions);

                    // add the application to the assembly
                    applications.add(application);
                }
            }
        }
        catch (Throwable throwable)
        {
            // ensure all recently launched applications are shutdown to prevent applications staying around
            for (A application : applications)
            {
                try
                {
                    application.close();
                }
                catch (Throwable t)
                {
                    // we ignore any issues when the application fails to close
                }
            }

            throw new RuntimeException("Failed to create " + this.getClass().getSimpleName()
                                       + ". Automatically closed " + applications.size()
                                       + " of the successfully created applications out of the "
                                       + characteristics.stream().collect(Collectors.summingInt(c -> c.count))
                                       + " that were meant to be created.",
                                       throwable);
        }
    }


    /**
     * Create an {@link Assembly} based on the specified collection of {@link Application}s.
     *
     * @param applications   the collection of {@link Application}s.
     * @param optionsByType  the {@link OptionsByType} used to launch the {@link Application}s
     *
     * @return An {@link Assembly} implementation.
     */
    abstract protected G createAssembly(List<A>       applications,
                                        OptionsByType optionsByType);


    /**
     * Encapsulates the characteristics for one or more specific types of {@link Application}
     * to be created as part of an {@link Assembly} when it is realized.
     */
    protected static class Characteristics<A extends Application>
    {
        /**
         * The number of {@link Application} instances to be created.
         */
        private int count;

        /**
         * The class of {@link Application} to create.
         */
        private Class<? extends A> applicationClass;

        /**
         * The {@link Option}s for realizing the {@link Application}s.
         */
        private Option[] options;


        /**
         * Constructs an {@link Characteristics}.
         *
         * @param count             the number of {@link Application} instances to be created
         * @param applicationClass  the class of {@link Application} to create
         * @param options           the {@link Option}s for launching the application
         */
        public Characteristics(int                count,
                               Class<? extends A> applicationClass,
                               Option...          options)
        {
            this.count            = count;
            this.applicationClass = applicationClass;
            this.options          = options;
        }


        /**
         * Obtains the number of {@link Application} instances to build
         * in the {@link Assembly}.
         *
         * @return  the instance count
         */
        public int getCount()
        {
            return count;
        }


        /**
         * Obtains the class of {@link Application} to create.
         *
         * @return  the class of {@link Application}
         */
        public Class<? extends A> getApplicationClass()
        {
            return applicationClass;
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
