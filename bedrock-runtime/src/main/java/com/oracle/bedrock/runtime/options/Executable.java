/*
 * File: Executable.java
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
import com.oracle.bedrock.runtime.Platform;

/**
 * An {@link Option} to specify the name of an executable or command to launch with a {@link Platform}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Executable implements Option
{
    /**
     * The name of the executable/command to launch (should not contain any arguments or parameters).
     */
    private String name;


    /**
     * Constructs a {@link Executable} given the specified name.
     *
     * @param name  the name of the {@link Executable}
     */
    private Executable(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Program names can not be null");
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Obtains the name of the {@link Executable} to launch.
     *
     * @return  the name of the {@link Executable}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains a {@link Executable} of a specified name.
     *
     * @param name  the name of the {@link Executable}
     *
     * @return a {@link Executable} of the specified name
     */
    public static Executable named(String name)
    {
        return new Executable(name);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Executable))
        {
            return false;
        }

        Executable executable = (Executable) o;

        return name.equals(executable.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
