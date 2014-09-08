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
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
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

import java.util.Properties;

/**
 * An {@link AbstractJavaApplicationSchema} is a base implementation of a {@link JavaApplicationSchema}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplicationSchema<A extends JavaApplication,
                                                    S extends AbstractJavaApplicationSchema<A, S>>
    extends AbstractApplicationSchema<A, S> implements FluentJavaApplicationSchema<A, S>
{
    /**
     * The class name for the {@link JavaApplication}.
     */
    private String applicationClassName;

    /**
     * The {@link ClassPath} for the {@link JavaApplication}.
     */
    private ClassPath classPath;

    /**
     * The system properties for the {@link JavaApplication}.
     */
    private PropertiesBuilder systemPropertiesBuilder;


    /**
     * Constructs an {@link AbstractJavaApplicationSchema} based on another
     * {@link JavaApplicationSchema}.
     *
     * @param schema  the other {@link JavaApplicationSchema}
     */
    public AbstractJavaApplicationSchema(JavaApplicationSchema<A> schema)
    {
        super(schema);

        this.applicationClassName    = schema.getApplicationClassName();
        this.classPath               = new ClassPath(schema.getClassPath());
        this.systemPropertiesBuilder = new PropertiesBuilder(schema.getSystemPropertiesBuilder());

        configureDefaults();
    }


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

        this.applicationClassName    = applicationClassName;
        this.classPath               = new ClassPath(classPath);
        this.systemPropertiesBuilder = new PropertiesBuilder();

        configureDefaults();
    }


    /**
     * Configures the default settings for the {@link JavaApplicationSchema}.
     */
    protected abstract void configureDefaults();


    @Override
    public PropertiesBuilder getSystemPropertiesBuilder()
    {
        return systemPropertiesBuilder;
    }


    @Override
    public Properties getSystemProperties(Platform platform)
    {
        return getSystemPropertiesBuilder().realize(null, platform);
    }


    @Override
    public ClassPath getClassPath()
    {
        return classPath;
    }


    @Override
    public String getApplicationClassName()
    {
        return applicationClassName;
    }


    @Override
    public S setApplicationClassName(String className)
    {
        this.applicationClassName = className;

        return (S) this;
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
        this.classPath = new ClassPath(classPath);

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
        this.classPath = classPath;

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
        systemPropertiesBuilder.setProperty(name, value);

        return (S) this;
    }


    /**
     * Obtains the specified system property from the system properties builder
     * and casts it to the specified type.  Should the property not exist the default value
     * is returned.
     *
     * @param name           the name of the system property
     * @param propertyClass  the desired property value type
     * @param defaultValue   the default value to use if the property is not defined
     * @param <T>            the type of the system proeprty
     *
     * @return  the system property value alternatively the specified default value
     *          should the system property not be defined
     */
    public <T> T getSystemProperty(String   name,
                                   Class<T> propertyClass,
                                   T        defaultValue)
    {
        if (systemPropertiesBuilder.containsProperty(name))
        {
            Object property = systemPropertiesBuilder.getProperty(name);

            return propertyClass.cast(property);
        }
        else
        {
            return defaultValue;
        }
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
        systemPropertiesBuilder.setPropertyIfAbsent(name, value);

        return (S) this;
    }


    /**
     * Sets a default value for specified system property (to be used if it's not defined)
     *
     * @param name  The name of the system property
     * @param value The value for the system property
     * @return the {@link JavaApplicationSchema}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public S setDefaultSystemProperty(String name,
                                      Object value)
    {
        systemPropertiesBuilder.setDefaultProperty(name, value);

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
        systemPropertiesBuilder.addProperties(systemProperties);

        return (S) this;
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
            setSystemPropertyIfAbsent(JAVA_RMI_SERVER_HOSTNAME, LocalPlatform.getInstance().getHostName());
        }
        else
        {
            systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE);
            systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE_PORT);
            systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE);
            systemPropertiesBuilder.removeProperty(SUN_MANAGEMENT_JMXREMOTE_SSL);
            systemPropertiesBuilder.removeProperty(JAVA_RMI_SERVER_HOSTNAME);
        }

        return (S) this;
    }


    /**
     * Specifies if IPv4 is required.
     *
     * @param enabled  is IPv4 required
     *
     * @return the {@link JavaApplicationSchema}
     */
    public S setPreferIPv4(boolean enabled)
    {
        return setSystemProperty(JAVA_NET_PREFER_IPV4_STACK, enabled);
    }


    @Override
    public boolean isIPv4Preferred()
    {
        if (systemPropertiesBuilder.containsProperty(JAVA_NET_PREFER_IPV4_STACK))
        {
            Object isIPv4Preferred = systemPropertiesBuilder.getProperty(JAVA_NET_PREFER_IPV4_STACK);

            return isIPv4Preferred == null ? false : Boolean.valueOf(isIPv4Preferred.toString());
        }
        else
        {
            return false;
        }
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
    @SuppressWarnings("unchecked")
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
        Object value = systemPropertiesBuilder.getProperty(JAVA_AWT_HEADLESS);

        return value instanceof Boolean && ((Boolean) value);
    }
}
