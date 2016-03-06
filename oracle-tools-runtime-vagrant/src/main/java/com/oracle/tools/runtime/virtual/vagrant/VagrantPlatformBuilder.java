/*
 * File: VagrantPlatformBuilder.java
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

package com.oracle.tools.runtime.virtual.vagrant;

import com.oracle.tools.Option;
import com.oracle.tools.runtime.PlatformBuilder;
import com.oracle.tools.runtime.PlatformSchema;

/**
 * An implementation of a {@link PlatformBuilder} that will build {@link VagrantPlatform}
 * instances that represent a virtual machine managed by Vagrant.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantPlatformBuilder<P extends VagrantPlatform> implements PlatformBuilder<P>
{
    /** The singleton instance of the {@link VagrantPlatformBuilder} */
    public static final VagrantPlatformBuilder<VagrantPlatform> INSTANCE = new VagrantPlatformBuilder<>();


    /**
     * Construct a new {@link VagrantPlatformBuilder}
     */
    protected VagrantPlatformBuilder()
    {
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends P, S extends PlatformSchema<T>> T realize(String name, S platformSchema, Option... options)
    {
        AbstractVagrantPlatformSchema schema   = (AbstractVagrantPlatformSchema) platformSchema;
        VagrantPlatform               platform = schema.realize(name, options);

        platform.start();

        return (T) platform;
    }
}
