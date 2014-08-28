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

import com.oracle.tools.runtime.LocalPlatform;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.io.PrintStream;

import java.util.HashSet;

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
    private static PlatformScope platformScope;

    /**
     * The {@link DefaultScope} to use when a suitable {@link ContainerScope}
     * can't be located.   This is primarily when a ClassLoader and/or Thread is
     * not associated with an {@link Scope}.
     */
    private static DefaultScope defaultScope;

    /**
     * The {@link ContainerScope} associated with the current {@link Thread}.
     */
    private static InheritableThreadLocal<ContainerScope> threadScope;

    /**
     * The current set of {@link ContainerScope}s being managed by the {@link Container}.
     */
    private static HashSet<ContainerScope> scopes;

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
            // establish the default scope to use when a thread isn't isolated
            defaultScope = new DefaultScope(platformScope);

            // ensure that the JMX MBean Server for the default Scope is the
            // DelegatingMBeanServerBuilder so that we can isolate MBeanServers
            defaultScope.getProperties().setProperty(ContainerMBeanServerBuilder.PROPERTY_JMX_MBEAN_SERVER_BUILDER,
                                                     DelegatingMBeanServerBuilder.class.getCanonicalName());

            System.setProperties(new DelegatingProperties(defaultScope.getProperties()));
            System.setOut(new PrintStream(new DelegatingStdOutOutputStream(platformScope.getStandardOutput()), true));
            System.setErr(new PrintStream(new DelegatingStdErrOutputStream(platformScope.getStandardError()), true));
            System.setIn(new DelegatingStdInInputStream(platformScope.getStandardInput()));

            // establish the scopes set to track the scopes being managed
            scopes = new HashSet<ContainerScope>();
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
            System.setProperties(platformScope.getProperties());
            System.setOut(platformScope.getStandardOutput());
            System.setErr(platformScope.getStandardError());

            // close and clear the default scope as we are no longer running in a container
            defaultScope.close();
            defaultScope = null;
        }
        else
        {
            // SKIP: we're not running as a container so do nothing!
        }
    }


    /**
     * Adds the specified {@link ContainerScope} to the {@link Container} for managing.
     * <p>
     * Should the {@link Container} not be {@link #start()}ed, it will automatically be started.
     *
     * @param scope  the {@link ContainerScope} to manage
     */
    public synchronized static void manage(ContainerScope scope)
    {
        // add the scope we're now managing
        if (scopes.add(scope))
        {
            // ensure that the container is started
            start();
        }
    }


    /**
     * Removes the specified {@link ContainerScope} from being managed.
     * <p>
     * Should the {@link ContainerScope} being unmanaged be the last known
     * managed {@link ContainerScope}, the {@link Container} will automatically be {@link #stop()}ed.
     *
     * @param scope  the {@link ContainerScope} to unmanage
     */

    public synchronized static void unmanage(ContainerScope scope)
    {
        if (scopes.remove(scope))
        {
            stop();
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
    public static ContainerScope getContainerScope()
    {
        // attempt to determine the scope based on the calling Thread
        ContainerScope scope = threadScope.get();

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
     * Obtains a {@link PlatformScope} that represents the underlying Java
     * Virtual Machine Platform itself.
     *
     * @return  an {@link PlatformScope} for the Java Virtual Machine platform
     */
    public static PlatformScope getPlatformScope()
    {
        return platformScope;
    }


    /**
     * Obtains a {@link DefaultScope} to use when running in container-mode
     * but a {@link ContainerScope} can't be determined when calling
     * {@link #getContainerScope()}.
     *
     * @return  the {@link DefaultScope} or <code>null</code> if not running
     *          in container-mode.
     */
    public static DefaultScope getDefaultScope()
    {
        return defaultScope;
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
            Scope existingScope = threadScope.get();

            if (existingScope == null)
            {
                threadScope.set(scope);
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
        if (threadScope.get() == null)
        {
            // SKIP: do nothing if the current thread is not isolated
        }
        else
        {
            threadScope.remove();
        }
    }


    /**
     * Obtains the {@link AvailablePortIterator} for the {@link Container}.
     *
     * @return the {@link AvailablePortIterator}
     *
     * @deprecated use LocalPlatform.INSTANCE.getAvailablePorts()
     */
    public static AvailablePortIterator getAvailablePorts()
    {
        return LocalPlatform.getInstance().getAvailablePorts();
    }


    /**
     * Static Initialization.
     */
    static
    {
        // establish the ability to track Scopes by thread
        threadScope = new InheritableThreadLocal<ContainerScope>();

        // create a PlatformScope representing the platform itself
        platformScope = new PlatformScope(getAvailablePorts());

        // establish the scopes set to track scopes being managed
        scopes = new HashSet<ContainerScope>();
    }
}
