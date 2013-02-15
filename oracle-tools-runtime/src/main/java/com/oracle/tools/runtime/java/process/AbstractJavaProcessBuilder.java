/*
 * File: AbstractJavaProcessBuilder.java
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

package com.oracle.tools.runtime.java.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link AbstractJavaProcessBuilder} is a base implementation of a
 * {@link JavaProcessBuilder}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public abstract class AbstractJavaProcessBuilder implements JavaProcessBuilder
{
    /**
     * We delegate most of the {@link JavaProcessBuilder} onto a regular
     * Java {@link ProcessBuilder}.
     */
    protected ProcessBuilder m_delegateProcessBuilder;

    /**
     * The system properties for the {@link JavaProcessBuilder}.
     */
    protected Properties m_systemProperties;

    /**
     * The arguments for the {@link JavaProcessBuilder}.
     */
    protected List<String> m_arguments;

    /**
     * The application class name that will be used to execute the
     * Java {@link Process}.
     */
    protected String m_applicationClassName;

    /**
     * The name of the application.  This is typically used for display purposes.
     */
    protected String m_applicationName;


    /**
     * Construct an {@link AbstractJavaProcessBuilder} for the
     * specified application.
     *
     * @param applicationName       the name of the application
     * @param applicationClassName  the name of the application class
     */
    protected AbstractJavaProcessBuilder(String applicationName,
                                         String applicationClassName)
    {
        this("java", applicationName, applicationClassName);
    }


    /**
     * Construct an {@link AbstractJavaProcessBuilder} with the
     * specified executable name for the specified application
     *
     * @param executableName        the name of the jvm executable (typically "java")
     * @param applicationName       the name of the application
     * @param applicationClassName  the name of the application class
     */
    public AbstractJavaProcessBuilder(String executableName,
                                      String applicationName,
                                      String applicationClassName)
    {
        m_applicationName        = applicationName;
        m_applicationClassName   = applicationClassName;
        m_delegateProcessBuilder = new ProcessBuilder(executableName);
        m_systemProperties       = new Properties();
        m_arguments              = new ArrayList<String>();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getApplicationName()
    {
        return m_applicationName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getApplicationClassName()
    {
        return m_applicationClassName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder setCommands(List<String> commands)
    {
        if (commands == null)
        {
            throw new NullPointerException();
        }

        m_delegateProcessBuilder.command(new ArrayList<String>(commands));

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder setCommands(String... commands)
    {
        return setCommands(Arrays.asList(commands));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getCommands()
    {
        return m_delegateProcessBuilder.command();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getEnvironment()
    {
        return m_delegateProcessBuilder.environment();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public File getWorkingDirectory()
    {
        return m_delegateProcessBuilder.directory();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder setWorkingDirectory(File directory)
    {
        m_delegateProcessBuilder.directory(directory);

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getSystemProperties()
    {
        return m_systemProperties;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder setSystemProperty(String name,
                                                String value)
    {
        m_systemProperties.setProperty(name, value);

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getArguments()
    {
        return m_arguments;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder addArgument(String argument)
    {
        m_arguments.add(argument);

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder addArguments(Iterable<String> arguments)
    {
        if (arguments != null)
        {
            for (String argument : arguments)
            {
                addArgument(argument);
            }
        }

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JavaProcessBuilder addArguments(String... arguments)
    {
        for (String argument : arguments)
        {
            addArgument(argument);
        }

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Process realize() throws IOException
    {
        return m_delegateProcessBuilder.start();
    }
}
