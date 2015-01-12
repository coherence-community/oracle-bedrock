/*
 * File: JavaHome.java
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

import com.oracle.tools.runtime.Platform;

/**
 * An {@link Option} to define the location of the Java Home.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JavaHome implements Option
{
    /**
     * The {@link Platform} specific location of the Java Home.
     */
    private String javaHome;


    /**
     * Privately constructs a {@link JavaHome}
     *
     * @param javaHome  the {@link Platform} specific Java Home
     */
    private JavaHome(String javaHome)
    {
        this.javaHome = javaHome;
    }


    /**
     * Obtains the {@link Platform} specific Java Home.
     *
     * @return  the Java Home
     */
    public String get()
    {
        return javaHome;
    }


    @Override
    public String toString()
    {
        return "JavaHome{" + javaHome + "}";
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof JavaHome))
        {
            return false;
        }

        JavaHome javaHome1 = (JavaHome) other;

        if (!javaHome.equals(javaHome1.javaHome))
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return javaHome.hashCode();
    }


    /**
     * Constructs a {@link JavaHome} given a specified string.
     *
     * @param javaHome  the {@link Platform} specific Java Home
     *
     * @return  the {@link JavaHome}
     */
    public static JavaHome at(String javaHome)
    {
        return new JavaHome(javaHome);
    }
}
