/*
 * File: AbstractDelegatingInputStream.java
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

package com.oracle.bedrock.runtime.java.container;

import com.oracle.bedrock.annotations.Internal;

import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link AbstractDelegatingInputStream} is a {@link java.io.InputStream}
 * that delegates output to an appropriate {@link Scope}
 * {@link java.io.InputStream}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
@Internal
public abstract class AbstractDelegatingInputStream extends InputStream
{
    /**
     * The default input stream to be delegated to if there is no scope
     * attached to the calling thread.
     */
    protected volatile InputStream m_inputStream;


    /**
     * Constructs a {@link AbstractDelegatingOutputStream}
     *
     * @param inputStream  the {@link java.io.OutputStream} to which to submit output
     *                     when a {@link Scope} can't be acquired
     *                     for a calling {@link Thread}
     */
    public AbstractDelegatingInputStream(InputStream inputStream)
    {
        this.m_inputStream = inputStream;
    }


    /**
     * {@inheritDoc}
     */
    public int read() throws IOException
    {
        return getDelegateInputStream().read();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException
    {
        return getDelegateInputStream().read(b);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b,
                    int    off,
                    int    len) throws IOException
    {
        return getDelegateInputStream().read(b, off, len);
    }


    /**
     * {@inheritDoc}
     */
    public long skip(long n) throws IOException
    {
        return getDelegateInputStream().skip(n);
    }


    /**
     * {@inheritDoc}
     */
    public int available() throws IOException
    {
        return getDelegateInputStream().available();
    }


    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
        getDelegateInputStream().close();
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void mark(int readlimit)
    {
        getDelegateInputStream().mark(readlimit);
    }


    /**
     * {@inheritDoc}
     */
    public synchronized void reset() throws IOException
    {
        getDelegateInputStream().reset();
    }


    /**
     * {@inheritDoc}
     */
    public boolean markSupported()
    {
        return getDelegateInputStream().markSupported();
    }


    /**
     * Obtain the {@link java.io.InputStream} to delegate to.
     * If there is a {@link Scope} attached to the current thread
     * this method will call
     * {@link AbstractDelegatingInputStream#getInputStreamFor(Scope)}
     * otherwise it returns the delegate
     * {@link AbstractDelegatingInputStream#m_inputStream}.
     *
     * @return the {@link java.io.InputStream} to delegate to.
     */
    public InputStream getDelegateInputStream()
    {
        ContainerScope scope = Container.getContainerScope();

        if (scope == null)
        {
            return m_inputStream;
        }

        return getInputStreamFor(scope);
    }


    /**
     * Obtains the {@link java.io.InputStream} from the {@link Scope} to
     * which input operations will be delegated.
     *
     * @param scope the {@link Scope}
     *
     * @return the {@link java.io.InputStream} for the {@link Scope}
     */
    public abstract InputStream getInputStreamFor(Scope scope);
}
