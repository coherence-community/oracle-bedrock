/*
 * File: AbstractPipedApplicationConsole.java
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
    /**
     * The default pipe size.
     */
    public static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * Readers and Writers for managing stdout.
     */
    protected BufferedReader stdoutReader;
    protected PrintWriter    stdoutWriter;
    protected PipedWriter    stdoutPipedWriter;

    /**
     * Readers and Writers for managing stderr.
     */
    protected BufferedReader stderrReader;
    protected PrintWriter    stderrWriter;
    protected PipedWriter    stderrPipedWriter;

    /**
     * Readers and Writers for managing stdin.
     */
    protected PipedReader stdinReader;
    protected PrintWriter stdinWriter;

    /**
     * Will all stdout and stderr also be output to the underlying
     * native platform streams.
     */
    protected boolean diagnosticMode;


    /**
     * Constructs an {@link AbstractPipedApplicationConsole}.
     *
     * @param pipeSize         the size of the pipe's buffers
     * @param diagnosticMode   if true, output to this console is formatted
     *                         with application details and line numbers
     *
     * @see java.io.PipedReader
     * @see java.io.PipedWriter
     *
     * @throws RuntimeException if an error occurs creating this {@link AbstractPipedApplicationConsole}
     */
    public AbstractPipedApplicationConsole(int     pipeSize,
                                           boolean diagnosticMode)
    {
        try
        {
            PipedReader pipedOutputReader = new PipedReader(pipeSize);

            this.diagnosticMode    = diagnosticMode;

            this.stdoutReader      = new BufferedReader(pipedOutputReader);
            this.stdoutPipedWriter = new PipedWriter(pipedOutputReader);
            this.stdoutWriter      = new PrintWriter(stdoutPipedWriter);

            PipedReader pipedErrorReader = new PipedReader(pipeSize);

            this.stderrReader      = new BufferedReader(pipedErrorReader);
            this.stderrPipedWriter = new PipedWriter(pipedErrorReader);
            this.stderrWriter      = new PrintWriter(stderrPipedWriter);

            this.stdinReader       = new PipedReader(pipeSize);
            this.stdinWriter       = new PrintWriter(new PipedWriter(stdinReader));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error creating console streams", e);
        }
    }


    @Override
    public PrintWriter getOutputWriter()
    {
        return stdoutWriter;
    }


    @Override
    public PrintWriter getErrorWriter()
    {
        return stderrWriter;
    }


    @Override
    public Reader getInputReader()
    {
        return stdinReader;
    }


    @Override
    public boolean isDiagnosticsEnabled()
    {
        return diagnosticMode;
    }


    @Override
    public void close()
    {
        try
        {
            stdinReader.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }

        try
        {
            stdoutPipedWriter.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }

        try
        {
            stderrPipedWriter.close();
        }
        catch (IOException e)
        {
            // SKIP: ignore exceptions
        }
    }
}
