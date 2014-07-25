/*
 * File: SimpleRemoteApplicationBuilder.java
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

package com.oracle.tools.runtime.remote;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.FluentApplicationSchema;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationRuntime;
import com.oracle.tools.runtime.SimpleApplicationSchema;

/**
 * A simple implementation of a {@link RemoteApplicationBuilder}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleRemoteApplicationBuilder
    extends AbstractRemoteApplicationBuilder<SimpleApplication, SimpleRemoteApplicationEnvironment,
                                             SimpleRemoteApplicationBuilder>
{
    /**
     * Constructs a {@link SimpleRemoteApplicationBuilder} (using the default port).
     *
     * @param hostName        the remote host name
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public SimpleRemoteApplicationBuilder(String         hostName,
                                          String         userName,
                                          Authentication authentication)
    {
        this(hostName, DEFAULT_PORT, userName, authentication);
    }


    /**
     * Constructs a {@link SimpleRemoteApplicationBuilder}.
     *
     * @param hostName        the remote host name
     * @param port            the remote port
     * @param userName        the user name on the remote host
     * @param authentication  the {@link Authentication} for connecting to the host
     */
    public SimpleRemoteApplicationBuilder(String         hostName,
                                          int            port,
                                          String         userName,
                                          Authentication authentication)
    {
        super(hostName, port, userName, authentication);
    }


    @Override
    protected <T extends SimpleApplication,
        S extends ApplicationSchema<T>> SimpleRemoteApplicationEnvironment getRemoteApplicationEnvironment(S applicationSchema,
        Platform                                                                                             platform)
    {
        SimpleApplicationSchema schema = (SimpleApplicationSchema) applicationSchema;

        return new SimpleRemoteApplicationEnvironment(schema, platform);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected <T extends SimpleApplication, S extends ApplicationSchema<T>> T createApplication(Platform                           platform,
                                                                                                S                                  schema,
                                                                                                SimpleRemoteApplicationEnvironment environment,
                                                                                                String                             applicationName,
                                                                                                RemoteApplicationProcess           process,
                                                                                                ApplicationConsole                 console)
    {
        SimpleApplicationRuntime runtime = new SimpleApplicationRuntime(applicationName,
                                                                        platform,
                                                                        process,
                                                                        console,
                                                                        environment.getRemoteEnvironmentVariables(),
                                                                        schema.isDiagnosticsEnabled(),
                                                                        schema.getDefaultTimeout(),
                                                                        schema.getDefaultTimeoutUnits());

        return (T) new SimpleApplication(runtime,
                                         schema instanceof FluentApplicationSchema
                                         ? ((FluentApplicationSchema<SimpleApplication,
                                                                     ?>) schema).getLifecycleInterceptors() : null);
    }
}
