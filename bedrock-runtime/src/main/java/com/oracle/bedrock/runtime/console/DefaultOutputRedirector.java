/*
 * File: DefaultOutputRedirector.java
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
import com.oracle.bedrock.runtime.ApplicationProcess;
import com.oracle.bedrock.runtime.java.container.Container;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;

/**
 * An {@link DefaultOutputRedirector} pipes output from an {@link InputStream},
 * typically of some {@link ApplicationProcess} to an {@link ApplicationConsole}.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class DefaultOutputRedirector
    extends OutputRedirector
{
    @Override
    public void start(String             applicationName,
                      String             prefix,
                      InputStream        inputStream,
                      ApplicationConsole console,
                      long               processId,
                      boolean            diagnosticsEnabled)
    {
        super.start(applicationName, prefix, inputStream, console, processId, diagnosticsEnabled);
    }

    /**
     * Obtain the {@link PrintWriter} to use to redirect output to.
     *
     * @return  the {@link PrintWriter} to use to redirect output to
     */
    protected abstract PrintWriter ensurePrintWriter();

    @Override
    public void run()
    {
        InputStream        inputStream               = getInputStream();
        String             applicationName           = getApplicationName();
        String             prefix                    = getPrefix();
        long               processId                 = getProcessId();
        ApplicationConsole console                   = getConsole();
        boolean            diagnosticsEnabled        = isDiagnosticsEnabled();
        boolean            consoleDiagnosticsEnabled = console.isDiagnosticsEnabled();
        PrintWriter        printWriter               = ensurePrintWriter();
        long               lineNumber                = 1;

        try
        {
            BufferedReader reader  =
                new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));

            boolean        running = true;

            while (running || reader.ready())
            {
                try
                {
                    String line = reader.readLine();

                    if (line == null)
                    {
                        break;
                    }

                    String diagnosticOutput = (diagnosticsEnabled
                                               || consoleDiagnosticsEnabled) ? String.format("[%s:%s%s] %4d: %s",
                                                                                             applicationName,
                                                                                             prefix,
                                                                                             processId < 0
                                                                                             ? "" : ":" + processId,
                                                                                             lineNumber++,
                                                                                             line) : null;

                    String output = consoleDiagnosticsEnabled ? diagnosticOutput : line;

                    if (diagnosticsEnabled)
                    {
                        Container.getPlatformScope().getStandardOutput().println(output);
                    }

                    printWriter.println(output);
                    printWriter.flush();
                }
                catch (InterruptedIOException e)
                {
                    running = false;
                }
            }
        }
        catch (Exception exception)
        {
            // SKIP: deliberately empty as we safely assume exceptions
            // are always due to process termination.
        }

        try
        {
            String diagnosticOutput = (diagnosticsEnabled
                                       || consoleDiagnosticsEnabled) ? String.format("[%s:%s%s] %4d: (terminated)",
                                                                                     applicationName,
                                                                                     prefix,
                                                                                     processId < 0
                                                                                     ? "" : ":" + processId,
                                                                                     lineNumber) : null;

            String output = consoleDiagnosticsEnabled ? diagnosticOutput : "(terminated)";

            if (diagnosticsEnabled)
            {
                Container.getPlatformScope().getStandardOutput().println(output);
            }

            printWriter.println(output);
            printWriter.flush();
        }
        catch (Exception e)
        {
            // SKIP: deliberately empty as we safely assume exceptions
            // are always due to process termination.
        }
    }
}
