/*
 * File: BridgedNetwork.java
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
import java.util.Optional;

/**
 * An {@link Option} to specify a {@link BridgedNetwork} for a {@link VagrantPlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class BridgedNetwork extends AbstractNetwork
{
    /**
     * The network interface that will be used for the {@link BridgedNetwork}.
     */
    private String bridgeInterface;


    /**
     * Constructs a {@link BridgedNetwork}.
     *
     * @param id               the network identifier
     * @param macAddress       the mac address
     * @param bridgeInterface  the bridge interface
     */
    public BridgedNetwork(String id,
                          String macAddress,
                          String bridgeInterface)
    {
        super(id, macAddress);

        this.bridgeInterface = bridgeInterface;
    }


    @Override
    public boolean isPublic()
    {
        return false;
    }


    /**
     * Obtains the {@link BridgedNetwork} bridge interface.
     *
     * @return
     */
    public String getBridgeInterface()
    {
        return bridgeInterface;
    }


    @Override
    public Optional<HostName> write(PrintWriter writer,
                                    String      prefix,
                                    String      padding)
    {
        writer.printf("%s    %s.vm.network ", padding, prefix);

        writer.print("\"public_network\"");

        if (bridgeInterface != null &&!bridgeInterface.isEmpty())
        {
            writer.printf(", bridge: %s", bridgeInterface);
        }

        String sMacAddress = getMacAddress();

        if (sMacAddress != null &&!sMacAddress.isEmpty())
        {
            writer.printf(", mac: \"%s\"", sMacAddress);
        }

        writer.println();

        return Optional.empty();
    }
}
