/*
 * File: InfrastructureBuilder.java
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

import com.oracle.tools.util.Pair;
import com.oracle.tools.util.Quadruple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A builder that can build an {@link Infrastructure} containing
 * a set of {@link Platform}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 *
 * @param <P>  the type of {@link Platform} that the realized {@link Infrastructure}
 *             will contain
 */
public class InfrastructureBuilder<P extends Platform>
{
    /**
     * The {@link PlatformSchema} that define the various
     * {@link Platform}s to add to the {@link Infrastructure}
     * realized by this {@link InfrastructureBuilder}
     */
    private final List<Quadruple<PlatformBuilder<P>,PlatformSchema<P>,String,Integer>> schema;

    /**
     * A list of already instantiated {@link Platform}s to add to the realized {@link Infrastructure}
     */
    private final List<P> platforms;

    /**
     * The {@link ExecutorService} to use to build the {@link Platform}s in parallel
     */
    private ExecutorService executor;

    /**
     * Construct a new {@link InfrastructureBuilder}.
     */
    public InfrastructureBuilder()
    {
        this(null);
    }


    /**
     * Construct a new {@link InfrastructureBuilder}.
     */
    public InfrastructureBuilder(ExecutorService executor)
    {
        this.executor  = executor != null ? executor : Executors.newFixedThreadPool(5);
        this.schema    = new LinkedList<Quadruple<PlatformBuilder<P>,PlatformSchema<P>,String,Integer>>();
        this.platforms = new LinkedList<P>();
    }

    /**
     * Add a platform to this {@link InfrastructureBuilder}
     *
     * @param platform  the existing {@link Platform} to add to the realized {@link Infrastructure}
     *
     * @param <T>  the type of {@link Platform}
     *
     * @return this {@link InfrastructureBuilder} for chaining methods
     */
    @SuppressWarnings("unchecked")
    public <T extends P> InfrastructureBuilder<P> addPlatform(T platform)
    {
        if (platform != null)
        {
            platforms.add(platform);
        }

        return this;
    }

    /**
     * Add a platform to this {@link InfrastructureBuilder}
     *
     * @param builder         the {@link PlatformBuilder} that will build the {@link Platform}
     * @param platformSchema  the {@link PlatformSchema} that defines the {@link Platform}
     *
     * @param <T>  the type of {@link Platform} to be realized
     * @param <B>  the type of {@link PlatformBuilder}
     * @param <S>  the type of {@link PlatformSchema}
     *
     * @return this {@link InfrastructureBuilder} for chaining methods
     */
    @SuppressWarnings("unchecked")
    public <T extends P, B extends PlatformBuilder<T>,
            S extends PlatformSchema<T>> InfrastructureBuilder<P> addPlatform(B builder,
                                                                              S platformSchema)
    {
        return addPlatform(builder, platformSchema, platformSchema.getName(), 1);
    }

    /**
     * Add a platform to this {@link InfrastructureBuilder}
     *
     * @param builder         the {@link PlatformBuilder} that will build the {@link Platform}
     * @param platformSchema  the {@link PlatformSchema} that defines the {@link Platform}
     * @param name            the name to apply to the realized {@link Platform}
     *
     * @param <T>  the type of {@link Platform} to be realized
     * @param <B>  the type of {@link PlatformBuilder}
     * @param <S>  the type of {@link PlatformSchema}
     *
     * @return this {@link InfrastructureBuilder} for chaining methods
     */
    @SuppressWarnings("unchecked")
    public <T extends P, B extends PlatformBuilder<T>,
            S extends PlatformSchema<T>> InfrastructureBuilder<P> addPlatform(B      builder,
                                                                              S      platformSchema,
                                                                              String name)
    {
        return addPlatform(builder, platformSchema, name, 1);
    }

