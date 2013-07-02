/*
 * File: DeferredMBeanInfo.java
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

package com.oracle.tools.deferred.jmx;

import com.oracle.tools.deferred.Deferred;
import com.oracle.tools.deferred.InstanceUnavailableException;
import com.oracle.tools.deferred.UnresolvableInstanceException;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;

/**
 * A {@link DeferredMBeanInfo} is a {@link Deferred} for an {@link MBeanInfo}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredMBeanInfo implements Deferred<MBeanInfo>
{
    /**
     * A {@link Deferred} for the {@link JMXConnector}
     * that should be used to determine the {@link MBeanInfo}.
     */
    private Deferred<JMXConnector> m_deferredJMXConnector;

    /**
     * The {@link ObjectName} for the required {@link MBeanInfo}.
     */
    private ObjectName m_objectName;


    /**
     * Constructs a {@link DeferredMBeanInfo} given a {@link Deferred}
     * for the {@link JMXConnector} and the name of the {@link MBeanInfo}.
     *
     * @param deferredJMXConnector  the {@link Deferred} for the
     *                              {@link JMXConnector} from which to acquire the
     *                              {@link MBeanInfo}
     * @param objectName            the {@link ObjectName} of the {@link MBeanInfo}
     */
    public DeferredMBeanInfo(Deferred<JMXConnector> deferredJMXConnector,
                             ObjectName             objectName)
    {
        m_deferredJMXConnector = deferredJMXConnector;
        m_objectName           = objectName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanInfo get() throws UnresolvableInstanceException, InstanceUnavailableException
    {
        try
        {
            JMXConnector connector = m_deferredJMXConnector.get();

            if (connector == null)
            {
                throw new InstanceUnavailableException(this);
            }
            else
            {
                MBeanServerConnection connection = connector.getMBeanServerConnection();

                return connection.getMBeanInfo(m_objectName);
            }
        }
        catch (IOException e)
        {
            // an IOException represents a failed connection attempt
            throw new InstanceUnavailableException(this, e);
        }
        catch (NullPointerException e)
        {
            // an NPE would only occur when the server connection isn't available
            throw new InstanceUnavailableException(this, e);
        }
        catch (InstanceNotFoundException e)
        {
            // although the mbean isn't currently registered by the server,
            // it may be registered in the future
            throw new InstanceUnavailableException(this, e);
        }
        catch (InstanceUnavailableException e)
        {
            throw e;
        }
        catch (UnresolvableInstanceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new UnresolvableInstanceException(this, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MBeanInfo> getDeferredClass()
    {
        return MBeanInfo.class;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("Deferred<MBeanInfo>{on=%s, object=%s}", m_deferredJMXConnector, m_objectName);
    }
}
