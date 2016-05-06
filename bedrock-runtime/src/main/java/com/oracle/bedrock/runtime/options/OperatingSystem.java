/*
 * File: OperatingSystem.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

/**
 * An {@link Option} to specify an {@link OperatingSystem}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class OperatingSystem implements Option
{
    /**
     * Name of the {@link OperatingSystem}.
     */
    private String name;


    /**
     * Constructs a {@link OperatingSystem} for the specified name.
     *
     * @param name  the name
     */
    private OperatingSystem(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("OperatingSystem can't be null");
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Obtains the name of the {@link OperatingSystem}.
     *
     * @return  the name of the {@link OperatingSystem}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains an {@link OperatingSystem} for a specified name.
     *
     * @param name  the name of the {@link OperatingSystem}
     *
     * @return an {@link OperatingSystem} for the specified name
     */
    public static OperatingSystem of(String name)
    {
        return new OperatingSystem(name);
    }


    /**
     * Auto-Detects the current {@link OperatingSystem}.
     *
     * @return a the current {@link OperatingSystem}
     */
    @Options.Default
    public static OperatingSystem autoDetect()
    {
        return new OperatingSystem(System.getProperty("os.name"));
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof OperatingSystem))
        {
            return false;
        }

        OperatingSystem displayName = (OperatingSystem) o;

        return name.equals(displayName.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
