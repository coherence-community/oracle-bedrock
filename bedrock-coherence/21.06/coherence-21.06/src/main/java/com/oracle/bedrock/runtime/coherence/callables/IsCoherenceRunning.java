/*
 * File: IsCoherenceRunning.java
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

package com.oracle.bedrock.runtime.coherence.callables;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Coherence;
import com.tangosol.net.Service;

/**
 * A {@link RemoteCallable} to remotely determine if a {@link Coherence} instance is running
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 */
public class IsCoherenceRunning implements RemoteCallable<Boolean>
{
    /**
     * The name of the {@link Coherence} instance.
     */
    private final String name;


    /**
     * Constructs an {@link IsCoherenceRunning} for the
     * default {@link Coherence} instance.
     */
    public IsCoherenceRunning()
    {
        this(Coherence.DEFAULT_NAME);
    }


    /**
     * Constructs an {@link IsCoherenceRunning}
     *
     * @param name  the optional name of the service
     */
    public IsCoherenceRunning(String name)
    {
        this.name = name == null ? Coherence.DEFAULT_NAME : name;
    }


    @Override
    public Boolean call() throws Exception
    {
        return Coherence.getInstances()
                        .stream()
                        .filter(c -> name.equals(c.getName()))
                        .map(Coherence::isStarted)
                        .findFirst()
                        .orElse(false);
    }
}