    /**
     * Add a platform to this {@link InfrastructureBuilder}
     *
     * @param builder         the {@link PlatformBuilder} that will build the {@link Platform}
     * @param platformSchema  the {@link PlatformSchema} that defines the {@link Platform}
     * @param count           the number of instances of the VM to start
     *
     * @param <T>  the type of {@link Platform} to be realized
     * @param <B>  the type of {@link PlatformBuilder}
     * @param <S>  the type of {@link PlatformSchema}
     *
     * @return this {@link InfrastructureBuilder} for chaining methods
     */
    @SuppressWarnings("unchecked")
    public <T extends P, B extends PlatformBuilder<T>,
            S extends PlatformSchema<T>> InfrastructureBuilder<P> addPlatform(B   builder,
                                                                              S   platformSchema,
                                                                              int count)
    {
        return addPlatform(builder, platformSchema, platformSchema.getName(), count);
    }

    /**
     * Add a platform to this {@link InfrastructureBuilder}
     *
     * @param builder         the {@link PlatformBuilder} that will build the {@link Platform}
     * @param platformSchema  the {@link PlatformSchema} that defines the {@link Platform}
     * @param count           the number of instances of the VM to start
     *
     * @param <T>  the type of {@link Platform} to be realized
     * @param <B>  the type of {@link PlatformBuilder}
     * @param <S>  the type of {@link PlatformSchema}
     *
     * @return this {@link InfrastructureBuilder} for chaining methods
     */
    @SuppressWarnings("unchecked")
    public <T extends P, B extends PlatformBuilder<T>,
            S extends PlatformSchema<T>> InfrastructureBuilder<P> addPlatform(B      builder,
                                                                              S      platformSchema,
                                                                              String name,
                                                                              int    count)
    {
        if (count <= 0)
        {
            throw new IllegalArgumentException("Count must be greater than zero");
        }

        schema.add(new Quadruple<PlatformBuilder<P>,PlatformSchema<P>,String,Integer>((PlatformBuilder<P>) builder,
                                                                                      (PlatformSchema<P>) platformSchema,
                                                                                      name,
                                                                                      count));

        return this;
    }


    /**
     * Realize an instance of an {@link Infrastructure} based on the
     * platform definitions that have been added to this {@link InfrastructureBuilder}
     *
     * @return the realized {@link Infrastructure}
     */
    public Infrastructure<P> realize()
    {
        try
        {
            Map<String, P>                platformMap = new HashMap<String, P>();
            List<Future<Pair<String, P>>> futures     = new ArrayList<Future<Pair<String, P>>>();

            for (P platform : platforms)
            {
                platformMap.put(platform.getName(), platform);
            }

            for (Quadruple<PlatformBuilder<P>,PlatformSchema<P>,String,Integer> quad : schema)
            {
                PlatformBuilder<P>      builder = quad.getA();
                PlatformSchema<P>       schema  = quad.getB();
                String                  name    = quad.getC();
                int                     count   = quad.getD();

                if (count == 1)
                {
                    Future<Pair<String, P>> future = executor.submit(new PlatformRealizer(name, builder, schema));
                    futures.add(future);
                }
                else
                {
                    for (int i=1; i<=count; i++)
                    {
                        String                  instanceName = name + '-' + i;
                        Future<Pair<String, P>> future       = executor.submit(new PlatformRealizer(instanceName,
                                                                                                    builder,
                                                                                                    schema));
                        futures.add(future);
                    }
                }
            }

            for (Future<Pair<String, P>> future : futures)
            {
                Pair<String, P> pair = future.get();

                platformMap.put(pair.getX(), pair.getY());
            }

            return new Infrastructure<P>(platformMap);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error realizing platforms", e);
        }
    }

    /**
     * A {@link Callable} used to build a specific {@link Platform}
     */
    private class PlatformRealizer implements Callable<Pair<String, P>>
    {
        private String             name;
        private PlatformBuilder<P> builder;
        private PlatformSchema<P>  schema;


        private PlatformRealizer(String             name,
                                 PlatformBuilder<P> builder,
                                 PlatformSchema<P>  schema)
        {
            this.name    = name;
            this.builder = builder;
            this.schema  = schema;
        }

        @Override
        public Pair<String, P> call() throws Exception
        {
            P platform = builder.realize(name, schema);

            return new Pair<String, P>(name, platform);
        }
    }
}
