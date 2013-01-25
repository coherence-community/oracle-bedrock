/*
 * File: AbstractDelegatingOutputStream.java
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

package com.oracle.tools.runtime.java.virtualization;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link AbstractDelegatingOutputStream} is a {@link FilterOutputStream}
 * that delegates output to an appropriate {@link VirtualizedSystem}
 * {@link OutputStream}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractDelegatingOutputStream extends FilterOutputStream
{
    /**
     * Constructs a {@link AbstractDelegatingOutputStream}
     *
     * @param outputStream  the {@link OutputStream} to which to send output
     *                      when a {@link VirtualizedSystem} can't be acquired
     *                      for a calling {@link Thread}
     */
    public AbstractDelegatingOutputStream(OutputStream outputStream)
    {
        super(outputStream);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int value) throws IOException
    {
        VirtualizedSystem system = Virtualization.getSystem();

        if (system == null)
        {
            super.write(value);
        }
        else
        {
            getOutputStreamFor(system).write(value);
        }
    }


    /**
     * Obtains the {@link OutputStream} from the {@link VirtualizedSystem} to
     * which output will be delegated.
     *
     * @param system  the {@link VirtualizedSystem}
     *
     * @return  the {@link OutputStream} for the {@link VirtualizedSystem}
     */
    public abstract OutputStream getOutputStreamFor(VirtualizedSystem system);
}
