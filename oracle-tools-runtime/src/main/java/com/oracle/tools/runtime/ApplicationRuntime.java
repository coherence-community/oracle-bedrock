/*
 * File: ApplicationRuntime.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import java.util.Properties;

import java.util.concurrent.TimeUnit;

/**
 * An <strong>internal</strong> interface providing important information
 * concerning the runtime of {@link Application}.
 * <p>
 * Each {@link Application} has an instance of an {@link ApplicationRuntime}
 * allowing the {@link Application} to access and interact with the environment
 * in which it is running.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <P>  the type of {@link ApplicationProcess} used to represent
 *             the underlying {@link Application} at runtime
 */
public interface ApplicationRuntime<P extends ApplicationProcess>
{
    /**
     * Obtains the name of the {@link Application}.  This is used
     * for logging, diagnostic and runtime reporting purposes.
     *
     * @return  the display name of the {@link Application}
     */
    public String getApplicationName();


    /**
     * Obtains the {@link Platform} on which the {@link Application} is running.
     *
     * @return  the {@link Platform}
     */
    public Platform getPlatform();


    /**
     * Obtains the {@link Option}s that were specified when the {@link Application}
     * was realized.
     *
     * @return  the {@link Options}
     */
    public Options getOptions();


    /**
     * Obtains the {@link ApplicationProcess} representing the {@link Application}
     * running on the {@link Platform}.
     *
     * @return  the {@link ApplicationProcess}
     */
    public P getApplicationProcess();


    /**
     * Obtains the {@link ApplicationConsole} that is being used by the
     * {@link Application} to manage stdin, stdout and stderr.
     *
     * @return  the {@link ApplicationConsole}
     */
    public ApplicationConsole getApplicationConsole();


    /**
     * Obtains the operating system environment variables established
     * when the {@link Application} was started.
     *
     * @return  the operating system environment variables
     */
    public Properties getEnvironmentVariables();
}
