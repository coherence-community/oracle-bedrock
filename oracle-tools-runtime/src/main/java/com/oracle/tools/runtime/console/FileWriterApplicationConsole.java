/*
 * File: FileWriterApplicationConsole.java
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

import com.oracle.tools.runtime.java.container.Container;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * An {@link ApplicationConsole} that writes standard output and error streams
 * to a specified file, and uses the platform standard input for an input stream.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class FileWriterApplicationConsole implements ApplicationConsole
{
    /**
     * The {@link FileWriter} used to write to a file.
     */
    private FileWriter m_fileWriter;

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
     * Constructs a {@link FileWriterApplicationConsole}.
     *
     * @param fileWriter  the {@link FileWriter} to use for the console
     */
    public FileWriterApplicationConsole(FileWriter fileWriter)
    {
        m_fileWriter   = fileWriter;

        m_outputWriter = new PrintWriter(fileWriter);
        m_errorWriter  = new PrintWriter(fileWriter);
        m_inputReader  = new InputStreamReader(Container.getPlatformScope().getStandardInput());

    }


    /**
     * Constructs a {@link FileWriterApplicationConsole}.
     *
     * @param fileName  the file name of the log file
     *
     * @throws IOException if opening the file fails
     */
    public FileWriterApplicationConsole(String fileName) throws IOException
    {
        this(new FileWriter(fileName, true));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        try
        {
            m_fileWriter.close();
        }
        catch (IOException e)
        {
            // SKIP: we don't care if an exception occurs - we're closing
        }
        finally
        {
            m_fileWriter   = null;
            m_outputWriter = null;
            m_errorWriter  = null;
            m_inputReader  = null;
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
