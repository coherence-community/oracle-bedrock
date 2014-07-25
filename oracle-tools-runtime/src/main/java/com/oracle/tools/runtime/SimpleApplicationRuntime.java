/*
 * File: SimpleApplicationRuntime.java
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
 * An simple internal implementation of an {@link ApplicationRuntime}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleApplicationRuntime extends AbstractApplicationRuntime<ApplicationProcess>
{
    /**
     * Constructs a {@link SimpleApplicationRuntime}.
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
    public SimpleApplicationRuntime(String             applicationName,
                                    Platform           platform,
                                    ApplicationProcess process,
                                    ApplicationConsole console,
                                    Properties         environmentVariables,
                                    boolean            diagnosticsEnabled,
                                    long               defaultTimeout,
                                    TimeUnit           defaultTimeoutUnits)
    {
        super(applicationName,
              platform,
              process,
              console,
              environmentVariables,
              diagnosticsEnabled,
              defaultTimeout,
              defaultTimeoutUnits);
    }
}
