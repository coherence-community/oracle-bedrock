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
     * Obtain the host name for this {@link Platform}.
     *
     * @return the host name for this {@link Platform}.
     */
    public String getHostname();

    /**
     * Obtains a suitable {@link ApplicationBuilder} for a specific class of {@link Application}.
     *
     * @param <A>  the type of {@link Application}
     * @param <B>  the type of {@link ApplicationBuilder}
     *
     * @param applicationClass  the {@link Class} of {@link Application} for which a {@link ApplicationBuilder}
     *                          is required
     * @return  the {@link ApplicationBuilder} or null if this {@link Platform} cannot supply a builder for
     *          the specified {@link Application} {@link Class}
     */
    public <A extends Application, B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass);
}
