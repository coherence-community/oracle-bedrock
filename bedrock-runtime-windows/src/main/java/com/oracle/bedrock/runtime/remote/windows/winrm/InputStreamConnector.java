/*
 * File: InputStreamConnector.java
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

package com.oracle.bedrock.runtime.remote.windows.winrm;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A {@link Thread} that connects an {@link java.io.InputStream} to the
 * stdin stream of an executing command.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class InputStreamConnector extends Thread implements Closeable
{
    /**
     * The {@link WindowsSession} running the remote command process.
     */
    private final WindowsSession session;

    /**
     * The {@link java.io.InputStream} to pipe to the StdIn of the
     * current command.
     */
    private final InputStream inputStream;


    /**
     * Create an {@link InputStreamConnector} that listens to the
     * specified {@link InputStream} and pipes lines read from it
     * to the stdin stream of the current command.
     *
     * @param session     the {@link WindowsSession} running the command
     * @param inputStream the {@link InputStream} to pipe to the stdin
     *                    stream of the command
     */
    protected InputStreamConnector(WindowsSession session,
                                   InputStream    inputStream)
    {
        super("InputStreamConnector-" + session.getCommandId());
        this.session     = session;
        this.inputStream = inputStream;
        setDaemon(true);
    }


    /**
     * Obtain the {@link InputStream} this connector pipes
     * to the remote process stdin.
     *
     * @return the {@link InputStream} this connector pipes
     *          to the remote process stdin
     */
    public InputStream getInputStream()
    {
        return inputStream;
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
     * Close this {@link InputStreamConnector}
     */
    @Override
    public void close() throws IOException
    {
        inputStream.close();
    }


    /**
     *  The run loop for this {@link InputStreamConnector}.
     */
    @Override
    public void run()
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        boolean        run    = true;

        while (run)
        {
            run = pollForInput(reader);
        }
    }


    /**
     * Read a line from the specified {@link BufferedReader}
     * and send the data to the stdin stream of the remote
     * process.
     *
     * @param reader the {@link BufferedReader} to read a line
     *               of data from
     *
     * @return true if a line was read or false if EOF was
     *         read from the {@link BufferedReader}.
     *
     */
    protected boolean pollForInput(BufferedReader reader)
    {
        try
        {
            String line = reader.readLine();

            if (line == null)
            {
                return false;
            }

            session.writeToInputStream(line);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return true;
    }
}
