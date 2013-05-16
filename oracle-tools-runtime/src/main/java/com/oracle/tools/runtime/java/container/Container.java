/*
 * File: Container.java
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

package com.oracle.tools.runtime.java.container;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.io.PrintStream;

import java.net.UnknownHostException;

/**
 * The {@link Container} class provides mechanisms to establish and dismantal
 * "contained" and "isolated" applications, much like a Java Application Server
 * or container would, but mostly for regular Java Applications.
 * <p>
 * Fundamentally this includes providing mechanisms to dynamically load and
 * associate application {@link Class}es together with their {@link Thread}s
 * with appropriate {@link Scope}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Container
{
    /**
     * A {@link PlatformScope} that represents the Java Virtual Machine runtime
     * platform resources.   These are the underlying resources on which we
     * may ultimately delegate requests.   We also use this {@link Scope} to
     * return the Java Virtual Machine runtime back to normal when we want to
     * stop running in a container-based mode.
     */
    private static PlatformScope s_platformScope;

    /**
     * The {@link ContainerScope} to use when a suitable {@link ContainerScope}
     * can't be located.   This is primarily when a ClassLoader and/or Thread is
     * not associated with an {@link Scope}.
     */
    private static ContainerScope s_defaultScope;

    /**
     * The {@link ContainerScope} associated with the current {@link Thread}.
     */
    private static InheritableThreadLocal<ContainerScope> s_threadScope;

    /**
     * The {@link AvailablePortIterator} that the {@link Container}
     * will use for allocating ports
     */
    private static AvailablePortIterator s_availablePortIterator;

    /**
     * The number of bytes to reserve for i/o buffers used by
     * pipes between application i/o streams.
     */
    public static final int PIPE_BUFFER_SIZE_BYTES = 16 * 1024;


    /**
     * Starts the containment of application resources.
     */
    public synchronized static void start()
    {
        // are we running as a container?
        if (System.getProperties() instanceof DelegatingProperties)
        {
            // SKIP: we're already running as a container so do nothing!
        }
        else
        {
            // establish the default ContainerScope to use when a thread isn't isolated
            s_defaultScope = new ContainerScope("(Default)",
                                                s_platformScope.getProperties(),
                                                s_availablePortIterator,
                                                new ContainerMBeanServerBuilder(s_availablePortIterator),
                                                false,
                                                PIPE_BUFFER_SIZE_BYTES);

            // ensure that the JMX MBean Server for the default Scope is the
            // DelegatingMBeanServerBuilder so that we can isolate MBeanServers
            s_defaultScope.getProperties().setProperty(ContainerMBeanServerBuilder.PROPERTY_JMX_MBEAN_SERVER_BUILDER,
                                                       DelegatingMBeanServerBuilder.class.getCanonicalName());

            System.setProperties(new DelegatingProperties());
            System.setOut(new PrintStream(new DelegatingStdOutOutputStream(s_platformScope.getStandardOutput()), true));
            System.setErr(new PrintStream(new DelegatingStdErrOutputStream(s_platformScope.getStandardError()), true));
        }
    }


    /**
     * Stops the containment of application resources.
     */
    public synchronized static void stop()
    {
        // are we running as a container?
        if (System.getProperties() instanceof DelegatingProperties)
        {
            System.setProperties(s_platformScope.getProperties());
            System.setOut(s_platformScope.getStandardOutput());
            System.setErr(s_platformScope.getStandardError());
        }
        else
        {
            // SKIP: we're not running as a container so do nothing!
        }
    }


    /**
     * Determines the {@link ContainerScope} of the calling {@link Thread}.
     * Should the calling {@link Thread} is not be associated with a
     * {@link ContainerScope} <code>null</code> is returned.
     * <p>
     * There is no need for the {@link Container} to have been started for this
     * method to return the correct {@link ContainerScope} of the calling {@link Thread}.
     *
     * @return  the {@link ContainerScope} or <code>null</code>
     */
    public static ContainerScope determineContainerScope()
    {
        // attempt to determine the scope based on the calling Thread
        ContainerScope scope = s_threadScope.get();

        if (scope == null)
        {
            // as it's not associated with the calling thread, try to find
            // the scope using the class loader of the thread
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            while (scope == null && classLoader != null)
            {
                if (classLoader instanceof ContainerScopeClassLoader)
                {
                    scope = ((ContainerScopeClassLoader) classLoader).getContainerScope();
                }
                else
                {
                    classLoader = classLoader.getParent();
                }
            }
        }

        return scope;
    }


    /**
     * Obtains the {@link Scope} for the calling {@link Thread}.
     * <p>
     * If the {@link Thread} is not currently scoped, the default {@link Scope}
     * is returned.  That is, unlike the {@link #determineContainerScope()} method, this
     * method never returns <code>null</code>.
     *
     * @return  a {@link Scope}
     */
    public static ContainerScope getContainerScope()
    {
        ContainerScope scope = determineContainerScope();

        return scope == null ? s_defaultScope : scope;
    }


    /**
     * Obtains a {@link PlatformScope} that represents the underlying Java
     * Virtual Machine Platform itself.
     *
     * @return  an {@link PlatformScope} for the Java Virtual Machine platform
     */
    public static PlatformScope getPlatformScope()
    {
        return s_platformScope;
    }


    /**
     * Associates the specified {@link ContainerScope} with the calling {@link Thread}
     * (if not already associated).
     *
     * @param scope  the {@link ContainerScope} for the calling {@link Thread}
     *
     * @throws IllegalStateException  if the current calling {@link Thread} is
     *                                already associated with a {@link ContainerScope}
     */
    public static void associateThreadWith(ContainerScope scope)
    {
        if (scope == null)
        {
            // SKIP: do nothing if we attempt to associate with a null scope
        }
        else
        {
            Scope existingScope = s_threadScope.get();

            if (existingScope == null)
            {
                s_threadScope.set(scope);
            }
            else
            {
                throw new IllegalStateException(String
                    .format("Attempted to associateThreadWith(%s) on Thread [%s] when it is already associated with ContainerScope(%s)",
                            scope.getName(), Thread.currentThread(), existingScope.getName()));
            }
        }
    }


    /**
     * Dissociates the {@link ContainerScope} currently associated with the calling
     * {@link Thread}.
     * <p>
     * After calling this method, the calling {@link Thread} will no longer be
     * directly associated with a {@link Scope}, except perhaps via its context
     * {@link ClassLoader}.
     */
    public static void dissociateThread()
    {
        if (s_threadScope.get() == null)
        {
            // SKIP: do nothing if the current thread is not isolated
        }
        else
        {
            s_threadScope.remove();
        }
    }


    /**
     * Obtains the {@link AvailablePortIterator} for the {@link Container}.
     *
     * @return the {@link AvailablePortIterator}
     */
    public static AvailablePortIterator getAvailablePorts()
    {
        return s_availablePortIterator;
    }


    /**
     * Static Initialization.
     */
    static
    {
        // establish the ability to track Scopes by thread
        s_threadScope = new InheritableThreadLocal<ContainerScope>();

        // create an available port iterator for allocating ports
        try
        {
            // attempt to create an available port iterator on this host
            s_availablePortIterator = new AvailablePortIterator(30000);

            // create a PlatformScope representing the platform itself
            s_platformScope = new PlatformScope(s_availablePortIterator);
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException("Failed to start Container", e);
        }
    }
}
