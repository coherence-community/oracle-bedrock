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
import com.oracle.tools.runtime.network.Constants;

import static com.oracle.tools.runtime.java.JavaApplication.JAVA_AWT_HEADLESS;
import static com.oracle.tools.runtime.java.JavaApplication.JAVA_NET_PREFER_IPV4_STACK;
import static com.oracle.tools.runtime.java.JavaApplication.JAVA_RMI_SERVER_HOSTNAME;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE_PORT;
import static com.oracle.tools.runtime.java.JavaApplication.SUN_MANAGEMENT_JMXREMOTE_SSL;

import java.util.ArrayList;
import java.util.List;

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
     * The {@link ClassPath} for the {@link JavaApplication}.
     */
    private ClassPath m_classPath;

    /**
     * The JVM options for the {@link JavaApplication}.
     */
    private ArrayList<String> m_jvmOptions;

    /**
     * The system properties for the {@link JavaApplication}.
     */
    private PropertiesBuilder m_systemPropertiesBuilder;

    /**
     * The value of the JAVA_HOME environment variable
     * (or <code>null</code> for the platform default)
     */
    private String javaHome;


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
        m_classPath               = new ClassPath(classPath);
        m_jvmOptions              = new ArrayList<String>();
        m_systemPropertiesBuilder = new PropertiesBuilder();
        javaHome                  = null;

        configureDefaults();
    }


    /**
     * Configures the default settings for the {@link JavaApplicationSchema}.
     */
    protected abstract void configureDefaults();


    @Override
    public PropertiesBuilder getSystemPropertiesBuilder()
    {
        return m_systemPropertiesBuilder;
    }


    @Override
    public ClassPath getClassPath()
    {
        return m_classPath;
    }


    @Override
    public String getApplicationClassName()
    {
        return m_applicationClassName;
    }


    @Override
    public String getJavaHome()
    {
        return javaHome;
    }


    /**
     * Sets the value to use for JAVA_HOME or <code>null</code> to
     * use the underlying platform setting.
     *
     * @param javaHome  the value of JAVA_HOME
     */
    public void setJavaHome(String javaHome)
    {
        this.javaHome = javaHome;
    }


    /**
     * Sets the class path for the Java application.
     *
     * @param classPath The class-path of the {@link JavaApplication}.
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setClassPath(String classPath)
    {
        m_classPath = new ClassPath(classPath);

        return (S) this;
    }


    /**
     * Sets the class path for the Java application.
     *
     * @param classPath The {@link ClassPath} of the {@link JavaApplication}
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setClassPath(ClassPath classPath)
    {
        m_classPath = classPath;

        return (S) this;
    }


    /**
     * Sets the specified system property.
     *
     * @param name  The name of the system property
     * @param value The value for the system property
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setSystemProperty(String name,
                               Object value)
    {
        m_systemPropertiesBuilder.setProperty(name, value);

        return (S) this;
    }


    /**
     * Optionally sets the specified system property.
     *
     * @param name   the name of the system property
     * @param value  the value for the system property
     *
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setSystemPropertyIfAbsent(String name,
                                       Object value)
    {
        m_systemPropertiesBuilder.setPropertyIfAbsent(name, value);

        return (S) this;
    }


    /**
     * Sets a default value for specified system property (to be used if it's not defined)
     *
     * @param name  The name of the system property
     * @param value The value for the system property
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public S setDefaultSystemProperty(String name,
                                      Object value)
    {
        m_systemPropertiesBuilder.setDefaultProperty(name, value);

        return (S) this;
    }


    /**
     * Adds the properties defined by the {@link PropertiesBuilder} to this {@link JavaApplicationSchema}.
     *
     * @param systemProperties The system {@link PropertiesBuilder}
     * @return the {@link JavaApplicationSchema}
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
     * @return the {@link JavaApplicationSchema}
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
     * @param option The JVM option
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setOption(String option)
    {
        addOption(option);

        return (S) this;
    }


    @Override
    public List<String> getJVMOptions()
    {
        return m_jvmOptions;
    }


    /**
     * Enables/Disables JMX support for the build {@link JavaApplication}s.
     * <p/>
     * When enabling JMX support, the following system properties are configured
     * for the {@link JavaApplicationSchema};
     * <p/>
     * <ol>
     *   <li>Adds the {@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE} property.</li>
     *   <li>Sets {@link JavaApplication#JAVA_RMI_SERVER_HOSTNAME} to {@link Constants#getLocalHost()}
     *       (if not already defined).</li>
     *   <li>Sets {@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE_PORT} to 9000,
     *       the Java default for remote JMX management
     *       (if not already defined).
     *       You can override this setting by calling {@link #setJMXPort(int)} or
     *       {@link #setJMXPort(AvailablePortIterator)}.</li>
     *   <li>Sets {@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE_SSL} to false (off)
     *       (if not already defined).</li>
     *   <li>Sets {@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE} to false (off)
     *       (if not already defined).
     *       You can override this setting by calling {@link #setJMXAuthentication(boolean)}.</li>
     * </ol>
     * </p>
     * When disabling JMX support, the following system properties are removed
     * from the {@link JavaApplicationSchema};
     * <ol>
     *   <li>{@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE}</li>
     *   <li>{@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE_PORT}</li>
     *   <li>{@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE}</li>
     *   <li>{@link JavaApplication#SUN_MANAGEMENT_JMXREMOTE_SSL}</li>
     *   <li>{@link JavaApplication#JAVA_RMI_SERVER_HOSTNAME}</li>
     * </ol>
     *
     *
     * @param enabled  should JMX support be enabled
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setJMXSupport(boolean enabled)
    {
        if (enabled)
        {
            setSystemProperty(SUN_MANAGEMENT_JMXREMOTE, "");
            setSystemPropertyIfAbsent(SUN_MANAGEMENT_JMXREMOTE_PORT, 9000);
            setSystemPropertyIfAbsent(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE, false);
            setSystemPropertyIfAbsent(SUN_MANAGEMENT_JMXREMOTE_SSL, false);
            setSystemPropertyIfAbsent(JAVA_RMI_SERVER_HOSTNAME, Constants.getLocalHost());
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
     * @return the {@link JavaApplicationSchema}
     */
    public S setPreferIPv4(boolean enabled)
    {
        return setSystemProperty(JAVA_NET_PREFER_IPV4_STACK, enabled);
    }


    /**
     * Specifies if JMX authentication is enabled.
     *
     * @param enabled Is JMX Authentication required
     * @return the {@link JavaApplicationSchema}
     */
    public S setJMXAuthentication(boolean enabled)
    {
        return setSystemProperty(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE, enabled);
    }


    /**
     * Specifies the JMX remote port.
     *
     * @param port The port on which remote JMX should be enabled.
     * @return the {@link JavaApplicationSchema}
     */
    public S setJMXPort(int port)
    {
        return setSystemProperty(SUN_MANAGEMENT_JMXREMOTE_PORT, port);
    }


    /**
     * Specifies the JMX remote port using an AvailablePortIterator.
     *
     * @param portIterator The {@link AvailablePortIterator} that will be used to determine the JMX remote port
     * @return the {@link JavaApplicationSchema}
     */
    public S setJMXPort(AvailablePortIterator portIterator)
    {
        return setSystemProperty(SUN_MANAGEMENT_JMXREMOTE_PORT, portIterator);
    }


    /**
     * Specifies the RMI Server Host Name.  By default this is typically "localhost".
     *
     * @param rmiServerHostName The hostname
     * @return the {@link JavaApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public S setRMIServerHostName(String rmiServerHostName)
    {
        setSystemProperty(JAVA_RMI_SERVER_HOSTNAME, rmiServerHostName);

        return (S) this;
    }


    /**
     * Specifies if a {@link JavaApplication} will run in a "headless" mode.
     *
     * @param isHeadless  should the {@link JavaApplication} run in "headless" mode.
     * @return the {@link JavaApplicationSchema}
     *
     * @see JavaApplication#JAVA_AWT_HEADLESS
     */
    public S setHeadless(boolean isHeadless)
    {
        setSystemProperty(JAVA_AWT_HEADLESS, isHeadless);

        return (S) this;
    }


    /**
     * Determines if a {@link JavaApplication} will run in a "headless" mode.
     *
     * @return the {@link JavaApplicationSchema}
     *
     * @see JavaApplication#JAVA_AWT_HEADLESS
     */
    public boolean isHeadless()
    {
        Object value = m_systemPropertiesBuilder.getProperty(JAVA_AWT_HEADLESS);

        return value instanceof Boolean && ((Boolean) value);
    }
}
