/*
 * File: CoherenceCacheServer.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LifecycleEventInterceptor;

import com.oracle.tools.runtime.java.JavaProcess;

import java.util.Properties;

import java.util.concurrent.TimeUnit;

/**
 * A runtime representation of a {@link CoherenceCacheServer}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CoherenceCacheServer extends AbstractCoherenceClusterMember<CoherenceCacheServer>
{
    /**
     * Construct a {@link CoherenceCacheServer}.
     *
     * @param process               the {@link Process} representing the {@link CoherenceCacheServer}
     * @param name                  the name of the {@link CoherenceCacheServer}
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the
     *                              realized {@link CoherenceCacheServer}. This may be <code>null</code> if not required
     * @param environmentVariables  the environment variables used when starting the {@link CoherenceCacheServer}
     * @param systemProperties      the system properties provided to the {@link CoherenceCacheServer}
     * @param isDiagnosticsEnabled  should diagnostic information be logged/output
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     */
    CoherenceCacheServer(JavaProcess                                                       process,
                         String                                                            name,
                         ApplicationConsole                                                console,
                         Properties                                                        environmentVariables,
                         Properties                                                        systemProperties,
                         boolean                                                           isDiagnosticsEnabled,
                         long                                                              defaultTimeout,
                         TimeUnit                                                          defaultTimeoutUnits,
                         Iterable<LifecycleEventInterceptor<? super CoherenceCacheServer>> interceptors)
    {
        super(process,
              name,
              console,
              environmentVariables,
              systemProperties,
              isDiagnosticsEnabled,
              defaultTimeout,
              defaultTimeoutUnits,
              interceptors);
    }
}
