/*
 * File: AbstractApplicationRuntime.java
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

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import java.util.Properties;

import java.util.concurrent.TimeUnit;

/**
 * An abstract internal implementation of an {@link ApplicationRuntime}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <P>  the type of {@link ApplicationProcess} used to internally represent
 *             the underlying {@link Application} at runtime
 */
public class AbstractApplicationRuntime<P extends ApplicationProcess> implements ApplicationRuntime<P>
{
    /**
     * The name of the {@link Application}.
     */
    private String applicationName;

    /**
     * The {@link Platform} on which the {@link Application} is being run.
     */
    private final Platform platform;

    /**
     * The {@link ApplicationProcess} representing the running {@link Application}
     * on the {@link Platform}.
     */
    private final P process;

    /**
     * The {@link ApplicationConsole} for an {@link Application}.
     */
    private final ApplicationConsole console;

    /**
     * A flag indicating if diagnostics is enabled for the {@link Application},
     * especially the {@link Application} lifecycle.
     */
    private boolean diagnosticsEnabled;

    /**
     * The operating system environment variables established for the {@link Application}.
     */
    private Properties environmentVariables;

    /**
     * The default timeout duration to use for the {@link Application}, specified in
     * {@link #getDefaultTimeoutUnits()}.
     */
    private long defaultTimeout;

    /**
     * The default timeout {@link TimeUnit}s.
     */
    private TimeUnit defaultTimeoutUnits;


    /**
     * Constructs an {@link AbstractApplicationRuntime}.
     *
     * @param applicationName       the name of the {@link Application}
     * @param platform              the {@link Platform} on which the {@link ApplicationProcess} is running
     * @param process               the {@link ApplicationProcess}
     * @param console               the {@link ApplicationConsole} for the {@link ApplicationProcess}
     * @param environmentVariables  the environment variables established for the {@link ApplicationProcess}
     * @param diagnosticsEnabled    should diagnostics be enabled for the {@link Application}
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}s
     */
    public AbstractApplicationRuntime(String             applicationName,
                                      Platform           platform,
                                      P                  process,
                                      ApplicationConsole console,
                                      Properties         environmentVariables,
                                      boolean            diagnosticsEnabled,
                                      long               defaultTimeout,
                                      TimeUnit           defaultTimeoutUnits)
    {
        this.applicationName      = applicationName;
        this.platform             = platform;
        this.process              = process;
        this.console              = console == null ? new SystemApplicationConsole() : console;
        this.environmentVariables = environmentVariables;
        this.diagnosticsEnabled   = Settings.isDiagnosticsEnabled(diagnosticsEnabled);
        this.defaultTimeout       = defaultTimeout;
        this.defaultTimeoutUnits  = defaultTimeoutUnits;
    }


    @Override
    public String getApplicationName()
    {
        return applicationName;
    }


    @Override
    public Platform getPlatform()
    {
        return platform;
    }


    @Override
    public P getApplicationProcess()
    {
        return process;
    }


    @Override
    public ApplicationConsole getApplicationConsole()
    {
        return console;
    }


    @Override
    public boolean isDiagnosticsEnabled()
    {
        return diagnosticsEnabled;
    }


    @Override
    public long getDefaultTimeout()
    {
        return defaultTimeout;
    }


    @Override
    public TimeUnit getDefaultTimeoutUnits()
    {
        return defaultTimeoutUnits;
    }


    @Override
    public Properties getEnvironmentVariables()
    {
        return environmentVariables;
    }
}
