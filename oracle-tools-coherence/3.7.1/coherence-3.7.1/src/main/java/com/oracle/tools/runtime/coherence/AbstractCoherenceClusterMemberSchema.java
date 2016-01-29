/*
 * File: AbstractCoherenceClusterMemberSchema.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;

import com.oracle.tools.runtime.java.AbstractJavaApplicationSchema;
import com.oracle.tools.runtime.java.ContainerBasedJavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationSchema;
import com.oracle.tools.runtime.java.options.SystemProperties;
import com.oracle.tools.runtime.java.options.SystemProperty;

import com.oracle.tools.runtime.remote.RemotePlatform;

import com.oracle.tools.util.CompletionListener;

import java.net.InetAddress;

import java.util.Iterator;
import java.util.Properties;

/**
 * An abstract implementation of a {@link CoherenceClusterMemberSchema}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractCoherenceClusterMemberSchema<A extends CoherenceClusterMember,
                                                           S extends AbstractCoherenceClusterMemberSchema<A, S>>
    extends AbstractJavaApplicationSchema<A, S> implements FluentCoherenceClusterMemberSchema<A, S>,
                                                           ContainerBasedJavaApplicationBuilder.ApplicationController
{
    /**
     * Constructs an {@link AbstractCoherenceClusterMemberSchema} based on
     * a {@link CoherenceClusterMemberSchema}.
     *
     * @param schema the other {@link CoherenceClusterMemberSchema}
     */
    public AbstractCoherenceClusterMemberSchema(CoherenceClusterMemberSchema<A> schema)
    {
        super(schema);
    }


    /**
     * Constructs an {@link AbstractCoherenceClusterMemberSchema} based on
     * a {@link JavaApplicationSchema}.
     *
     * @param schema the {@link JavaApplicationSchema}
     */
    public AbstractCoherenceClusterMemberSchema(JavaApplicationSchema<A> schema)
    {
        super(schema);
    }


    /**
     * Construct a {@link AbstractCoherenceClusterMemberSchema} with a given application class name,
     * using the class path of the executing application.
     *
     * @param applicationClassName the fully qualified class name of the Java application
     */
    public AbstractCoherenceClusterMemberSchema(String applicationClassName)
    {
        super(applicationClassName);
    }


    /**
     * Construct a {@link AbstractCoherenceClusterMemberSchema} with a given application class name,
     * but using the class path of the executing application.
     *
     * @param applicationClassName the fully qualified class name of the Java application
     * @param classPath            the class path for the Java application.
     */
    public AbstractCoherenceClusterMemberSchema(String applicationClassName,
                                                String classPath)
    {
        super(applicationClassName, classPath);
    }


    /**
     * Construct a {@link AbstractCoherenceClusterMemberSchema}.
     *
     * @param executableName       the executable name to run
     * @param applicationClassName the fully qualified class name of the Java application
     * @param classPath            the class path for the Java application
     */
    public AbstractCoherenceClusterMemberSchema(String executableName,
                                                String applicationClassName,
                                                String classPath)
    {
        super(executableName, applicationClassName, classPath);
    }


    @Override
    public void start(ContainerBasedJavaApplicationBuilder.ControllableApplication application,
                      CompletionListener<Void>                                     listener)
    {
        RemoteCallable<Void> callable = new RemoteCallableStaticMethod<Void>(DEFAULT_CACHE_SERVER_CLASSNAME, "start");

        application.submit(callable, listener);
    }


    @Override
    public void destroy(ContainerBasedJavaApplicationBuilder.ControllableApplication application,
                        CompletionListener<Void>                                     listener)
    {
        RemoteCallable<Void> callable = new RemoteCallableStaticMethod<Void>(DEFAULT_CACHE_SERVER_CLASSNAME,
                                                                             "shutdown");

        application.submit(callable, listener);
    }


    @Override
    protected void configureDefaults()
    {
        setPreferIPv4(true);

        // the following is to ensure Coherence will work with Java 1.8.0 b91
        setSystemProperty("javax.xml.accessExternalSchema", "file");

        // ensure we're running in headless mode
        setHeadless(true);

        SystemProperties systemProperties = getOptions().get(SystemProperties.class);

        systemProperties =
            systemProperties.addIfAbsent(SystemProperty.of(CoherenceCacheServerSchema.PROPERTY_LOCALHOST_ADDRESS,
                                                           new SystemProperty.ContextSensitiveValue()
                                                           {
                                                               @Override
                                                               public Object getValue(String                name,
                                                                                      Platform              platform,
                                                                                      JavaApplicationSchema schema)
                                                               {
                                                                   if (platform
                                                                       instanceof RemotePlatform)
                                                                   {
                                                                       InetAddress inetAddress = platform.getAddress();

                                                                       if (inetAddress == null)
                                                                       {
                                                                           return null;    // property doesn't exist
                                                                       }
                                                                       else
                                                                       {
                                                                           return inetAddress.getHostAddress();
                                                                       }
                                                                   }
                                                                   else
                                                                   {
                                                                       return null;        // property doesn't exist
                                                                   }

                                                               }
                                                           }));

        systemProperties =
            systemProperties.addIfAbsent(SystemProperty.of(CoherenceCacheServerSchema.PROPERTY_MACHINE_NAME,
                                                           new SystemProperty.ContextSensitiveValue()
                                                           {
                                                               @Override
                                                               public Object getValue(String                name,
                                                                                      Platform              platform,
                                                                                      JavaApplicationSchema schema)
                                                               {
                                                                   if (platform
                                                                       instanceof RemotePlatform)
                                                                   {
                                                                       return platform.getName();
                                                                   }
                                                                   else
                                                                   {
                                                                       return null;
                                                                   }
                                                               }
                                                           }));

        getOptions().add(systemProperties);
    }


    @Override
    public S setCacheConfigURI(String cacheConfigURI)
    {
        return setSystemProperty(PROPERTY_CACHECONFIG, cacheConfigURI);
    }


    @Override
    public String getCacheConfigURI()
    {
        return getSystemProperty(PROPERTY_CACHECONFIG, String.class, null);
    }


    @Override
    public S setClusterName(String name)
    {
        return setSystemProperty(PROPERTY_CLUSTER_NAME, name);
    }


    @Override
    public String getClusterName()
    {
        return getSystemProperty(PROPERTY_CLUSTER_NAME, String.class, null);
    }


    @Override
    public S setClusterPort(int port)
    {
        return setSystemProperty(PROPERTY_CLUSTER_PORT, port);
    }


    @Override
    public S setClusterPort(Iterator<Integer> ports)
    {
        return setSystemProperty(PROPERTY_CLUSTER_PORT, ports);
    }


    @Override
    public S setLocalHostAddress(String address)
    {
        return setSystemProperty(PROPERTY_LOCALHOST_ADDRESS, address);
    }


    @Override
    public String getLocalHostAddress()
    {
        return getSystemProperty(PROPERTY_LOCALHOST_ADDRESS, String.class, null);
    }


    @Override
    public S setLocalHostPort(int port)
    {
        return setSystemProperty(PROPERTY_LOCALHOST_PORT, port);
    }


    @Override
    public S setLocalHostPort(Iterator<Integer> ports)
    {
        return setSystemProperty(PROPERTY_LOCALHOST_PORT, ports);
    }


    @Override
    public S setLog(String destination)
    {
        return setSystemProperty(PROPERTY_LOG, destination);
    }


    @Override
    public String getLog()
    {
        return getSystemProperty(PROPERTY_LOG, String.class, null);
    }


    @Override
    public S setLogLevel(int level)
    {
        return setSystemProperty(PROPERTY_LOG_LEVEL, level);
    }


    @Override
    public int getLogLevel()
    {
        return getSystemProperty(PROPERTY_LOG_LEVEL, Integer.class, -1);
    }


    @Override
    @SuppressWarnings("unchecked")
    public S setJMXManagementMode(JMXManagementMode mode)
    {
        setJMXSupport((mode == JMXManagementMode.ALL || mode == JMXManagementMode.LOCAL_ONLY));
        setSystemProperty(PROPERTY_MANAGEMENT_MODE, mode.toSystemProperty());

        return (S) this;
    }


    @Override
    public JMXManagementMode getJMXManagementMode()
    {
        return JMXManagementMode.fromSystemProperty(getSystemProperty(PROPERTY_MANAGEMENT_MODE, String.class, null));
    }


    @Override
    public S setMulticastTTL(int ttl)
    {
        return setSystemProperty(PROPERTY_MULTICAST_TTL, ttl);
    }


    @Override
    public int getMulticastTTL()
    {
        return getSystemProperty(PROPERTY_MULTICAST_TTL, Integer.class, -1);
    }


    @Override
    public S setOperationalOverrideURI(String operationalOverrideURI)
    {
        return setSystemProperty(PROPERTY_OPERATIONAL_OVERRIDE, operationalOverrideURI);
    }


    @Override
    public String getOperationalOverrideURI()
    {
        return getSystemProperty(PROPERTY_OPERATIONAL_OVERRIDE, String.class, null);
    }


    @Override
    public S setPofConfigURI(String pofConfigURI)
    {
        return setSystemProperty(PROPERTY_POF_CONFIG, pofConfigURI);
    }


    @Override
    public String getPofConfigURI()
    {
        return getSystemProperty(PROPERTY_POF_CONFIG, String.class, null);
    }


    @Override
    public S setPofEnabled(boolean isEnabled)
    {
        return setSystemProperty(PROPERTY_POF_ENABLED, isEnabled);
    }


    @Override
    public boolean isPofEnabled()
    {
        return getSystemProperty(PROPERTY_POF_ENABLED, Boolean.class, false);
    }


    @Override
    public S setSiteName(String name)
    {
        return setSystemProperty(PROPERTY_SITE_NAME, name);
    }


    @Override
    public String getSiteName()
    {
        return getSystemProperty(PROPERTY_SITE_NAME, String.class, null);
    }


    @Override
    public S setStorageEnabled(boolean isStorageEnabled)
    {
        return setSystemProperty(PROPERTY_DISTRIBUTED_LOCALSTORAGE, isStorageEnabled);
    }


    @Override
    public boolean isStorageEnabled()
    {
        return getSystemProperty(PROPERTY_DISTRIBUTED_LOCALSTORAGE, Boolean.class, true);
    }


    @Override
    @SuppressWarnings("unchecked")
    public S setRemoteJMXManagement(boolean isEnabled)
    {
        setJMXSupport(isEnabled);
        setSystemProperty(PROPERTY_MANAGEMENT_REMOTE, isEnabled);

        return (S) this;

    }


    @Override
    public boolean isRemoteJMXManagement()
    {
        return getSystemProperty(PROPERTY_MANAGEMENT_REMOTE, Boolean.class, false);
    }


    @Override
    public S setRoleName(String name)
    {
        return setSystemProperty(PROPERTY_ROLE_NAME, name);
    }


    @Override
    public String getRoleName()
    {
        return getSystemProperty(PROPERTY_ROLE_NAME, String.class, null);
    }


    @Override
    public S setMachineName(String name)
    {
        return setSystemProperty(PROPERTY_MACHINE_NAME, name);
    }


    @Override
    public String getMachineName()
    {
        return getSystemProperty(PROPERTY_MACHINE_NAME, String.class, null);
    }


    @Override
    public S setTCMPEnabled(boolean isTCMPEnabled)
    {
        return setSystemProperty(PROPERTY_TCMP_ENABLED, isTCMPEnabled);
    }


    @Override
    public boolean isTCMPEnabled()
    {
        return getSystemProperty(PROPERTY_TCMP_ENABLED, Boolean.class, true);
    }


    @Override
    @SuppressWarnings("unchecked")
    public S useLocalHostMode()
    {
        // TODO: this needs to be changed to use the local host of the platform, not the LocalPlatform
        setLocalHostAddress(InetAddress.getLoopbackAddress().getHostAddress());
        setMulticastTTL(0);

        return (S) this;
    }


    @Override
    public S setWellKnownAddress(String address)
    {
        return setSystemProperty(PROPERTY_WELL_KNOWN_ADDRESS, address);
    }


    @Override
    public String getWellKnownAddress()
    {
        return getSystemProperty(PROPERTY_WELL_KNOWN_ADDRESS, String.class, null);
    }


    @Override
    public S setWellKnownAddressPort(int port)
    {
        return setSystemProperty(PROPERTY_WELL_KNOWN_ADDRESS_PORT, port);
    }


    @Override
    public S setWellKnownAddressPort(Iterator<Integer> ports)
    {
        return setSystemProperty(PROPERTY_WELL_KNOWN_ADDRESS_PORT, ports);
    }
}
