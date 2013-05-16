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
 * An {@link com.oracle.tools.runtime.ApplicationConsole} pipes all output to
 * appropriate readers and all input to appropriate writers.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PipedApplicationConsole implements ApplicationConsole
{
    private BufferedReader m_outputReader;
    private PrintWriter    m_outputWriter;
    private BufferedReader m_errorReader;
    private PrintWriter    m_errorWriter;
    private PipedReader    m_inputReader;
    private PrintWriter    m_inputWriter;


    /**
     * Constructs {@link }PipedApplicationConsole}.
     *
     * @throws IOException
     */
    public PipedApplicationConsole() throws IOException
    {
        PipedReader pipedOutputReader = new PipedReader();

        m_outputReader = new BufferedReader(pipedOutputReader);
        m_outputWriter = new PrintWriter(new PipedWriter(pipedOutputReader));

        PipedReader pipedErrorReader = new PipedReader();

        m_errorReader = new BufferedReader(pipedErrorReader);
        m_errorWriter = new PrintWriter(new PipedWriter(pipedErrorReader));

        m_inputReader = new PipedReader();
        m_inputWriter = new PrintWriter(new PipedWriter(m_inputReader));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getOutputWriter()
    {
        return m_outputWriter;
    }


    /**
     * Obtains a {@link BufferedReader} that can be used to read the stdout
     * from an {@link ApplicationConsole}.
     *
     * @return a {@link BufferedReader}
     */
    public BufferedReader getOutputReader()
    {
        return m_outputReader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getErrorWriter()
    {
        return m_errorWriter;
    }


    /**
     * Obtains a {@link BufferedReader} that can be used to read the stderr
     * from an {@link ApplicationConsole}.
     *
     * @return a {@link BufferedReader}
     */
    public BufferedReader getErrorReader()
    {
        return m_errorReader;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getInputReader()
    {
        return m_inputReader;
    }


    /**
     * Obtains a {@link PrintWriter} that can be used to write to the stdin
     * of an {@link ApplicationConsole}.
     *
     * @return a {@link PrintWriter}
     */
    public PrintWriter getInputWriter()
    {
        return m_inputWriter;
    }


    /**
     * {@inheritDoc}
     */
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

        m_inputWriter.close();

        try
        {
            m_outputReader.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }

        m_outputWriter.close();

        try
        {
            m_errorReader.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }

        m_errorWriter.close();
    }
}
