/*
 * File: OutputRedirector.java
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

import java.io.InputStream;

/**
 * An {@link OutputRedirector} pipes output from an {@link InputStream},
 * typically of some {@link ApplicationProcess} to an {@link ApplicationConsole}.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class OutputRedirector
    extends Thread
{
    /**
     * The name of the {@link com.oracle.bedrock.runtime.Application}
     * who's output is being redirected.
     */
    private String applicationName;

    /**
     * The prefix to write in-front of lines sent to the {@link ApplicationConsole}.
     */
    private String prefix;

    /**
     * The {@link ApplicationProcess} identifier.
     */
    private long processId;

    /**
     * Should diagnostic information be logged/output.
     */
    private boolean diagnosticsEnabled;

    /**
     * The {@link InputStream} from which context will be read.
     */
    private InputStream inputStream;

    /**
     * The {@link ApplicationConsole} to which to write content.
     */
    private ApplicationConsole console;
    
    /**
     * Start this {@link OutputRedirector}.
     *
     * @param applicationName            the name of the application
     * @param prefix                     the prefix to output on each console line
     *                                   (typically this is the abbreviation of the stream
     *                                   like "stderr" or "stdout")
     * @param inputStream                the {@link InputStream} from which to read content
     * @param console                    the {@link ApplicationConsole} to which to write content
     * @param processId                  the {@link ApplicationProcess} identifier
     * @param diagnosticsEnabled         should diagnostic information be logged/output
     */
    public void start(String             applicationName,
                      String             prefix,
                      InputStream        inputStream,
                      ApplicationConsole console,
                      long               processId,
                      boolean            diagnosticsEnabled)
    {
        this.applicationName           = applicationName;
        this.prefix                    = prefix;
        this.inputStream               = inputStream;
        this.console                   = console;
        this.processId                 = processId;
        this.diagnosticsEnabled        = diagnosticsEnabled && !(console instanceof SystemApplicationConsole);

        setDaemon(true);
        start();
    }

    /**
     * Obtain the name of the application.
     *
     * @return  the name of the application
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Obtain the prefix to output on each console line.
     *
     * @return the prefix to output on each console line
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Obtain
     *
     * @return
     */
    public long getProcessId()
    {
        return processId;
    }

    /**
     * Determine whether diagnostic information be logged/output
     *
     * @return  {@code true} if diagnostic information be logged/output
     */
    public boolean isDiagnosticsEnabled()
    {
        return diagnosticsEnabled;
    }

    /**
     * Obtain the {@link InputStream} from which to read content.
     *
     * @return  the {@link InputStream} from which to read content
     */
    public InputStream getInputStream()
    {
        return inputStream;
    }

    /**
     * Obtain the {@link ApplicationConsole} to which to write content.
     *
     * @return  the {@link ApplicationConsole} to which to write content
     */
    public ApplicationConsole getConsole()
    {
        return console;
    }
}
