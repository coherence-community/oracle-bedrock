/*
 * File: ClusterPort.java
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
import com.oracle.bedrock.runtime.*;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.java.JavaVirtualMachine;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.util.Capture;
import com.oracle.bedrock.util.PerpetualIterator;

import java.util.Iterator;

/**
 * An {@link Option} to specify one or more cluster ports for use by a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClusterPort implements Profile, Option
{
    /**
     * The tangosol.coherence.clusterport property.
     */
    public static final String PROPERTY = "coherence.clusterport";

    /**
     * The cluster port for a {@link CoherenceClusterMember}.
     * <p>
     * When <code>null</code> the cluster port will automatically be chosen
     * based on the {@link Platform}.
     */
    private Iterator<Integer> ports;


    /**
     * Constructs a {@link ClusterPort} for the specified port.
     *
     * @param ports  the ports
     */
    private ClusterPort(Iterator<Integer> ports)
    {
        this.ports = ports;
    }


    /**
     * Obtains the possible values of the {@link ClusterPort}, returning
     * <code>null</code> if it is to be automatically chosen.
     *
     * @return  the possible values of the {@link ClusterPort}
     */
    public Iterator<Integer> getPorts()
    {
        return ports;
    }


    /**
     * Obtains a {@link ClusterPort} for a specified port(s).
     *
     * @param ports  the possible ports for a {@link ClusterPort}
     *
     * @return a {@link ClusterPort} for the specified port(s)
     */
    public static ClusterPort from(Iterator<Integer> ports)
    {
        return new ClusterPort(ports);
    }


    /**
     * Obtains a {@link ClusterPort} for a specified port.
     *
     * @param port  the  port for a {@link ClusterPort}
     *
     * @return a {@link ClusterPort} for the specified port
     */

    public static ClusterPort of(int port)
    {
        return new ClusterPort(new PerpetualIterator<>(port));
    }


    /**
     * Obtains a {@link ClusterPort} for a specified port(s).
     *
     * @param ports  the possible ports for a {@link ClusterPort}
     *
     * @return a {@link ClusterPort} for the specified port(s)
     */
    public static ClusterPort from(AvailablePortIterator ports)
    {
        return new ClusterPort(ports);
    }


    /**
     * Obtains a {@link ClusterPort} that is automatically chosen at runtime.
     *
     * @return a {@link ClusterPort} that is automatically chosen
     */
    public static ClusterPort automatic()
    {
        return new ClusterPort(null);
    }


    /**
     * Obtains a {@link ClusterPort} for a specified port.
     *
     * @param port  the  port for a {@link ClusterPort}
     *
     * @return a {@link ClusterPort} for the specified port
     */
    public static ClusterPort of(Capture<Integer> port)
    {
        return new ClusterPort(port);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        if (ports != null &&!ports.hasNext())
        {
            throw new IllegalStateException("Exhausted the available ports for the ClusterPort");
        }
        else
        {
            SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

            if (systemProperties != null)
            {
                int port;

                if (ports == null)
                {
                    if (platform instanceof LocalPlatform || platform instanceof JavaVirtualMachine)
                    {
                        ports = new Capture<>(LocalPlatform.get().getAvailablePorts());
                    }
                    else
                    {
                        throw new IllegalStateException("Can't automatically determine a ClusterPort for the non-LocalPlatform, non-JavaVirtualMachine Platform :"
                                                        + platform.getName());
                    }
                }

                // now acquire a port
                port = ports.next();

                optionsByType.add(SystemProperty.of(PROPERTY, port));
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

        if (!(o instanceof ClusterPort))
        {
            return false;
        }

        ClusterPort that = (ClusterPort) o;

        return ports.equals(that.ports);

    }


    @Override
    public int hashCode()
    {
        return ports.hashCode();
    }
}
