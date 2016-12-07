/*
 * File: JmxProfile.java
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

package com.oracle.bedrock.runtime.java.profiles;

import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.features.JmxFeature;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.util.Capture;

import java.util.Optional;

/**
 * Defines a {@link Profile} to enable and configure Remote Java Management Extensions (JMX)
 * for {@link JavaApplication}s.
 * <p>
 * The {@link JmxFeature} will automatically be added to {@link JavaApplication}s launched
 * using this {@link Profile}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @see JmxFeature
 *
 * @author Brian Oliver
 */
public class JmxProfile implements Profile, ComposableOption<JmxProfile>
{
    /**
     * The {@link Optional} hostname to configure for JMX.
     */
    private Optional<String> hostName;

    /**
     * The {@link Optional} port to configure for JMX.
     */
    private Optional<Capture<Integer>> port;

    /**
     * The {@link Optional} flag to determine if authentication should be configured for JMX.
     */
    private Optional<Boolean> authenticate;

    /**
     * The {@link Optional} flag to determine if SSL should be configured for JMX.
     */
    private Optional<Boolean> ssl;


    /**
     * Constructs a {@link JmxProfile}.
     */
    private JmxProfile()
    {
        this.hostName     = Optional.empty();
        this.port         = Optional.empty();
        this.authenticate = Optional.empty();
        this.ssl          = Optional.empty();
    }


    /**
     * Constructs a {@link JmxProfile} based on another {@link JmxProfile}.
     *
     * @param other  the other {@link JmxProfile}
     */
    private JmxProfile(JmxProfile other)
    {
        this.hostName     = other.hostName;
        this.port         = other.port;
        this.authenticate = other.authenticate;
        this.ssl          = other.ssl;
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        // acquire the system properties to update
        SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

        // ----- configure remote jmx -----
        systemProperties = systemProperties.addIfAbsent(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE));

        // ----- configure the host name system property for RMI connectivity -----
        if (hostName.isPresent())
        {
            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(JavaApplication.JAVA_RMI_SERVER_HOSTNAME,
                                                                              hostName.get()));
        }
        else
        {
            // automatically set the host name to that of the platform
            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(JavaApplication.JAVA_RMI_SERVER_HOSTNAME,
                                                                              platform.getAddress().getHostAddress()));
        }

        // ensure that the RMI server doesn't eagerly load JMX classes
        systemProperties = systemProperties.addIfAbsent(SystemProperty.of("java.rmi.server.useCodebaseOnly", "true"));

        // ----- configure the JMX port -----
        if (port.isPresent())
        {
            systemProperties.addIfAbsent(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_PORT, port.get().get()));
        }
        else
        {
            // automatically chose a port from the platform
            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_PORT,
                                                                              LocalPlatform.get().getAvailablePorts()));
        }

        // ----- configure the authentication -----
        if (authenticate.isPresent())
        {
            systemProperties =
                systemProperties.addIfAbsent(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE,
                                                               authenticate.get()));
        }

        // ----- configure ssl -----
        if (ssl.isPresent())
        {
            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(JmxFeature.SUN_MANAGEMENT_JMXREMOTE_SSL,
                                                                              ssl.get()));
        }

        // update the properties
        optionsByType.add(systemProperties);
    }


    @Override
    public void onLaunched(Platform      platform,
                           Application   application,
                           OptionsByType optionsByType)
    {
        // nothing to do when an application is launched
    }


    @Override
    public void onClosing(Platform      platform,
                          Application   application,
                          OptionsByType optionsByType)
    {
        // nothing to do when an application is closed
    }


    /**
     * Chooses the {@link Optional} that is present, defaulting to
     * the first specified when both are present.   When neither is present
     * {@link Optional#empty()} is returned.
     *
     * @param first   the first {@link Optional} to consider
     * @param second  the second {@link Optional} to consider
     * @param <T>     the type of the {@link Optional} values
     *
     * @return an {@link Optional} that is present or {@link Optional#empty()}
     *         when neither are present
     */
    private <T> Optional<T> oneOf(Optional<T> first,
                                  Optional<T> second)
    {
        if ((first != null && first.isPresent()) && (second == null ||!second.isPresent()))
        {
            return first;
        }
        else if ((first == null ||!first.isPresent()) && (second != null && second.isPresent()))
        {
            return second;
        }
        else if ((first != null && first.isPresent()) && (second != null && second.isPresent()))
        {
            return first;
        }
        else
        {
            return Optional.empty();
        }
    }


    @Override
    public JmxProfile compose(JmxProfile other)
    {
        // establish the composed JmxProfile
        JmxProfile composed = new JmxProfile();

        composed.hostName     = oneOf(other.hostName, this.hostName);
        composed.port         = oneOf(other.port, this.port);
        composed.authenticate = oneOf(other.authenticate, this.authenticate);
        composed.ssl          = oneOf(other.ssl, this.ssl);

        return composed;
    }


    /**
     * Obtains a {@link JmxProfile} that configures Remote Java Management Extensions (JMX)
     * for the current platform, <strong>without</strong> authentication and ssl.
     *
     * @return a {@link JmxProfile}
     */
    public static JmxProfile enabled()
    {
        JmxProfile jmxProfile = new JmxProfile();

        jmxProfile.authenticate = Optional.of(false);
        jmxProfile.ssl          = Optional.of(false);

        return jmxProfile;
    }


    /**
     * Obtains a {@link JmxProfile} that configures {@link JmxFeature#SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE}
     * system property.
     *
     * @param enabled  to enable / disable authentication
     *
     * @return a {@link JmxProfile}
     */
    public static JmxProfile authentication(boolean enabled)
    {
        JmxProfile jmxProfile = new JmxProfile();

        jmxProfile.authenticate = Optional.of(enabled);

        return jmxProfile;
    }


    /**
     * Obtains a {@link JmxProfile} that configures {@link JmxFeature#SUN_MANAGEMENT_JMXREMOTE_SSL}
     * system property.
     *
     * @param enabled  to enable / disable ssl
     *
     * @return a {@link JmxProfile}
     */
    public static JmxProfile ssl(boolean enabled)
    {
        JmxProfile jmxProfile = new JmxProfile();

        jmxProfile.ssl = Optional.of(enabled);

        return jmxProfile;
    }


    /**
     * Obtains a {@link JmxProfile} that configures {@link JavaApplication#JAVA_RMI_SERVER_HOSTNAME}
     * system property.
     *
     * @param hostname  the desired hostname configuration
     *
     * @return a {@link JmxProfile}
     */
    public static JmxProfile hostname(String hostname)
    {
        JmxProfile jmxProfile = new JmxProfile();

        jmxProfile.hostName = Optional.of(hostname);

        return jmxProfile;
    }
}
