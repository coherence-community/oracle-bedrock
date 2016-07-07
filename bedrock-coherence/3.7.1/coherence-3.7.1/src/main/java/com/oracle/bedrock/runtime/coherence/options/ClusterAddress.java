/*
 * File: ClusterAddress.java
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

/**
 * An {@link Option} to specify the cluster address for a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClusterAddress implements Profile, Option
{
    /**
     * The tangosol.coherence.clusteraddress property.
     */
    public static final String PROPERTY = "tangosol.coherence.clusteraddress";

    /**
     * The cluster address for a {@link CoherenceClusterMember}.
     * <p>
     * When <code>null</code> the cluster port will automatically be chosen
     * based on the {@link Platform}.
     */
    private String address;


    /**
     * Constructs a {@link ClusterAddress} using the specified address.
     *
     * @param address  the address
     */
    private ClusterAddress(String address)
    {
        this.address = address;
    }


    /**
     * Obtains a {@link ClusterAddress} for a specified address.
     *
     * @param address  the address for a {@link ClusterAddress}
     *
     * @return a {@link ClusterAddress} for the specified address
     */

    public static ClusterAddress of(String address)
    {
        return new ClusterAddress(address);
    }


    /**
     * Obtains a {@link ClusterAddress} that is automatically chosen at runtime.
     *
     * @return a {@link ClusterAddress} that is automatically chosen
     */
    @OptionsByType.Default
    public static ClusterAddress automatic()
    {
        return new ClusterAddress(null);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        if (address != null)
        {
            SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

            if (systemProperties != null)
            {
                optionsByType.add(SystemProperty.of(PROPERTY, address));
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

        if (!(o instanceof ClusterAddress))
        {
            return false;
        }

        ClusterAddress that = (ClusterAddress) o;

        return address.equals(that.address);

    }


    @Override
    public int hashCode()
    {
        return address.hashCode();
    }
}
