/*
 * File: ApplicationProcess.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.options.Timeout;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An internal interface used to represent an executing or previously executed
 * {@link Application}.
 * <p>
 * This interface is inspired by the {@link java.lang.Process} class, but is
 * represented as an interface so that we can provide multiple implementations,
 * namely to represent and control different types of executing applications,
 * including those running on the local operating system, on a remote operating
 * system, with in a container and/or multi-tenant virtualized/cloud environment.
 * <p>
 * Typically application developers would not use this interface directly as
 * the {@link Application} interface provides both higher-level concepts and
 * increased functionality over that of which is defined here.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public interface ApplicationProcess extends Closeable
{
    /**
     * Determines the {@link ApplicationProcess} identifier.
     * <p>
     * The value returned is implementation and operating system dependent.  If
     * however an identifier can't be determined then a value of -1 is returned.
     *
     * @return the numerical identifier of the process, or -1 if not available
     *         or it can't be determined.
     */
    public long getId();


    /**
     * Closes the {@link ApplicationProcess} and releases all resources maintained
     * by the {@link ApplicationProcess}.
     * <p>
     * If the {@link ApplicationProcess} is already closed, calling
     * this method has no effect.
     */
    @Override
    public void close();


    /**
     * Determines the exit value of the {@link ApplicationProcess} after it terminated.
     *
     * @return the exit value.  By convention, the value 0 indicates normal termination
     *
     * @throws IllegalStateException if the {@link ApplicationProcess} has not yet completed
     */
    public int exitValue();


    /**
     * Determines the {@link InputStream} that can be used to read StdErr
     * content from the {@link ApplicationProcess}.
     *
     * @return the InputStream of StdErr
     */
    public InputStream getErrorStream();


    /**
     * Determines the {@link InputStream} that can be used to read StdOut
     * content from the {@link ApplicationProcess}.
     *
     * @return the InputStream of StdOut
     */
    public InputStream getInputStream();


    /**
     * Determines the {@link OutputStream} that can be used to write to the StdIn
     * of the {@link ApplicationProcess}.
     *
     * @return the OutputStream to the StdIn
     */
    public OutputStream getOutputStream();


    /**
     * Causes the current thread to wait, if necessary, until the application
     * represented by this {@link ApplicationProcess} has terminated.
     *
     * @param options  the {@link Option}s to be used for waiting, including
     *                 {@link Timeout} requirements
     *
     * @return the exit code of the {@link ApplicationProcess}. By convention,
     *         the value 0 indicates normal termination
     *
     * @throws RuntimeException  if there was a problem wait for termination
     */
    public int waitFor(Option... options);
}
