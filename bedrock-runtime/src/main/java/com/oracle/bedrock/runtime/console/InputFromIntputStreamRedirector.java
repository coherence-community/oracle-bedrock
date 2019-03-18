/*
 * File: InputFromIntputStreamRedirector.java
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

import com.oracle.bedrock.runtime.ApplicationProcess;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * An {@link InputFromIntputStreamRedirector} pipes output to an {@link InputStream},
 * typically of some {@link ApplicationProcess} from an {@link OutputStream}.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class InputFromIntputStreamRedirector
    extends InputRedirector
{
    /**
     * The default pipe size.
     */
    public static final int DEFAULT_PIPE_SIZE = 1024;

    /**
     * The {@link InputStream} to pipe data from.
     */
    private final InputStream inputStream;

    /**
     * The size of the buffer to use to pipe data.
     */
    private final int bufferSize;


    /**
     * A flag indicating that this {@link InputFromIntputStreamRedirector} is running.
     */
    private CountDownLatch running = new CountDownLatch(1);

    /**
     * Create a {@link InputFromIntputStreamRedirector}.
     *
     * @param inputStream  the {@link InputStream} to pipe data from
     */
    public  InputFromIntputStreamRedirector(InputStream inputStream)
    {
    this(inputStream, DEFAULT_PIPE_SIZE);
    }


    /**
     * Create a {@link InputFromIntputStreamRedirector}.
     *
     * @param inputStream  the {@link InputStream} to pipe data from
     * @param bufferSize   the size of the buffer to use to pipe data
     */
    public InputFromIntputStreamRedirector(InputStream inputStream, int bufferSize)
    {
        this.inputStream = inputStream;
        this.bufferSize   = bufferSize <= 0 ? DEFAULT_PIPE_SIZE : bufferSize;
    }


    /**
     * Determine whether this {@link InputFromIntputStreamRedirector} is running.
     *
     * @return  {@code true} if this {@link InputFromIntputStreamRedirector} is running
     */
    public boolean isRunning()
    {
        return running.getCount() > 0;
    }


    /**
     * Determine whether this {@link InputFromIntputStreamRedirector} is running.
     *
     * @param timeout  the maximum time to wait
     * @param unit     the time unit of the {@code timeout} argument
     *
     * @return  {@code true} if the count reached zero and {@code false}
     *          if the waiting time elapsed before the count reached zero
     */
    public boolean awaitRunning(long timeout, TimeUnit unit) throws InterruptedException
    {
        return running.await(timeout, unit);
    }


    @Override
    public void run()
    {
        try
        {
            running.countDown();
            
            OutputStream outputStream = getOutputStream();
            byte[]       buffer       = new byte[bufferSize];
            int          len          = inputStream.read(buffer);

            while(len >= 0)
            {
                if (len > 0)
                {
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();
                }

                len = inputStream.read(buffer);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            // SKIP: deliberately empty as we safely assume exceptions
            // are always due to process termination.
        }
    }
}
