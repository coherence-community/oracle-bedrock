/*
 * File: LocalPlatformSchema.java
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
 * A {@link PlatformSchema} encapsulating configuration and operational settings that a
 * {@link com.oracle.tools.runtime.PlatformBuilder} will use to build a
 * {@link LocalPlatform} platform.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class LocalPlatformSchema implements PlatformSchema<LocalPlatform>
{
    /**
     * The singleton instance of the {@link LocalPlatformSchema}.
     */
    public static LocalPlatformSchema INSTANCE = new LocalPlatformSchema();


    /**
     * This constructor is private as there is no configuration required for the
     * {@link com.oracle.tools.runtime.java.JavaVirtualMachine} platform, which is also a singleton.
     */
    private LocalPlatformSchema()
    {
    }


    @Override
    public String getName()
    {
        return LocalPlatform.get().getName();
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
