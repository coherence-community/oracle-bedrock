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

import com.oracle.tools.util.Triple;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link AbstractApplicationGroupBuilder} is a base implementation of an {@link ApplicationGroupBuilder}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractApplicationGroupBuilder<A extends Application<A>, S extends ApplicationSchema<A, S>,
                                                      B extends ApplicationBuilder<A, S>,
                                                      G extends ApplicationGroup<A>>
    implements ApplicationGroupBuilder<A, S, B, G>
{
    /**
     * The map of {@link ApplicationBuilder}s to create applications, keyed by application prefix name.
     */
    protected LinkedHashMap<String, Triple<B, S, Integer>> m_builders;


    /**
     * Construct an {@link AbstractApplicationGroupBuilder}.
     */
    public AbstractApplicationGroupBuilder()
    {
        m_builders = new LinkedHashMap<String, Triple<B, S, Integer>>();
    }


    /**
     * {@inheritDoc}
     */
    public void addBuilder(B bldrApplication,
                           S schema,
                           String sApplicationPrefix,
                           int cRequiredInstances)
    {
        m_builders.put(sApplicationPrefix, new Triple<B, S, Integer>(bldrApplication, schema, cRequiredInstances));
    }


    /**
     * {@inheritDoc}
     */
    public G realize(ApplicationConsole console) throws java.io.IOException
    {
        // build a list of applications
        LinkedList<A> applications = new LinkedList<A>();

        for (String prefix : m_builders.keySet())
        {
            Triple<B, S, Integer> triple             = m_builders.get(prefix);
            B                     builder            = triple.getX();
            S                     schema             = triple.getY();
            int                   cRequiredInstances = triple.getZ();

            for (int i = 0; i < cRequiredInstances; i++)
            {
                String applicationName = String.format("%s-%d", prefix, i);

                applications.add(builder.realize(schema, applicationName, console));
            }
        }

        return createApplicationGroup(applications);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public G realize() throws IOException
    {
        return realize(new NullApplicationConsole());
    }


    /**
     * Create an {@link ApplicationGroup} based on the specified collection of {@link Application}s.
     *
     * @param applications  The collection of {@link Application}s.
     *
     * @return An {@link ApplicationGroup} implementation.
     */
    abstract protected G createApplicationGroup(List<A> applications);
}
