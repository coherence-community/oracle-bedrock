/*
 * File: AbstractRemoteTerminal.java
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

package com.oracle.tools.runtime.remote;

/**
 * An abstract {@link RemoteTerminal}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class AbstractRemoteTerminal implements RemoteTerminal
{
    /**
     * The {@link RemotePlatform} on which the {@link RemoteTerminal} will operate.
     */
    private RemotePlatform platform;


    /**
     * Constructs an {@link AbstractRemoteTerminal} for a {@link RemotePlatform}
     *
     * @param platform  the {@link RemotePlatform}
     */
    public AbstractRemoteTerminal(RemotePlatform platform)
    {
        this.platform = platform;
    }


    /**
     * Obtain the {@link RemotePlatform}.
     *
     * @return the {@link RemotePlatform}
     */
    public RemotePlatform getRemotePlatform()
    {
        return platform;
    }
}
