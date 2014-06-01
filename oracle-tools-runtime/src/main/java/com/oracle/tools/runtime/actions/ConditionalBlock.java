/*
 * File: ConditionalBlock.java
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

import com.oracle.tools.predicate.Predicate;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationGroup;
import com.oracle.tools.runtime.Assembly;

/**
 * A specialized {@link Block} that is only executed if and only if a {@link Predicate} is satisfied.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ConditionalBlock<A extends Application, G extends Assembly<A>> extends Block<A, G>
    implements ConditionalAction<A, G>
{
    /**
     * The {@link Predicate} to be satisfied.
     */
    private Predicate<G> predicate;


    /**
     * Constructs a {@link ConditionalBlock}.
     *
     * @param predicate  the {@link Predicate} to be satisfied
     */
    public ConditionalBlock(Predicate<G> predicate)
    {
        super();

        this.predicate = predicate;
    }


    @Override
    public Predicate<G> getPredicate()
    {
        return predicate;
    }
}
