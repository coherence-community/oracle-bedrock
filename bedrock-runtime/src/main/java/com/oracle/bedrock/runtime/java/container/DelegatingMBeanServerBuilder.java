/*
 * File: DelegatingMBeanServerBuilder.java
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

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;

/**
 * A {@link DelegatingMBeanServerBuilder} is an {@link MBeanServerBuilder}
 * that delegates requests onto the appropriate {@link Scope}
 * {@link MBeanServerBuilder}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DelegatingMBeanServerBuilder extends MBeanServerBuilder
{
    /**
     * Constructs a {@link DelegatingMBeanServerBuilder}.
     */
    public DelegatingMBeanServerBuilder()
    {
        // required for the MBeanServerFactory contract
    }


    /**
     * Obtains the {@link MBeanServerBuilder} on which to delegate calls.
     *
     * @return  the {@link MBeanServerBuilder} delegate.
     */
    private MBeanServerBuilder getDelegate()
    {
        ContainerScope scope = Container.getContainerScope();

        return scope == null ? Container.getDefaultScope().getMBeanServerBuilder() : scope.getMBeanServerBuilder();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanServer newMBeanServer(String              domain,
                                      MBeanServer         mBeanServer,
                                      MBeanServerDelegate mBeanServerDelegate)
    {
        return getDelegate().newMBeanServer(domain, mBeanServer, mBeanServerDelegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanServerDelegate newMBeanServerDelegate()
    {
        return getDelegate().newMBeanServerDelegate();
    }
}
