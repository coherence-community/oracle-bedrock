/*
 * File: AbstractContainerScope.java
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

import java.io.InputStream;
import java.io.PrintStream;

import java.util.Properties;

/**
 * An abstract {@link Scope} that provides isolation of container-specific
 * resources, in particular MBeanServers and a copy of System properties.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractContainerScope extends AbstractScope
{
    /**
     * The {@link ContainerMBeanServerBuilder} to be used when an application
     * is in this {@link AbstractContainerScope}.
     */
    private ContainerMBeanServerBuilder m_mBeanServerBuilder;


    /**
     * Constructs an {@link AbstractContainerScope}.
     *
     * @param name                the name of the {@link Scope}
     * @param properties          the {@link java.util.Properties} for the {@link Scope}
     * @param availablePorts      an {@link AvailablePortIterator} to provide the
     *                            {@link Scope} with unique and available ports at runtime
     * @param mBeanServerBuilder  the {@link ContainerMBeanServerBuilder}
     *                            (if null a default will be created)
     */
    public AbstractContainerScope(String                      name,
                                  Properties                  properties,
                                  AvailablePortIterator       availablePorts,
                                  ContainerMBeanServerBuilder mBeanServerBuilder)
    {
        super(name, new Properties(), availablePorts);

        // add a copy of the specified properties to the scope
        // (instead of holding a strong reference)
        if (properties != null)
        {
            m_properties.putAll(properties);
        }

        m_mBeanServerBuilder = mBeanServerBuilder == null
                               ? new ContainerMBeanServerBuilder(m_availablePorts) : mBeanServerBuilder;
    }


    /**
     * Obtains the {@link javax.management.MBeanServerBuilder} for this {@link ContainerScope}.
     *
     * @return the {@link javax.management.MBeanServerBuilder}
     */
    public ContainerMBeanServerBuilder getMBeanServerBuilder()
    {
        return m_mBeanServerBuilder;
    }


    /**
     * {@inheritDoc}
     */
    public boolean close()
    {
        if (super.close())
        {
            // close the MBeanServers created by the MBeanServerBuilder
            m_mBeanServerBuilder.close();

            return true;
        }
        else
        {
            return false;
        }
    }
}
