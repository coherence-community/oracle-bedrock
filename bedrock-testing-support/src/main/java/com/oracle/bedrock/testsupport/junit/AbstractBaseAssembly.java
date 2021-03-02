/*
 * File: AbstractBaseAssembly.java
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

package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.*;
import com.oracle.bedrock.runtime.options.PlatformPredicate;
import com.oracle.bedrock.util.Triple;
import org.junit.rules.ExternalResource;

import java.util.ArrayList;

/**
 * An abstract JUnit extension to configure and build an {@link Assembly} for use with JUnit tests.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application} within the {@link Assembly}
 * @param <G>  the type of {@link Assembly} resource produced
 * @param <R>  the type of the {@link AbstractBaseAssembly} to permit fluent-style method calls
 *
 * @see AssemblyBuilder
 */
public abstract class AbstractBaseAssembly<A extends Application, G extends Assembly<A>,
        R extends AbstractBaseAssembly<A, G, R>>
{
    /**
     * Definitions for the number, types and options of each {@link Application} to launch.
     */
    protected ArrayList<Triple<Integer, Class<? extends A>, OptionsByType>> launchDefinitions;

    /**
     * The {@link AssemblyBuilder} to create the {@link Assembly} of {@link Application}s.
     */
    protected AssemblyBuilder<A, G> assemblyBuilder;

    /**
     * The {@link Infrastructure} on which to create the {@link Assembly}.
     */
    protected Infrastructure infrastructure;

    /**
     * The {@link OptionsByType} to use as the basis for each {@link Application} launched for the {@link Assembly}.
     */
    protected OptionsByType commonOptionsByType;

    /**
     * The {@link OptionsByType} to use when building the {@link Assembly} with {@link AssemblyBuilder#build(Infrastructure, Option...)}.
     */
    protected OptionsByType creationOptionsByType;

    /**
     * The {@link OptionsByType} to use when closing the {@link Assembly}
     */
    protected OptionsByType closingOptionsByType;

    /**
     * The {@link Assembly} created by the {@link AssemblyBuilder}.
     */
    protected G assembly;


    /**
     * Constructor for {@link AbstractBaseAssembly}.
     */
    protected AbstractBaseAssembly()
    {
        this.launchDefinitions = new ArrayList<>();

        // assume no infrastructure (ie: local)
        this.infrastructure        = null;

        this.commonOptionsByType   = OptionsByType.empty();
        this.creationOptionsByType = OptionsByType.empty();
        this.closingOptionsByType  = OptionsByType.empty();

        this.assembly              = null;

        // create the assembly builder now so sub-classes may enhance it
        this.assemblyBuilder = createBuilder();
    }


    /**
     * Creates a new {@link AssemblyBuilder} for creating an {@link Assembly}.
     *
     * @return a new {@link AssemblyBuilder}
     */
    protected abstract AssemblyBuilder<A, G> createBuilder();


    protected void start() throws Exception
    {
        // establish the launch definitions for the assembly builder
        for (Triple<Integer, Class<? extends A>, OptionsByType> launchDefinition : launchDefinitions)
        {
            // create the options for the application, based on the common options
            OptionsByType optionsByType = OptionsByType.of(commonOptionsByType).addAll(launchDefinition.getZ());

            // include in the assembly builder
            assemblyBuilder.include(launchDefinition.getX(), launchDefinition.getY(), optionsByType.asArray());
        }

        // build the assembly
        assembly = assemblyBuilder.build(infrastructure == null ? Infrastructure.local() : infrastructure,
                                         creationOptionsByType.asArray());
    }


    protected void close()
    {
        // close the assembly
        assembly.close(closingOptionsByType.asArray());
    }


    /**
     * Defines the necessary information for launching one or more {@link Application}s of a specified
     * type as part of the {@link Assembly} when the {@link ExternalResource} is established.
     * <p>
     * The {@link Platform} on which the {@link Application}s are launched is based on the
     * {@link PlatformPredicate} specified as an {@link Option}.  By default this is {@link PlatformPredicate#any()}.
     * <p>
     * Multiple calls to this method are permitted, allowing an {@link Assembly} to be created containing
     * multiple different types of {@link Application}s.
     *
     * @param count             the number of instances of the {@link Application} that should be launched for
     *                          the {@link Assembly}
     * @param applicationClass  the class of {@link Application}
     * @param options           the {@link Option}s to use for launching the {@link Application}s
     *
     * @return the {@link AbstractBaseAssembly} to permit fluent-style method calls
     */
    public R include(int                count,
                     Class<? extends A> applicationClass,
                     Option...          options)
    {
        // remember the launch definition
        launchDefinitions.add(new Triple<>(count, applicationClass, OptionsByType.of(options)));

        return (R) this;
    }


    /**
     * Specifies the {@link Infrastructure} that will be used to launch the specified {@link Application}s,
     * allowing different {@link Platform}s to be used for each {@link Application} when required.
     * <p>
     * By default this is {@link Infrastructure#local()}, which is the {@link LocalPlatform}.
     *
     * @param infrastructure  the {@link Infrastructure}
     *
     * @return the {@link AbstractBaseAssembly} to permit fluent-style method calls
     */
    public R using(Infrastructure infrastructure)
    {
        this.infrastructure = infrastructure;

        return (R) this;
    }


    /**
     * Specifies the {@link Platform}s that will be used to launch the specified {@link Application}s.
     * <p>
     * This is equivalent to calling {@link #using(Infrastructure)} using {@link Infrastructure#using(Platform...)}
     * with the provided {@link Platform}s.
     *
     * @param platforms  the {@link Platform}s
     *
     * @return the {@link AbstractBaseAssembly} to permit fluent-style method calls
     */
    public R using(Platform... platforms)
    {
        return using(Infrastructure.using(platforms));
    }


    /**
     * The {@link Option}s to be used as the basis launching each {@link Application}s.   This will be overridden
     * by those specifically provided for each application and those defined by {@link #withOverridingOptions(Option...)}.
     *
     * @param options  the {@link Option}s
     *
     * @return the {@link AbstractBaseAssembly} to permit fluent-style method calls
     *
     * @see AssemblyBuilder#build(Infrastructure, Option...)
     */
    public R with(Option... options)
    {
        this.commonOptionsByType.addAll(options);

        return (R) this;
    }


    /**
     * The {@link Option}s to be provided when launching {@link Application}s, overriding those
     * that may be been defined for each {@link Application}.
     *
     * @param options  the {@link Option}s
     *
     * @return the {@link AbstractBaseAssembly} to permit fluent-style method calls
     *
     * @see AssemblyBuilder#build(Infrastructure, Option...)
     */
    public R withOverridingOptions(Option... options)
    {
        this.creationOptionsByType.addAll(options);

        return (R) this;
    }


    /**
     * The {@link Option}s to be used when closing the {@link Assembly}.
     *
     * @param options  the {@link Option}s
     *
     * @return the {@link AbstractBaseAssembly} to permit fluent-style method calls
     *
     * @see Assembly#close(Option...)
     */
    public R withClosingOptions(Option... options)
    {
        this.closingOptionsByType.addAll(options);

        return (R) this;
    }
}
