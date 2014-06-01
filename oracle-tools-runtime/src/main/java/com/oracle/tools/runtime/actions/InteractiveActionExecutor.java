/*
 * File: InteractiveActionExecutor.java
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
import com.oracle.tools.runtime.ApplicationGroup;
import com.oracle.tools.runtime.Assembly;

import java.io.IOException;

import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

/**
 * An {@link ActionExecutor} that executes {@link Action}(s) in an interactive,
 * one {@link Action} at-a-time manner.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class InteractiveActionExecutor<A extends Application, G extends Assembly<A>> implements ActionExecutor<A, G>
{
    /**
     * The {@link Assembly} being controlled by the {@link ActionExecutor}.
     */
    private G assembly;

    /**
     * The initial {@link Action} provided to the {@link ActionExecutor}.
     */
    private Action<A, G> initialAction;

    /**
     * The {@link Stack} of {@link Action}s to execute).
     */
    private Stack<Iterator<Action<A, G>>> actionsStack;


    /**
     * Constructs an {@link InteractiveActionExecutor}.
     *
     * @param assembly  the {@link Assembly} on which to execute the {@link Sequence}
     * @param action    the {@link Action} to execute
     */
    public InteractiveActionExecutor(G            assembly,
                                     Action<A, G> action)
    {
        this.assembly      = assembly;
        this.initialAction = action;

        // now start the action
        restart();
    }


    /**
     * Restarts the {@link InteractiveActionExecutor} execution for the
     * {@link Assembly}, starting again with the initially provided
     * {@link Action}.
     */
    public void restart()
    {
        // create a new stack of actions to execute
        actionsStack = new Stack<Iterator<Action<A, G>>>();

        // we can only execute non-null actions
        if (initialAction != null)
        {
            actionsStack.push(Collections.singletonList(initialAction).iterator());
        }
    }


    /**
     * Executes the next {@link Action}.
     *
     * @return <code>true</code> there are more {@link Action}s to execute,
     *         <code>false</code> if there are no more {@link Action}s to execute
     */
    public boolean executeNext()
    {
        // assume we haven't executed an action
        boolean hasExecutedAnAction = false;

        while (!actionsStack.isEmpty() &&!hasExecutedAnAction)
        {
            // get the next iterator of actions to execute from the stack
            Iterator<Action<A, G>> actions = actionsStack.pop();

            // get the next action to execute
            Action<A, G> action = actions.next();

            // place the actions back on the stack (if it still contains actions)
            if (actions.hasNext())
            {
                actionsStack.push(actions);
            }

            if ((action instanceof ConditionalAction && ((ConditionalAction) action).getPredicate().evaluate(assembly))
                ||!(action instanceof ConditionalAction))
            {
                if (action instanceof CustomAction)
                {
                    CustomAction<A, G> customAction = (CustomAction) action;

                    customAction.perform(assembly);

                    hasExecutedAnAction = true;
                }
                else if (action instanceof Sequence)
                {
                    Sequence<A, G> sequence = (Sequence) action;

                    actions = sequence.getActions();

                    if (actions != null && actions.hasNext())
                    {
                        actionsStack.push(actions);
                    }
                }
            }
        }

        return !actionsStack.isEmpty();
    }


    /**
     * Executes all of the remaining {@link Action}s.
     */
    public void executeAll()
    {
        while (executeNext());
    }


    @Override
    public G getAssembly()
    {
        return assembly;
    }


    @Override
    public void close() throws IOException
    {
        // we simply clear the stack when closing
        actionsStack.clear();
    }
}
