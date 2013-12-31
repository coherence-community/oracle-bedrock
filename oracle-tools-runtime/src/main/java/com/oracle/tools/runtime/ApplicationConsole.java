/*
 * File: ApplicationConsole.java
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

package com.oracle.tools.runtime;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Provides a mechanism to interact with the console of an {@link Application}
 * including stdout, stderr and stdin.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface ApplicationConsole extends Closeable
{
    /**
     * Obtains the {@link PrintWriter} that an {@link Application} will use
     * for stdout.
     */
    public PrintWriter getOutputWriter();


    /**
     * Obtains the {@link PrintWriter} that an {@link Application} will use
     * for stderr.
     */
    public PrintWriter getErrorWriter();


    /**
     * Obtains the {@link Reader} that an {@link Application} will use
     * for stdin.
     */
    public Reader getInputReader();


    /**
     * Closes the {@link ApplicationConsole}, after which it may nolonger
     * be used.
     */
    public void close();
}
