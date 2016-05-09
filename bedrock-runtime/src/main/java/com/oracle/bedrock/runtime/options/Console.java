/*
 * File: Console.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import com.oracle.bedrock.runtime.console.FileWriterApplicationConsole;
import com.oracle.bedrock.runtime.console.NullApplicationConsole;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;

/**
 * An {@link Option} to represent an builder of an {@link ApplicationConsole}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Console implements ApplicationConsoleBuilder
{
    /**
     * The {@link ApplicationConsole}.
     */
    private ApplicationConsole console;


    /**
     * Constructs a {@link Console}.
     *
     * @param console  the {@link ApplicationConsole} to return
     */
    private Console(ApplicationConsole console)
    {
        this.console = console;
    }


    /**
     * Constructs an {@link ApplicationConsoleBuilder} for the specified {@link ApplicationConsole}.
     *
     * @param console  the {@link ApplicationConsole}
     *
     * @return a {@link Console}
     */
    public static Console of(ApplicationConsole console)
    {
        return new Console(console);
    }


    /**
     * Constructs an {@link ApplicationConsoleBuilder} for the {@link SystemApplicationConsole}.
     *
     * @return an {@link ApplicationConsoleBuilder}
     */
    public static ApplicationConsoleBuilder system()
    {
        return SystemApplicationConsole.builder();
    }


    /**
     * Constructs an {@link ApplicationConsoleBuilder} for the {@link NullApplicationConsole}.
     *
     * @return an {@link ApplicationConsoleBuilder}
     */
    public static ApplicationConsoleBuilder none()
    {
        return NullApplicationConsole.builder();
    }


    /**
     * Constructs an {@link ApplicationConsoleBuilder} for the {@link FileWriterApplicationConsole}.
     *
     * @param directory  the directory in which to write the files
     * @param prefix     the file-name prefix
     *
     * @return an {@link ApplicationConsoleBuilder}
     */
    public static ApplicationConsoleBuilder file(String directory,
                                                 String prefix)
    {
        return FileWriterApplicationConsole.builder(directory, prefix);
    }


    @Override
    public ApplicationConsole build(String applicationName)
    {
        return console;
    }
}
