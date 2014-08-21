/*
 * File: AbstractJavaApplicationRuntime.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.AbstractApplicationRuntime;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.Platform;

import java.util.Properties;

import java.util.concurrent.TimeUnit;

/**
 * An abstract internal implementation of a {@link JavaApplicationRuntime}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <P>  the type of {@link JavaApplicationProcess} used to represent
 *             the underlying {@link JavaApplication}
 */
public abstract class AbstractJavaApplicationRuntime<P extends JavaApplicationProcess>
    extends AbstractApplicationRuntime<P> implements JavaApplicationRuntime<P>
{
    /**
     * The system properties used to create the {@link JavaApplicationProcess} represented
     * by this {@link JavaApplicationRuntime}.
     */
    private Properties systemProperties;

    /**
     * The port this Java Virtual Machine is listening on for remote debug connections
     * or <= 0 if remote debugging is disabled.
     */
    private int remoteDebuggingPort;


    /**
     * Constructs an {@link AbstractJavaApplicationRuntime}.
     *
     * @param applicationName       the name of the {@link JavaApplication}
     * @param platform              the {@link Platform} on which the {@link JavaApplicationProcess} is running
     * @param options               the {@link Options} for the {@link JavaApplication}
     * @param process               the {@link JavaApplicationProcess}
     * @param console               the {@link ApplicationConsole} for the {@link JavaApplicationProcess}
     * @param environmentVariables  the environment variables established for the {@link JavaApplicationProcess}
     * @param systemProperties      the system properties for the {@link JavaApplication}
     * @param remoteDebuggingPort   the port the {@link JavaApplication} should connect back to for remote debugging
     *                              (< 0 if remote debugging is not required)
     */
    public AbstractJavaApplicationRuntime(String             applicationName,
                                          Platform           platform,
                                          Options            options,
                                          P                  process,
                                          ApplicationConsole console,
                                          Properties         environmentVariables,
                                          Properties         systemProperties,
                                          int                remoteDebuggingPort)
    {
        super(applicationName, platform, options, process, console, environmentVariables);

        this.systemProperties    = systemProperties;
        this.remoteDebuggingPort = remoteDebuggingPort;
    }


    @Override
    public Properties getSystemProperties()
    {
        return systemProperties;
    }


    @Override
    public int getRemoteDebuggingPort()
    {
        return remoteDebuggingPort;
    }
}
