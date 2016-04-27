/*
 * File: Clustering.java
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
import com.oracle.tools.runtime.MetaClass;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.java.options.SystemProperties;
import com.oracle.tools.runtime.java.options.SystemProperty;

/**
 * An {@link Option} to specify when {@link CoherenceClusterMember} is allowed to cluster (using tcmp).
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Clustering implements Profile, Option
{
    /**
     * The tangosol.coherence.tcmp.enabled property.
     */
    public static final String PROPERTY = "tangosol.coherence.tcmp.enabled";

    /**
     * Is local storage enabled?
     */
    private boolean enabled;


    /**
     * Constructs a {@link Clustering} for the specified value.
     *
     * @param enabled  is local storage enabled?
     */
    private Clustering(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Determines if {@link Clustering} is enabled.
     *
     * @return  <code>true</code> if {@link Clustering} is enabled,
     *          <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains a {@link Clustering} for a specified value.
     *
     * @param enabled  is local storage enabled?
     *
     * @return a {@link Clustering} for the specified value
     */
    public static Clustering enabled(boolean enabled)
    {
        return new Clustering(enabled);
    }


    /**
     * Obtains a {@link Clustering} that is enabled.
     *
     * @return a {@link Clustering} that is enabled
     */
    public static Clustering enabled()
    {
        return new Clustering(true);
    }


    /**
     * Obtains a {@link Clustering} that is disabled.
     *
     * @return a {@link Clustering} that is disabled
     */
    public static Clustering disabled()
    {
        return new Clustering(false);
    }


    @Override
    public void onLaunching(Platform  platform,
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

        if (!(o instanceof Clustering))
        {
            return false;
        }

        Clustering that = (Clustering) o;

        return enabled == that.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
