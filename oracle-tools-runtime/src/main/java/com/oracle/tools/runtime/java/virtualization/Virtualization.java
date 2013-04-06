/*
 * File: Virtualization.java
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

package com.oracle.tools.runtime.java.virtualization;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.io.PrintStream;
import java.net.UnknownHostException;

/**
 * The {@link Virtualization} class provides helpers to establish and dismantal
 * {@link System} virtualizations.  This includes providing mechanisms to
 * associate and acquire appropriate {@link VirtualizedSystem} instances for
 * given {@link Thread}s and/or {@link VirtualizedSystemClassLoader}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Virtualization
{
    /**
     * A {@link VirtualizedSystem} representation of the underlying physical
     * (non-virtualized) {@link System} resources.
     * <p>
     * <ol>
     *      <li>This is used to capture the {@link System} resource so that they
     *          can be returned to normal when {@link Virtualization} is stopped.
     *      <li>As the underlying source of resources for other
     *          {@link VirtualizedSystem}s that don't provide their own
     *          implementations of {@link System} resources.
     * </ol>
     */
    private static VirtualizedSystem PHYSICAL_SYSTEM;

    /**
     * A {@link VirtualizedSystem} to use when a {@link VirtualizedSystem}
     * for a {@link VirtualizedSystemClassLoader} can't be located.
     */
    private static VirtualizedSystem s_defaultSystem;

    /**
     * The {@link VirtualizedSystem} associated with the current {@link Thread}.
     */
    private static InheritableThreadLocal<VirtualizedSystem> s_virtualizedSystem;

    /**
     * The {@link AvailablePortIterator} that the {@link Virtualization}
     * will use for allocating ports
     */
    private static AvailablePortIterator s_availablePortIterator;


    /**
     * Starts the virtualization of {@link System} resources through the use
     * {@link ClassLoader} isolation of {@link VirtualizedSystem}s.
     */
    public synchronized static void start()
    {
        // are we virtualizing?
        if (System.getProperties() instanceof DelegatingProperties)
        {
            // skip: we're already virtualized
        }
        else
        {
            // establish the default VirtualizedSystem to use in place of the
            // physical system when a thread isn't part of a VirtualizedSystem
            s_defaultSystem = new VirtualizedSystem("(Default Virtual System)",
                                                    PHYSICAL_SYSTEM.getProperties(),
                                                    PHYSICAL_SYSTEM.getStdOut(),
                                                    PHYSICAL_SYSTEM.getStdErr(),
                                                    new VirtualizedMBeanServerBuilder(s_availablePortIterator));

            // ensure that the JMX MBean Server for the default system is the
            // DelegatingMBeanServerBuilder so that we can isolate MBeanServers
            s_defaultSystem.getProperties().setProperty(VirtualizedMBeanServerBuilder.PROPERTY_JMX_MBEAN_SERVER_BUILDER,
                                                        DelegatingMBeanServerBuilder.class.getCanonicalName());

            System.setProperties(new DelegatingProperties());
            System.setOut(new PrintStream(new DelegatingStdOutOutputStream(PHYSICAL_SYSTEM.getStdOut()), true));
            System.setErr(new PrintStream(new DelegatingStdErrOutputStream(PHYSICAL_SYSTEM.getStdErr()), true));
        }
    }


    /**
     * Stops the virtualization of {@link System} resources.
     */
    public synchronized static void stop()
    {
        // are we virtualizing?
        if (System.getProperties() instanceof DelegatingProperties)
        {
            System.setProperties(PHYSICAL_SYSTEM.getProperties());
            System.setOut(PHYSICAL_SYSTEM.getStdOut());
            System.setErr(PHYSICAL_SYSTEM.getStdErr());
        }
        else
        {
            // we're not virtualized
        }
    }


    /**
     * Obtains the {@link VirtualizedSystem} for the calling {@link Thread}
     * or <code>null</code> if the {@link Thread} is not associated with
     * a {@link VirtualizedSystem}.
     * <p>
     * {@link Virtualization} does not need to have started for this method
     * to return the correct {@link VirtualizedSystem}.
     *
     * @return  the {@link VirtualizedSystem} or <code>null</code>
     */
    public static VirtualizedSystem getVirtualizedSystem()
    {
        // attempt to determine the virtualized system based on the calling Thread
        VirtualizedSystem system = s_virtualizedSystem.get();

        if (system == null)
        {
            // as it's not associated with the calling thread, try to find
            // the virtualized system using the class loader of the thread
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            while (system == null && classLoader != null)
            {
                if (classLoader instanceof VirtualizedSystemClassLoader)
                {
                    system = ((VirtualizedSystemClassLoader) classLoader).getVirtualizedSystem();
                }
                else
                {
                    classLoader = classLoader.getParent();
                }
            }
        }

        return system;
    }


    /**
     * Obtains the {@link VirtualizedSystem} for the calling {@link Thread}.
     * <p>
     * If the {@link Thread} is not being virtualized, the default
     * {@link VirtualizedSystem} is returned.  That is,
     * unlike {@link #getVirtualizedSystem()}, this method never returns
     * <code>null</code>.
     *
     * @return  a {@link VirtualizedSystem}
     */
    public static VirtualizedSystem getSystem()
    {
        VirtualizedSystem system = getVirtualizedSystem();

        return system == null ? s_defaultSystem : system;
    }


    /**
     * Obtains a {@link VirtualizedSystem} representation of the physical {@link System}.
     *
     * @return  a {@link VirtualizedSystem} representation of the physical {@link System}
     */
    public static VirtualizedSystem getPhysicalSystem()
    {
        return PHYSICAL_SYSTEM;
    }


    /**
     * Associates the specified {@link VirtualizedSystem} with the calling
     * {@link Thread} (if not already associated).
     *
     * @param system  the {@link VirtualizedSystem} for the calling {@link Thread}
     */
    public static void associateThreadWith(VirtualizedSystem system)
    {
        if (system == null)
        {
            // nothing to do when attempts to associate with a null VirtualSystem
        }
        else
        {
            VirtualizedSystem existingSystem = s_virtualizedSystem.get();

            if (existingSystem == null)
            {
                s_virtualizedSystem.set(system);
            }
            else
            {
                throw new IllegalStateException(String
                    .format("Attempted to setVirtualizedSystem(%s) on Thread [%s] when it is already associated with VirtualizedSystem(%s)",
                            system.getName(), Thread.currentThread(), existingSystem.getName()));
            }
        }
    }


    /**
     * Dissociates the {@link VirtualizedSystem} currently associated with the calling
     * {@link Thread}.
     * <p>
     * After calling this method, the calling {@link Thread} will no longer be associated
     * with it's {@link VirtualizedSystem}, except perhaps via its context
     * {@link ClassLoader}.
     */
    public static void dissociateThread()
    {
        if (s_virtualizedSystem.get() == null)
        {
            // nothing to do when a thread isn't associated with a VirtualSystem
        }
        else
        {
            s_virtualizedSystem.remove();
        }
    }


    /**
     * Obtains the {@link AvailablePortIterator} for the {@link Virtualization}.
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
        // establish the virtualized system thread local to track them
        s_virtualizedSystem = new InheritableThreadLocal<VirtualizedSystem>();

        // create an available port iterator for allocating ports
        try
        {
            // attempt to create an available port iterator on this host
            s_availablePortIterator = new AvailablePortIterator(30000);

            // create a representation of the physical system, as a virtual system
            PHYSICAL_SYSTEM = new VirtualizedSystem(s_availablePortIterator);
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException("Failed to start Virtualization", e);
        }
    }
}
