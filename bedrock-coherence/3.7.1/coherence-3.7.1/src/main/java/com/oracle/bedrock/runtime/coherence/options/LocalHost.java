/*
 * File: LocalHost.java
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

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.util.Capture;
import com.oracle.bedrock.util.PerpetualIterator;

import java.net.InetAddress;
import java.util.Iterator;

/**
 * An {@link Option} to specify the localhost address and port of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalHost implements Profile, Option
{
    /**
     * The tangosol.coherence.localhost property.
     */
    public static final String PROPERTY = "tangosol.coherence.localhost";

    /**
     * The tangosol.coherence.localport property.
     */
    public static final String PROPERTY_PORT = "tangosol.coherence.localport";

    /**
     * The local address of an {@link CoherenceClusterMember}.
     *
     * <code>null</code> when not defined.
     */
    private String address;

    /**
     * The local address port for a {@link CoherenceClusterMember}.
     *
     * <code>null</code> when not defined.
     */
    private Iterator<Integer> ports;


    /**
     * Constructs a {@link LocalHost} for the specified address and ports.
     * <p>
     * When both the address and ports are <code>null</code>, the {@link LocalHost}
     * will be configured for "local only mode".
     * </p>
     *
     * @param address  the address of the {@link LocalHost}
     * @param ports    the possible ports for the {@link LocalHost}
     */
    private LocalHost(String            address,
                      Iterator<Integer> ports)
    {
        this.address = address;
        this.ports   = ports;
    }


    /**
     * Obtains the address of the {@link LocalHost}, <code>null</code> if not defined
     *
     * @return the address of the {@link LocalHost}
     */
    public String getAddress()
    {
        return address;
    }


    /**
     * Obtains the possible ports of the {@link LocalHost}, <code>null</code>  if not defined.
     *
     * @return the possible ports of the {@link LocalHost}
     */
    public Iterator<Integer> getPorts()
    {
        return ports;
    }


    /**
     * Obtains a {@link LocalHost} for a specified address.
     *
     * @param address the address of the {@link LocalHost}
     *
     * @return a {@link LocalHost} for the specified address
     */
    public static LocalHost of(String address)
    {
        return new LocalHost(address, null);
    }


    /**
     * Obtains a {@link LocalHost} for a specified address and port.
     *
     * @param address the address of the {@link LocalHost}
     * @param port    the port of the {@link LocalHost}
     *
     * @return a {@link LocalHost} for the specified address and port
     */
    public static LocalHost of(String address,
                               int    port)
    {
        return new LocalHost(address, new PerpetualIterator<>(port));
    }


    /**
     * Obtains a {@link LocalHost} for a specified address and port.
     *
     * @param address the address of the {@link LocalHost}
     * @param port    the port of the {@link LocalHost}
     *
     * @return a {@link LocalHost} for the specified address and port
     */
    public static LocalHost of(String           address,
                               Capture<Integer> port)
    {
        return new LocalHost(address, port);
    }


    /**
     * Obtains a {@link LocalHost} for a specified address and ports.
     *
     * @param address the address of the {@link LocalHost}
     * @param ports   the ports of the {@link LocalHost}
     *
     * @return a {@link LocalHost} for the specified address and ports
     */
    public static LocalHost of(String            address,
                               Iterator<Integer> ports)
    {
        return new LocalHost(address, ports);
    }


    /**
     * Obtains a {@link LocalHost} for a specified address and ports.
     *
     * @param address the address of the {@link LocalHost}
     * @param ports   the ports of the {@link LocalHost}
     *
     * @return a {@link LocalHost} for the specified address and ports
     */
    public static LocalHost of(String                address,
                               AvailablePortIterator ports)
    {
        return new LocalHost(address, ports);
    }


    /**
     * Obtains a {@link LocalHost} configured for "local host only" mode.
     *
     * @return a {@link LocalHost} for local-only mode
     */
    public static LocalHost only()
    {
        return new LocalHost(null, null);
    }


    @Override
    public void onLaunching(Platform platform,
                            MetaClass metaClass,
                            Options   options)
    {
        if (ports != null &&!ports.hasNext())
        {
            throw new IllegalStateException("Exhausted the available ports for the LocalHost");
        }
        else
        {
            SystemProperties systemProperties = options.get(SystemProperties.class);

            if (systemProperties != null)
            {
                if (address == null && ports == null)
                {
                    // setup local-only mode
                    options.add(SystemProperty.of(PROPERTY, InetAddress.getLoopbackAddress().getHostAddress()));

                    // set TTL to 0
                    options.add(SystemProperty.of("tangosol.coherence.ttl", "0"));
                }
                else
                {
                    if (address != null)
                    {
                        options.add(SystemProperty.of(PROPERTY, address));
                    }

                    if (ports != null)
                    {
                        options.add(SystemProperty.of(PROPERTY_PORT, ports.next()));
                    }
                }
            }
        }
    }


    @Override
    public void onLaunched(Platform    platform,
                           Application application,
                           Options     options)
    {
    }


    @Override
    public void onClosing(Platform    platform,
                          Application application,
                          Options     options)
    {
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof LocalHost))
        {
            return false;
        }

        LocalHost that = (LocalHost) o;

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
