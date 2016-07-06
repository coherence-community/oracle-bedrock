/*
 * File: Pof.java
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

import com.oracle.bedrock.ComposableOption;
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
 * An {@link Option} to specify Portable Object Format configuration for a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Pof implements Profile, ComposableOption<Pof>
{
    /**
     * The tangosol.pof.config property.
     */
    public static final String PROPERTY_CONFIG = "tangosol.pof.config";

    /**
     * The tangosol.pof.enabled property.
     */
    public static final String PROPERTY_ENABLED = "tangosol.pof.enabled";

    /**
     * The configuration uri (null if not set).
     */
    private String configUri;

    /**
     * Is POF enabled (null if not set).
     */
    private Boolean enabled;


    /**
     * Constructs a {@link Pof} given the specified parameters.
     *
     * @param configUri  the pof configuration (null means use default)
     * @param enabled    is pof enabled (null means use default)
     */
    private Pof(String  configUri,
                Boolean enabled)
    {
        this.configUri = configUri;
        this.enabled   = enabled;
    }


    /**
     * Obtains a {@link Pof} for the specified config uri.
     *
     * @param configUri  the uri for the {@link Pof} configuration
     *
     * @return a {@link Pof} for the specified config uri
     */
    public static Pof config(String configUri)
    {
        return new Pof(configUri, null);
    }


    /**
     * Obtains a {@link Pof} based on the specified parameter.
     *
     * @param enabled  is {@link Pof} to be enabled?
     *
     * @return a {@link Pof} based on the specified parameter
     */
    public static Pof enabled(boolean enabled)
    {
        return new Pof(null, enabled);
    }


    /**
     * Obtains a {@link Pof} that is disabled.
     *
     * @return a disabled {@link Pof}
     */
    public static Pof disabled()
    {
        return new Pof(null, false);
    }


    /**
     * Obtains a {@link Pof} that is enabled.
     *
     * @return a disabled {@link Pof}
     */
    public static Pof enabled()
    {
        return new Pof(null, true);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

        if (systemProperties != null && configUri != null)
        {
            optionsByType.add(SystemProperty.of(PROPERTY_CONFIG, configUri));

            // when a configuration is defined, we automatically enabled pof
            optionsByType.add(SystemProperty.of(PROPERTY_ENABLED, true));
        }

        if (systemProperties != null && enabled != null)
        {
            optionsByType.add(SystemProperty.of(PROPERTY_ENABLED, enabled));
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
    public Pof compose(Pof other)
    {
        return new Pof(this.configUri == null ? other.configUri : this.configUri,
                       this.enabled == null ? other.enabled : this.enabled);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Pof))
        {
            return false;
        }

        Pof logging = (Pof) o;

        if (configUri != null ? !configUri.equals(logging.configUri) : logging.configUri != null)
        {
            return false;
        }

        return enabled != null ? enabled.equals(logging.enabled) : logging.enabled == null;

    }


    @Override
    public int hashCode()
    {
        int result = configUri != null ? configUri.hashCode() : 0;

        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);

        return result;
    }
}
