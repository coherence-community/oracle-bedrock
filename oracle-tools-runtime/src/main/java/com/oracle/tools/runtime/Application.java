/*
 * File: Application.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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
 * An {@link Application} provides a mechanism to represent, access and control
 * an executing application process, typically managed by an operating system.
 *
 * @param <A> the concrete type of {@link Application}
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Application<A>
{
    /**
     * The {@link Application} {@link EventKind}s.
     */
    public static enum EventKind {REALIZED,
                                  DESTROYED}


    ;

    /**
     * Obtains the environment variables that were supplied to the
     * {@link Application} when it was realized.
     *
     * @return {@link Properties} of containing name value pairs, each one
     *         representing an environment variable provided to the
     *         {@link Application} when it was realized
     */
    public Properties getEnvironmentVariables();


    /**
     * Obtains the name of the {@link Application}.
     *
     * @return The name of the {@link Application}
     */
    public String getName();


    /**
     * Terminates and destroys the running {@link Application}.  Upon returning
     * from this method you can safely assume the {@link Application} is no longer
     * running.
     *
     * @return the exit value of the {@link Application}
     *         (by convention <code>0</code> indicates normal termination)
     */
    public int destroy();


    /**
     * Causes the calling {@link Thread} to block and wait until the
     * {@link Application} has terminated, either naturally or due to a call
     * to {@link #destroy()} by another {@link Thread}.
     * <p>
     * This method returns immediately if the {@link Application} has already
     * been terminated.
     *
     * @return the exit value of the {@link Application}
     *         (by convention <code>0</code> indicates normal termination)
     *
     * @throws InterruptedException if the calling {@link Thread} is
     *                              {@linkplain Thread#interrupt() interrupted}
     *                              by another thread while it is waiting.
     */
    public int waitFor() throws InterruptedException;


    /**
     * Obtains the exit value for the terminated {@link Application}.
     *
     * @return the exit value of the {@link Application}
     *
     * @throws IllegalThreadStateException if the {@link Application} has not
     *                                     yet terminated
     */
    public int exitValue();


    /**
     * Obtain the operating system process id for the {@link Application}.
     *
     * @return The process id or -1 if it can't be determined.
     */
    public long getPid();


    /**
     * Obtains the default duration to used by the {@link Application}
     * for timeout values that have not been specified.
     *
     * @return the timeout duration (measured in {@link #getTimeoutUnits()})
     */
    public long getDefaultTimeout();


    /**
     * Obtains the default duration timeout {@link TimeUnit}.
     *
     * @return the {@link TimeUnit} for default {@link Application} timeouts
     */
    public TimeUnit getDefaultTimeoutUnits();


    /**
     * Obtains the {@link LifecycleEventInterceptor}s for the {@link Application}.
     *
     * @return  the {@link LifecycleEventInterceptor}s
     */
    public Iterable<LifecycleEventInterceptor<A>> getLifecycleInterceptors();
}
