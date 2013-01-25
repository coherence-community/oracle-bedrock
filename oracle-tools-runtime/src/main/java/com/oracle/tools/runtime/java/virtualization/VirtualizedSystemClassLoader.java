/*
 * File: VirtualizedSystemClassLoader.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

package com.oracle.tools.runtime.java.virtualization;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.Properties;

/**
 * A {@link VirtualizedSystemClassLoader} is a {@link URLClassLoader}
 * that defines a {@link VirtualizedSystem}, that of which is used to
 * encapsulate and isolate {@link System}-level resources for classes loaded
 * with the said {@link ClassLoader}.
 * <p>
 * This class is primarily used by the {@link Virtualization} class to
 * locate, redirect and delegate requests to {@link System} resources (by loaded
 * classes) to the {@link VirtualizedSystem} resources.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class VirtualizedSystemClassLoader extends URLClassLoader
{
    /**
     * The {@link VirtualizedSystem} for the {@link VirtualizedSystemClassLoader}.
     */
    private VirtualizedSystem m_system;


    /**
     * Constructs a {@link VirtualizedSystemClassLoader} with the parent
     * {@link ClassLoader} being the current {@link Thread} context and
     * the {@link VirtualizedSystem} being the Actual System.
     */
    public VirtualizedSystemClassLoader()
    {
        this(Virtualization.getSystem());
    }


    /**
     * Constructs a {@link VirtualizedSystemClassLoader} with the
     * parent {@link ClassLoader} being the current {@link Thread} context.
     * <p>
     * Initially the {@link Properties} collection for the {@link ClassLoader}
     * will be empty.
     */
    public VirtualizedSystemClassLoader(VirtualizedSystem system)
    {
        this(Thread.currentThread().getContextClassLoader(), system);
    }


    /**
     * Constructs a {@link VirtualizedSystemClassLoader} with the
     * specified parent {@link ClassLoader}.
     * <p>
     * Initially the {@link Properties} collection for the {@link ClassLoader}
     * will be empty.
     *
     * @param parent  the parent {@link ClassLoader}
     */
    public VirtualizedSystemClassLoader(ClassLoader parent,
                                        VirtualizedSystem system)
    {
        this(new URL[0], parent, system);
    }


    /**
     * Constructs a {@link VirtualizedSystemClassLoader} with the specified
     * parent {@link ClassLoader}.  Classes defined by the specified {@link URL}s
     * will be loaded by this {@link ClassLoader} and not delegated to the parent.
     * <p>
     * Initially the {@link Properties} collection for the {@link ClassLoader}
     * will be empty.
     *
     * @param urls    the {@link URL}s to be loaded by this {@link ClassLoader}
     * @param parent  the parent {@link ClassLoader}
     */
    public VirtualizedSystemClassLoader(URL[] urls,
                                        ClassLoader parent,
                                        VirtualizedSystem system)
    {
        super(urls, parent);
        m_system = system;
    }


    /**
     * Obtains the {@link VirtualizedSystem} for the {@link ClassLoader}.
     *
     * @return the {@link VirtualizedSystem}
     */
    public VirtualizedSystem getVirtualizedSystem()
    {
        return m_system;
    }
}
