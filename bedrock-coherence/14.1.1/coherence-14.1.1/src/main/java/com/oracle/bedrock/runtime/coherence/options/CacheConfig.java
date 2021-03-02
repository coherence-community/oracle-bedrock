/*
 * File: CacheConfig.java
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
 * An {@link Option} to specify the Cache Configuration of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CacheConfig implements Profile, Option
{
    /**
     * The tangosol.coherence.cacheconfig property.
     */
    public static final String PROPERTY = "tangosol.coherence.cacheconfig";

    /**
     * The cache config uri.
     */
    private String uri;


    /**
     * Constructs a {@link CacheConfig} for the specified uri.
     *
     * @param uri  the name
     */
    private CacheConfig(String uri)
    {
        if (uri == null)
        {
            throw new NullPointerException("CacheConfig must not be null");
        }
        else
        {
            this.uri = uri;
        }
    }


    /**
     * Obtains the uri of the {@link CacheConfig}.
     *
     * @return  the uri of the {@link CacheConfig}
     */
    public String getUri()
    {
        return uri;
    }


    /**
     * Obtains a {@link CacheConfig} for a specified uri.
     *
     * @param uri  the uri of the {@link CacheConfig}
     *
     * @return a {@link CacheConfig} for the specified uri
     */
    public static CacheConfig of(String uri)
    {
        return new CacheConfig(uri);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

        if (systemProperties != null)
        {
            optionsByType.add(SystemProperty.of(PROPERTY, uri));
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

        if (!(o instanceof CacheConfig))
        {
            return false;
        }

        CacheConfig executable = (CacheConfig) o;

        return uri.equals(executable.uri);

    }


    @Override
    public int hashCode()
    {
        return uri.hashCode();
    }
}
