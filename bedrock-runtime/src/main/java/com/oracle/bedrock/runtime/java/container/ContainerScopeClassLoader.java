/*
 * File: ContainerScopeClassLoader.java
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

package com.oracle.bedrock.runtime.java.container;

import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.java.ClassPath;

import java.net.URLClassLoader;

/**
 * A {@link URLClassLoader} that defines an associated {@link ContainerScope},
 * that of which is used to encapsulate and isolate resources used by, associated
 * with and/or loaded by {@link Class}es by the {@link URLClassLoader}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
@Internal
public class ContainerScopeClassLoader extends URLClassLoader
{
    /**
     * The {@link ContainerScope} for the {@link ContainerScopeClassLoader}.
     */
    private ContainerScope m_containerScope;


    /**
     * Constructs a {@link ContainerScopeClassLoader} with the parent {@link ClassLoader}
     * being the current {@link Thread} context and the {@link ContainerScope} being the
     * based on the current {@link ContainerScope}.
     */
    public ContainerScopeClassLoader()
    {
        this(Container.getContainerScope());
    }


    /**
     * Constructs a {@link ContainerScopeClassLoader} with the parent {@link ClassLoader}
     * being the current {@link Thread} context.
     *
     * @param containerScope  the {@link ContainerScope} for the {@link ClassLoader}
     */
    public ContainerScopeClassLoader(ContainerScope containerScope)
    {
        this(Thread.currentThread().getContextClassLoader(), containerScope);
    }


    /**
     * Constructs a {@link ContainerScopeClassLoader} with the specified parent {@link ClassLoader}.
     *
     * @param parentClassLoader  the parent {@link ClassLoader}
     * @param containerScope     the {@link ContainerScope} for the {@link ClassLoader}
     */
    public ContainerScopeClassLoader(ClassLoader    parentClassLoader,
                                     ContainerScope containerScope)
    {
        this(new ClassPath(), parentClassLoader, containerScope);
    }


    /**
     * Constructs a {@link ContainerScopeClassLoader} with the specified parent {@link ClassLoader}.
     *
     * @param classPath          the {@link ClassPath} of Classes that will be
     *                           loaded by this {@link ClassLoader}
     * @param parentClassLoader  the parent {@link ClassLoader}
     * @param containerScope     the {@link ContainerScope} for the {@link ClassLoader}
     */
    public ContainerScopeClassLoader(ClassPath      classPath,
                                     ClassLoader    parentClassLoader,
                                     ContainerScope containerScope)
    {
        super(classPath.getURLs(), parentClassLoader);
        m_containerScope = containerScope;
    }


    /**
     * Obtains the {@link ContainerScope} for the {@link ClassLoader}.
     *
     * @return the {@link ContainerScope}
     */
    public ContainerScope getContainerScope()
    {
        return m_containerScope;
    }
}
