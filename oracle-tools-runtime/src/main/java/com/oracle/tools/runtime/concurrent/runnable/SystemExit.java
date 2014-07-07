/*
 * File: SystemExit.java
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

package com.oracle.tools.runtime.concurrent.runnable;

import com.oracle.tools.runtime.concurrent.RemoteRunnable;

/**
 * A {@link RemoteRunnable} to perform a {@link System#exit(int)}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SystemExit implements RemoteRunnable
{
    /**
     * The desired system exit code (by default 0);
     */
    private int exitCode;


    /**
     * Constructs a {@link SystemExit}
     * (using the default exit code 0).
     */
    public SystemExit()
    {
        this(0);
    }


    /**
     * Constructs a {@link SystemExit} for a specific exit code.
     *
     * @param exitCode  the desired exit code
     */
    public SystemExit(int exitCode)
    {
        this.exitCode = exitCode;
    }


    @Override
    public void run()
    {
        System.out.println("Terminating Application (due to SystemExit) [exitcode=" + exitCode + "]");

        System.exit(exitCode);
    }
}
