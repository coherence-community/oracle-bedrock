/*
 * File: SimpleJavaApplication.java
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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LifecycleEventInterceptor;
import com.oracle.tools.runtime.Platform;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A {@link SimpleJavaApplication} is concrete implementation of a {@link JavaApplication}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleJavaApplication extends AbstractJavaApplication<SimpleJavaApplication, JavaProcess>
{
    /**
     * Construct a {@link SimpleJavaApplication}.
     *
     * @param process               the {@link JavaProcess} representing the {@link SimpleJavaApplication}
     * @param name                  the name of the {@link JavaApplication}
     * @param platform              the {@link Platform} that this {@link Application} is running on
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the
     *                              realized {@link Application}. This may be <code>null</code> if not required
     * @param environmentVariables  the environment variables used when starting the {@link JavaApplication}
     * @param systemProperties      the system properties provided to the {@link JavaApplication}
     * @param isDiagnosticsEnabled  should diagnostic information be logged/output
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     * @param remoteDebuggingPort   the port this process is listening on for remote debugger connections if
     *                              enabled, or <= 0 if disabled
     */
    public SimpleJavaApplication(JavaProcess                                                        process,
                                 String                                                             name,
                                 Platform                                                           platform,
                                 ApplicationConsole                                                 console,
                                 Properties                                                         environmentVariables,
                                 Properties                                                         systemProperties,
                                 boolean                                                            isDiagnosticsEnabled,
                                 long                                                               defaultTimeout,
                                 TimeUnit                                                           defaultTimeoutUnits,
                                 Iterable<LifecycleEventInterceptor<? super SimpleJavaApplication>> interceptors,
                                 int                                                                remoteDebuggingPort)
    {
        super(process,
              name,
              platform,
              console,
              environmentVariables,
              systemProperties,
              isDiagnosticsEnabled,
              defaultTimeout,
              defaultTimeoutUnits,
              interceptors,
              remoteDebuggingPort);
    }
}
