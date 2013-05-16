/*
 * File: AbstractJavaApplication.java
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

import com.oracle.tools.deferred.Cached;
import com.oracle.tools.deferred.Deferred;
import com.oracle.tools.deferred.NeverAvailable;
import com.oracle.tools.deferred.ObjectNotAvailableException;

import com.oracle.tools.deferred.jmx.DeferredJMXConnector;
import com.oracle.tools.deferred.jmx.DeferredMBeanAttribute;
import com.oracle.tools.deferred.jmx.DeferredMBeanInfo;
import com.oracle.tools.deferred.jmx.DeferredMBeanProxy;

import com.oracle.tools.runtime.AbstractApplication;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LifecycleEventInterceptor;

import com.oracle.tools.runtime.network.Constants;

import static com.oracle.tools.deferred.DeferredHelper.cached;
import static com.oracle.tools.deferred.DeferredHelper.ensured;

import java.io.IOException;

import java.util.Properties;
import java.util.Set;

import java.util.concurrent.TimeUnit;

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;

import javax.management.remote.JMXConnector;

/**
 * A {@link AbstractJavaApplication} is a base implementation of a {@link JavaApplication} that has
 * a Console (with standard output, standard error and standard input streams).
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplication<A extends JavaApplication<A>, P extends JavaProcess>
    extends AbstractApplication<A, P> implements JavaApplication<A>
{
    /**
     * The System Properties used to create the underlying {@link Process} represented by
     * the {@link AbstractJavaApplication}.
     */
    protected Properties m_systemProperties;

    /**
     * The {@link Cached} representing the {@link JMXConnector}.
     */
    protected Cached<JMXConnector> m_cachedJMXConnector;


    /**
     * Construct a {@link AbstractJavaApplication}.
     *
     * @param process               the {@link Process} representing the {@link JavaApplication}
     * @param name                  the name of the {@link JavaApplication}
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the
     *                              realized {@link Application}. This may be <code>null</code> if not required
     * @param environmentVariables  the environment variables used when starting the {@link JavaApplication}
     * @param systemProperties      the system properties provided to the {@link JavaApplication}
     * @param isDiagnosticsEnabled  should diagnostic information be logged/output
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     */
    public AbstractJavaApplication(P                                      process,
                                   String                                 name,
                                   ApplicationConsole                     console,
                                   Properties                             environmentVariables,
                                   Properties                             systemProperties,
                                   boolean                                isDiagnosticsEnabled,
                                   long                                   defaultTimeout,
                                   TimeUnit                               defaultTimeoutUnits,
                                   Iterable<LifecycleEventInterceptor<A>> interceptors)
    {
        super(process,
              name,
              console,
              environmentVariables,
              isDiagnosticsEnabled,
              defaultTimeout,
              defaultTimeoutUnits,
              interceptors);

        m_systemProperties = systemProperties;

        // ensure that the RMI server doesn't eagerly load JMX classes
        System.setProperty("java.rmi.server.useCodebaseOnly", "true");

        // establish our JMX connector
        if (isJMXEnabled())
        {
            // define a DeferredResource representation of a JMXConnector
            // for this application.  we use a DeferredResource as the
            // application may not have started, or be ready for JMX connections
            String url = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi",
                                       getRMIServerHostName(),
                                       getRemoteJMXPort());

            // use a CachedResource as once the JMXConnector is established
            // we don't want to create another JMXConnector for the application
            m_cachedJMXConnector = cached(new DeferredJMXConnector(url));
        }
        else
        {
            m_cachedJMXConnector = cached(new NeverAvailable<JMXConnector>(JMXConnector.class));
        }
    }


    /**
     * Obtains the underlying {@link com.oracle.tools.runtime.ApplicationProcess} that controls the
     * {@link Application}.
     *
     * @return the {@link com.oracle.tools.runtime.ApplicationProcess} for the {@link Application}
     */
    P getJavaProcess()
    {
        return super.getApplicationProcess();
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
    public String getSystemProperty(String name)
    {
        return m_systemProperties.getProperty(name);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isJMXEnabled()
    {
        return m_systemProperties.containsKey(SUN_MANAGEMENT_JMXREMOTE_PORT);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getRemoteJMXPort()
    {
        if (isJMXEnabled())
        {
            return Integer.parseInt(m_systemProperties.getProperty(SUN_MANAGEMENT_JMXREMOTE_PORT));
        }
        else
        {
            throw new UnsupportedOperationException("Application is not enabled for remote JMX management");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getRMIServerHostName()
    {
        String hostname = m_systemProperties.getProperty(JAVA_RMI_SERVER_HOSTNAME);

        return hostname == null ? Constants.getLocalHost() : hostname;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Deferred<JMXConnector> getDeferredJMXConnector()
    {
        return m_cachedJMXConnector;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Deferred<T> getDeferredMBeanProxy(ObjectName objectName,
                                                 Class<T>   proxyClass)
    {
        return new Cached<T>(new DeferredMBeanProxy<T>(m_cachedJMXConnector, objectName, proxyClass));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getMBeanProxy(ObjectName objectName,
                               Class<T>   proxyClass)
    {
        return ensured(getDeferredMBeanProxy(objectName, proxyClass),
                       getDefaultTimeout(),
                       getDefaultTimeoutUnits()).get();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Deferred<MBeanInfo> getDeferredMBeanInfo(ObjectName objectName)
    {
        return new DeferredMBeanInfo(m_cachedJMXConnector, objectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanInfo getMBeanInfo(ObjectName objectName)
    {
        return ensured(getDeferredMBeanInfo(objectName), getDefaultTimeout(), getDefaultTimeoutUnits()).get();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Deferred<T> getDeferredMBeanAttribute(ObjectName objectName,
                                                     String     attributeName,
                                                     Class<T>   attributeClass)
    {
        return new DeferredMBeanAttribute<T>(m_cachedJMXConnector, objectName, attributeName, attributeClass);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getMBeanAttribute(ObjectName objectName,
                                   String     attributeName,
                                   Class<T>   attributeClass)
    {
        return ensured(getDeferredMBeanAttribute(objectName, attributeName, attributeClass),
                       getDefaultTimeout(),
                       getDefaultTimeoutUnits()).get();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name,
                                           QueryExp   query)
    {
        try
        {
            return ensured(getDeferredJMXConnector(),
                           getDefaultTimeout(),
                           getDefaultTimeoutUnits()).get().getMBeanServerConnection().queryMBeans(name, query);
        }
        catch (IOException e)
        {
            throw new ObjectNotAvailableException(getDeferredJMXConnector(), e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int destroy()
    {
        // close the JMXConnector (if we've got one)
        JMXConnector jmxConnector = m_cachedJMXConnector.release();

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

        return super.destroy();
    }
}
