/*
 * File: LineNumberingOutputStream.java
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
 * A {@link LineNumberingOutputStream} is an {@link OutputStream} that
 * numbers each new line as it is output.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class LineNumberingOutputStream extends FilterOutputStream
{
    /**
     * The next line number to output.
     */
    private int m_lineNumber;

    /**
     * The optional prefix to output with the line numbers.
     * <p>
     * When <code>null</code> no prefix will be output.
     */
    private String m_prefix;

    /**
     * A flag to indicate if the last output was an EOL.
     */
    private boolean m_lastOutputWasEOL;


    /**
     * Constructs a {@link LineNumberingOutputStream}.
     *
     * @param outputStream  the {@link OutputStream} where output will be sent
     * @param prefix        the prefix to output with each line number
     */
    public LineNumberingOutputStream(OutputStream outputStream,
                                     String prefix)
    {
        super(outputStream);

        m_lineNumber       = 1;
        m_lastOutputWasEOL = true;
        m_prefix           = prefix == null ? "" : prefix.trim();
        m_prefix           = m_prefix.isEmpty() ? m_prefix : m_prefix + ":";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int value) throws IOException
    {
        if (m_lastOutputWasEOL && value != '\n' && value != '\r')
        {
            byte[] bytes = String.format("[%-10s%4d]: ", m_prefix, m_lineNumber++).getBytes();

            for (byte c : bytes)
            {
                super.write(c);
            }

            m_lastOutputWasEOL = false;
        }

        super.write(value);

        if (value == '\n' || value == '\r')
        {
            m_lastOutputWasEOL = true;
        }
    }
}
