/*
 * File: Scope.java
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

package com.oracle.tools.runtime.java.container;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import java.util.Properties;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServerBuilder;

/**
 * Defines the resources used by an executing Java application with in the
 * constraints of a particular names {@link Scope}.
 * <p>
 * The primarily use for {@link Scope}s is to capture and isolate the
 * resources used by Java applications that are executing "in-process", in much
 * the same way as an application server would do with a Java EE application.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Scope
{
    /**
     * Obtains the name of the {@link Scope}.
     *
     * @return the name of the {@link Scope}
     */
    public String getName();


    /**
     * Obtains the {@link Properties} defined for the {@link Scope}.
     *
     * @return the {@link Scope} {@link Properties}
     */
    public Properties getProperties();


    /**
     * Obtains the Standard Out {@link PrintStream} for the {@link Scope}.
     *
     * @return the Standard Out {@link PrintStream}
     */
    public PrintStream getStandardOutput();


    /**
     * Obtains the Standard Error {@link PrintStream} for the {@link Scope}.
     *
     * @return the Standard Error {@link PrintStream}
     */
    public PrintStream getStandardError();


    /**
     * Obtains the Standard Input {@link InputStream} for the {@link Scope}.
     *
     * @return the Standard Input {@link InputStream}
     */
    public InputStream getStandardInput();


    /**
     * Obtains an {@link AvailablePortIterator} that may be used to acquire
     * unique ports at runtime for the {@link Scope}.
     *
     * @return an {@link AvailablePortIterator}
     */
    public AvailablePortIterator getAvailablePorts();


    /**
     * Closes the {@link Scope}, after which the resources may longer be used.
     *
     * @return <code>true</code> if the {@link Scope} was closed,
     *         <code>false</code> if the {@link Scope} was previously closed.
     */
    public boolean close();
}
