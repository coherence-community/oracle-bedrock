/*
 * File: JmxFeature.java
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

package com.oracle.bedrock.runtime.java.features;

import com.oracle.bedrock.deferred.Cached;
import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.deferred.PermanentlyUnavailableException;
import com.oracle.bedrock.deferred.jmx.DeferredJMXConnector;
import com.oracle.bedrock.deferred.jmx.DeferredMBeanAttribute;
import com.oracle.bedrock.deferred.jmx.DeferredMBeanInfo;
import com.oracle.bedrock.deferred.jmx.DeferredMBeanProxy;
import com.oracle.bedrock.extensible.AbstractFeature;
import com.oracle.bedrock.extensible.Extensible;
import com.oracle.bedrock.extensible.Feature;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.java.JavaApplication;

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.Set;

import static com.oracle.bedrock.deferred.DeferredHelper.cached;
import static com.oracle.bedrock.deferred.DeferredHelper.ensured;
import static com.oracle.bedrock.deferred.DeferredHelper.within;

/**
 * A {@link Feature} for {@link JavaApplication}s that provides the ability to interact
 * with Java Management Extensions (JMX).
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see JavaApplication
 * @see Feature
 */
public class JmxFeature extends AbstractFeature
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
     * The {@link Cached} representing the {@link JMXConnector}.
     */
    protected Cached<JMXConnector> cachedJmxConnector;


    /**
     * Determines if the {@link JmxFeature} is supportable by the specified {@link JavaApplication}.
     *
     * @param application  the {@link JavaApplication}
     *
     * @return  <code>true</code> if the {@link JavaApplication} can support the {@link JmxFeature},
     *          <code>false</code> otherwise
     */
    public static boolean isSupportedBy(JavaApplication application)
    {
        return application.getSystemProperties().containsKey(SUN_MANAGEMENT_JMXREMOTE);
    }


    /**
     * Obtains the {@link JavaApplication} that is supporting JMX.
     *
     * @return  the {@link JavaApplication}
     */
    public JavaApplication getJavaApplication()
    {
        return getExtensible().get(JavaApplication.class);
    }


    /**
     * Obtains the default duration to used by the {@link JavaApplication}
     * for timeouts.
     *
     * @return a {@link Timeout}
     */
    public Timeout getDefaultTimeout()
    {
        return getJavaApplication().getDefaultTimeout();
    }


    @Override
    public void onAddingTo(Extensible extensible)
    {
        super.onAddingTo(extensible);

        JavaApplication application = getJavaApplication();

        int jmxRemotePort =
            Integer.parseInt(application.getSystemProperties().getProperty(SUN_MANAGEMENT_JMXREMOTE_PORT).toString());

        String jmxRemoteHostName =
            application.getSystemProperties().getProperty(JavaApplication.JAVA_RMI_SERVER_HOSTNAME);

        jmxRemoteHostName = jmxRemoteHostName == null
                            ? application.getPlatform().getAddress().getHostAddress() : jmxRemoteHostName;

        // ensure that the RMI server doesn't eagerly load JMX classes
        System.setProperty("java.rmi.server.useCodebaseOnly", "true");

        // establish our JMX connector
        // define a DeferredResource representation of a JMXConnector
        // for this application.  we use a DeferredResource as the
        // application may not have started, or be ready for JMX connections
        String url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", jmxRemoteHostName, jmxRemotePort);

        // use a CachedResource as once the JMXConnector is established
        // we don't want to create another JMXConnector for the application
        cachedJmxConnector = cached(new DeferredJMXConnector(url));
    }


    @Override
    public void onRemovingFrom(Extensible extensible)
    {
        super.onRemovingFrom(extensible);

        // close the JMXConnector (if we've got one)
        JMXConnector jmxConnector = cachedJmxConnector.release();

        if (jmxConnector != null)
        {
            try
            {
                jmxConnector.close();
            }
            catch (IOException e)
            {
                // nothing to do here as we don't care
            }
        }
    }


    /**
     * Obtains the {@link Deferred} for the {@link JMXConnector}
     * to the {@link JmxFeature}.
     *
     * @return  the {@link Deferred} of the {@link JMXConnector}
     */
    public Deferred<JMXConnector> getDeferredJMXConnector()
    {
        return cachedJmxConnector;
    }


    /**
     * Obtains a {@link Deferred} representing the value of an
     * MBean attribute registered with the JMX infrastructure of the
     * {@link JmxFeature}.
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
                                                     Class<T>   attributeClass)
    {
        return new DeferredMBeanAttribute<T>(cachedJmxConnector, objectName, attributeName, attributeClass);
    }


    /**
     * Obtains the value of the specified MBean attribute registered with the
     * JMX infrastructure of the {@link JmxFeature}.
     * <p>
     * If the JMX infrastructure in the {@link JmxFeature} is not yet
     * available, it will block at wait for the default application timeout
     * until it becomes available.
     *
     * @param <T>             the type of the MBean attribute
     * @param objectName      the name of the MBean defining the attribute
     * @param attributeName   the name of the MBean attribute
     * @param attributeClass  the {@link Class} of the MBean attribute
     *
     * @return the MBean attribute value
     */
    public <T> T getMBeanAttribute(ObjectName objectName,
                                   String     attributeName,
                                   Class<T>   attributeClass)
    {
        return ensured(getDeferredMBeanAttribute(objectName, attributeName, attributeClass),
                       within(getDefaultTimeout())).get();
    }


    /**
     * Obtains a {@link Deferred} representing a local proxy to an MBean
     * registered with the JMX infrastructure of the {@link JmxFeature}.
     *
     * @param objectName  the name of the MBean
     * @param proxyClass  the type of the proxy
     *
     * @param <T>         the type of the {@link Deferred} value
     *
     * @return a {@link Deferred} of type T
     */
    public <T> Deferred<T> getDeferredMBeanProxy(ObjectName objectName,
                                                 Class<T>   proxyClass)
    {
        return new Cached<T>(new DeferredMBeanProxy<T>(cachedJmxConnector, objectName, proxyClass));
    }


    /**
     * Obtains a local proxy of an MBean registered with the JMX infrastructure
     * in the {@link JmxFeature}.
     * <p>
     * If the JMX infrastructure in the {@link JmxFeature} is not yet
     * available, it will block and wait for the default application timeout
     * until it becomes available.
     *
     * @param <T>         the type of the MBean
     * @param objectName  the name of the MBean
     * @param clazz       the class of the proxy to create
     *
     * @return a proxy of type T
     */
    public <T> T getMBeanProxy(ObjectName objectName,
                               Class<T>   clazz)
    {
        return ensured(getDeferredMBeanProxy(objectName, clazz), within(getDefaultTimeout())).get();
    }


    /**
     * Obtains a {@link Deferred} representing an {@link MBeanInfo}
     * registered with the JMX infrastructure of the {@link JmxFeature}.
     *
     * @param objectName  the name of the MBean
     *
     * @return a {@link Deferred} of an {@link MBeanInfo}
     */
    public Deferred<MBeanInfo> getDeferredMBeanInfo(ObjectName objectName)
    {
        return new DeferredMBeanInfo(cachedJmxConnector, objectName);
    }


    /**
     * Obtains the {@link MBeanInfo} of the specified MBean using the JMX
     * infrastructure in the {@link JmxFeature}.
     * <p>
     * If the JMX infrastructure in the {@link JmxFeature} is not yet
     * available, it will block at wait for the default application timeout
     * until it becomes available.
     *
     * @param objectName  the name of the MBean
     *
     * @return an {@link MBeanInfo}
     */
    public MBeanInfo getMBeanInfo(ObjectName objectName)
    {
        return ensured(getDeferredMBeanInfo(objectName), within(getDefaultTimeout())).get();
    }


    /**
     * Obtains a the result of an MBeans Query against the JMX infrastructure
     * of the {@link JmxFeature}.
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
                                           QueryExp   query)
    {
        try
        {
            return ensured(getDeferredJMXConnector(),
                           within(getDefaultTimeout())).get().getMBeanServerConnection().queryMBeans(name, query);
        }
        catch (IOException e)
        {
            throw new PermanentlyUnavailableException(getDeferredJMXConnector(), e);
        }
    }
}
