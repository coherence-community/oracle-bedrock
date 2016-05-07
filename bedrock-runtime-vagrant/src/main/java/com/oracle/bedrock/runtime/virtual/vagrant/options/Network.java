/*
 * File: Network.java
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

package com.oracle.bedrock.runtime.virtual.vagrant.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.remote.options.HostName;
import com.oracle.bedrock.runtime.virtual.vagrant.VagrantPlatform;

import java.io.PrintWriter;
import java.util.Optional;

/**
 * An {@link Option} representing a {@link Network} for a {@link VagrantPlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Network extends Option.Collectable
{
    /**
     * Obtain the identifier for the {@link Network}.
     * <p>
     * For example: "eth1"
     *
     * @return  the network identifier
     */
    String getId();


    /**
     * Determines if the {@link Network} is visible outside of the Virtual Machine
     * represented by the {@link VagrantPlatform}.
     *
     * @return  <code>true</code> if the {@link Network} is visible outside fo the
     *          {@link VagrantPlatform}, <code>false</code> otherwise
     */
    boolean isPublic();


    /**
     * Obtains the mac address for the {@link Network}.
     *
     * @return  the mac address
     */
    String getMacAddress();


    /**
     * Write the configuration of the network to the specified {@link PrintWriter}, optionally
     * returning the public hostname of the network if public.
     *
     * @param writer   the {@link PrintWriter}
     * @param prefix   the prefix
     * @param padding  the padding
     *
     * @return the {@link Optional} {@link HostName} of the network interface (when applicable)
     */
    Optional<HostName> write(PrintWriter writer,
                             String      prefix,
                             String      padding);


    @Override
    default Class<? extends Collector> getCollectorClass()
    {
        return Networks.class;
    }
}
