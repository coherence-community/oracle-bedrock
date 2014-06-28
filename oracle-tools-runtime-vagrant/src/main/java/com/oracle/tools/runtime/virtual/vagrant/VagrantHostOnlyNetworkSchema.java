/*
 * File: VagrantHostOnlyNetworkSchema.java
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

import java.io.PrintWriter;
import java.util.Iterator;

/**
 * A {@link VagrantNetworkSchema} defining a Vagrant Host-Only network.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantHostOnlyNetworkSchema extends VagrantNetworkSchema
{
    /** The subnet mask for the network interface on the VM */
    private String subNetMask;

    /** The name for the network interface on the VM */
    private String adapter;

    /** whther the network interface will automatically be enabled on the VM */
    private boolean autoConfig = true;


    /**
     * Constructs a new {@link VagrantHostOnlyNetworkSchema}
     *
     * @param id         the identifier for the network interface
     * @param addresses  an {@link Iterator} of IP address for the
     *                   network interface, one of which will be
     *                   used for the address of this schema
     */
    public VagrantHostOnlyNetworkSchema(String id,
                                        Iterator<String> addresses)
    {
        super(id, addresses, true);
    }

    /**
     * Obtain the subnet mask for the network interface
     *
     * @return the subnet mask for the network interface
     */
    public String getSubNetMask()
    {
        return subNetMask;
    }


    /**
     * Set the subnet mask for the network interface
     *
     * @param subNetMask  the subnet mask for the network interface
     *
     * @return this {@link VagrantHostOnlyNetworkSchema} for method chaining
     */
    public VagrantHostOnlyNetworkSchema setSubNetMask(String subNetMask)
    {
        this.subNetMask = subNetMask;

        return this;
    }


    /**
     * Obtain the name of the network interface.
     *
     * @return the name of the network interface
     */
    public String getAdapter()
    {
        return adapter;
    }


    /**
     * Set the name of the network interface
     *
     * @param adapter  the name of the network interface
     *
     * @return this {@link VagrantHostOnlyNetworkSchema} for method chaining
     */
    public VagrantHostOnlyNetworkSchema setAdapter(String adapter)
    {
        this.adapter = adapter;

        return this;
    }


    /**
     * Obtain the flag indicating whether the network interface is
     * automatically enabled on the VM.
     *
     * @return true if the network interface is automatically
     *         enabled on the VM
     */
    public boolean isAutoConfig()
    {
        return autoConfig;
    }


    /**
     * Set the flag indicating whether the network interface is
     * automatically enabled on the VM
     *
     * @param autoConfig  true if the network interface is automatically
     *                    enabled on the VM
     *
     * @return this {@link VagrantHostOnlyNetworkSchema} for method chaining
     */
    public VagrantHostOnlyNetworkSchema setAutoConfig(boolean autoConfig)
    {
        this.autoConfig = autoConfig;

        return this;
    }


    /**
     * Write the configuration of the network to the vagrantFile
     *
     * @param writer   the {@link PrintWriter} being used to write the vagrantFile
     * @param prefix   the prefix of the VM name
     * @param padding  the padding to prefix to any lines written to the vagrantFile
     *
     * @return the host name of the network interface just created if applicable
     */
    public String realize(PrintWriter writer,
                        String prefix,
                        String padding)
    {
        writer.printf("%s    %s.vm.network ", padding, prefix);

        writer.print("'private_network'");

        String address = getNextAddress();

        if (address == null || address.isEmpty())
        {
            writer.print(", type: 'dhcp'");
        }
        else
        {
            writer.printf(", ip: '%s'", address);
        }

        writer.printf(", auto_config: %s", autoConfig);

        String sMacAddress = getMacAddress();

        if (sMacAddress != null &&!sMacAddress.isEmpty())
        {
            writer.printf(", mac: '%s'", sMacAddress);
        }

        writer.println();

        return address;
    }
}
