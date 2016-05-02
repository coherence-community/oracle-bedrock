/*
 * File: LocalStorage.java
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
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;

/**
 * An {@link Option} to specify when {@link CoherenceClusterMember} is local-storage enabled.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalStorage implements Profile, Option
{
    /**
     * The tangosol.coherence.distributed.localstorage property.
     */
    public static final String PROPERTY = "tangosol.coherence.distributed.localstorage";

    /**
     * Is local storage enabled?
     */
    private boolean enabled;


    /**
     * Constructs a {@link LocalStorage} for the specified value.
     *
     * @param enabled  is local storage enabled?
     */
    private LocalStorage(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if {@link LocalStorage} is enabled.
     *
     * @return  <code>true</code> if {@link LocalStorage} is enabled,
     *          <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains a {@link LocalStorage} for a specified value.
     *
     * @param enabled  is local storage enabled?
     *
     * @return a {@link LocalStorage} for the specified value
     */
    public static LocalStorage enabled(boolean enabled)
    {
        return new LocalStorage(enabled);
    }


    /**
     * Obtains a {@link LocalStorage} that is enabled.
     *
     * @return a {@link LocalStorage} that is enabled
     */
    public static LocalStorage enabled()
    {
        return new LocalStorage(true);
    }


    /**
     * Obtains a {@link LocalStorage} that is disabled.
     *
     * @return a {@link LocalStorage} that is disabled
     */
    public static LocalStorage disabled()
    {
        return new LocalStorage(false);
    }


    @Override
    public void onLaunching(Platform platform,
                            MetaClass metaClass,
                            Options   options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null)
        {
            options.add(SystemProperty.of(PROPERTY, enabled));
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

        if (!(o instanceof LocalStorage))
        {
            return false;
        }

        LocalStorage that = (LocalStorage) o;

        return enabled == that.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
