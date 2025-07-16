/*
 * File: Profiles.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Helpers for {@link Profile}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Profiles
{
    public static final String  ORACLE_TOOLS_PROFILE = "bedrock.profile.";

    /**
     * Auto-detect, instantiate and configure the set of {@link Profile}s.
     *
     * @return an {@link Iterable}
     */
    public static OptionsByType getProfiles()
    {
        OptionsByType options = OptionsByType.empty();
        List<Profile> profiles = getOrderedProfiles();
        for (Profile profile : profiles)
        {
            if (profile instanceof Option)
            {
            options.add((Option) profile);
            }
        }
        return options;
    }

    /**
     * Auto-detect, instantiate and configure the list of {@link Profile}s
     * in priority order (the highest priority comes first).
     *
     * @return a {@link List} of {@link Profile} instances in priority order
     */
    @SuppressWarnings("unchecked")
    public static List<Profile> getOrderedProfiles()
    {
        OptionsByType options  = OptionsByType.empty();
        List<Profile> profiles = new ArrayList<>();

        for (String name : System.getProperties().stringPropertyNames())
        {
            if (name.startsWith(ORACLE_TOOLS_PROFILE))
            {
                // determine the profile name
                String profileName = name.substring(ORACLE_TOOLS_PROFILE.length()).trim().toLowerCase();

                // determine the profile value
                String profileValue = System.getProperty(name);

                // when the profile name contains a "." we don't process this system property
                if (!profileName.contains("."))
                {
                    String profileClassName = System.getProperty(name + ".classname");
                    if (profileClassName == null)
                    {
                        // create a default class name based on the profile name
                        profileClassName = "com.oracle.bedrock." + profileName + "."
                                + profileName.substring(0, 1).toUpperCase() + profileName.substring(1)
                                + "Profile";
                    }

                    try
                    {
                        // attempt to load the class for the profile
                        Class<?> profileClass = Class.forName(profileClassName);

                        // ensure that the Profile is an Option
                        if (Option.class.isAssignableFrom(profileClass))
                        {
                            // by attempting to request the Profile from the Profiles collection
                            // it will be instantiated using the appropriate default constructor / factory method
                            Profile profile = (Profile) options.get((Class<Option>) profileClass, profileValue);
                            profiles.add(profile);
                        }
                        else
                        {
                            Profile profile = (Profile) profileClass.getDeclaredConstructor().newInstance();
                            profiles.add(profile);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Unable to instantiate profile '" + profileName + "'", e);
                    }
                }
            }

            ServiceLoader<Profile> loader = ServiceLoader.load(Profile.class);
            for (Profile profile : loader)
            {
                profiles.add(profile);
            }
        }

        profiles.sort(Profile.ProfileOrderer.INSTANCE);
        return profiles;
    }
}
