/*
 * File: AbstractScope.java
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

package com.oracle.bedrock.runtime.java.container;

import com.oracle.bedrock.runtime.network.AvailablePortIterator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A base implementation of a {@link Scope}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractScope implements Scope
{
    /**
     * The name of the application scope.  This is primarily used for diagnostic
     * purposes.
     */
    protected String name;

    /**
     * The System {@link java.util.Properties} to be used when an application is
     * running in this {@link Scope}.
     */
    protected Properties properties;

    /**
     * Is the {@link Scope} closed?  If it is, it can be used.
     */
    protected AtomicBoolean closed;

    /**
     * The Standard Out {@link java.io.PrintStream} to be used when an application
     * is in this {@link Scope}.
     */
    protected PrintStream stdout;

    /**
     * The Standard Error {@link java.io.PrintStream} to be used when an application
     * is in this {@link Scope}.
     */
    protected PrintStream stderr;

    /**
     * The Standard Input {@link java.io.InputStream} to be used when an application
     * is in this {@link Scope}.
     */
    protected InputStream stdin;

    /**
     * The {@link AvailablePortIterator} that can be used by an application
     * to determine available ports in this {@link Scope}.
     */
    protected AvailablePortIterator availablePorts;


    /**
     * Constructs a {@link AbstractScope}.
     *
     * @param name            the name of the {@link Scope}
     * @param properties      the {@link java.util.Properties} for the {@link Scope}
     * @param availablePorts  an {@link AvailablePortIterator} to provide the
     *                        {@link Scope} with unique and available ports at runtime
     */
    public AbstractScope(String                name,
                         Properties            properties,
                         AvailablePortIterator availablePorts)
    {
        this.name           = name;
        this.properties     = properties;
        this.availablePorts = availablePorts;
        this.closed         = new AtomicBoolean(false);
        this.stdout         = null;
        this.stderr         = null;
        this.stdin          = null;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public Properties getProperties()
    {
        return properties;
    }


    @Override
    public PrintStream getStandardOutput()
    {
        return stdout;
    }


    @Override
    public PrintStream getStandardError()
    {
        return stderr;
    }


    @Override
    public InputStream getStandardInput()
    {
        return stdin;
    }


    @Override
    public AvailablePortIterator getAvailablePorts()
    {
        return availablePorts;
    }


    @Override
    public boolean close()
    {
        return closed.compareAndSet(false, true);
    }
}
