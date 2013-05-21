/*
 * File: PlatformScope.java
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

package com.oracle.tools.runtime.java.container;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import java.util.Properties;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link Scope} that encapsulates the native platform resources.
 * <p>
 * This is {@link Scope} is useful to make a snapshot of the underlying Java
 * Virtual Machine resources so that then may then be mutated and later returned
 * to their original state.  Additionally instance of this {@link Scope} may be
 * used at runtime to directly access the Java Virtual Machine platform resources,
 * especially when running applications "in container" mode.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class PlatformScope extends AbstractScope
{
    /**
     * (strictly package level only) Constructs a {@link PlatformScope} based on the
     * current state of the Java Virtual Machine Platform.
     * <p>
     * The resulting {@link PlatformScope} will thus represent the current Java
     * Virtual Machine settings, resources and properties.
     *
     * @param portIterator  the {@link com.oracle.tools.runtime.network.AvailablePortIterator} for requesting
     *                      system ports.
     */
    public PlatformScope(AvailablePortIterator portIterator)
    {
        super("(Java Virtual Machine)", System.getProperties(), portIterator);

        m_stdout = System.out;
        m_stderr = System.err;
        m_stdin  = System.in;
    }
}
