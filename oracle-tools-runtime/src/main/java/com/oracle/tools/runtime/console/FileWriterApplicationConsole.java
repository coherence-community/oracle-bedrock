/*
 * File: FileWriterApplicationConsole.java
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

package com.oracle.tools.runtime.console;

import com.oracle.tools.runtime.ApplicationConsole;

import java.io.FileWriter;
import java.io.IOException;

/**
 * A {@link FileWriterApplicationConsole} is an implementation of an
 * {@link ApplicationConsole} that logs output to a file.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Christer Fahlgren
 */
public class FileWriterApplicationConsole implements ApplicationConsole
{
    /**
     * The {@link FileWriter} used to write to a file.
     */
    private FileWriter m_fileWriter;


    /**
     * Constructs a {@link FileWriterApplicationConsole}.
     *
     * @param fileWriter  the {@link FileWriter} to use for the console
     */
    public FileWriterApplicationConsole(FileWriter fileWriter)
    {
        m_fileWriter = fileWriter;
    }


    /**
     * Constructs a {@link FileWriterApplicationConsole}.
     *
     * @param fileName  the file name of the log file
     *
     * @throws IOException if opening the file fails
     */
    public FileWriterApplicationConsole(String fileName) throws IOException
    {
        this(new FileWriter(fileName, true));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void printf(String    format,
                       Object... args)
    {
        String formattedString = String.format(format, args);

        try
        {
            m_fileWriter.write(formattedString);
            m_fileWriter.flush();
        }
        catch (IOException e)
        {
            // failing to log is serious, but we have no outlet but to log the occurrence to stdout
            System.out.println("Exception occured while writing to " + m_fileWriter + " the exception was " + e);
            e.printStackTrace();
        }
    }
}
