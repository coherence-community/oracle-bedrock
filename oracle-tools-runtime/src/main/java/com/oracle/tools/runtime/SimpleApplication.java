/*
 * File: SimpleApplication.java
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

package com.oracle.tools.runtime;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A {@link SimpleApplication} is a simple implementation of an {@link Application}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleApplication extends AbstractApplication<SimpleApplication>
{
    /**
     * Construct a {@link SimpleApplication}.
     *
     * @param process               the {@link Process} representing the
     *                              {@link Application}
     * @param name                  the name of the application
     * @param console               the {@link ApplicationConsole} that will be
     *                              used for I/O by the {@link Application}
     * @param environmentVariables  the environment variables used when
     *                              establishing the {@link Application}
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     */
    public SimpleApplication(Process                                                process,
                             String                                                 name,
                             ApplicationConsole                                     console,
                             Properties                                             environmentVariables,
                             long                                                   defaultTimeout,
                             TimeUnit                                               defaultTimeoutUnits,
                             Iterable<LifecycleEventInterceptor<SimpleApplication>> interceptors)
    {
        super(process, name, console, environmentVariables, defaultTimeout, defaultTimeoutUnits, interceptors);
    }
}
