/*
 * File: AbstractJavaApplicationSchema.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.AbstractApplicationSchema;
import com.oracle.tools.runtime.PropertiesBuilder;
import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.util.ArrayList;
import java.util.List;

import static com.oracle.tools.runtime.java.JavaApplication.JAVA_NET_PREFER_IPV4_STACK;
import static com.oracle.tools.runtime.java.JavaApplication.JAVA_RMI_SERVER_HOSTNAME;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE_PORT;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE_SSL;

/**
 * An {@link AbstractJavaApplicationSchema} is a base implementation of a {@link JavaApplicationSchema}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplicationSchema<A extends JavaApplication<A>,
                                                    S extends JavaApplicationSchema<A, S>>
    extends AbstractApplicationSchema<A, S> implements JavaApplicationSchema<A, S>
{
    /**
     * The class name for the {@link JavaApplication}.
     */
    private String m_applicationClassName;

    /**
     * The class path for the {@link JavaApplication}.
     */
    private String m_classPath;

    /**
     * The JVM options for the {@link JavaApplication}.
     */
    private ArrayList<String> m_jvmOptions;

    /**
     * The system properties for the {@link JavaApplication}.
     */
    private PropertiesBuilder m_systemPropertiesBuilder;

    /**
     * This is the method that will be called to start the Java {@link Process}.
     */
    private String m_startMethodName;

    /**
     * This is the method that will be called to stop the Java {@link Process}.
     */
    private String m_stopMethodName;


    /**
     * Construct a {@link JavaApplicationSchema} with a given application class name,
     * but using the class path of the executing application.
     *
     * @param applicationClassName The fully qualified class name of the Java application.
     */
    public AbstractJavaApplicationSchema(String applicationClassName)
    {
        this("java", applicationClassName, System.getProperty("java.class.path"));
    }


    /**
     * Construct a {@link JavaApplicationSchema} with a given application class name,
     * but using the class path of the executing application.
     *
     * @param applicationClassName The fully qualified class name of the Java application.
     * @param classPath            The class path for the Java application.
     */
    public AbstractJavaApplicationSchema(String applicationClassName,
                                         String classPath)
    {
        this("java", applicationClassName, classPath);
    }


    /**
     * Construct a {@link JavaApplicationSchema}.
     *
     * @param executableName       The executable name to run
     * @param applicationClassName The fully qualified class name of the Java application.
     * @param classPath            The class path for the Java application.
     */
    public AbstractJavaApplicationSchema(String executableName,
                                         String applicationClassName,
                                         String classPath)
    {
        super(executableName);

        m_applicationClassName    = applicationClassName;
        m_classPath               = classPath;
        m_jvmOptions              = new ArrayList<String>();
        m_systemPropertiesBuilder = new PropertiesBuilder();
        m_startMethodName         = "main";
        m_stopMethodName          = "stop";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PropertiesBuilder getSystemPropertiesBuilder()
    {
        return m_systemPropertiesBuilder;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassPath()
    {
        return m_classPath;
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
     * Sets the class path for the Java application.
     *
     * @param classPath The classPath of the {@link JavaApplication}.
     * @return The {@link JavaApplicationSchema}.
     */
    @SuppressWarnings("unchecked")
    public S setClassPath(String classPath)
    {
        this.m_classPath = classPath;

        return (S) this;
    }


    /**
     * Sets the specified system property.
     *
     * @param name  The name of the system property.
     * @param value The value for the system property.
     * @return The {@link JavaApplicationSchema}.
     */
    @SuppressWarnings("unchecked")
    public S setSystemProperty(String name,
                               Object value)
    {
        m_systemPropertiesBuilder.setProperty(name, value);

        return (S) this;
    }


    /**
     * Sets a default value for specified system property (to be used if it's not defined)
     *
     * @param name  The name of the system property.
     * @param value The value for the system property.
     * @return The {@link JavaApplicationSchema}.
     */
    @SuppressWarnings("unchecked")
    public S setDefaultSystemProperty(String name,
                                      Object value)
    {
        m_systemPropertiesBuilder.setDefaultProperty(name, value);

        return (S) this;
    }


    /**
     * Adds the properties defined by the {@link PropertiesBuilder} to this {@link JavaApplicationSchema}.
     *
     * @param systemProperties The system {@link PropertiesBuilder}.
     * @return The {@link JavaApplicationSchema}.
     */
    @SuppressWarnings("unchecked")
    public S setSystemProperties(PropertiesBuilder systemProperties)
    {
        m_systemPropertiesBuilder.addProperties(systemProperties);

        return (S) this;
    }


    /**
     * Adds a JVM Option to use when starting the Java application.
     *
     * @param option The JVM option
     * @return The {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S addOption(String option)
    {
        // drop the "-" if specified
        m_jvmOptions.add(option.startsWith("-") ? option.substring(1) : option);

        return (S) this;
    }


    /**
     * Adds a JVM Option to use when starting the Java application.
     *
     * @param option The JVM option.
     * @return The {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setOption(String option)
    {
        addOption(option);

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getJVMOptions()
    {
        return m_jvmOptions;
    }


    /**
     * Enables/Disables JMX support for the build {@link JavaApplication}s.
     * <p/>
     * This method sets/removes numerous properties to enable/disable JMX.  Including:
     * <p/>
     * <ol>
     * <li>The remote JMX port to 9000, the Java default for remote JMX management.
     * You can override this setting by calling {@link #setJMXPort(int)} or
     * {@link #setJMXPort(AvailablePortIterator)}.
     * <p/>
     * <li>The java.rmi.server.hostname to be {@link com.oracle.tools.runtime.network.Constants#getLocalHost()}
     * You can override this setting by calling {@link #setRMIServerHostName(String)}.
     * <p/>
     * <li>The sun.management.jmxremote.ssl to false (off)
     * <p/>
     * <li>Disables JMX authentication.
     * You can override this setting by calling {@link #setJMXAuthentication(boolean)}.
     *
     * @param enabled Should JMX support be enabled
     *
     * @return The {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setJMXSupport(boolean enabled)
    {
        if (enabled)
        {
            setDefaultSystemProperty(SUN_MANAGEMENT_JMXREMOTE_PORT, 9000);
            setDefaultSystemProperty(SUN_MANAGEMENT_JMXREMOTE, "");
            setDefaultSystemProperty(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE, false);
            setDefaultSystemProperty(SUN_MANAGEMENT_JMXREMOTE_SSL, false);
        }
        else
        {
            m_systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE);
            m_systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE_PORT);
            m_systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE);
            m_systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE_SSL);
            m_systemPropertiesBuilder.removeProperty(JAVA_RMI_SERVER_HOSTNAME);
        }

        return (S) this;
    }


    /**
     * Specifies if IPv4 is required.
     *
     * @param enabled
     * @return
     */
    public S setPreferIPv4(boolean enabled)
    {
        return setDefaultSystemProperty(JAVA_NET_PREFER_IPV4_STACK, enabled);
    }


    /**
     * Specifies if JMX authentication is enabled.
     *
     * @param enabled Is JMX Authentication required.
     * @return The {@link JavaApplication}.
     */
    public S setJMXAuthentication(boolean enabled)
    {
        return setDefaultSystemProperty(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE, enabled);
    }


    /**
     * Specifies the JMX remote port.
     *
     * @param port The port on which remote JMX should be enabled.
     * @return The {@link JavaApplication}.
     */
    public S setJMXPort(int port)
    {
        return setSystemProperty(SUN_MANAGEMENT_JMXREMOTE_PORT, port);
    }


    /**
     * Specifies the JMX remote port using an AvailablePortIterator.
     *
     * @param portIterator The {@link AvailablePortIterator} that will be used to determine the JMX remote port.
     * @return The {@link JavaApplication}.
     */
    public S setJMXPort(AvailablePortIterator portIterator)
    {
        return setSystemProperty(SUN_MANAGEMENT_JMXREMOTE_PORT, portIterator);
    }


    /**
     * Specifies the RMI Server Host Name.  By default this is typically "localhost".
     *
     * @param rmiServerHostName The hostname
     * @return The {@link JavaApplication}.
     */
    @SuppressWarnings("unchecked")
    public S setRMIServerHostName(String rmiServerHostName)
    {
        setDefaultSystemProperty(JAVA_RMI_SERVER_HOSTNAME, rmiServerHostName);

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getStartMethodName()
    {
        return m_startMethodName;
    }

    /**
     * Specifies the name of the method to call to start the Java {@link Process}.
     *
     * @param methodName - the name of the method to use to start the Java {@link Process}
     * @return This {@link JavaApplicationSchema} to allow method chaining.
     */
    public S setStartMethodName(String methodName)
    {
        m_startMethodName = methodName;
        return (S) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStopMethodName()
    {
        return m_stopMethodName;
    }

    /**
     * Specifies the name of the method to call to stop the Java {@link Process}.
     *
     * @param methodName - the name of the method to use to stop the Java {@link Process}
     * @return This {@link JavaApplicationSchema} to allow method chaining.
     */
    public S setStopMethodName(String methodName)
    {
        m_stopMethodName = methodName;
        return (S) this;
    }
}
