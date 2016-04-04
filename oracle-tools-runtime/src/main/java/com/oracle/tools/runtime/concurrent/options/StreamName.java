/*
 * File: StreamName.java
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

package com.oracle.tools.runtime.concurrent.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.concurrent.RemoteEvent;

/**
 * A name used to identify a stream of related {@link RemoteEvent}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class StreamName implements Option
{
    /**
     * The stream name for a {@link RemoteEvent}.
     */
    private String name;


    /**
     * Constructs a {@link StreamName} for the specified name.
     *
     * @param name  the name
     */
    private StreamName(String name)
    {
        if (name == null)
        {
            this.name = "default";
        }
        else
        {
            this.name = name;
        }
    }


    /**
     * Obtains the {@link StreamName} as a {@link String}.
     *
     * @return  the {@link String} representation of a {@link StreamName}
     */
    public String get()
    {
        return name;
    }


    /**
     * Obtains a {@link StreamName} for a specified name.
     *
     * @param name  the name of the {@link StreamName}
     *
     * @return a {@link StreamName} for the specified name
     */
    public static StreamName of(String name)
    {
        return new StreamName(name);
    }


    /**
     * Automatically creates a default {@link StreamName}.
     *
     * @return a default {@link StreamName}
     */
    @Options.Default
    public static StreamName automatic()
    {
        return new StreamName(null);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof StreamName))
        {
            return false;
        }

        StreamName streamName = (StreamName) o;

        return name.equals(streamName.name);

    }


    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
