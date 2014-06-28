/*
 * File: Infrastructure.java
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

package com.oracle.tools.runtime;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An Infrastructure represents a set of one or more {@link Platform}s.
 * </p>
 * An Infrastructure is created using an {@link InfrastructureBuilder}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Infrastructure<P extends Platform> implements Iterable<P>, Closeable
{
    /**
     * The {@link Platform}s that make up this {@link Infrastructure}.
     */
    private final Map<String, P> platforms;

    /**
     * Construct an empty {@link Infrastructure}.
     */
    public Infrastructure()
    {
        this(null);
    }

    /**
     * Construct an {@link Infrastructure} made up of the specified
     * {@link Platform}s.
     *
     * @param platforms  the {@link Platform}s that this {@link Infrastructure} will contain
     */
    public Infrastructure(Map<String, P> platforms)
    {
        this.platforms = new HashMap<String,P>();
        if (platforms != null)
        {
            this.platforms.putAll(platforms);
        }
    }


    /**
     * Obtain the number of {@link Platform}s in this {@link Infrastructure}.
     *
     * @return the number of {@link Platform}s in this {@link Infrastructure}
     */
    public int size()
    {
        return platforms.size();
    }

    /**
     * Obtain the {@link Platform} with the specified name from
     * this {@link Infrastructure}.
     *
     * @param name  the name of the {@link Platform} to obtain
     *
     * @return the {@link Platform} with the specified name or null
     *         if this {@link Infrastructure} contains no {@link Platform}
     *         with the specified name
     */
    @SuppressWarnings("unchecked")
    public <T extends P> T getPlatform(String name)
    {
        return (T) platforms.get(name);
    }

    /**
     * Add an existing {@link Platform} to this infrastructure.
     *
     * @param platform  the {@link Platform} to add
     *
     * @throws IllegalArgumentException if a {@link Platform} with the same name
     *                                  already exists in this {@link Infrastructure}
     */
    public void addPlatform(P platform)
    {
        String name = platform.getName();
        if (platforms.containsKey(name))
        {
            throw new IllegalArgumentException("This infrastructure already contains a platform with the name " + name);
        }

        platforms.put(name, platform);
    }

    /**
     * Obtain an {@link Iterator} that will iterate over
     * the {@link Platform}s contained within this
     * {@link Infrastructure}.
     *
     * @return an {@link Iterator} that will iterate over
     *         the {@link Platform}s contained within this
     *         {@link Infrastructure}
     */
    @Override
    public Iterator<P> iterator()
    {
        return Collections.unmodifiableCollection(platforms.values()).iterator();
    }

    /**
     * Close the {@link Platform} with the specified name
     * and remove it from this {@link Infrastructure}.
     *
     * @param name  the name of the {@link Platform} to close
     */
    public void closePlatform(String name)
    {
        P platform = platforms.remove(name);
        if (platform instanceof Closeable)
        {
            try
            {
                ((Closeable) platform).close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close this {@link Infrastructure} by closing all of the
     * {@link Platform}s that are contained within it that also
     * implement {@link java.io.Closeable}.
     *
     * @throws IOException if an error occurs
     *
     * @see java.io.Closeable
     */
    @Override
    public void close() throws IOException
    {
        for (P platform : platforms.values())
        {
            if (platform instanceof Closeable)
            {
                try
                {
                    ((Closeable) platform).close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        platforms.clear();
    }
}
