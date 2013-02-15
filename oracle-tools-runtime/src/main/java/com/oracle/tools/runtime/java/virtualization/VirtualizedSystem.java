/*
 * File: VirtualizedSystem.java
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

package com.oracle.tools.runtime.java.virtualization;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import javax.management.MBeanServerBuilder;
import java.io.PrintStream;
import java.util.Properties;

/**
 * A {@link VirtualizedSystem} encapsulates state and functionality typically
 * defined by the Java {@link System} class.
 * <p>
 * This is primarily used for creating, isolating and executing Java
 * applications in-process as pseudo processes, in much the same was as an
 * application server or container would do for a Java EE application.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class VirtualizedSystem
{
    /**
     * The name of the {@link VirtualizedSystem}.  This is primarily used
     * for diagnostic purposes and output identification.
     */
    private String m_name;

    /**
     * The {@link Properties} of the {@link VirtualizedSystem}.
     */
    private Properties m_properties;

    /**
     * The Standard Out {@link PrintStream} of the {@link VirtualizedSystem}.
     */
    private PrintStream m_stdout;

    /**
     * The Standard Error {@link PrintStream} of the {@link VirtualizedSystem}.
     */
    private PrintStream m_stderr;

    /**
     * The {@link VirtualizedMBeanServerBuilder} for the {@link VirtualizedSystem}.
     */
    private VirtualizedMBeanServerBuilder m_mBeanServerBuilder;


    /**
     * Constructs a {@link VirtualizedSystem} based on the current
     * System resources.
     * <p>
     * The resulting {@link VirtualizedSystem} is actually the real system
     * represented as a {@link VirtualizedSystem}.
     *
     * @param portIterator  the {@link AvailablePortIterator} for requesting
     *                      system ports.
     */
    public VirtualizedSystem(AvailablePortIterator portIterator)
    {
        m_name               = "(Java Virtual Machine)";
        m_properties         = System.getProperties();
        m_stdout             = System.out;
        m_stderr             = System.err;
        m_mBeanServerBuilder = new VirtualizedMBeanServerBuilder(portIterator);
    }


    /**
     * Constructs a {@link VirtualizedSystem} based on another
     * {@link VirtualizedSystem}.
     *
     * @param name    the name of the {@link VirtualizedSystem}
     * @param system  the {@link VirtualizedSystem} from which to inherit
     *                system resources
     */
    public VirtualizedSystem(String            name,
                             VirtualizedSystem system)
    {
        this(name,
             system.getProperties(),
             new PrintStream(new LineNumberingOutputStream(system.getStdOut(),
                                                           name)),
             new PrintStream(new LineNumberingOutputStream(system.getStdErr(), name)),
             system.getMBeanServerBuilder());
    }


    /**
     * Constructs a {@link VirtualizedSystem} based on another
     * {@link VirtualizedSystem}.
     *
     * @param name                the name of the {@link VirtualizedSystem}
     * @param system              the {@link VirtualizedSystem} from which to
     *                            inherit system resources
     * @param mBeanServerBuilder  the {@link VirtualizedMBeanServerBuilder} for
     *                            the {@link VirtualizedSystem}
     */
    public VirtualizedSystem(String                        name,
                             VirtualizedSystem             system,
                             VirtualizedMBeanServerBuilder mBeanServerBuilder)
    {
        this(name,
             system.getProperties(),
             new PrintStream(new LineNumberingOutputStream(system.getStdOut(),
                                                           name)),
             new PrintStream(new LineNumberingOutputStream(system.getStdErr(), name)),
             mBeanServerBuilder);
    }


    /**
     * Constructs a {@link VirtualizedSystem}.
     *
     * @param name                the name of the {@link VirtualizedSystem}
     * @param properties          the system {@link Properties}
     * @param stdout              the standard out
     * @param stderr              the standard err
     * @param mBeanServerBuilder  the {@link VirtualizedMBeanServerBuilder}
     */
    public VirtualizedSystem(String                        name,
                             Properties                    properties,
                             PrintStream                   stdout,
                             PrintStream                   stderr,
                             VirtualizedMBeanServerBuilder mBeanServerBuilder)
    {
        m_name       = name;
        m_stdout     = stdout;
        m_stderr     = stderr;

        m_properties = new Properties();

        if (properties != null)
        {
            m_properties.putAll(properties);
        }

        m_mBeanServerBuilder = mBeanServerBuilder;
    }


    /**
     * Obtains the name of the {@link VirtualizedSystem}.
     *
     * @return the name of the {@link VirtualizedSystem}
     */
    public String getName()
    {
        return m_name;
    }


    /**
     * Obtains the {@link Properties} of the {@link VirtualizedSystem}.
     *
     * @return the {@link VirtualizedSystem} {@link Properties}
     */
    public Properties getProperties()
    {
        return m_properties;
    }


    /**
     * Obtains the Standard Out {@link PrintStream} of the
     * {@link VirtualizedSystem}.
     *
     * @return the Standard Out {@link PrintStream}
     */
    public PrintStream getStdOut()
    {
        return m_stdout;
    }


    /**
     * Obtains the Standard Error {@link PrintStream} of the
     * {@link VirtualizedSystem}.
     *
     * @return the Standard Error {@link PrintStream}
     */
    public PrintStream getStdErr()
    {
        return m_stderr;
    }


    /**
     * Obtains the {@link MBeanServerBuilder} for this {@link VirtualizedSystem}.
     *
     * @return the {@link MBeanServerBuilder}
     */
    public VirtualizedMBeanServerBuilder getMBeanServerBuilder()
    {
        return m_mBeanServerBuilder;
    }
}
