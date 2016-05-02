/*
 * File: HostOnlyNetwork.java
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

import com.oracle.bedrock.runtime.remote.options.HostName;
import com.oracle.bedrock.runtime.virtual.vagrant.VagrantPlatform;
import com.oracle.bedrock.Option;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Optional;

/**
 * An {@link Option} to specify a {@link HostOnlyNetwork} for a {@link VagrantPlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class HostOnlyNetwork extends AbstractNetwork
{
    /**
     * The addresses to configure for the {@link HostOnlyNetwork}.
     */
    private Iterator<String> addresses;


    /**
     * Constructs a {@link HostOnlyNetwork}.
     *
     * @param id          the network identifier
     * @param macAddress  the mac address
     * @param addresses   the addresses
     */
    public HostOnlyNetwork(String           id,
                           String           macAddress,
                           Iterator<String> addresses)
    {
        super(id, macAddress);

        this.addresses = addresses;
    }


    @Override
    public boolean isPublic()
    {
        return true;
    }


    /**
     * Write the configuration of the network to the vagrantFile
     *
     * @param writer   the {@link PrintWriter} being used to write the vagrantFile
     * @param prefix   the prefix of the VM name
     * @param padding  the padding to prefix to any lines written to the vagrantFile
     *
     * @return the {@link HostName} of the network interface just created if applicable
     */
    public Optional<HostName> write(PrintWriter writer,
                                    String      prefix,
                                    String      padding)
    {
        writer.printf("%s    %s.vm.network ", padding, prefix);

        writer.print("'private_network'");

        String address = addresses == null ? null : addresses.hasNext() ? addresses.next() : null;

        if (address == null || address.isEmpty())
        {
            writer.print(", type: 'dhcp'");
        }
        else
        {
            writer.printf(", ip: '%s'", address);
        }

        boolean autoConfig = true;

        writer.printf(", auto_config: %s", autoConfig);

        String sMacAddress = getMacAddress();

        if (sMacAddress != null &&!sMacAddress.isEmpty())
        {
            writer.printf(", mac: '%s'", sMacAddress);
        }

        writer.println();

        return address == null ? Optional.empty() : Optional.of(HostName.of(address));
    }
}
