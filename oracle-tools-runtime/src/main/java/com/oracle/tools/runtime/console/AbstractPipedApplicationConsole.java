/*
 * File: PipedApplicationConsole.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * A base class for {@link ApplicationConsole} implementations that pipe all output to
 * appropriate readers and all input to appropriate writers.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 *
 * @see java.io.PipedReader
 * @see java.io.PipedWriter
 */
public abstract class AbstractPipedApplicationConsole implements ApplicationConsole
{
    public static final int DEFAULT_PIPE_SIZE = 1024;

    protected BufferedReader m_outputReader;
    protected PrintWriter    m_outputWriter;
    protected PipedWriter    m_outputPipedWriter;
    protected BufferedReader m_errorReader;
    protected PrintWriter    m_errorWriter;
    protected PipedWriter    m_errorPipedWriter;
    protected PipedReader    m_inputReader;
    protected PrintWriter    m_inputWriter;
    protected boolean        m_diagnosticMode;

    /**
     * Constructs an {@link AbstractPipedApplicationConsole}.
     *
     * @param pipeSize   the size of the pipe's buffers
     * @param diagnosticMode   if true, output to this console is formatted
     *                         with application details and line numbers
     *
     * @see java.io.PipedReader
     * @see java.io.PipedWriter
     *
     * @throws java.io.IOException if an error occurs creating this {@link AbstractPipedApplicationConsole}
     */
    public AbstractPipedApplicationConsole(int pipeSize, boolean diagnosticMode) throws IOException
    {
        PipedReader pipedOutputReader = new PipedReader(pipeSize);

        m_diagnosticMode    = diagnosticMode;

        m_outputReader      = new BufferedReader(pipedOutputReader);
        m_outputPipedWriter = new PipedWriter(pipedOutputReader);
        m_outputWriter      = new PrintWriter(m_outputPipedWriter);

        PipedReader pipedErrorReader = new PipedReader(pipeSize);

        m_errorReader      = new BufferedReader(pipedErrorReader);
        m_errorPipedWriter = new PipedWriter(pipedErrorReader);
        m_errorWriter      = new PrintWriter(m_errorPipedWriter);

        m_inputReader      = new PipedReader(pipeSize);
        m_inputWriter      = new PrintWriter(new PipedWriter(m_inputReader));
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


    @Override
    public boolean isDiagnosticsEnabled()
    {
        return m_diagnosticMode;
    }


    @Override
    public void close()
    {
        try
        {
            m_inputReader.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }

        try
        {
            m_outputPipedWriter.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }

        try
        {
            m_errorPipedWriter.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }
    }
}
