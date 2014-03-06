package com.oracle.tools.runtime.java.container;

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
    public int read() throws IOException {
        return getDelegateInputStream().read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return getDelegateInputStream().read(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getDelegateInputStream().read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    public long skip(long n) throws IOException {
        return getDelegateInputStream().skip(n);
    }

    /**
     * {@inheritDoc}
     */
    public int available() throws IOException {
        return getDelegateInputStream().available();
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        getDelegateInputStream().close();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void mark(int readlimit) {
        getDelegateInputStream().mark(readlimit);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void reset() throws IOException {
        getDelegateInputStream().reset();
    }

    /**
     * {@inheritDoc}
     */
    public boolean markSupported() {
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

        if (scope == null) {
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
