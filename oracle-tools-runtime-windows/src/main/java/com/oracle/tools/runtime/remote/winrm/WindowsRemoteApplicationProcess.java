/*
 * File: WindowsRemoteApplicationProcess.java
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

import com.oracle.tools.runtime.remote.RemoteApplicationProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.util.List;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link RemoteApplicationProcess} running in a Windows O/S.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsRemoteApplicationProcess implements RemoteApplicationProcess
{
    /**
     * An {@link AtomicLong} used to provide a unique ID for
     * each {@link WindowsRemoteApplicationProcess}
     */
    private static AtomicLong idCounter = new AtomicLong(0L);

    /**
     * The {@link WindowsSession} running the process that this
     * {@link WindowsRemoteApplicationProcess} represents.
     */
    private final WindowsSession session;

    /**
     * The {@link PipedInputStream} to use to read the remote
     * process stdout stream.
     */
    private PipedInputStream pipedStdOutInputStream;

    /**
     * The {@link PipedOutputStream} to that the remote process
     * stdout will be written to.
     */
    private PipedOutputStream pipedStdOutOutputStream;

    /**
     * The {@link PipedInputStream} to use to read the remote
     * process stderr stream.
     */
    private PipedInputStream pipedStdErrInputStream;

    /**
     * The {@link PipedOutputStream} to that the remote process
     * stderr will be written to.
     */
    private PipedOutputStream pipedStdErrOutputStream;

    /**
     * The {@link PipedInputStream} that will be read from to
     * pipe input to the remote process stdin stream.
     */
    private PipedInputStream pipedStdInInputStream;

    /**
     * The {@link PipedOutputStream} to use to write to the
     * remote process stdin stream.
     */
    private PipedOutputStream pipedStdInOutputStream;

    /**
     * The ID of this{@link WindowsRemoteApplicationProcess}.
     */
    private long id;


    /**
     * Create a {@link WindowsRemoteApplicationProcess} wrapping
     * the specified {@link WindowsSession} running the remote
     * process.
     *
     * @param session {@link WindowsSession} running the
     *                remote process.
     */
    public WindowsRemoteApplicationProcess(WindowsSession session)
    {
        try
        {
            this.session                 = session;
            this.id                      = idCounter.incrementAndGet();
            this.pipedStdOutInputStream  = new PipedInputStream(2048);
            this.pipedStdErrInputStream  = new PipedInputStream(2048);
            this.pipedStdInInputStream   = new PipedInputStream(2048);
            this.pipedStdOutOutputStream = new PipedOutputStream(pipedStdOutInputStream);
            this.pipedStdErrOutputStream = new PipedOutputStream(pipedStdErrInputStream);
            this.pipedStdInOutputStream  = new PipedOutputStream(pipedStdInInputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error creating remote process streams", e);
        }
    }


    @Override
    public void close()
    {
        session.close();
    }


    @Override
    public long getId()
    {
        return id;
    }


    @Override
    @Deprecated
    public void destroy()
    {
        close();
    }


    @Override
    public int exitValue()
    {
        return session.exitValue();
    }


    @Override
    public InputStream getErrorStream()
    {
        return pipedStdErrInputStream;
    }


    @Override
    public InputStream getInputStream()
    {
        return pipedStdOutInputStream;
    }


    @Override
    public OutputStream getOutputStream()
    {
        return pipedStdInOutputStream;
    }


    @Override
    public int waitFor()
    {
        return session.waitFor();
    }


    /**
     * Execute the specified command using the current
     * {@link WindowsSession}.
     *
     * @param command the command to execute
     * @param args    the command line arguments for the command
     *
     * @throws IOException if an error occurs executing the command
     */
    public void execute(String       command,
                        List<String> args) throws IOException
    {
        session.execute(command, args, pipedStdInInputStream, pipedStdOutOutputStream, pipedStdErrOutputStream);
    }
}
