package com.oracle.tools.runtime.java.container;

import java.io.InputStream;

/**
 * A {@link DelegatingStdInInputStream} is an {@link AbstractDelegatingInputStream}
 * implementation for the StdIn {@link java.io.InputStream} of a {@link Scope}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DelegatingStdInInputStream extends AbstractDelegatingInputStream {

    /**
     * Constructs a {@link DelegatingStdInInputStream}
     *
     * @param inputStream  the fall-back input stream to use if the
     *                     {@link Scope} can't be determined
     */
    public DelegatingStdInInputStream(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStreamFor(Scope scope) {
        return scope.getStandardInput();
    }
}
