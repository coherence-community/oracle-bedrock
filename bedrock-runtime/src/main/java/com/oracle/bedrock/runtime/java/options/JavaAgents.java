/*
 * File: JavaAgents.java
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
 * A {@link Collector} for {@link JavaAgent}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JavaAgents implements Option.Collector<JavaAgent, JavaAgents>
{
    /**
     * The {@link JavaAgent}s.
     */
    private LinkedHashSet<JavaAgent> agents;


    /**
     * Constructs an empty {@link JavaAgents}.
     */
    @Options.Default
    public JavaAgents()
    {
        this.agents = new LinkedHashSet<>();
    }


    /**
     * Constructs {@link JavaAgents} based on an array of {@link JavaAgent}s.
     *
     * @param agents  the {@link JavaAgent}s
     */
    public JavaAgents(JavaAgent... agents)
    {
        this();

        if (agents != null)
        {
            for (JavaAgent agent : agents)
            {
                this.agents.add(agent);
            }
        }
    }


    /**
     * Constructs a {@link JavaAgents} based on another {@link JavaAgents}.
     *
     * @param javaAgents  the other {@link JavaAgents}
     */
    public JavaAgents(JavaAgents javaAgents)
    {
        this();

        for (JavaAgent javaAgent : javaAgents)
        {
            this.agents.add(javaAgent);
        }
    }


    @Override
    public JavaAgents with(JavaAgent agent)
    {
        JavaAgents result = new JavaAgents(this);

        result.agents.add(agent);

        return result;
    }


    @Override
    public JavaAgents without(JavaAgent agent)
    {
        JavaAgents result = new JavaAgents(this);

        result.agents.remove(agent);

        return result;
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(JavaAgent.class))
        {
            return (Iterable<O>) agents;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    @Override
    public Iterator<JavaAgent> iterator()
    {
        return agents.iterator();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof JavaAgents))
        {
            return false;
        }

        JavaAgents agents1 = (JavaAgents) o;

        return agents != null ? agents.equals(agents1.agents) : agents1.agents == null;

    }


    @Override
    public int hashCode()
    {
        return agents != null ? agents.hashCode() : 0;
    }
}
