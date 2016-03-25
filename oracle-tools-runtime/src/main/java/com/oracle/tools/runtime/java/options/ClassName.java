/*
 * File: ClassName.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Option;

import com.oracle.tools.runtime.java.JavaApplication;

/**
 * An {@link Option} to specify the fully-qualified-name of a {@link Class} for a {@link JavaApplication}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ClassName implements Option
{
    /**
     * The fully-qualified-name of a {@link Class}.
     */
    private String name;


    /**
     * Constructs a {@link ClassName} for the specified name.
     *
     * @param name  the name
     */
    private ClassName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("ClassName's can't be null");
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Obtains the name of the {@link ClassName}.
     *
     * @return  the name of the {@link ClassName}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains a {@link ClassName} for a specified name.
     *
     * @param name  the name of the {@link ClassName}
     *
     * @return a {@link ClassName} for the specified name
     */
    public static ClassName of(String name)
    {
        return new ClassName(name);
    }


    /**
     * Obtains a {@link ClassName} for a specified {@link Class}.
     *
     * @param classToUse  the {@link Class} to use for determining the {@link ClassName}
     *
     * @return a {@link ClassName} for the specified {@link Class}
     */
    public static ClassName of(Class<?> classToUse)
    {
        if (classToUse == null)
        {
            throw new IllegalArgumentException("ClassName's can't be null");
        }
        else
        {
            return new ClassName(classToUse.getName());
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof ClassName))
        {
            return false;
        }

        ClassName executable = (ClassName) o;

        return name.equals(executable.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
