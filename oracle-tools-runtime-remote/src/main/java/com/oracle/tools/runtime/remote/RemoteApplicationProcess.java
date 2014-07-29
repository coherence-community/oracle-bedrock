/*
 * File: RemoteApplicationProcess.java
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

package com.oracle.tools.runtime.remote;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents an {@link ApplicationProcess} that is securely executing or executed remotely.
 * <p>
 * Typically application developers would not use this interface directly as
 * the {@link Application} interface provides both higher-level concepts and
 * increased functionality over that of which is defined here.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteApplicationProcess implements ApplicationProcess
{
    /**
     * The {@link Session} for the remote application.
     */
    protected Session session;

    /**
     * The {@link ChannelExec} for the remote application.
     */
    protected ChannelExec channel;

    /**
     * The {@link InputStream} from which to read stdout from the remote application.
     */
    private InputStream inputStream;

    /**
     * The {@link OutputStream} from which to write to the stdin of the remote application.
     */
    private OutputStream outputStream;

    /**
     * The {@link InputStream} from which to read stderr from the remote application.
     */
    private InputStream errorStream;

    /**
     * The exit status of the remote application (null means we don't yet know)
     */
    private Integer exitStatus;


    /**
     * Constructs an {@link RemoteApplicationProcess}
     *
     * @param session  the {@link Session} for the remote application
     * @param channel  the {@link ChannelExec} for the remote application
     *
     * @throws IOException  when the {@link RemoteApplicationProcess} can't establish
     *                      the necessary input/output streams
     */
    public RemoteApplicationProcess(Session     session,
                                    ChannelExec channel) throws IOException
    {
        this.session = session;
        this.channel = channel;

        // establish the input/output streams for the Channel
        this.inputStream  = channel.getInputStream();
        this.outputStream = channel.getOutputStream();
        this.errorStream  = channel.getErrStream();

        // initially we don't know the exit status
        this.exitStatus = null;
    }


    @Override
    public long getId()
    {
        return channel.getId();
    }


    @Override
    @Deprecated
    public void destroy()
    {
        close();
    }


    @Override
    public void close()
    {
        // prior to closing, attempt to get the exit status (if we can)
        if (exitStatus == null &&!channel.isClosed())
        {
            exitStatus = channel.getExitStatus();
        }

        channel.disconnect();
        session.disconnect();
    }


    @Override
    public int waitFor()
    {
        if (exitStatus == null)
        {
            if (channel == null || session == null)
            {
                throw new RuntimeException("The remote application has terminated.  No exit status is available");
            }
            else
            {
                int status = channel.getExitStatus();

                while (status == -1)
                {
                    try
                    {
                        Thread.sleep(500);

                        status = channel.getExitStatus();
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException("Interrupted while waiting for application to terminate", e);
                    }
                }

                exitStatus = status;
            }
        }

        return exitStatus;
    }


    @Override
    public OutputStream getOutputStream()
    {
        return outputStream;
    }


    @Override
    public InputStream getInputStream()
    {
        return inputStream;
    }


    @Override
    public InputStream getErrorStream()
    {
        return errorStream;
    }


    @Override
    public int exitValue()
    {
        return exitStatus == null ? -1 : exitStatus;
    }
}
