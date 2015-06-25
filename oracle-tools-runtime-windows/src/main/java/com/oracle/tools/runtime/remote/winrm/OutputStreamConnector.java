/*
 * File: OutputStreamConnector.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.microsoft.wsman.shell.CommandStateType;
import com.microsoft.wsman.shell.ReceiveResponse;
import com.microsoft.wsman.shell.StreamType;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An implementation of a {@link Thread} that polls a
 * remote WinRM shell command for the contents of its
 * output streams and then pipes that data to corresponding
 * {@link OutputStream}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class OutputStreamConnector extends Thread implements Closeable
{
    /**
     * The {@link WindowsSession} running the remote command process.
     */
    private final WindowsSession session;

    /**
     * The {@link java.io.OutputStream} to write the process stdout stream to.
     */
    private OutputStream stdOut;

    /**
     * The {@link OutputStream} to write the process stderr stream to.
     */
    private OutputStream stdErr;

    /**
     * Flag indicating that the process loop should continue to run
     */
    private final AtomicBoolean run = new AtomicBoolean(true);

    /**
     * The exit code of the remote process
     */
    private Integer exitCode = null;


    /**
     * Create an {@link OutputStreamConnector} to link the output streams
     * of the remote process controlled by the {@link WindowsSession}.
     *
     * @param session the {@link WindowsSession} running the remote process
     * @param stdOut  the {@link OutputStream} to write the process stdout stream to
     * @param stdErr  the {@link OutputStream} to write the process stderr stream to
     */
    protected OutputStreamConnector(WindowsSession session,
                                    OutputStream   stdOut,
                                    OutputStream   stdErr)
    {
        super("OutputStreamConnector-" + session.getCommandId());
        this.session = session;
        this.stdOut  = stdOut;
        this.stdErr  = stdErr;
        setDaemon(true);
    }


    /**
     * Obtain the {@link OutputStream} this connector
     * pipes the remote process stdout to.
     *
     * @return the {@link OutputStream} this connector
     *         pipes the remote process stdout to
     */
    public OutputStream getOutputStream()
    {
        return stdOut;
    }


    /**
     * Obtain the {@link OutputStream} this connector
     * pipes the remote process stderr to.
     *
     * @return the {@link OutputStream} this connector
     *         pipes the remote process stderr to
     */
    public OutputStream getErrorStream()
    {
        return stdErr;
    }


    /**
     * Obtain the {@link WindowsSession} running the
     * remote process.
     *
     * @return the {@link WindowsSession} running
     *         the remote process
     */
    public WindowsSession getSession()
    {
        return session;
    }


    /**
     * Closes this {@link OutputStreamConnector} and
     * stops the run loop.
     */
    @Override
    public void close()
    {
        run.set(false);
        waitFor();
    }


    /**
     * Causes the current thread to wait, if necessary, until the remote
     * command has terminated.
     *
     * @return the exit code of the remote command. By convention,
     *         the value 0 indicates normal termination
     *
     * @throws RuntimeException if there was a problem waiting for termination
     */
    public Integer waitFor()
    {
        synchronized (this)
        {
            if (exitCode != null)
            {
                return exitCode;
            }

            try
            {
                this.wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return exitCode;
    }


    /**
     * Obtain the exit value of the remote process or -1
     * if the process has not yet terminated.
     *
     * @return the exit value of the remote process or -1
     *          if the process has not yet terminated
     */
    public int getExitCode()
    {
        return exitCode != null ? exitCode : -1;
    }


    /**
     * Poll for the current process output from the remote command.
     *
     * @return true if the process is still running otherwise false.
     *
     * @throws IOException if an error occurs.
     */
    public boolean pollOutput() throws IOException
    {
        ReceiveResponse response       = session.readOutputStreams();
        boolean         shouldContinue = true;

        for (StreamType output : response.getStream())
        {
            if (output.isSetName() && output.isSetValue())
            {
                if ("stderr".equals(output.getName()) && stdErr != null)
                {
                    stdErr.write(output.getValue());
                    stdErr.flush();
                }
                else if ("stdout".equals(output.getName()) && stdOut != null)
                {
                    stdOut.write(output.getValue());
                    stdOut.flush();
                }
            }
        }

        if (response.isSetCommandState())
        {
            CommandStateType commandState = response.getCommandState();

            if (commandState.isSetExitCode())
            {
                exitCode       = commandState.getExitCode().intValue();
                shouldContinue = false;
            }
        }

        return shouldContinue;
    }


    /**
     * The run loop for this {@link Thread}.
     */
    @Override
    public void run()
    {
        try
        {
            while (run.get())
            {
                run.compareAndSet(true, pollOutput());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            exitCode = 1;
        }

        synchronized (this)
        {
            if (exitCode == null)
            {
                exitCode = 0;
            }

            this.notifyAll();
        }
    }
}
