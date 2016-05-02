/*
 * File: BoxName.java
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

package com.oracle.bedrock.runtime.virtual.vagrant.options;

import com.oracle.bedrock.Option;

/**
 * An {@link Option} to specify the name of a Vagrant box.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class BoxName implements Option
{
    /**
     * The name of the Vagrant box.
     */
    private String name;


    /**
     * Constructs a {@link BoxName} for the specified name.
     *
     * @param name  the name
     */
    private BoxName(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("The BoxName can't be null");
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Obtains the name of the Vagrant box.
     *
     * @return  the name
     */
    public String get()
    {
        return name;
    }


    /**
     * Obtains a {@link BoxName} for a specified name.
     *
     * @param name  the name of the {@link BoxName}
     *
     * @return a {@link BoxName} for the specified name
     */
    public static BoxName of(String name)
    {
        return new BoxName(name);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof BoxName))
        {
            return false;
        }

        BoxName boxName = (BoxName) o;

        return name.equals(boxName.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
