/*
 * File: RepetitiveAction.java
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

import com.oracle.tools.util.RepetitiveIterator;

import java.util.Iterator;

/**
 * A specialized {@link Sequence} that defines an {@link Action} to be performed a number of times.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RepetitiveAction<A extends Application, G extends Assembly<A>> implements Sequence<A, G>
{
    /**
     * The {@link Action} to be performed.
     */
    private Action<A, G> action;

    /**
     * The number of times to perform the {@link Action}.
     */
    private int count;


    /**
     * Constructs a {@link RepetitiveAction} based on a single {@link Action}
     */
    public RepetitiveAction(Action<A, G> action,
                            int          count)
    {
        this.action = action;
        this.count  = count;
    }


    @Override
    public Iterator<Action<A, G>> getActions()
    {
        return new RepetitiveIterator<Action<A, G>>(action, count);
    }
}
