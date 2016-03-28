/*
 * File: RoleName.java
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

package com.oracle.tools.runtime.coherence.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.java.options.SystemProperties;
import com.oracle.tools.runtime.java.options.SystemProperty;

import java.util.UUID;

/**
 * An {@link Option} to specify the role name of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RoleName implements Profile, Option
{
    /**
     * The tangosol.coherence.role property.
     */
    public static final String PROPERTY = "tangosol.coherence.role";

    /**
     * The role name of an {@link CoherenceClusterMember}.
     */
    private String name;


    /**
     * Constructs a {@link RoleName} for the specified name.
     *
     * @param name  the name
     */
    private RoleName(String name)
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
     * Obtains the name of the {@link RoleName}.
     *
     * @return  the name of the {@link RoleName}
     */
    public String get()
    {
        return name;
    }


    /**
     * Obtains a {@link RoleName} for a specified name.
     *
     * @param name  the name of the {@link RoleName}
     *
     * @return a {@link RoleName} for the specified name
     */
    public static RoleName of(String name)
    {
        return new RoleName(name);
    }


    @Override
    public void onBeforeLaunch(Platform platform,
                               Options  options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null)
        {
            options.add(SystemProperty.of(PROPERTY, name));
        }
    }


    @Override
    public void onAfterLaunch(Platform    platform,
                              Application application,
                              Options     options)
    {
    }


    @Override
    public void onBeforeClose(Platform    platform,
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

        if (!(o instanceof RoleName))
        {
            return false;
        }

        RoleName executable = (RoleName) o;

        return name.equals(executable.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
