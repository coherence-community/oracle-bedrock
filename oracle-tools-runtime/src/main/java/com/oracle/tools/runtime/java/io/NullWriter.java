/*
 * File: NullWriter.java
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

package com.oracle.tools.runtime.java.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link java.io.Writer} that discards of all output sent to it.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NullWriter extends Writer
{
    /**
     * Has the Writer been closed?
     */
    private AtomicBoolean m_isClosed;


    /**
     * Constructs a {@link NullWriter}.
     */
    public NullWriter()
    {
        super();
        m_isClosed = new AtomicBoolean(false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        m_isClosed.set(true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException
    {
        if (m_isClosed.get())
        {
            throw new IOException("NullWriter is closed");
        }
    }


    /**
     * Method description
     *
     * @param chars
     * @param i
     * @param i2
     *
     * @throws IOException
     */
    @Override
    public void write(char[] chars,
                      int    i,
                      int    i2) throws IOException
    {
        if (m_isClosed.get())
        {
            throw new IOException("NullWriter is closed");
        }
    }
}
