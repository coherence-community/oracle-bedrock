/*
 * File: PlatformBuilder.java
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
 * A {@link PlatformBuilder} is responsible the creation of {@link Platform}s based on {@link PlatformSchema}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 *
 * @param <P>  the type of the {@link Platform}s the {@link PlatformBuilder} will realize
 */
public interface PlatformBuilder<P extends Platform>
{
    /**
     * Realizes an instance of a {@link Platform}.
     *
     * @param name            the name for the {@link Platform} that will be realized
     * @param platformSchema  the {@link PlatformSchema} to use for realizing the {@link Platform}
     *
     * @return a {@link Platform} representing the platform realized by the {@link PlatformBuilder}
     *
     * @throws RuntimeException  when a problem occurs while starting the platform
     */
    public <T extends P, S extends PlatformSchema<T>> T realize(String name, S platformSchema);
}
