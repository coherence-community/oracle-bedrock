/*
 * File: Networks.java
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
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.remote.options.HostName;
import com.oracle.bedrock.runtime.virtual.vagrant.VagrantPlatform;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An {@link Option} representing a collection of {@link Network}s for a {@link VagrantPlatform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Networks implements Option.Collector<Network, Networks>
{
    /**
     * The {@link Network}s being collected.
     */
    private HashMap<String, Network> networks;


    /**
     * Constructs an empty {@link Networks}.
     */
    private Networks()
    {
        this.networks = new HashMap<>();
    }


    /**
     * Constructs a new {@link Networks} based on another {@link Networks}.
     *
     * @param other the other {@link Networks}
     */
    private Networks(Networks other)
    {
        this.networks = new HashMap<>();
        this.networks.putAll(other.networks);
    }


    /**
     * Write the configuration of the networks to the specified {@link PrintWriter}, optionally
     * returning the public hostname of the network if public.
     *
     * @param writer  the {@link PrintWriter}
     * @param prefix  the prefix
     * @param padding the padding
     *
     * @return the {@link Optional} host name of the network interface (when applicable)
     */
    public Optional<HostName> write(PrintWriter writer,
                                    String      prefix,
                                    String      padding)
    {
        // initially assume there's no public host name
        Optional<HostName> result = Optional.empty();

        for (Network network : networks.values())
        {
            Optional<HostName> hostName = network.write(writer, prefix, padding);

            if (network.isPublic() && hostName.isPresent())
            {
                result = hostName;
            }
        }

        return result;
    }


    /**
     * Creates an empty {@link Networks}.
     *
     * @return an empty {@link Networks}
     */
    @Options.Default
    public static Networks none()
    {
        return new Networks();
    }


    @Override
    public Networks with(Network network)
    {
        Networks networks = new Networks(this);

        networks.networks.put(network.getId(), network);

        return networks;
    }


    @Override
    public Networks without(Network network)
    {
        if (networks.containsKey(network.getId()))
        {
            Networks networks = new Networks(this);

            networks.networks.remove(network.getId());

            return networks;
        }
        else
        {
            return this;
        }
    }


    @Override
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        return networks.values().stream().filter(network -> requiredClass.isInstance(network)).map(requiredClass::cast)
        .collect(Collectors.toList());
    }


    @Override
    public Iterator<Network> iterator()
    {
        return networks.values().iterator();
    }
}
