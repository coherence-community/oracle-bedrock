/*
 * File: CapturingApplicationConsole.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.LinkedList;
import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An implementation of an {@link com.oracle.tools.runtime.ApplicationConsole} that
 * captures and keeps the last <i>n</i> lines of output on StdOut and StdErr. This
 * console also allows StdIn to be piped to the application.
 * The number of lines to capture and keep is configurable by a constructor argument.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CapturingApplicationConsole extends AbstractPipedApplicationConsole
{
    /**
     * The maximum number of lines of output that will be captured and stored
     * by this {link CapturingApplicationConsole}.
     */
    private int maximumLines;

    /**
     * The lines of output captured from StdOut
     */
    private ConcurrentLinkedQueue<String> stdoutBuffer;

    /**
     * The lines of output captured from StdErr
     */
    private ConcurrentLinkedQueue<String> stderrBuffer;

    /**
     * The {@link Thread} capturing StdOut lines
     */
    protected Thread stdoutThread;

    /**
     * The {@link Thread} capturing StdErr lines
     */
    protected Thread stderrThread;


    /**
     * Constructs {@link CapturingApplicationConsole}.
     * </p>
     * This constructor will set the maximum number of lines to capture to {@link Integer#MAX_VALUE}.
     *
     * @throws java.io.IOException
     */
    public CapturingApplicationConsole() throws IOException
    {
        this(Integer.MAX_VALUE, false, DEFAULT_PIPE_SIZE);
    }


    /**
     * Constructs {@link CapturingApplicationConsole}.
     *
     * @param maximumLines  the number of lines of output to capture
     *
     * @throws IOException
     */
    public CapturingApplicationConsole(int maximumLines) throws IOException
    {
        this(maximumLines, false, DEFAULT_PIPE_SIZE);
    }


    /**
     * Constructs {@link CapturingApplicationConsole}.
     *
     * @param maximumLines    the number of lines of output to keep
     * @param diagnosticMode  if true, output to this console is not formatted
     *                        with application details or line numbers
     *
     * @throws IOException
     */
    public CapturingApplicationConsole(int     maximumLines,
                                       boolean diagnosticMode) throws IOException
    {
        this(maximumLines, diagnosticMode, DEFAULT_PIPE_SIZE);
    }


    /**
     * Constructs {@link CapturingApplicationConsole}.
     *
     * @param maximumLines    the number of lines of output to keep
     * @param diagnosticMode  if true, output to this console is not formatted
     *                        with application details or line numbers
     * @param pipeSize        the size of the pipe's buffers
     *
     * @throws IOException
     */
    public CapturingApplicationConsole(int     maximumLines,
                                       boolean diagnosticMode,
                                       int     pipeSize) throws IOException
    {
        super(pipeSize, diagnosticMode);

        this.maximumLines = maximumLines;

        this.stdoutBuffer = new ConcurrentLinkedQueue<String>();
        this.stderrBuffer = new ConcurrentLinkedQueue<String>();

        this.stdoutThread = new Thread(new OutputCaptor(stdoutReader, stdoutBuffer));
        this.stderrThread = new Thread(new OutputCaptor(stderrReader, stderrBuffer));

        this.stdoutThread.start();
        this.stderrThread.start();
    }


    @Override
    public void close()
    {
        super.close();

        try
        {
            stdoutThread.join();
            stderrThread.join();
        }
        catch (InterruptedException e)
        {
            // Ignored
        }
    }


    /**
     * Obtain a {@link LinkedList} containing the lines captured
     * from the applications StdOut. The most recent line will
     * be at the tail of the list and the oldest line will be
     * at the head of the list.
     *
     * @return a {@link LinkedList} containing the lines captured
     *         from the applications StdOut
     */
    public Queue<String> getCapturedOutputLines()
    {
        return stdoutBuffer;
    }


    /**
     * Obtain a {@link LinkedList} containing the lines captured
     * from the applications StdErr. The most recent line will
     * be at the tail of the list and the oldest line will be
     * at the head of the list.
     *
     * @return a {@link LinkedList} containing the lines captured
     *         from the applications StdErr
     */
    public Queue<String> getCapturedErrorLines()
    {
        return stderrBuffer;
    }


    /**
     * Obtains a {@link PrintWriter} that can be used to write to the stdin
     * of an {@link com.oracle.tools.runtime.ApplicationConsole}.
     *
     * @return a {@link PrintWriter}
     */
    public PrintWriter getInputWriter()
    {
        return stdinWriter;
    }


    /**
     * The {@link Runnable} used to capture lines of output.
     */
    class OutputCaptor implements Runnable
    {
        /**
         * The {@link BufferedReader} to capture output from
         */
        BufferedReader reader;

        /**
         * The {@link LinkedList} to store lines of output in
         */
        ConcurrentLinkedQueue<String> lines;


        /**
         * Create an {@link OutputCaptor}.
         *
         * @param reader  The {@link BufferedReader} to capture output from
         * @param lines   The {@link LinkedList} to store lines of output in
         */
        OutputCaptor(BufferedReader                reader,
                     ConcurrentLinkedQueue<String> lines)
        {
            this.reader = reader;
            this.lines  = lines;
        }


        /**
         * The {@link Runnable#run()} method for this {@link OutputCaptor}
         * that will capture output.
         */
        @Override
        public void run()
        {
            try
            {
                String line = reader.readLine();

                while (line != null)
                {
                    if (lines.size() >= CapturingApplicationConsole.this.maximumLines)
                    {
                        lines.poll();
                    }

                    lines.add(line);
                    line = reader.readLine();
                }
            }
            catch (IOException e)
            {
                // Skip: Likely caused by application termination
            }
        }
    }
}
