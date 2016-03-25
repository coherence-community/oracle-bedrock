/*
 * File: DisplayName.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;

import java.util.UUID;

/**
 * An {@link Option} to specify the display name of an {@link Application}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DisplayName implements Option
{
    /**
     * The display name of an {@link Application}.
     */
    private String name;


    /**
     * Constructs a {@link DisplayName} for the specified name.
     *
     * @param name  the name
     */
    private DisplayName(String name)
    {
        if (name == null)
        {
            this.name = UUID.randomUUID().toString();
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Resolves the {@link DisplayName} using the provided {@link Options}
     *
     * @param options  the {@link Options}
     *
     * @return  the name of the {@link DisplayName}
     */
    public String resolve(Options options)
    {
        if (options == null)
        {
            return name;
        }
        else
        {
            // include the discriminator in the name
            Discriminator discriminator = options.get(Discriminator.class);

            return discriminator == null ? name : name + "-" + discriminator.getValue();
        }
    }


    /**
     * Obtains a {@link DisplayName} for a specified name.
     *
     * @param name  the name of the {@link DisplayName}
     *
     * @return a {@link DisplayName} for the specified name
     */
    public static DisplayName of(String name)
    {
        return new DisplayName(name);
    }


    /**
     * Auto-generates a unique {@link DisplayName}.
     *
     * @return a unique {@link DisplayName}
     */
    @Options.Default
    public static DisplayName autoGenerate()
    {
        return new DisplayName(null);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof DisplayName))
        {
            return false;
        }

        DisplayName executable = (DisplayName) o;

        return name.equals(executable.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
