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
    /**
     * Auto-detect, instantiate and configure the set of {@link Profile}s.
     *
     * @return an {@link Iterable}
     */
    public static OptionsByType getProfiles()
    {
        final String  ORACLE_TOOLS_PROFILE = "bedrock.profile.";

        OptionsByType profiles             = OptionsByType.empty();

        for (String name : System.getProperties().stringPropertyNames())
        {
            if (name.startsWith(ORACLE_TOOLS_PROFILE))
            {
                // determine the profile name
                String profileName = name.substring(ORACLE_TOOLS_PROFILE.length()).trim().toLowerCase();

                // determine the profile value
                String profileValue = System.getProperty(name);

                // when the profile name contains a "." we don't process this system property
                if (profileName.indexOf(".") < 0)
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
                            Profile profile = (Profile) profiles.get((Class<Option>) profileClass, profileValue);
                        }
                        else
                        {
                            // TODO: the specified profile is not an Option
                        }
                    }
                    catch (Exception e)
                    {
                        // TODO: failed to load the specified profile
                    }
                }
            }
        }

        return profiles;
    }
}
