/*
 * File: NullInputStream.java
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
import java.io.InputStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link NullInputStream} is an {@link InputStream} that records input
 * to an internal queue.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class NullInputStream extends InputStream
{
    private BlockingQueue<Byte> m_bytes;


    /**
     * Constructs a {@link NullInputStream}.
     */
    public NullInputStream()
    {
        m_bytes = new LinkedBlockingQueue<Byte>();
    }


    /**
     * Add a byte to be read from the {@link InputStream}.
     *
     * @param b  the byte that is readable
     */
    public void addByte(byte b)
    {
        m_bytes.add(b);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
        try
        {
            return m_bytes.take();
        }
        catch (InterruptedException e)
        {
            throw new IOException(e);
        }
    }
}
