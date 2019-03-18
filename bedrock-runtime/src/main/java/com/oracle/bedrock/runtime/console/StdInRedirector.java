/*
 * File: StdInRedirector.java
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

package com.oracle.bedrock.runtime.console;

import com.oracle.bedrock.runtime.ApplicationConsole;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * A {@link InputRedirector} pipes input to an {@link OutputStream},
 * typically from an {@link ApplicationConsole} to a {@link Process}.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class StdInRedirector
        extends InputRedirector
{
    @Override
    public void run()
    {
        try
        {
            Reader         reader         = getConsole().getInputReader();
            BufferedReader bufferedReader = new BufferedReader(reader);
            OutputStream   outputStream   = getOutputStream();
            PrintWriter    printWriter    = new PrintWriter(outputStream);

            while (true)
            {
                String line = bufferedReader.readLine();

                if (line == null)
                {
                    break;
                }

                printWriter.println(line);
                printWriter.flush();
            }
        }
        catch (Exception exception)
        {
            // SKIP: deliberately empty as we safely assume exceptions
            // are always due to process termination.
        }
    }
}
