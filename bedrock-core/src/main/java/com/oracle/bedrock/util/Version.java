/*
 * File: Version.java
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

package com.oracle.bedrock.util;

/**
 * A utility class for representing {@link Version} numbers and comparing them.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Version
{
    /**
     * The {@link Version} number.
     */
    private String number;


    /**
     * Constructs a {@link Version} given a number.
     *
     * @param number  the {@link Version} number
     */
    private Version(String number)
    {
        this.number = number;
    }


    /**
     * Constructs a {@link Version} by parsing the specified version number
     *
     * @param number  the {@link Version} number.
     *
     * @return a {@link Version}
     */
    public static Version of(String number)
    {
        return new Version(number);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Version))
        {
            return false;
        }

        Version version = (Version) o;

        return number != null ? number.equals(version.number) : version.number == null;
    }


    @Override
    public int hashCode()
    {
        return number != null ? number.hashCode() : 0;
    }


    @Override
    public String toString()
    {
        return number;
    }
}
