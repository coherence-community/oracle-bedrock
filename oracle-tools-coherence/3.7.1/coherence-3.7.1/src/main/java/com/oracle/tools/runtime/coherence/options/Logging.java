/*
 * File: Logging.java
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

import com.oracle.tools.ComposableOption;
import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;

import com.oracle.tools.runtime.coherence.CoherenceClusterMember;

import com.oracle.tools.runtime.java.options.SystemProperties;
import com.oracle.tools.runtime.java.options.SystemProperty;

/**
 * An {@link Option} to specify the logging destination and level for a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Logging implements Profile, ComposableOption<Logging>
{
    /**
     * The tangosol.coherence.log property.
     */
    public static final String PROPERTY = "tangosol.coherence.log";

    /**
     * The tangosol.coherence.log.level property.
     */
    public static final String PROPERTY_LEVEL = "tangosol.coherence.log.level";

    /**
     * The logging destination (null if not set).
     */
    private String destination;

    /**
     * The logging level (null if not set).
     */
    private Integer level;


    /**
     * Constructs a {@link Logging} for the specified destination and level.
     *
     * @param destination  the destination  (null means use default)
     * @param level        the level (null means use default)
     */
    private Logging(String  destination,
                    Integer level)
    {
        this.destination = destination;
        this.level       = level;
    }


    /**
     * Obtains a {@link Logging} for a specified destination.
     *
     * @param destination  the destination of the {@link Logging}
     *
     * @return a {@link Logging} for the specified destination
     */
    public static Logging to(String destination)
    {
        return new Logging(destination, null);
    }


    /**
     * Obtains a {@link Logging} for the stderr.
     *
     * @return a {@link Logging} for the stderr
     */
    public static Logging toStdErr()
    {
        return new Logging("stderr", null);
    }


    /**
     * Obtains a {@link Logging} for the stdout.
     *
     * @return a {@link Logging} for the stdout
     */
    public static Logging toStdOut()
    {
        return new Logging("stdout", null);
    }


    /**
     * Obtains a {@link Logging} for the specified level.
     *
     * @param level  the level of the {@link Logging}
     *
     * @return a {@link Logging} for the specified level
     */
    public static Logging at(int level)
    {
        return new Logging(null, level);
    }


    @Override
    public void onBeforeLaunch(Platform platform,
                               Options  options)
    {
        SystemProperties systemProperties = options.get(SystemProperties.class);

        if (systemProperties != null && destination != null)
        {
            options.add(SystemProperty.of(PROPERTY, destination));
        }

        if (systemProperties != null && level != null)
        {
            options.add(SystemProperty.of(PROPERTY_LEVEL, level));
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
    public Logging compose(Logging other)
    {
        return new Logging(this.destination == null ? other.destination : this.destination,
                           this.level == null ? other.level : this.level);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Logging))
        {
            return false;
        }

        Logging logging = (Logging) o;

        if (destination != null ? !destination.equals(logging.destination) : logging.destination != null)
        {
            return false;
        }

        return level != null ? level.equals(logging.level) : logging.level == null;

    }


    @Override
    public int hashCode()
    {
        int result = destination != null ? destination.hashCode() : 0;

        result = 31 * result + (level != null ? level.hashCode() : 0);

        return result;
    }
}
