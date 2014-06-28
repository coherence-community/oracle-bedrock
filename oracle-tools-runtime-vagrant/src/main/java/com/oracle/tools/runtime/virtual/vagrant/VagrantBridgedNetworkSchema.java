/*
 * File: VagrantBridgedNetworkSchema.java
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

/**
 * A {@link VagrantNetworkSchema} that represents a Bridged Network in a Vagrant VM
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class VagrantBridgedNetworkSchema extends VagrantNetworkSchema
{
    /** The name of the host NIC to use for the bridge */
    private String bridgeDevice;

    /**
     * Construct a {@link VagrantBridgedNetworkSchema} with the specified id.
     *
     * @param sId  the ID of this schema
     */
    protected VagrantBridgedNetworkSchema(String sId)
    {
        super(sId, null, false);
    }


    /**
     * Obtain the name of the host network interface to use for the bridge
     *
     * @return the name of the host network interface to use for the bridge
     */
    public String getBridgeInterface()
    {
        return bridgeDevice;
    }


    /**
     * Set the name of the host network interface to use for the bridge
     *
     * @param bridgeDevice the name of the host network interface to use for the bridge
     *
     * @return this {@link VagrantBridgedNetworkSchema} for method chaining
     */
    public VagrantBridgedNetworkSchema setBridgeInterface(String bridgeDevice)
    {
        this.bridgeDevice = bridgeDevice;

        return this;
    }


    /**
     * Write the configuration of the network to the vagrantFile
     *
     * @param writer   the {@link PrintWriter} being used to write the vagrantFile
     * @param prefix  the prefix of the VM name
     * @param padding     the padding to prefix to any lines written to the vagrantFile
     */
    public String realize(PrintWriter writer,
                        String prefix,
                        String padding)
    {
        writer.printf("%s    %s.vm.network ", padding, prefix);

        writer.print("\"public_network\"");

        if (bridgeDevice != null &&!bridgeDevice.isEmpty())
        {
            writer.printf(", bridge: %s", bridgeDevice);
        }

        String sMacAddress = getMacAddress();

        if (sMacAddress != null &&!sMacAddress.isEmpty())
        {
            writer.printf(", mac: \"%s\"", sMacAddress);
        }

        writer.println();

        return null;
    }
}
