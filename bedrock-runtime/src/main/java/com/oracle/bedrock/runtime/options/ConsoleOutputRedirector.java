/*
 * File: ConsoleOutputRedirector.java
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
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.console.OutputRedirector;
import com.oracle.bedrock.runtime.console.StdOutRedirector;

/**
 * An {@link Option} to set the redirector to use to redirect a
 * process stdout stream.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ConsoleOutputRedirector
        implements Option
{
    private final OutputRedirector redirector;

    private ConsoleOutputRedirector(OutputRedirector redirector)
    {
        this.redirector = redirector == null ? new StdOutRedirector() : redirector;
    }

    /**
     * Obtain the configured {@link OutputRedirector}.
     *
     * @return  the configured {@link OutputRedirector}
     */
    public OutputRedirector getRedirector()
    {
        return redirector;
    }

    /**
     * Obtain the default {@link ConsoleOutputRedirector} option.
     *
     * @return  the default {@link ConsoleOutputRedirector} option
     */
    @OptionsByType.Default
    public static ConsoleOutputRedirector defaultRedirector()
    {
        return new ConsoleOutputRedirector(new StdOutRedirector());
    }

    /**
     * Obtain a {@link ConsoleOutputRedirector} option that uses
     * the specified {@link OutputRedirector}.
     *
     * @return a {@link ConsoleOutputRedirector} option that uses
     *         the specified {@link OutputRedirector}
     */
    public static ConsoleOutputRedirector of(OutputRedirector redirector)
    {
        return new ConsoleOutputRedirector(redirector);
    }
}
