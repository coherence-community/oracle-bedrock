/*
 * File: VirtualMachinePlatformSchema.java
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

package com.oracle.tools.runtime.virtual;

import com.oracle.tools.runtime.FluentPlatformSchema;

/**
 * An {@link FluentPlatformSchema} extension defining a {@link VirtualPlatform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 *
 * @param <P>  the type of {@link com.oracle.tools.runtime.Platform} that can be configured by the {@link FluentPlatformSchema}
 * @param <S>  the type of {@link FluentPlatformSchema} that will be returned from fluent methods
 */
public abstract class VirtualPlatformSchema<P extends VirtualPlatform,
                                                   S extends VirtualPlatformSchema<P, S>>
    implements FluentPlatformSchema<P, S>
{
    /** The name of the virtual machine */
    private String      name;

    /** The {@link CloseAction} to take when the VM is closed */
    private CloseAction closeAction;

    /** A flag indicating whether this schema can be used to realize multiple VMs */
    private boolean isSingleton = true;

    /**
     * Construct a new {@link VirtualPlatformSchema}
     *
     * @param name         the name of the VM
     * @param isSingleton  flag indicating whether this schema can be
     *                     used to realize multiple VMs
     */
    protected VirtualPlatformSchema(String name, boolean isSingleton)
    {
        this.name        = name;
        this.isSingleton = isSingleton;
    }


    @Override
    public boolean isSingleton()
    {
        return isSingleton;
    }

    /**
     * Obtain the name of the VM to realize from this schema
     *
     * @return the name of the VM to realize from this schema
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtain the action to take on closing the VM.
     *
     * @return the action to take on closing the VM
     */
    public CloseAction getCloseAction()
    {
        return closeAction;
    }


    /**
     * Set the action to take on closing the VM
     *
     * @param closeAction  the action to take on closing the VM
     *
     * @return this {@Link VirtualPlatformSchema} for method chaining
     */
    @SuppressWarnings("unchecked")
    public S setCloseAction(CloseAction closeAction)
    {
        this.closeAction = closeAction;

        return (S) this;
    }
}
