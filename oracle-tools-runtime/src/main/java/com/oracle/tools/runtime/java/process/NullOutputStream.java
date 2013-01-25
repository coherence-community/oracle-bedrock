/*
 * File: NullOutputStream.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

package com.oracle.tools.runtime.java.process;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that does basically nothing.  Note that multiple
 * instances are required because the API dictates that the close method
 * must cause further invocations to all other methods to throw an
 * {@link IOException}
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Cameron Purdy
 */
public class NullOutputStream extends OutputStream
{
    /**
     * A flag to indicate if the {@link NullOutputStream} is closed.
     */
    private boolean m_isClosed;

    /**
     * The number of bytes written to the {@link NullOutputStream}.
     */
    private int m_byteCount;


    /**
     * Construct a {@link NullInputStream}.
     */
    NullOutputStream()
    {
        m_isClosed  = false;
        m_byteCount = 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException
    {
        ensureOpen();
        advance(1);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte b[]) throws IOException
    {
        ensureOpen();
        advance(b.length);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte b[],
                      int off,
                      int len) throws IOException
    {
        ensureOpen();
        advance(len);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException
    {
        ensureOpen();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        m_isClosed = true;
    }


    /**
     * Ensure that the {@link NullOutputStream} isn't closed.
     *
     * @throws IOException if the {@link NullOutputStream} is closed
     */
    private void ensureOpen() throws IOException
    {
        if (m_isClosed)
        {
            // same as PrintStream
            throw new IOException("Stream closed");
        }
    }


    /**
     * Advance the number of bytes to count
     *
     * @param count  the number of bytes to count
     */
    private void advance(int count)
    {
        count       += m_byteCount;
        m_byteCount = count < 0 ? Integer.MAX_VALUE : count;
    }
}
