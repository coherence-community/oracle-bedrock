/*
 * File: TemporaryDirectory.java
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

import java.io.File;
import java.nio.file.Path;

/**
 * An {@link Option} to define the location of the {@link TemporaryDirectory}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class TemporaryDirectory implements Option
{
    /**
     * The {@link Platform} specific {@link Path} to the {@link TemporaryDirectory}.
     */
    private Path path;


    /**
     * Privately constructs a {@link TemporaryDirectory}
     *
     * @param location  the location of the {@link TemporaryDirectory}
     */
    private TemporaryDirectory(Path location)
    {
        this.path = location;
    }


    /**
     * Privately constructs a {@link TemporaryDirectory}
     *
     * @param location  the location of the {@link TemporaryDirectory}
     */
    private TemporaryDirectory(String location)
    {
        this.path = new File(location).toPath();
    }


    /**
     * Obtains the {@link Platform} specific {@link Path} to the {@link TemporaryDirectory}.
     *
     * @return  the temporary directory {@link Path}
     */
    public Path get()
    {
        return path;
    }


    @Override
    public String toString()
    {
        return "TemporaryDirectory{" + path + "}";
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof TemporaryDirectory))
        {
            return false;
        }

        TemporaryDirectory that = (TemporaryDirectory) other;

        if (!path.equals(that.path))
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return path.hashCode();
    }


    /**
     * Constructs a {@link TemporaryDirectory} given a specified string.
     *
     * @param location  the location of the {@link TemporaryDirectory}
     *
     * @return  the {@link TemporaryDirectory}
     */
    public static TemporaryDirectory at(String location)
    {
        return new TemporaryDirectory(location);
    }


    /**
     * Constructs a {@link TemporaryDirectory} given a {@link Path}.
     *
     * @param location  the location of the {@link TemporaryDirectory}
     *
     * @return  the {@link TemporaryDirectory}
     */
    public static TemporaryDirectory at(Path location)
    {
        return new TemporaryDirectory(location);
    }
}
