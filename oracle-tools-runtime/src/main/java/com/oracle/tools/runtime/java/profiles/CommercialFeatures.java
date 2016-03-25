/*
 * File: CommercialFeatures.java
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

package com.oracle.tools.runtime.java.profiles;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.options.Freeform;

import java.lang.management.ManagementFactory;

/**
 * Defines a {@link Profile} to enable/disable using commercial features with the
 * Oracle Java Virtual Machine
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CommercialFeatures implements Profile, Option
{
    /**
     * Should remote debugging be enabled for a {@link JavaApplication}.
     */
    private boolean enabled;


    /**
     * Privately constructs a {@link CommercialFeatures} {@link Profile}.
     *
     * @param enabled           is the use of commercial features enabled
     */
    private CommercialFeatures(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * Obtains if {@link CommercialFeatures} is enabled.
     *
     * @return  <code>true</code> if {@link CommercialFeatures} is enabled, <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public void onBeforeLaunch(Platform platform,
                               Options  options)
    {
        if (enabled)
        {
            options.add(new Freeform("-XX:+UnlockCommercialFeatures"));
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


    /**
     * Obtains an enabled {@link CommercialFeatures} {@link Profile}.
     *
     * @return  a {@link CommercialFeatures} {@link Option}
     */
    public static CommercialFeatures enabled()
    {
        return new CommercialFeatures(true);
    }


    /**
     * Obtains a disabled {@link CommercialFeatures} {@link Profile}.
     *
     * @return  a {@link CommercialFeatures} {@link Option}
     */
    public static CommercialFeatures disabled()
    {
        return new CommercialFeatures(false);
    }


    /**
     * Obtains a {@link CommercialFeatures} {@link Profile}, auto-detecting if it should be
     * enabled based on the Java process in which the thread is executing.
     *
     * @return  a {@link CommercialFeatures} {@link Option}
     */
    @Options.Default
    public static CommercialFeatures autoDetect()
    {
        try
        {
            return ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
            .indexOf("-XX:+UnlockCommercialFeatures") > 0 ? enabled() : disabled();
        }
        catch (Throwable t)
        {
            System.err.println("Error trying to determine commercial feaures status - " + t.getMessage());

            return disabled();
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof CommercialFeatures))
        {
            return false;
        }

        CommercialFeatures that = (CommercialFeatures) o;

        return enabled == that.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
