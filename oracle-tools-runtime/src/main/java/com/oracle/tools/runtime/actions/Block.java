/*
 * File: Block.java
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

package com.oracle.tools.runtime.actions;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Assembly;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A {@link Block} is a simple implementation of a {@link Sequence}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Block<A extends Application, G extends Assembly<A>> implements Sequence<A, G>
{
    /**
     * The {@link Action}s to be performed.
     */
    private LinkedList<Action<A, G>> actions;


    /**
     * Constructs an empty {@link Block}.
     */
    public Block()
    {
        this.actions = new LinkedList<Action<A, G>>();
    }


    /**
     * Constructs a {@link Block} of {@link Action}s.
     *
     * @param actions  the {@link Action}s for the {@link Block}
     */
    public Block(Action<A, G>... actions)
    {
        this.actions = new LinkedList<Action<A, G>>();

        if (actions != null)
        {
            for (Action<A, G> action : actions)
            {
                this.actions.add(action);
            }
        }
    }


    /**
     * Constructs a {@link Block} based on the {@link Action}s
     * in another {@link Block}.
     */
    public Block(Block<A, G> block)
    {
        this.actions = new LinkedList<Action<A, G>>(block.actions);
    }


    /**
     * Adds an {@link Action} to the end of the {@link Block}.
     *
     * @param action  the {@link Action} to add
     *
     * @return  the {@link Block} to allow for fluent-style method calls
     */
    public Block<A, G> add(Action<A, G> action)
    {
        actions.add(action);

        return this;
    }


    @Override
    public Iterator<Action<A, G>> getActions()
    {
        return actions.iterator();
    }
}
