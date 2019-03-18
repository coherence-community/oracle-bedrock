/*
 * File: WellKnownAddress.java
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

package com.oracle.bedrock.runtime.coherence.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.util.Capture;
import com.oracle.bedrock.util.PerpetualIterator;

import java.util.Iterator;

/**
 * An {@link Option} to specify the well known address and port of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class WellKnownAddress implements Profile, Option
{
    /**
     * The tangosol.coherence.wka property.
     */
    public static final String PROPERTY = "tangosol.coherence.wka";

    /**
     * The tangosol.coherence.wka.port property.
     */
    public static final String PROPERTY_PORT = "tangosol.coherence.wka.port";

    /**
     * The well known address of an {@link CoherenceClusterMember}.
     */
    private String address;

    /**
     * The well known address port for a {@link CoherenceClusterMember}.
     */
    private Iterator<Integer> ports;


    /**
     * Constructs a {@link WellKnownAddress}.
     *
     * @param address the address
     */
    private WellKnownAddress(String            address,
                             Iterator<Integer> ports)
    {
        this.address = address;
        this.ports   = ports;
    }


    /**
     * Obtains the address of the {@link WellKnownAddress}.
     *
     * @return the address of the {@link WellKnownAddress}
     */
    public String getAddress()
    {
        return address;
    }


    /**
     * Obtains the possible ports of the {@link WellKnownAddress}.
     *
     * @return the possible ports of the {@link WellKnownAddress}
     */
    public Iterator<Integer> getPorts()
    {
        return ports;
    }


    /**
     * Obtains a {@link WellKnownAddress} for a specified address and port.
     *
     * @param address the address of the {@link WellKnownAddress}
     * @param port    the port of the {@link WellKnownAddress}
     *
     * @return a {@link WellKnownAddress} for the specified address and port
     */
    public static WellKnownAddress of(String address,
                                      int    port)
    {
        return new WellKnownAddress(address, new PerpetualIterator<>(port));
    }


    /**
     * Obtains a {@link WellKnownAddress} for a specified address and port.
     *
     * @param address the address of the {@link WellKnownAddress}
     * @param port    the port of the {@link WellKnownAddress}
     *
     * @return a {@link WellKnownAddress} for the specified address and port
     */
    public static WellKnownAddress of(String           address,
                                      Capture<Integer> port)
    {
        return new WellKnownAddress(address, port);
    }


    /**
     * Obtains a {@link WellKnownAddress} for a specified address and ports.
     *
     * @param address the address of the {@link WellKnownAddress}
     * @param ports   the ports of the {@link WellKnownAddress}
     *
     * @return a {@link WellKnownAddress} for the specified address and ports
     */
    public static WellKnownAddress of(String            address,
                                      Iterator<Integer> ports)
    {
        return new WellKnownAddress(address, ports);
    }


    /**
     * Obtains a {@link WellKnownAddress} for a specified address and ports.
     *
     * @param address the address of the {@link WellKnownAddress}
     * @param ports   the ports of the {@link WellKnownAddress}
     *
     * @return a {@link WellKnownAddress} for the specified address and ports
     */
    public static WellKnownAddress of(String                address,
                                      AvailablePortIterator ports)
    {
        return new WellKnownAddress(address, ports);
    }


    /**
     * Obtains a {@link WellKnownAddress} for a specified address.
     *
     * @param address the address of the {@link WellKnownAddress}
     *
     * @return a {@link WellKnownAddress} for the specified address
     */
    public static WellKnownAddress of(String address)
    {
        return new WellKnownAddress(address, null);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        if (ports != null && !ports.hasNext())
        {
            throw new IllegalStateException("Exhausted the available ports for the WellKnownAddress");
        }
        else
        {
            SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

            if (systemProperties != null)
            {
                optionsByType.add(SystemProperty.of(PROPERTY, address));
                if (ports != null)
                {
                    optionsByType.add(SystemProperty.of(PROPERTY_PORT, ports.next()));
                }
            }
        }
    }


    @Override
    public void onLaunched(Platform      platform,
                           Application   application,
                           OptionsByType optionsByType)
    {
    }


    @Override
    public void onClosing(Platform      platform,
                          Application   application,
                          OptionsByType optionsByType)
    {
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof WellKnownAddress))
        {
            return false;
        }

        WellKnownAddress that = (WellKnownAddress) o;

        if (address != null ? !address.equals(that.address) : that.address != null)
        {
            return false;
        }

        return ports != null ? ports.equals(that.ports) : that.ports == null;

    }


    @Override
    public int hashCode()
    {
        int result = address != null ? address.hashCode() : 0;

        result = 31 * result + (ports != null ? ports.hashCode() : 0);

        return result;
    }
}
