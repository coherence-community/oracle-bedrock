/*
 * File: WindowsTerminals.java
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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;

import com.oracle.tools.runtime.remote.winrm.WindowsRemoteTerminal;

/**
 * Helper methods to construct various builders for Windows-based
 * {@link RemoteTerminal}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsTerminals
{
    /**
     * Create a {@link RemoteTerminalBuilder} that creates {@link WindowsRemoteTerminal} instances
     * running Windows cmd.exe.
     *
     * @return a {@link RemoteTerminalBuilder} that creates {@link WindowsRemoteTerminal} instances
     *         running cmd.exe
     */
    public static RemoteTerminalBuilder cmd()
    {
        return new RemoteTerminalBuilder()
        {
            @Override
            public <A extends Application, S extends ApplicationSchema<A>,
                    E extends RemoteApplicationEnvironment> RemoteTerminal<A, S, E> realize(RemotePlatform platform)
            {
                return new WindowsRemoteTerminal<>(platform);
            }
        };
    }
}
