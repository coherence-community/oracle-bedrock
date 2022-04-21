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

import java.util.LinkedList;

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
public abstract class AbstractAssemblyBuilder<A extends Application, G extends AbstractAssembly<A>, B extends AbstractAssemblyBuilder<A, G, B>>
    implements AssemblyBuilder<A, G, B>
{
    /**
     * The map of required {@link Assembly} {@link Characteristics}
     */
    protected LinkedList<Characteristics<A>> characteristics;

    /**
     * The common {@link OptionsByType} to be used when launching all {@link Application}s.
     */
    protected OptionsByType optionsByType;


    /**
     * Construct an {@link AbstractAssemblyBuilder}.
     */
    public AbstractAssemblyBuilder()
    {
        characteristics = new LinkedList<>();
        optionsByType   = OptionsByType.empty();
    }


    @Override
    public B with(Option... options)
    {
        optionsByType = OptionsByType.of(options);
        return castThis();
    }


    @Override
    public B include(int                count,
                        Class<? extends A> applicationClass,
                        Option...          options)
    {
        if (count > 0)
        {
            characteristics.add(new Characteristics<>(count, applicationClass, options));
        }
        return castThis();
    }


    @Override
    public G build(Infrastructure infrastructure,
                   Option...      options)
    {
        // establish the assembly starting with a copy of the common options
        G assembly = createAssembly(OptionsByType.of(optionsByType));

        // use the characteristics to expand the assembly on the infrastructure
        for (Characteristics<A> characteristic : characteristics)
        {
            int                instanceCount    = characteristic.getCount();
            Class<? extends A> applicationClass = characteristic.getApplicationClass();
            OptionsByType      launchOptions    = OptionsByType.of(characteristic.getOptions()).addAll(options);

            // expand the assembly based on the desired characteristics
            assembly.expand(instanceCount, infrastructure, applicationClass, launchOptions.asArray());
        }

        return assembly;
    }


    /**
     * Create an empty {@link Assembly} of the required type based on the specified {@link OptionsByType}.
     *
     * @param optionsByType  the {@link OptionsByType} for the {@link Assembly}
     *
     * @return an empty {@link Assembly}
     */
    abstract public G createAssembly(OptionsByType optionsByType);

    /**
     * Cast this builder to a {@code B}.
     *
     * @return this builder cast to a {@code B}.
     */
    @SuppressWarnings("unchecked")
    private B castThis()
    {
        return (B) this;
    }

    /**
     * Encapsulates the characteristics for one or more specific types of {@link Application}
     * to be created as part of an {@link Assembly} when it is realized.
     */
    protected static class Characteristics<A extends Application>
    {
        /**
         * The number of {@link Application} instances to be created.
         */
        private final int count;

        /**
         * The class of {@link Application} to create.
         */
        private final Class<? extends A> applicationClass;

        /**
         * The {@link Option}s for realizing the {@link Application}s.
         */
        private final Option[] options;


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
