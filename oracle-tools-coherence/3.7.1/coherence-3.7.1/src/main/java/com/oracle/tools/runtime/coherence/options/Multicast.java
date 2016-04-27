/*
 * File: Multicast.java
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
 * An {@link Option} to specify the multicast time-to-live a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Multicast implements Profile, Option
{
    /**
     * The tangosol.coherence.ttl property.
     */
    public static final String PROPERTY = "tangosol.coherence.ttl";

    /**
     * The multicast ttl value {@link CoherenceClusterMember}.
     */
    private int value;


    /**
     * Constructs a {@link Multicast} for the specified value.
     *
     * @param value  the value
     */
    private Multicast(int value)
    {
        this.value = value;
    }


    /**
     * Obtains a {@link Multicast} for a specified value.
     *
     * @param value  the ttl for the {@link Multicast}
     *
     * @return a {@link Multicast} for the specified name
     */
    public static Multicast ttl(int value)
    {
        return new Multicast(value);
    }


    @Override
    public void onLaunching(Platform  platform,
                            MetaClass metaClass,
                            Options   options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null)
        {
            options.add(SystemProperty.of(PROPERTY, value));
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

        if (!(o instanceof Multicast))
        {
            return false;
        }

        Multicast that = (Multicast) o;

        return value == that.value;

    }


    @Override
    public int hashCode()
    {
        return value;
    }
}
