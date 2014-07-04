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
import java.io.PrintWriter;

/**
 * An {@link com.oracle.tools.runtime.ApplicationConsole} pipes all output to
 * appropriate readers and all input to appropriate writers.
 * <p>
 * A {link PipedApplicationConsole} uses {@link java.io.PipedWriter}s and
 * {@link java.io.PipedReader}s to read from and write to an
 * {@link com.oracle.tools.runtime.Application}'s streams. These pipes have a
 * fixed size, which defaults to 1024 bytes and is configurable using a constructor
 * parameter. If the number of bytes written to stdout or stderr by the application
 * exceeds the size of the pipe then no more output will be written to the pipes until
 * space is made available by reading from the other end of the pipe; that is, by
 * reading from this {@link PipedApplicationConsole}'s OutputReader or ErrorReader.
 * </p>
 * <b>Note:</b> If attempting to read from this {@link PipedApplicationConsole}'s
 * OutputReader or ErrorReader after the underlying {@link com.oracle.tools.runtime.Application}
 * has been closed then this {@link PipedApplicationConsole} should be closed first as this
 * will then properly close the pipes and avoid exceptions being thrown due to the {@link Thread}
 * writing to the pipe's having terminated.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see java.io.PipedReader
 * @see java.io.PipedWriter
 */
public class PipedApplicationConsole extends AbstractPipedApplicationConsole
{
    /**
     * Constructs {@link PipedApplicationConsole}.
     * </p>
     * The buffers for the pipes used by this {@link PipedApplicationConsole}
     * will be set to the default size {@link PipedApplicationConsole#DEFAULT_PIPE_SIZE}.
     * Once the buffer is filled to this size then no more output will be captured until
     * the pipes are read from.
     *
     * @see java.io.PipedReader
     * @see java.io.PipedWriter
     *
     * @throws IOException if an error occurs creating this {@link PipedApplicationConsole}
     */
    public PipedApplicationConsole() throws IOException
    {
        this(DEFAULT_PIPE_SIZE, true);
    }

    /**
     * Constructs {@link PipedApplicationConsole}.
     *
     * @param pipeSize  the size of the buffers for the
     *                  pipes used by this {@link PipedApplicationConsole}.
     *
     * @see java.io.PipedReader
     * @see java.io.PipedWriter
     *
     * @throws IOException if an error occurs creating this {@link PipedApplicationConsole}
     */
    public PipedApplicationConsole(int pipeSize) throws IOException
    {
        this(pipeSize, false);
    }

    /**
     * Constructs {@link }PipedApplicationConsole}.
     *
     * @param pipeSize         the size of the buffers for the
     *                         pipes used by this {@link PipedApplicationConsole}.
     * @param diagnosticMode   if true, output to this console is formatted
     *                         with application details and line numbers
     *
     * @see java.io.PipedReader
     * @see java.io.PipedWriter
     *
     * @throws IOException if an error occurs creating this {@link PipedApplicationConsole}
     */
    public PipedApplicationConsole(int pipeSize, boolean diagnosticMode) throws IOException
    {
        super(pipeSize, diagnosticMode);
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
     * Obtains a {@link PrintWriter} that can be used to write to the stdin
     * of an {@link ApplicationConsole}.
     *
     * @return a {@link PrintWriter}
     */
    public PrintWriter getInputWriter()
    {
        return m_inputWriter;
    }
}
