/*
 * File: JMXManagementMode.java
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

package com.oracle.bedrock.runtime.coherence;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;

/**
 * Defines valid JMX Management Modes for a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public enum JMXManagementMode implements Option, Profile
{
    ALL,
    NONE,
    REMOTE_ONLY,
    LOCAL_ONLY;

    /**
     * The tangosol.coherence.management property.
     */
    public static final String PROPERTY = "tangosol.coherence.management";


    /**
     * Determines the system property representation of the {@link JMXManagementMode}
     *
     * @return A {@link String}
     */
    public String toSystemProperty()
    {
        // default to all
        String result = "all";

        if (this == NONE)
        {
            result = "none";
        }
        else if (this == REMOTE_ONLY)
        {
            result = "remote-only";
        }
        else if (this == LOCAL_ONLY)
        {
            result = "local-only";
        }

        return result;
    }


    /**
     * Obtains a {@link JMXManagementMode} based on system-property value.
     *
     * @param systemProperty  the system-property value
     *
     * @return  a {@link JMXManagementMode} or null if unknown
     */
    public static JMXManagementMode fromSystemProperty(String systemProperty)
    {
        systemProperty = systemProperty == null ? "" : systemProperty.trim().toLowerCase();

        if (systemProperty == null)
        {
            return null;
        }
        else if (systemProperty.equals("none"))
        {
            return NONE;
        }
        else if (systemProperty.equals("remote-only"))
        {
            return REMOTE_ONLY;
        }
        else if (systemProperty.equals("local-only"))
        {
            return LOCAL_ONLY;
        }
        else if (systemProperty.equals("all"))
        {
            return ALL;
        }
        else
        {
            return null;
        }
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

        if (systemProperties != null)
        {
            optionsByType.add(SystemProperty.of(PROPERTY, toSystemProperty()));
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
}
