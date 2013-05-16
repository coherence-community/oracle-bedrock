/*
 * File: NullApplicationConsole.java
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

import com.oracle.tools.runtime.java.io.NullReader;
import com.oracle.tools.runtime.java.io.NullWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * A {@link NullApplicationConsole} is an implementation of an
 * {@link ApplicationConsole} that silently ignores attempts to output.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NullApplicationConsole implements ApplicationConsole
{
    /**
     * The Reader for the {@link ApplicationConsole}.
     */
    private NullReader m_inputReader;

    /**
     * The Writer for the {@link ApplicationConsole} (stdout).
     */
    private PrintWriter m_outputWriter;

    /**
     * The Writer for the {@link ApplicationConsole} (stderr).
     */
    private PrintWriter m_errorWriter;


    /**
     * Constructs a {@link NullApplicationConsole}.
     */
    public NullApplicationConsole()
    {
        m_inputReader  = new NullReader();
        m_outputWriter = new PrintWriter(new NullWriter());
        m_errorWriter  = new PrintWriter(new NullWriter());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        m_outputWriter.close();
        m_errorWriter.close();

        try
        {
            m_inputReader.close();
        }
        catch (IOException e)
        {
            // SKIP: we ignore exceptions when closing
        }
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
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getErrorWriter()
    {
        return m_errorWriter;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getInputReader()
    {
        return m_inputReader;
    }
}
