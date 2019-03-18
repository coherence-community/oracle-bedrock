/*
 * File: ConsoleInputRedirector.java
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
import com.oracle.bedrock.runtime.console.InputRedirector;
import com.oracle.bedrock.runtime.console.StdInRedirector;

/**
 * An {@link Option} to set the redirector to use to redirect a
 * process stdin stream.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ConsoleInputRedirector
        implements Option
{
    private final InputRedirector redirector;

    private ConsoleInputRedirector(InputRedirector redirector)
    {
        this.redirector = redirector == null ? new StdInRedirector() : redirector;
    }

    /**
     * Obtain the configured {@link InputRedirector}.
     *
     * @return  the configured {@link InputRedirector}
     */
    public InputRedirector getRedirector()
    {
        return redirector;
    }

    /**
     * Obtain the default {@link ConsoleInputRedirector} option.
     *
     * @return  the default {@link ConsoleInputRedirector} option
     */
    @OptionsByType.Default
    public static ConsoleInputRedirector defaultRedirector()
    {
        return new ConsoleInputRedirector(new StdInRedirector());
    }

    /**
     * Obtain a {@link ConsoleInputRedirector} option that uses
     * the specified {@link InputRedirector}.
     *
     * @return a {@link ConsoleInputRedirector} option that uses
     *         the specified {@link InputRedirector}
     */
    public static ConsoleInputRedirector of(InputRedirector redirector)
    {
        return new ConsoleInputRedirector(redirector);
    }
}
