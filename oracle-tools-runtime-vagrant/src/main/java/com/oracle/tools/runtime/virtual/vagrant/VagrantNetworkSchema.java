/*
 * File: VagrantNetworkSchema.java
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
 * A definition of a network adapter in Vagrant.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class VagrantNetworkSchema
{
    /** The identifier for the network interface */
    private String id;

    /** The IP address of the network interface on the VM */
    private Iterator<String> addresses;

    /** The MAC address for the network interface */
    private String macAddress;

    /** Flag indicating whether this network interface is visible outside of the VM */
    private boolean isPublic = false;

    /**
     * Construct a new VagrantNetworkSchema
     *  @param id        the identifier for the network interface
     * @param addresses  the optional IP address of the network interface
     * @param isPublic   is this network interface visible outside of the VM
     */
    protected VagrantNetworkSchema(String id, Iterator<String> addresses, boolean isPublic)
    {
        this.id        = id;
        this.addresses = addresses;
        this.isPublic  = isPublic;
    }


    /**
     * Obtain the network interface identifier
     *
     * @return the network interface identifier
     */
    public String getId()
    {
        return id;
    }


    /**
     * Return true if this network interface is visible
     * outside of the VM.
     *
     * @return true if this network interface is visible
     * outside of the VM.
     */
    public boolean isPublic()
    {
        return isPublic;
    }


    /**
     * Obtain the next IP address for the network interface
     *
     * @return the next IP address for the network interface
     */
    public String getNextAddress()
    {
        return addresses != null && addresses.hasNext() ? addresses.next() : null;
    }


    /**
     * Obtain the MAC address of the network interface
     *
     * @return the MAC address of the network interface
     */
    public String getMacAddress()
    {
        return macAddress;
    }


    /**
     * Set the MAC address of the network interface
     *
     * @param macAddress  the MAC address of the network interface
     *
     * @return this {@link VagrantNetworkSchema} for method chaining
     */
    public VagrantNetworkSchema setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;

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
    public abstract String realize(PrintWriter writer,
                                 String prefix,
                                 String padding);
}
