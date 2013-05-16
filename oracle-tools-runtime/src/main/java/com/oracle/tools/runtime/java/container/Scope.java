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
 * Captures and encapsulates resources that need to be isolated for applications
 * running in a container.  Typical resources that require isolation the include
 * Java {@link System} properties, Standard I/O streams and MBean servers.
 * <p>
 * The primarily use of this class is for creating, scoping and isolating
 * resources required when attempting to execute regular Java applications
 * "in-process", in much the same way as an application server would do with
 * a Java EE application.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class Scope
{
    /**
     * The name of the {@link Scope}.  This is primarily used for diagnostic
     * purposes.
     */
    protected String m_name;

    /**
     * The System {@link Properties} to be used when an application is in this
     * {@link Scope}.
     */
    protected Properties m_properties;

    /**
     * Is the {@link Scope} closed?  If it is, it can be used.
     */
    protected AtomicBoolean m_isClosed;

    /**
     * The Standard Out {@link PrintStream} to be used when an application
     * is in this {@link Scope}.
     */
    protected PrintStream m_stdout;

    /**
     * The Standard Error {@link PrintStream} to be used when an application
     * is in this {@link Scope}.
     */
    protected PrintStream m_stderr;

    /**
     * The Standard Input {@link InputStream} to be used when an application
     * is in this {@link Scope}.
     */
    protected InputStream m_stdin;

    /**
     * The {@link AvailablePortIterator} that can be used by an application
     * to determine available ports.
     */
    protected AvailablePortIterator m_availablePorts;


    /**
     * Constructs a {@link Scope}.
     *
     * @param name                 the name of the {@link Scope}
     * @param properties           the System {@link Properties} for the {@link Scope}
     */
    public Scope(String                name,
                 Properties            properties,
                 AvailablePortIterator availablePorts)
    {
        m_name           = name;
        m_properties     = properties;
        m_availablePorts = availablePorts;
        m_isClosed       = new AtomicBoolean(false);
    }


    /**
     * Obtains the name of the {@link Scope}.
     *
     * @return the name of the {@link Scope}
     */
    public String getName()
    {
        return m_name;
    }


    /**
     * Obtains the {@link Properties} of the {@link Scope}.
     *
     * @return the {@link Scope} {@link Properties}
     */
    public Properties getProperties()
    {
        return m_properties;
    }


    /**
     * Obtains the Standard Out {@link PrintStream} of the {@link Scope}.
     *
     * @return the Standard Out {@link PrintStream}
     */
    public PrintStream getStandardOutput()
    {
        return m_stdout;
    }


    /**
     * Obtains the Standard Error {@link PrintStream} of the {@link Scope}.
     *
     * @return the Standard Error {@link PrintStream}
     */
    public PrintStream getStandardError()
    {
        return m_stderr;
    }


    /**
     * Obtains the Standard Input {@link InputStream} for the {@link Scope}.
     *
     * @return the Standard Input {@link InputStream}
     */
    public InputStream getStandardInput()
    {
        return m_stdin;
    }


    /**
     * Closes the {@link Scope}, after which the resources can't be used.
     */
    public abstract void close();
}
