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

import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationProcess;
import com.oracle.tools.runtime.Platform;

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
import com.oracle.tools.runtime.java.JavaApplicationProcess;
import com.oracle.tools.runtime.java.features.JmxFeature;

import com.tangosol.net.NamedCache;

import com.tangosol.util.UID;

import java.util.Set;

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
public abstract class AbstractCoherenceClusterMember extends AbstractJavaApplication<JavaApplicationProcess>
    implements CoherenceClusterMember
{
    /**
     * The MBean name of the Coherence Cluster MBean.
     */
    public static final String MBEAN_NAME_CLUSTER = "Coherence:type=Cluster";


    /**
     * Constructs an {@link AbstractCoherenceClusterMember}.
     *
     * @param platform  the {@link Platform} on which the {@link Application} was launched
     * @param process   the underlying {@link ApplicationProcess} representing the {@link Application}
     * @param options   the {@link Options} used to launch the {@link Application}
     */
    public AbstractCoherenceClusterMember(Platform               platform,
                                          JavaApplicationProcess process,
                                          Options                options)
    {
        super(platform, process, options);
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
        JmxFeature jmxFeature = get(JmxFeature.class);

        if (jmxFeature == null)
        {
            throw new UnsupportedOperationException("The JmxFeature (Java Management Extensions) haven't been enabled for this application");
        }
        else
        {
            try
            {
                return jmxFeature.getMBeanInfo(new ObjectName(MBEAN_NAME_CLUSTER));
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
    }


    @Override
    public int getClusterSize()
    {
        return submitAndGet(new GetClusterSize());
    }


    @Override
    public int getLocalMemberId()
    {
        return submitAndGet(new GetLocalMemberId());
    }


    @Override
    public UID getLocalMemberUID()
    {
        return submitAndGet(new GetLocalMemberUID());
    }


    @Override
    public Set<UID> getClusterMemberUIDs()
    {
        return submitAndGet(new GetClusterMemberUIDs());
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
        JmxFeature jmxFeature = get(JmxFeature.class);

        if (jmxFeature == null)
        {
            throw new UnsupportedOperationException("The JmxFeature (Java Management Extensions) haven't been enabled for this application");
        }
        else
        {
            try
            {
                return jmxFeature.getMBeanInfo(new ObjectName(String.format("Coherence:type=Service,name=%s,nodeId=%d",
                                                                            serviceName,
                                                                            nodeId)));
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new UnsupportedOperationException(String.format("Could not retrieve the Coherence Service MBean [%s]",
                                                                      serviceName),
                                                        e);
            }
        }
    }


    @Override
    public String getRoleName()
    {
        return submitAndGet(new GetLocalMemberRoleName());
    }


    @Override
    public String getSiteName()
    {
        return submitAndGet(new GetLocalMemberSiteName());
    }


    @Override
    public String getClusterName()
    {
        return submitAndGet(new GetClusterName());
    }


    @Override
    public NamedCache getCache(String cacheName)
    {
        return new CoherenceNamedCache(this, cacheName);
    }


    @Override
    public boolean isServiceRunning(String serviceName)
    {
        return submitAndGet(new IsServiceRunning(serviceName));
    }


    @Override
    public ServiceStatus getServiceStatus(String serviceName)
    {
        return submitAndGet(new GetServiceStatus(serviceName));
    }
}
