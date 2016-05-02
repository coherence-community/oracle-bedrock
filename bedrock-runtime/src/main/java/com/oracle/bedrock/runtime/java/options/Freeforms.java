/*
 * File: Freeforms.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A {@link Collector} of {@link Freeform} {@link JvmOption}s.
 */
public class Freeforms implements Option.Collector<Freeform, Freeforms>
{
    /**
     * The {@link Freeform}s.
     */
    private LinkedHashSet<Freeform> freeforms;


    /**
     * Constructs an empty {@link Freeforms}.
     */
    @Options.Default
    public Freeforms()
    {
        this.freeforms = new LinkedHashSet<>();
    }


    /**
     * Constructs a {@link Freeforms} based on some {@link Freeform}s.
     *
     * @param freeforms  the {@link Freeform}s
     */
    public Freeforms(Freeform... freeforms)
    {
        this();

        if (freeforms != null)
        {
            for (Freeform freeform : freeforms)
            {
                this.freeforms.add(freeform);
            }
        }
    }


    /**
     * Constructs a {@link Freeforms} based on an array of {@link String} values,
     * one {@link Freeform} for each {@link String}.
     *
     * @param values  the values
     */
    public Freeforms(String... values)
    {
        this();

        if (values != null)
        {
            for (String value : values)
            {
                this.freeforms.add(new Freeform(value));
            }
        }
    }


    @Override
    public Freeforms with(Freeform freeform)
    {
        Freeforms freeforms = new Freeforms();

        freeforms.freeforms.addAll(this.freeforms);
        freeforms.freeforms.add(freeform);

        return freeforms;
    }


    @Override
    public Freeforms without(Freeform freeform)
    {
        Freeforms freeforms = new Freeforms();

        freeforms.freeforms.addAll(this.freeforms);
        freeforms.freeforms.remove(freeform);

        return freeforms;
    }


    @Override
    public Iterator<Freeform> iterator()
    {
        return freeforms.iterator();
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(Freeform.class))
        {
            return (Iterable<O>) freeforms;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Freeforms))
        {
            return false;
        }

        Freeforms freeforms1 = (Freeforms) o;

        return freeforms != null ? freeforms.equals(freeforms1.freeforms) : freeforms1.freeforms == null;

    }


    @Override
    public int hashCode()
    {
        return freeforms != null ? freeforms.hashCode() : 0;
    }
}
