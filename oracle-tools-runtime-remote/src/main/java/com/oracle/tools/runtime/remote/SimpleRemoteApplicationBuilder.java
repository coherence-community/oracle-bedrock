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

import com.oracle.tools.Options;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.FluentApplicationSchema;
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
     * Constructs a {@link SimpleRemoteApplicationBuilder} for the specified
     * {@link RemotePlatform}.
     *
     * @param platform  the {@link RemotePlatform}
     */
    public SimpleRemoteApplicationBuilder(RemotePlatform platform)
    {
        super(platform);
    }


    @Override
    protected <T extends SimpleApplication,
        S extends ApplicationSchema<T>> SimpleRemoteApplicationEnvironment getRemoteApplicationEnvironment(S applicationSchema,
        Options                                                                                              options)
    {
        SimpleApplicationSchema schema = (SimpleApplicationSchema) applicationSchema;

        return new SimpleRemoteApplicationEnvironment(schema, platform, options);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected <T extends SimpleApplication, S extends ApplicationSchema<T>> T createApplication(Options                            options,
                                                                                                S                                  schema,
                                                                                                SimpleRemoteApplicationEnvironment environment,
                                                                                                String                             applicationName,
                                                                                                RemoteApplicationProcess           process,
                                                                                                ApplicationConsole                 console)
    {
        SimpleApplicationRuntime runtime = new SimpleApplicationRuntime(applicationName,
                                                                        platform,
                                                                        options,
                                                                        process,
                                                                        console,
                                                                        environment.getRemoteEnvironmentVariables());

        return (T) new SimpleApplication(runtime,
                                         schema instanceof FluentApplicationSchema
                                         ? ((FluentApplicationSchema<SimpleApplication,
                                                                     ?>) schema).getApplicationListeners() : null);
    }
}
