/*
 * File: JavaApplication.java
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

import com.oracle.tools.deferred.Deferred;

import com.oracle.tools.runtime.Application;

import com.oracle.tools.runtime.concurrent.RemoteExecutor;

import com.oracle.tools.util.CompletionListener;

import java.util.Properties;
import java.util.Set;

import java.util.concurrent.Callable;

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;

import javax.management.remote.JMXConnector;

/**
 * A {@link JavaApplication} is an {@link Application} for Java-based
 * application processes that use system properties in addition to environment
 * variables as provided by regular {@link Application}s.
 *
 * @see Application
 * @see JavaApplicationBuilder
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface JavaApplication<A> extends Application<A>, RemoteExecutor
{
    /**
     * The com.sun.management.jmxremote JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE = "com.sun.management.jmxremote";

    /**
     * The com.sun.management.jmxremote.url JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_URL = "com.sun.management.jmxremote.url";

    /**
     * The com.sun.management.jmxremote.port JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_PORT = "com.sun.management.jmxremote.port";

    /**
     * The com.sun.management.jmxremote.authenticate JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE = "com.sun.management.jmxremote.authenticate";

    /**
     * The com.sun.management.jmxremote.user JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_USER = "com.sun.management.jmxremote.user";

    /**
     * The com.sun.management.jmxremote.password JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_PASSWORD = "com.sun.management.jmxremote.password";

    /**
     * The com.sun.management.jmxremote.ssl JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_SSL = "com.sun.management.jmxremote.ssl";

    /**
     * The com.sun.management.jmxremote.password.file JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_PASSWORD_FILE = "com.sun.management.jmxremote.password.file";

    /**
     * The com.sun.management.jmxremote.access.file JVM property.
     */
    public static final String SUN_MANAGEMENT_JMXREMOTE_ACCESS_FILE = "com.sun.management.jmxremote.access.file";

    /**
     * The java.awt.headless JVM property.
     */
    public static final String JAVA_AWT_HEADLESS = "java.awt.headless";

    /**
     * The java.home JVM property.
     */
    public static final String JAVA_HOME = "java.home";

    /**
     * The java.rmi.server.hostname JVM property.
     */
    public static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";

    /**
     * The java.net.preferIPv4Stack JVM property (false by default in most JVMs)
     */
    public static final String JAVA_NET_PREFER_IPV4_STACK = "java.net.preferIPv4Stack";


    /**
     * Obtains the system {@link Properties} that were supplied to the
     * {@link JavaApplication} when it was realized.
     *
     * @return a {@link Properties} of name value pairs, each one representing
     *         a system property provided to the {@link JavaApplication} as
     *         -Dname=value parameters when it was realized
     */
    public Properties getSystemProperties();


    /**
     * Obtains an individual system property value for a specified system
     * property name, or <code>null</code> if the property is undefined.
     *
     * @param name  the name of the system property
     *
     * @return the value of the defined system property, or <code>null</code>
     *         if undefined
     */
    public String getSystemProperty(String name);


    /**
     * Determines if JMX has been configured and is enabled for the
     * {@link JavaApplication}.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isJMXEnabled();


    /**
     * Obtains the {@link Deferred} for the {@link JMXConnector}
     * to the {@link JavaApplication}.
     *
     * @return  the {@link Deferred} of the {@link JMXConnector}
     */
    public Deferred<JMXConnector> getDeferredJMXConnector();


    /**
     * Obtains a {@link Deferred} representing the value of an
     * MBean attribute registered with the JMX infrastructure of the
     * {@link JavaApplication}.
     *
     * @param <T>             the type of the MBean attribute
     * @param objectName      the name of the MBean defining the attribute
     * @param attributeName   the name of the MBean attribute
     * @param attributeClass  the {@link Class} of the MBean attribute
     *
     * @return a {@link Deferred} of type T for the attribute value
     */
    public <T> Deferred<T> getDeferredMBeanAttribute(ObjectName objectName,
                                                     String     attributeName,
                                                     Class<T>   attributeClass);


    /**
     * Obtains the value of the specified MBean attribute registered with the
     * JMX infrastructure of the {@link JavaApplication}.
     * <p>
     * If the JMX infrastructure in the {@link JavaApplication} is not yet
     * available, it will block at wait for the default application timeout
     * until it becomes available.
     *
     * @param <T>             the type of the MBean attribute
     * @param objectName      the name of the MBean defining the attribute
     * @param attributeName   the name of the MBean attribute
     * @param attributeClass  the {@link Class} of the MBean attribute
     *
     * @return the MBean attribute value
     *
     * @throws com.oracle.tools.deferred.UnresolvableInstanceException
     *                         when resource is unavailable
     */
    public <T> T getMBeanAttribute(ObjectName objectName,
                                   String     attributeName,
                                   Class<T>   attributeClass);


    /**
     * Obtains a {@link Deferred} representing a local proxy to an MBean
     * registered with the JMX infrastructure of the {@link JavaApplication}.
     *
     * @param <T>
     * @param objectName  the name of the MBean
     * @param proxyClass  the type of the proxy
     *
     * @return a {@link Deferred} of type T
     */
    public <T> Deferred<T> getDeferredMBeanProxy(ObjectName objectName,
                                                 Class<T>   proxyClass);


    /**
     * Obtains a local proxy of an MBean registered with the JMX infrastructure
     * in the {@link JavaApplication}.
     * <p>
     * If the JMX infrastructure in the {@link JavaApplication} is not yet
     * available, it will block and wait for the default application timeout
     * until it becomes available.
     *
     * @param <T>         the type of the MBean
     * @param objectName  the name of the MBean
     * @param clazz       the class of the proxy to create
     *
     * @return a proxy of type T
     *
     * @throws com.oracle.tools.deferred.UnresolvableInstanceException
     *                                        when resource is unavailable
     * @throws UnsupportedOperationException  when JMX is not enabled for the
     *                                        {@link JavaApplication}
     */
    public <T> T getMBeanProxy(ObjectName objectName,
                               Class<T>   clazz);


    /**
     * Obtains a {@link Deferred} representing an {@link MBeanInfo}
     * registered with the JMX infrastructure of the {@link JavaApplication}.
     *
     * @param objectName  the name of the MBean
     *
     * @return a {@link Deferred} of an {@link MBeanInfo}
     */
    public Deferred<MBeanInfo> getDeferredMBeanInfo(ObjectName objectName);


    /**
     * Obtains the {@link MBeanInfo} of the specified MBean using the JMX
     * infrastructure in the {@link JavaApplication}.
     * <p>
     * If the JMX infrastructure in the {@link JavaApplication} is not yet
     * available, it will block at wait for the default application timeout
     * until it becomes available.
     *
     * @param objectName  the name of the MBean
     *
     * @return an {@link MBeanInfo}
     *
     * @throws com.oracle.tools.deferred.UnresolvableInstanceException
     *                                        when resource is unavailable
     * @throws UnsupportedOperationException  when JMX is not enabled for the
     *                                        {@link JavaApplication}
     */
    public MBeanInfo getMBeanInfo(ObjectName objectName);


    /**
     * Obtains a the result of an MBeans Query against the JMX infrastructure
     * of the {@link JavaApplication}.
     *
     * @param name   the object name pattern identifying the MBeans to be retrieved.
     *               If <code>null</code> or no domain and key properties are
     *               specified, all the MBeans registered will be retrieved
     * @param query  the query expression to be applied for selecting MBeans
     *               If <code>null</code> no query expression will be applied
     *               for selecting MBeans
     *
     * @return a {@link Set} of {@link ObjectInstance}s
     */
    public Set<ObjectInstance> queryMBeans(ObjectName name,
                                           QueryExp   query);


    /**
     * Obtains the configured JMX port of the application (which by definition is remote)
     *
     * @return Port Number
     * @throws UnsupportedOperationException  when JMX is not enabled on the
     *                                        {@link JavaApplication}
     */
    public int getRemoteJMXPort();


    /**
     * Obtains the configured RMI Server Host Name for the RMI server possibly running in the
     * {@link SimpleJavaApplication}.
     *
     * @return {@link String} (<code>null</code> if not defined).
     */
    public String getRMIServerHostName();
}
