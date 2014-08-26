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
     * Obtain the private InetAddress for this {@link Platform}, this is
     * the address that will be used internally by Oracle Tools to
     * open connections to this {@link Platform}.
     *
     * @return the private InetAddress for this {@link Platform}.
     */
    public InetAddress getPrivateInetAddress();


    /**
     * Obtain the public InetAddress for this {@link Platform}, this is
     * the address that is visible to the outside world should be used
     * to open connections to this {@link Platform}.
     *
     * @return the public InetAddress for this {@link Platform}.
     */
    public InetAddress getPublicInetAddress();


    /**
     * Realizes an instance of an {@link Application}.
     *
     * @param applicationName    the name of the application
     * @param applicationSchema  the {@link ApplicationSchema} to use for realizing the {@link Application}
     * @param console            the {@link ApplicationConsole} that will be used for I/O by the
     *                           realized {@link Application}. This may be <code>null</code> if not required
     * @param options            the {@link Platform} specific {@link Option}s to be used when realizing the
     *                           {@link Application}
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
