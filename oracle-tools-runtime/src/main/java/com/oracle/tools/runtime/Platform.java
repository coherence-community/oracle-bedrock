/*
 * File: Platform.java
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

import java.net.InetAddress;

/**
 * Provides a means to represent a platform at runtime, a server, machine or
 * operating system on which {@link Application}s may be running, managed or deployed.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public interface Platform
{
    /**
     * Obtain the name of this {@link Platform}.
     *
     * @return the name of this {@link Platform}
     */
    public String getName();


    /**
     * Obtains the {@link InetAddress} that could <strong>feasibly</strong>
     * be used by {@link Application}s running on other {@link Platform}s to
     * connect to {@link Application}s running on this {@link Platform}.
     * <p/>
     * There is no guarantee that the {@link InetAddress} returned by this
     * method is actually reachable by other {@link Platform}s.
     * <p/>
     * In some cases it may not be possible to determine an {@link InetAddress}
     * of the {@link Platform}, in which case the {@link InetAddress#getLoopbackAddress()}
     * will be returned.  When this happens the {@link Platform} is considered
     * isolated to a single host; it may only be contacted by other {@link Platform}s
     * running on the same host.
     * <p/>
     * Should a specific {@link InetAddress} be returned, applications
     * can define the "oracletools.runtime.address" system-property.
     *
     * @return the {@link InetAddress} of the {@link Platform}
     */
    public InetAddress getAddress();


    /**
     * Obtains the {@link Options} configured for the {@link Platform}.
     * <p>
     * <strong>Changes to the {@link Options} may not be recognized
     * or used by the {@link Platform} after it was created.</strong>
     *
     * @return the {@link Options}
     *
     * @see #realize(String, ApplicationSchema, ApplicationConsole, Option...)
     */
    public Options getOptions();


    /**
     * Realizes an instance of an {@link Application}.
     *
     * @param applicationName    the name of the application
     * @param applicationSchema  the {@link ApplicationSchema} to use for realizing the {@link Application}
     * @param console            the {@link ApplicationConsole} that will be used for I/O by the
     *                           realized {@link Application}. This may be <code>null</code> if not required
     * @param options            the custom {@link Option}s to be used when realizing the {@link Application},
     *                           overriding those defined by the {@link ApplicationSchema} and the
     *                           {@link Platform} itself.
     *
     * @return an {@link Application} representing the realized application
     *
     * @throws RuntimeException when a problem occurs while starting the application
     */
    public <A extends Application, S extends ApplicationSchema<A>> A realize(String             applicationName,
                                                                             S                  applicationSchema,
                                                                             ApplicationConsole console,
                                                                             Option...          options);
}
