/*
 * File: AbstractCoherenceClusterMember.java
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

import com.oracle.tools.predicate.Predicate;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LifecycleEventInterceptor;

import com.oracle.tools.runtime.coherence.callables.GetClusterMemberUIDs;
import com.oracle.tools.runtime.coherence.callables.GetClusterName;
import com.oracle.tools.runtime.coherence.callables.GetClusterSize;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberId;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberRoleName;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberSiteName;
import com.oracle.tools.runtime.coherence.callables.GetLocalMemberUID;
import com.oracle.tools.runtime.coherence.callables.GetServiceStatus;
import com.oracle.tools.runtime.coherence.callables.IsServiceRunning;

import com.oracle.tools.runtime.java.AbstractJavaApplication;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.JavaProcess;
import com.oracle.tools.runtime.java.util.RemoteCallableStaticMethod;

import com.tangosol.net.NamedCache;

import com.tangosol.util.UID;

import java.lang.reflect.Method;

import java.util.Properties;
import java.util.Set;

import java.util.concurrent.TimeUnit;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

/**
 * An abstract implementation of a {@link CoherenceClusterMember}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractCoherenceClusterMember<A extends AbstractCoherenceClusterMember<A>>
    extends AbstractJavaApplication<A, JavaProcess> implements CoherenceClusterMember
{
    /**
     * The MBean name of the Coherence Cluster MBean.
     */
    public static final String MBEAN_NAME_CLUSTER = "Coherence:type=Cluster";


    /**
     * Construct a {@link AbstractCoherenceClusterMember}.
     *
     * @param process               the {@link Process} representing the {@link AbstractCoherenceClusterMember}
     * @param name                  the name of the {@link AbstractCoherenceClusterMember}
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the
     *                              realized {@link Application}. This may be <code>null</code> if not required
     * @param environmentVariables  the environment variables used when starting the {@link AbstractCoherenceClusterMember}
     * @param systemProperties      the system properties provided to the {@link AbstractCoherenceClusterMember}
     * @param isDiagnosticsEnabled  should diagnostic information be logged/output
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     */
    public AbstractCoherenceClusterMember(JavaProcess                                    process,
                                          String                                         name,
                                          ApplicationConsole                             console,
                                          Properties                                     environmentVariables,
                                          Properties                                     systemProperties,
                                          boolean                                        isDiagnosticsEnabled,
                                          long                                           defaultTimeout,
                                          TimeUnit                                       defaultTimeoutUnits,
                                          Iterable<LifecycleEventInterceptor<? super A>> interceptors)
    {
        super(process,
              name,
              console,
              environmentVariables,
              systemProperties,
              isDiagnosticsEnabled,
              defaultTimeout,
              defaultTimeoutUnits,
              interceptors);
    }


    /**
     * Obtains the Coherence Cluster {@link MBeanInfo} for the {@link AbstractCoherenceClusterMember}.
     * <p>
     * If the JMX infrastructure in the {@link JavaApplication} is not yet
     * available, it will block at wait for the default application timeout
     * until it becomes available.
     *
     * @return a {@link MBeanInfo}
     *
     * @throws com.oracle.tools.deferred.UnresolvableInstanceException
     *                                        when the resource is not available
     * @throws UnsupportedOperationException  when JMX is not enabled for the
     *                                        {@link JavaApplication}
     */
    public MBeanInfo getClusterMBeanInfo()
    {
        try
        {
            return getMBeanInfo(new ObjectName(MBEAN_NAME_CLUSTER));
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException("Could not retrieve the Coherence Cluster MBean", e);
        }
    }


    @Override
    public int getClusterSize()
    {
        return submit(new GetClusterSize());
    }


    @Override
    public int getLocalMemberId()
    {
        return submit(new GetLocalMemberId());
    }


    @Override
    public UID getLocalMemberUID()
    {
        return submit(new GetLocalMemberUID());
    }


    @Override
    public Set<UID> getClusterMemberUIDs()
    {
        return submit(new GetClusterMemberUIDs());
    }


    /**
     * Obtains the Coherence Service {@link MBeanInfo} for the {@link AbstractCoherenceClusterMember}.
     * <p/>
     * If the JMX infrastructure in the {@link JavaApplication} is not yet
     * available, it will block at wait for the default application timeout
     * until it becomes available.
     *
     * @param serviceName  the name of the service
     * @param nodeId       the nodeId on which the service is defined
     *
     * @return a {@link MBeanInfo}
     *
     * @throws com.oracle.tools.deferred.UnresolvableInstanceException
     *                                        when the resource is not available
     * @throws UnsupportedOperationException  when JMX is not enabled for the
     *                                        {@link JavaApplication}
     */
    public MBeanInfo getServiceMBeanInfo(String serviceName,
                                         int    nodeId)
    {
        try
        {
            return getMBeanInfo(new ObjectName(String.format("Coherence:type=Service,name=%s,nodeId=%d", serviceName,
                                                             nodeId)));
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new UnsupportedOperationException(String
                .format("Could not retrieve the Coherence Service MBean [%s]", serviceName),
                                                    e);
        }
    }


    @Override
    public String getRoleName()
    {
        return submit(new GetLocalMemberRoleName());
    }


    @Override
    public String getSiteName()
    {
        return submit(new GetLocalMemberSiteName());
    }


    @Override
    public String getClusterName()
    {
        return submit(new GetClusterName());
    }


    @Override
    public NamedCache getCache(String cacheName)
    {
        // some methods can't be proxied!
        final Predicate<Method> unsupportedMethodsPredicate = new Predicate<Method>()
        {
            @Override
            public boolean evaluate(Method method)
            {
                return method.getName().equals("getCacheService") || method.getName().equals("addMapListener")
                       || method.getName().equals("removeMapListener");
            }
        };

        return getProxyFor(NamedCache.class,
                           new RemoteCallableStaticMethod<NamedCache>("com.tangosol.net.CacheFactory",
                                                                      "getCache",
                                                                      cacheName),
                           unsupportedMethodsPredicate);
    }


    @Override
    public boolean isServiceRunning(String serviceName)
    {
        return submit(new IsServiceRunning(serviceName));
    }


    @Override
    public ServiceStatus getServiceStatus(String serviceName)
    {
        return submit(new GetServiceStatus(serviceName));
    }
}
