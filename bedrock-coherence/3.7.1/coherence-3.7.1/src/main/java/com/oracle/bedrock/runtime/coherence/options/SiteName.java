/*
 * File: SiteName.java
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
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.java.options.SystemProperty;

import java.util.UUID;

/**
 * An {@link Option} to specify the site name of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SiteName implements Profile, Option
{
    /**
     * The tangosol.coherence.site property.
     */
    public static final String PROPERTY = "tangosol.coherence.site";

    /**
     * The site name of an {@link CoherenceClusterMember}.
     */
    private String name;


    /**
     * Constructs a {@link SiteName} for the specified name.
     *
     * @param name  the name
     */
    private SiteName(String name)
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
     * Obtains the name of the {@link SiteName}.
     *
     * @return  the name of the {@link SiteName}
     */
    public String get()
    {
        return name;
    }


    /**
     * Obtains a {@link SiteName} for a specified name.
     *
     * @param name  the name of the {@link SiteName}
     *
     * @return a {@link SiteName} for the specified name
     */
    public static SiteName of(String name)
    {
        return new SiteName(name);
    }


    @Override
    public void onLaunching(Platform platform,
                            MetaClass metaClass,
                            Options   options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null)
        {
            options.add(SystemProperty.of(PROPERTY, name));
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

        if (!(o instanceof SiteName))
        {
            return false;
        }

        SiteName executable = (SiteName) o;

        return name.equals(executable.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
