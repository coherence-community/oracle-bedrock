/*
 * File: HostName.java
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

package com.oracle.tools.runtime.remote.options;

import com.oracle.tools.Option;

/**
 * An {@link Option} to define a {@link HostName}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class HostName implements Option
{
    /**
     * The name of the host.
     */
    private String name;


    /**
     * Constructs a {@link HostName} for the specified name.
     *
     * @param name  the name
     */
    private HostName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("The HostName can't be null");
        }
        else
        {
            this.name = name.trim();
        }
    }


    /**
     * Obtains the name of the host.
     *
     * @return  the name
     */
    public String get()
    {
        return name;
    }


    /**
     * Obtains a {@link HostName} for a specified name.
     *
     * @param name  the name of the {@link HostName}
     *
     * @return a {@link HostName} for the specified name
     */
    public static HostName of(String name)
    {
        return new HostName(name);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof HostName))
        {
            return false;
        }

        HostName hostName = (HostName) o;

        return name.equals(hostName.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
