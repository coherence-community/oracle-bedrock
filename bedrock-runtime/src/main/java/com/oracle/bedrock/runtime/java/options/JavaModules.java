/*
 * File: UseModules.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.JavaApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@link Option} to specify that a {@link JavaApplication} should run using Java 9 modules.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author JK
 */
public class JavaModules implements Option, Profile
{
    /**
     * Is modular mode enabled?
     */
    private final boolean enabled;


    private final Set<String> modules;

    /**
     * Constructs a {@link JavaModules} for the specified value.
     *
     * @param enabled if modular mode is enabled
     * @param modules the set of modules to add and export to bedrock
     */
    private JavaModules(boolean enabled, Set<String> modules)
    {
        this.enabled = enabled;
        this.modules = modules;
    }


    /**
     * Obtains a {@link JavaModules} for a specified value.
     *
     * @param enabled if modular mode is enabled
     *
     * @return a {@link JavaModules} for the specified value
     */
    public static JavaModules enabled(boolean enabled, String... modules)
    {
        Set<String> moduleSet;

        if (modules == null || modules.length == 0)
        {
            moduleSet = Collections.emptySet();
        }
        else
        {
            moduleSet = Arrays.stream(modules)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
        }

        return new JavaModules(enabled, moduleSet);
    }


    /**
     * Obtains an enabled {@link JavaModules} mode.
     *
     * @return an enabled {@link JavaModules} mode
     */
    public static JavaModules enabled(String... modules)
    {
        return enabled(true, modules);
    }


    /**
     * Obtains an disabled {@link JavaModules} mode.
     *
     * @return an disabled {@link JavaModules} mode
     */
    @OptionsByType.Default
    public static JavaModules disabled()
    {
        return enabled(false);
    }


    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public void onLaunching(Platform platform, MetaClass metaClass, OptionsByType optionsByType)
    {
        modules.forEach(module -> {
            optionsByType.add(JvmOptions.include("--add-exports",
                                                 module + "/com.oracle.coherence.server=com.oracle.bedrock.runtime",
                                                 "--add-modules",
                                                module));
        });
    }


    @Override
    public void onLaunched(Platform platform, Application application, OptionsByType optionsByType)
    {
    }


    @Override
    public void onClosing(Platform platform, Application application, OptionsByType optionsByType)
    {
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof JavaModules))
        {
            return false;
        }

        JavaModules other = (JavaModules) o;

        return enabled == other.enabled;

    }


    @Override
    public int hashCode()
    {
        return (enabled ? 1 : 0);
    }
}
