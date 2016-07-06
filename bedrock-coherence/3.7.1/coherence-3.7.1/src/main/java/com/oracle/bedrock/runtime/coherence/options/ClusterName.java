/*
 * File: ClusterName.java
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

import java.util.UUID;

/**
 * An {@link Option} to specify the cluster name of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClusterName implements Profile, Option
{
    /**
     * The tangosol.coherence.cluster property.
     */
    public static final String PROPERTY = "tangosol.coherence.cluster";

    /**
     * The cluster name of an {@link CoherenceClusterMember}.
     */
    private String name;


    /**
     * Constructs a {@link ClusterName} for the specified name.
     *
     * @param name  the name
     */
    private ClusterName(String name)
    {
        if (name == null)
        {
            this.name = UUID.randomUUID().toString();
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Obtains the name of the {@link ClusterName}.
     *
     * @return  the name of the {@link ClusterName}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains a {@link ClusterName} for a specified name.
     *
     * @param name  the name of the {@link ClusterName}
     *
     * @return a {@link ClusterName} for the specified name
     */
    public static ClusterName of(String name)
    {
        return new ClusterName(name);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

        if (systemProperties != null)
        {
            optionsByType.add(SystemProperty.of(PROPERTY, name));
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

        if (!(o instanceof ClusterName))
        {
            return false;
        }

        ClusterName executable = (ClusterName) o;

        return name.equals(executable.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
