/*
 * File: Headless.java
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
 * An {@link Option} to specify that a {@link JavaApplication} should run in "headless" mode.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Headless implements Profile, Option
{
    /**
     * Is headless mode enabled?
     */
    private boolean enabled;


    /**
     * Constructs a {@link Headless} for the specified value.
     *
     * @param enabled if headless mode is enabled
     */
    private Headless(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Obtains a {@link Headless} for a specified value.
     *
     * @param enabled if headless mode is enabled
     *
     * @return a {@link Headless} for the specified value
     */
    public static Headless enabled(boolean enabled)
    {
        return new Headless(enabled);
    }


    /**
     * Obtains an enabled {@link Headless} mode.
     *
     * @return an enabled {@link Headless} mode
     */
    public static Headless enabled()
    {
        return new Headless(true);
    }


    /**
     * Obtains an disabled {@link Headless} mode.
     *
     * @return an disabled {@link Headless} mode
     */
    public static Headless disabled()
    {
        return new Headless(false);
    }


    @Override
    public void onBeforeLaunch(Platform platform,
                               Options  options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null && enabled)
        {
            // modify the system properties
            systemProperties = systemProperties.addIfAbsent(SystemProperty.of("java.awt.headless", true));

            // add the modified system properties back into the options
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

        if (!(o instanceof Headless))
        {
            return false;
        }

        Headless headless = (Headless) o;

        return enabled == headless.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
