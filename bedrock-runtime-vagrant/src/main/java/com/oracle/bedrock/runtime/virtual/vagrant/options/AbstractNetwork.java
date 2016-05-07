/*
 * File: AbstractNetwork.java
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

package com.oracle.bedrock.runtime.virtual.vagrant.options;

/**
 * An abstract implementation of a {@link Network}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractNetwork implements Network
{
    /**
     * The identifier of the {@link Network}.
     */
    private String id;

    /**
     * The optional mac address of the {@link Network}.
     */
    private String macAddress;


    /**
     * Constructs an {@link AbstractNetwork}.
     *
     * @param id          the network id
     * @param macAddress  the mac address (may be null)
     */
    protected AbstractNetwork(String id,
                              String macAddress)
    {
        this.id         = id;
        this.macAddress = macAddress;
    }


    @Override
    public String getId()
    {
        return id;
    }


    @Override
    public String getMacAddress()
    {
        return macAddress;
    }
}
