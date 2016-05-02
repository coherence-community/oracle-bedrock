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

package com.oracle.bedrock.runtime.concurrent.runnable;

import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.options.ApplicationClosingBehavior;
import com.oracle.bedrock.Option;

import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;

/**
 * A {@link RemoteRunnable} to perform a {@link System#exit(int)}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SystemExit implements RemoteRunnable, ApplicationClosingBehavior<JavaApplication>
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
    private SystemExit(int exitCode)
    {
        this.exitCode = exitCode;
    }


    @Override
    public void run()
    {
        System.out.println("Terminating Application using System.exit(" + exitCode + ")");

        System.exit(exitCode);
    }


    @Override
    public void onBeforeClosing(JavaApplication application,
                                Option...       options)
    {
        try
        {
            // submit the System.exit request to the application
            application.submit(this);

            application.waitFor(options);
        }
        catch (IllegalStateException e)
        {
            // we ignore exceptions that occurred as we are attempting to close the application
        }
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof SystemExit))
        {
            return false;
        }

        SystemExit that = (SystemExit) other;

        if (exitCode != that.exitCode)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return exitCode;
    }


    /**
     * Constructs a {@link SystemExit} with a specified exit code.
     *
     * @param exitCode  the exit code to use
     *
     * @return a {@link SystemExit}
     */
    public static SystemExit withExitCode(int exitCode)
    {
        return new SystemExit(exitCode);
    }
}
