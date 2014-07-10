/*
 * File: ClusterMemberSchema.java
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

import com.oracle.tools.runtime.ApplicationConsole;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;

import com.oracle.tools.runtime.java.ContainerBasedJavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationSchema;
import com.oracle.tools.runtime.java.JavaProcess;

import com.oracle.tools.util.CompletionListener;

import com.tangosol.coherence.component.net.Management;

import com.tangosol.net.DefaultCacheServer;

import java.util.Properties;

/**
 * A {@link ClusterMemberSchema} is a Coherence-based specific {@link JavaApplicationSchema}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Deprecated
public class ClusterMemberSchema extends AbstractCoherenceClusterMemberSchema<ClusterMember, ClusterMemberSchema>
{
    /**
     * The {@link Management} enumeration specifies the valid JMX Management Modes for a cluster node.
     */
    public enum JMXManagementMode
    {
        ALL,
        NONE,
        REMOTE_ONLY,
        LOCAL_ONLY;

        /**
         * Determines the system property representation of the {@link JMXManagementMode}
         *
         * @return A {@link String}
         */
        public String getSystemProperty()
        {
            // default to all
            String result = "all";

            if (this == NONE)
            {
                result = "none";
            }
            else if (this == REMOTE_ONLY)
            {
                result = "remote-only";
            }
            else if (this == LOCAL_ONLY)
            {
                result = "local-only";
            }

            return result;
        }
    }


    /**
     * The tangosol.coherence.cacheconfig property.
     */
    public static final String PROPERTY_CACHECONFIG = "tangosol.coherence.cacheconfig";

    /**
     * The tangosol.coherence.cluster property.
     */
    public static final String PROPERTY_CLUSTER_NAME = "tangosol.coherence.cluster";

    /**
     * The tangosol.coherence.clusterport property.
     */
    public static final String PROPERTY_CLUSTER_PORT = "tangosol.coherence.clusterport";

    /**
     * The tangosol.coherence.distributed.localstorage property.
     */
    public static final String PROPERTY_DISTRIBUTED_LOCALSTORAGE = "tangosol.coherence.distributed.localstorage";

    /**
     * The tangosol.coherence.localhost property.
     */
    public static final String PROPERTY_LOCALHOST_ADDRESS = "tangosol.coherence.localhost";

    /**
     * The tangosol.coherence.localport property.
     */
    public static final String PROPERTY_LOCALHOST_PORT = "tangosol.coherence.localport";

    /**
     * The tangosol.coherence.log property.
     */
    public static final String PROPERTY_LOG = "tangosol.coherence.log";

    /**
     * The tangosol.coherence.log.level property.
     */
    public static final String PROPERTY_LOG_LEVEL = "tangosol.coherence.log.level";

    /**
     * The tangosol.coherence.override property.
     */
    public static final String PROPERTY_OPERATIONAL_OVERRIDE = "tangosol.coherence.override";

    /**
     * The tangosol.coherence.role property.
     */
    public static final String PROPERTY_ROLE_NAME = "tangosol.coherence.role";

    /**
     * The tangosol.coherence.site property.
     */
    public static final String PROPERTY_SITE_NAME = "tangosol.coherence.site";

    /**
     * The tangosol.coherence.tcmp.enabled property.
     */
    public static final String PROPERTY_TCMP_ENABLED = "tangosol.coherence.tcmp.enabled";

    /**
     * The tangosol.coherence.management property.
     */
    public static final String PROPERTY_MANAGEMENT_MODE = "tangosol.coherence.management";

    /**
     * The tangosol.coherence.management.remote property.
     */
    public static final String PROPERTY_MANAGEMENT_REMOTE = "tangosol.coherence.management.remote";

    /**
     * The tangosol.coherence.ttl property.
     */
    public static final String PROPERTY_MULTICAST_TTL = "tangosol.coherence.ttl";

    /**
     * The tangosol.pof.config property.
     */
    public static final String PROPERTY_POF_CONFIG = "tangosol.pof.config";

    /**
     * The tangosol.pof.enabled property.
     */
    public static final String PROPERTY_POF_ENABLED = "tangosol.pof.enabled";

    /**
     * The tangosol.coherence.wka property.
     */
    public static final String PROPERTY_WELL_KNOWN_ADDRESS = "tangosol.coherence.wka";

    /**
     * The tangosol.coherence.wka.port property.
     */
    public static final String PROPERTY_WELL_KNOWN_ADDRESS_PORT = "tangosol.coherence.wka.port";

    /**
     * The com.tangosol.net.DefaultCacheServer classname.
     */
    public static final String DEFAULT_CACHE_SERVER_CLASSNAME = "com.tangosol.net.DefaultCacheServer";


    /**
     * Constructs a {@link ClusterMemberSchema} for the {@link DefaultCacheServer}
     * defaulting to use the current classpath.
     */
    public ClusterMemberSchema()
    {
        this(DEFAULT_CACHE_SERVER_CLASSNAME);
    }


    /**
     * Constructs a {@link ClusterMemberSchema} defaulting to use the current classpath
     *
     * @param applicationClassName  The name of the class for the Coherence-based application.
     */
    public ClusterMemberSchema(String applicationClassName)
    {
        super(applicationClassName);
    }


    /**
     * Constructs a {@link ClusterMemberSchema}.
     *
     * @param applicationClassName  The name of the class for the Coherence-based application.
     * @param classPath             The class path of the application.
     */
    public ClusterMemberSchema(String applicationClassName,
                               String classPath)
    {
        super(applicationClassName, classPath);
    }


    /**
     * Sets the JMX management mode for {@link ClusterMember}s created with this {@link ClusterMemberSchema}.
     *
     * @param mode
     *
     * @return The {@link ClusterMemberSchema}.
     */
    public ClusterMemberSchema setJMXManagementMode(JMXManagementMode mode)
    {
        setJMXSupport((mode == JMXManagementMode.ALL || mode == JMXManagementMode.LOCAL_ONLY));
        setSystemProperty(PROPERTY_MANAGEMENT_MODE, mode.getSystemProperty());

        return this;
    }


    /**
     * Configures the {@link ClusterMemberSchema} so that when realized by a {@link JavaApplicationBuilder}
     * the resulting {@link ClusterMember} will be running locally in local-host only mode.
     * <p>
     * This method is now deprecated.   Use {@link #useLocalHostMode} instead.
     *
     * @return The {@link ClusterMemberSchema}.
     */
    public ClusterMemberSchema setSingleServerMode()
    {
        return useLocalHostMode();
    }


    @Override
    public ClusterMember createJavaApplication(JavaProcess        process,
                                               String             name,
                                               ApplicationConsole console,
                                               Properties         environmentVariables,
                                               Properties         systemProperties)
    {
        return new ClusterMember(process,
                                 name,
                                 console,
                                 environmentVariables,
                                 systemProperties,
                                 isDiagnosticsEnabled(),
                                 getDefaultTimeout(),
                                 getDefaultTimeoutUnits(),
                                 getLifecycleInterceptors());
    }


    @Override
    public void start(ContainerBasedJavaApplicationBuilder.ControllableApplication application,
                      CompletionListener<Void>                                     listener)
    {
        RemoteCallable<Void> callable = new RemoteCallableStaticMethod<Void>("com.tangosol.net.DefaultCacheServer",
                                                                             "start");

        application.submit(callable, listener);
    }


    @Override
    public void destroy(ContainerBasedJavaApplicationBuilder.ControllableApplication application,
                        CompletionListener<Void>                                     listener)
    {
        RemoteCallable<Void> callable = new RemoteCallableStaticMethod<Void>("com.tangosol.net.DefaultCacheServer",
                                                                             "shutdown");

        application.submit(callable, listener);
    }


    @Override
    public Class<ClusterMember> getApplicationClass()
    {
        return ClusterMember.class;
    }
}
