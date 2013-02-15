/*
 * File: VirtualProcessBuilder.java
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

import com.oracle.tools.runtime.java.virtualization.Virtualization;
import com.oracle.tools.runtime.java.virtualization.VirtualizationClassLoader;
import com.oracle.tools.runtime.java.virtualization.VirtualizedSystemClassLoader;

import java.io.IOException;

/**
 * An implementation of {@link AbstractJavaProcessBuilder} that builds an
 * instance of {@link VirtualProcess}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class VirtualProcessBuilder extends AbstractJavaProcessBuilder
{
    /**
     * The name of the start method for the application {@link Process}.
     */
    private String m_startMethodName;

    /**
     * The name of the stop method for the application {@link Process}.
     */
    private String m_stopMethodName;


    /**
     * Constructs an {@link VirtualProcessBuilder}.
     *
     * @param applicationName       the name of the application
     * @param applicationClassName  the {@link Class} of the application
     * @param startMethodName       the method to start the application
     * @param stopMethodName        the method to stop the application
     */
    public VirtualProcessBuilder(String applicationName,
                                 String applicationClassName,
                                 String startMethodName,
                                 String stopMethodName)
    {
        super(applicationName, applicationClassName);

        m_startMethodName = startMethodName;
        m_stopMethodName  = stopMethodName;
    }


    /**
     * Obtains the name of the method to start the application {@link Process}.
     *
     * @return  the name of the method to start the application
     */
    public String getStartMethodName()
    {
        return m_startMethodName;
    }


    /**
     * Obtains the name of the method to stop the application {@link Process}.
     *
     * @return  the name of the method to stop the application
     */
    public String getStopMethodName()
    {
        return m_stopMethodName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Process realize() throws IOException
    {
        try
        {
            // create a new classloader for the application
            VirtualizedSystemClassLoader classLoader = createClassLoader();

            // create an internal process for the application using the
            // classloader to isolate it
            VirtualProcess process = new VirtualProcess(getApplicationClassName(),
                                                        classLoader,
                                                        m_startMethodName,
                                                        m_stopMethodName,
                                                        m_arguments);

            // start the internal application
            process.start();

            return process;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to start InternalProcess", e);
        }
    }


    /**
     * Creates an appropriate {@link ClassLoader} for the application {@link VirtualProcess}.
     *
     * @return a {@link ClassLoader} for the application {@link VirtualProcess}
     *
     * @throws Exception when it's not possible to create an appropriate {@link ClassLoader}
     */
    VirtualizedSystemClassLoader createClassLoader() throws Exception
    {
        // create a new classloader for the application
        String classPath = getEnvironment().get("CLASSPATH");

        VirtualizedSystemClassLoader classLoader = VirtualizationClassLoader.newInstance(getApplicationName(),
                                                                                         classPath,
                                                                                         getSystemProperties());

        Virtualization.start();

        return classLoader;
    }
}
