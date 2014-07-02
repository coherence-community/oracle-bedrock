/*
 * File: SystemApplicationConsole.java
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

package com.oracle.tools.runtime.console;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationConsoleBuilder;
import com.oracle.tools.runtime.java.container.Container;
import com.oracle.tools.runtime.java.container.Scope;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * An {@link ApplicationConsole} that delegates i/o to the System.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SystemApplicationConsole implements ApplicationConsole
{
    /**
     * The Standard Output {@link PrintWriter}.
     */
    private PrintWriter m_outputWriter;

    /**
     * The Standard Error {@link PrintWriter}.
     */
    private PrintWriter m_errorWriter;

    /**
     * The Standard Input {@link Reader}.
     */
    private Reader m_inputReader;


    /**
     * Constructs a SystemApplicationConsole.
     */
    public SystemApplicationConsole()
    {
        init(Container.getPlatformScope());
    }

    /**
     * Initialise the streams for this console
     */
    void init(Scope scope)
    {
        m_outputWriter = new PrintWriter(scope.getStandardOutput())
        {
            @Override
            public void close()
            {
            }
        };
        m_errorWriter  = new PrintWriter(scope.getStandardError())
        {
            @Override
            public void close()
            {
            }
        };
        m_inputReader  = new InputStreamReader(scope.getStandardInput());
    }


    @Override
    public void close()
    {
        // SKIP: we don't close the System streams
    }


    @Override
    public PrintWriter getOutputWriter()
    {
        return m_outputWriter;
    }


    @Override
    public PrintWriter getErrorWriter()
    {
        return m_errorWriter;
    }


    @Override
    public Reader getInputReader()
    {
        return m_inputReader;
    }

    /**
     * Obtains the flag indicating whether output to this {@link ApplicationConsole}
     * should not be formatted to include information about the application.
     * </p>
     * This method always returns true as the {@link SystemApplicationConsole}
     * is writing to the controlling process StdOut or StdErr and the output
     * from {@link com.oracle.tools.runtime.Application}s should be differentiated
     * from the controlling process output.
     *
     * @return true as the output should always be formatted to include application information
     */
    @Override
    public boolean isDiagnosticsEnabled()
    {
        return true;
    }

    /**
     * Obtains a {@link ApplicationConsoleBuilder} for the
     * {@link SystemApplicationConsole}.
     *
     * @return a {@link ApplicationConsoleBuilder}
     */
    public static ApplicationConsoleBuilder builder()
    {
        return new ApplicationConsoleBuilder()
        {
            @Override
            public ApplicationConsole realize(String applicationName)
            {
                return new SystemApplicationConsole();
            }
        };
    }
}
