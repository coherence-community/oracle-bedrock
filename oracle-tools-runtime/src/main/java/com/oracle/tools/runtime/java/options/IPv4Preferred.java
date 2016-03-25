/*
 * File: IPv4Preferred.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;

import com.oracle.tools.runtime.java.JavaApplication;

/**
 * An {@link Option} to specify if an IPv4 network stack is preferred for a {@link JavaApplication}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class IPv4Preferred implements Profile, Option
{
    /**
     * The java.net.preferIPv4Stack JVM property (false by default in most JVMs)
     */
    public static final String JAVA_NET_PREFER_IPV4_STACK = "java.net.preferIPv4Stack";

    /**
     * Is IPv4 preferred?
     */
    private boolean preferred;


    /**
     * Constructs a {@link IPv4Preferred} with a specific setting.
     *
     * @param preferred  if IPv4 is preferred
     */
    private IPv4Preferred(boolean preferred)
    {
        this.preferred = preferred;
    }


    /**
     * Determine if an IPv4 network is preferred.
     *
     * @return  <code>true</code> if IPv4 is preferred, <code>false</code> otherwise
     */
    public boolean isPreferred()
    {
        return preferred;
    }


    /**
     * Obtains an {@link IPv4Preferred} based on the specified value.
     *
     * @param preferred  if IPv4 is preferred
     *
     * @return  an {@link IPv4Preferred} for the specified value
     */
    public static IPv4Preferred preferred(boolean preferred)
    {
        return new IPv4Preferred(preferred);
    }


    /**
     * Obtains an {@link IPv4Preferred} such that {@link IPv4Preferred#isPreferred()}
     * returns <code>true</code>.
     *
     * @return  an {@link IPv4Preferred}
     */
    public static IPv4Preferred yes()
    {
        return preferred(true);
    }


    /**
     * Obtains an {@link IPv4Preferred} such that {@link IPv4Preferred#isPreferred()}
     * returns <code>false</code>.
     *
     * @return  an {@link IPv4Preferred}
     */
    public static IPv4Preferred no()
    {
        return preferred(false);
    }


    /**
     * Obtains an {@link IPv4Preferred} based on the current Java Virtual Machine
     * settings.
     *
     * @return  an {@link IPv4Preferred}
     */
    @Options.Default
    public static IPv4Preferred autoDetect()
    {
        return preferred(Boolean.getBoolean(JAVA_NET_PREFER_IPV4_STACK));
    }


    @Override
    public void onBeforeLaunch(Platform platform,
                               Options  options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null)
        {
            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(JAVA_NET_PREFER_IPV4_STACK, preferred));

            options.add(systemProperties);
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

        if (!(o instanceof IPv4Preferred))
        {
            return false;
        }

        IPv4Preferred that = (IPv4Preferred) o;

        return preferred == that.preferred;

    }


    @Override
    public int hashCode()
    {
        return (preferred ? 1 : 0);
    }
}
