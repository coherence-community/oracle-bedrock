/*
 * File: Decorations.java
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

package com.oracle.bedrock.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A {@link Collector} of {@link Decoration}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Decoration
 */
public class Decorations implements Option.Collector<Decoration, Decorations>
{
    /**
     * The {@link Decoration}s collected by the {@link Decorations}.
     */
    private LinkedHashSet<Decoration> decorations;


    /**
     * Constructs an empty {@link Decorations}.
     */
    @Options.Default
    public Decorations()
    {
        this.decorations = new LinkedHashSet<>();
    }


    /**
     * Constructs a {@link Decorations} based on the supplied {@link Decorations}.
     *
     * @param decorations the {@link Decorations} on which to base
     *                   the new {@link Decorations}
     */
    public Decorations(Decoration... decorations)
    {
        this();

        if (decorations != null)
        {
            for (Decoration decoration : decorations)
            {
                add(decoration);
            }
        }
    }


    /**
     * Constructs a {@link Decorations} based on the {@link Decoration}s in another
     * {@link Decorations}.
     *
     * @param decorations the {@link Decorations} on which to base the new {@link Decorations}
     */
    public Decorations(Decorations decorations)
    {
        this();

        this.decorations.addAll(decorations.decorations);
    }


    /**
     * Obtains the number of {@link Decoration}s contained
     * by the {@link Decorations}.
     *
     * @return the number of {@link Decoration}s
     */
    public int size()
    {
        return decorations.size();
    }


    /**
     * Determines if the {@link Decorations} is empty (contains no {@link Decoration}s)
     *
     * @return <code>true</code> if the {@link Decorations} is empty, <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return decorations.isEmpty();
    }


    /**
     * Adds the specified {@link Decoration} to the {@link Decorations}, returning a new
     * {@link Decorations} containing the {@link Decoration}.
     *
     * @param decoration the {@link Decoration} to add
     *
     * @return the a new {@link Decorations} instance, including the existing {@link Decoration}s and the new {@link Variable}
     */
    public Decorations add(Decoration decoration)
    {
        if (decoration == null)
        {
            return this;
        }
        else
        {
            Decorations result = new Decorations(this);

            result.decorations.add(decoration);

            return result;
        }
    }


    /**
     * Removes the specified {@link Decoration} from the {@link Decorations}, returning a new
     * {@link Decorations} without the said {@link Decoration}.
     *
     * @param decoration the {@link Decoration} to remove
     *
     * @return the a new {@link Decorations} instance, excluding the specified {@link Decoration}
     */
    public Decorations remove(Decoration decoration)
    {
        if (decoration == null ||!decorations.contains(decoration))
        {
            return this;
        }
        else
        {
            Decorations result = new Decorations(this);

            result.decorations.remove(decoration);

            return result;
        }
    }


    /**
     * Adds all of the {@link Decorations} to this {@link Decorations}
     * returning a new {@link Decorations}.
     *
     * @param decorations the {@link Decorations}
     *
     * @return a new {@link Decorations}
     */
    public Decorations addAll(Decorations decorations)
    {
        Decorations result = new Decorations(this);

        for (Decoration decoration : decorations)
        {
            result.decorations.add(decoration);
        }

        return result;
    }


    /**
     * Determines if the {@link Decorations} contains a {@link Decoration}.
     *
     * @param decoration  the {@link Decoration}
     *
     * @return <code>true</code> if the {@link Decorations} contains the {@link Decoration}, <code>false</code> otherwise
     */
    public boolean contains(Decoration decoration)
    {
        return decorations.contains(decoration);
    }


    @Override
    public Decorations with(Decoration decoration)
    {
        return add(decoration);
    }


    @Override
    public Decorations without(Decoration decoration)
    {
        return remove(decoration);
    }


    @Override
    public Iterator<Decoration> iterator()
    {
        return decorations.iterator();
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        ArrayList<O> result = new ArrayList<>();

        for (Decoration decoration : decorations)
        {
            if (requiredClass.isInstance(decoration))
            {
                result.add((O) decoration);
            }

            if (requiredClass.isInstance(decoration.get()))
            {
                result.add((O) decoration.get());
            }
        }

        return result;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Decorations))
        {
            return false;
        }

        Decorations that = (Decorations) o;

        return decorations.equals(that.decorations);

    }


    @Override
    public int hashCode()
    {
        return decorations.hashCode();
    }
}
