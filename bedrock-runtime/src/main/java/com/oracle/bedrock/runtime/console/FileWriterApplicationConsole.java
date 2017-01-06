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

package com.oracle.bedrock.runtime.console;

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import com.oracle.bedrock.runtime.java.container.Container;

import java.io.File;
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
     * If true, application output should be formatted to
     * include application information.
     */
    private boolean m_diagnosticMode;


    /**
     * Constructs a {@link FileWriterApplicationConsole}.
     *
     * @param fileWriter  the {@link FileWriter} to use for the console
     */
    public FileWriterApplicationConsole(FileWriter fileWriter)
    {
        this(fileWriter, true);
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
        this(new FileWriter(fileName, true), true);
    }


    /**
     * Constructs a {@link FileWriterApplicationConsole}.
     *
     * @param fileWriter       the {@link FileWriter} to use for the console
     * @param diagnosticMode   if true, output to this console is formatted
     *                         with application details and line numbers
     */
    public FileWriterApplicationConsole(FileWriter fileWriter,
                                        boolean    diagnosticMode)
    {
        m_diagnosticMode = diagnosticMode;
        m_fileWriter     = fileWriter;

        m_outputWriter   = new PrintWriter(fileWriter);
        m_errorWriter    = new PrintWriter(fileWriter);
        m_inputReader    = new InputStreamReader(Container.getPlatformScope().getStandardInput())
        {
            @Override
            public void close() throws IOException
            {
            }
        };
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
            // SKIP: we don't care if an exception occurs - we're closing
        }

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


    /**
     * Obtains a {@link ApplicationConsoleBuilder} for the
     * {@link FileWriterApplicationConsole}.
     *
     * @param directory  the existing directory in which to create the files
     * @param prefix     the prefix to use for files (may be null)
     *
     * @return a {@link ApplicationConsoleBuilder}
     */
    public static ApplicationConsoleBuilder builder(final String directory,
                                                    final String prefix)
    {
        return new ApplicationConsoleBuilder()
        {
            @Override
            public ApplicationConsole build(String applicationName)
            {
                // normalize the directory
                String normalizedDirectory = directory.trim();

                if (!normalizedDirectory.endsWith(File.separator))
                {
                    normalizedDirectory = normalizedDirectory + File.separator;
                }

                // ensure the directory exists
                File file = new File(normalizedDirectory);

                if (file.exists())
                {
                    // normalize the prefix
                    String normalizedPrefix = prefix == null ? "" : prefix.trim();

                    if (normalizedPrefix.length() > 0 &&!normalizedPrefix.endsWith("-"))
                    {
                        normalizedPrefix = normalizedPrefix + "-";
                    }

                    // normalize the application name
                    String normalizedApplicationName = applicationName.trim();

                    String fileName = normalizedDirectory + normalizedPrefix + normalizedApplicationName;

                    try
                    {
                        return new FileWriterApplicationConsole(fileName);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException("Failed to create file:" + fileName, e);
                    }
                }
                else
                {
                    throw new RuntimeException("The specified directory [" + directory + "] does not exist");
                }
            }
        };
    }
}
