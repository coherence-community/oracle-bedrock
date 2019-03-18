/*
 * File: InputRedirector.java
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

import java.io.OutputStream;
import java.io.Reader;

/**
 * An {@link InputRedirector} pipes input to an {@link OutputStream},
 * typically from an {@link ApplicationConsole} to a {@link Process}.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class InputRedirector
        extends Thread
{
    /**
     * The {@link OutputStream} to which the content read from the
     * {@link Reader} will be written.
     */
    private OutputStream outputStream;

    /**
     * The {@link ApplicationConsole} from which to read content.
     */
    private ApplicationConsole console;

    /**
     * Start this {@link InputRedirector}.
     *
     * @param out      the {@link OutputStream} to which to write content
     * @param console  the {@link ApplicationConsole} for the process
     */
    public void start(OutputStream out, ApplicationConsole console)
    {
        this.outputStream = out;
        this.console      = console;
        
        setDaemon(true);
        start();
    }

    /**
     * Obtain the {@link OutputStream} to which the content read from
     *        the {@link Reader} will be written.
     * 
     * @return  the {@link OutputStream} to which the content read from
     *          the {@link Reader} will be written
     */
    public OutputStream getOutputStream()
    {
        return outputStream;
    }


    /**
     * Obtain the {@link ApplicationConsole} from which to read content.
     *
     * @return  the {@link ApplicationConsole} from which to read content
     */
    public ApplicationConsole getConsole()
    {
        return console;
    }
}
