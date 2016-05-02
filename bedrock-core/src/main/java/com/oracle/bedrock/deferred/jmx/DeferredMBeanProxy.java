/*
 * File: DeferredMBeanProxy.java
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

package com.oracle.bedrock.deferred.jmx;

import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.deferred.PermanentlyUnavailableException;
import com.oracle.bedrock.deferred.TemporarilyUnavailableException;
import com.oracle.bedrock.deferred.UnavailableException;

import java.io.IOException;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import javax.management.remote.JMXConnector;

/**
 * A {@link DeferredMBeanProxy} is a {@link Deferred} for a local
 * proxy to an MBean.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredMBeanProxy<T> implements Deferred<T>
{
    /**
     * A {@link Deferred} for the {@link JMXConnector}
     * that should be used to create the MBean proxy
     */
    private Deferred<JMXConnector> deferredJMXConnector;

    /**
     * The {@link ObjectName} for the required MBean.
     */
    private ObjectName objectName;

    /**
     * The class of the proxy to create.
     */
    private Class<T> proxyClass;


    /**
     * Constructs a {@link DeferredMBeanProxy} given a {@link Deferred}
     * for the {@link JMXConnector}, the name of the MBean object and proxy
     * class.
     *
     * @param deferredJMXConnector  the {@link Deferred} for the
     *                              {@link JMXConnector} from which to acquire the
     *                              MBean proxy
     * @param objectName            the {@link ObjectName} of the MBean
     * @param proxyClass            the proxy class
     */
    public DeferredMBeanProxy(Deferred<JMXConnector> deferredJMXConnector,
                              ObjectName             objectName,
                              Class<T>               proxyClass)
    {
        this.deferredJMXConnector = deferredJMXConnector;
        this.objectName           = objectName;
        this.proxyClass           = proxyClass;
    }


    @Override
    public T get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        try
        {
            JMXConnector connector = deferredJMXConnector.get();

            if (connector == null)
            {
                throw new TemporarilyUnavailableException(this);
            }
            else
            {
                MBeanServerConnection connection = connector.getMBeanServerConnection();

                return JMX.newMBeanProxy(connection, objectName, proxyClass);
            }
        }
        catch (IOException e)
        {
            // an IOException represents a failed connection attempt
            throw new TemporarilyUnavailableException(this, e);
        }
        catch (NullPointerException e)
        {
            // an NPE would only occur when the server connection isn't available
            throw new TemporarilyUnavailableException(this, e);
        }
        catch (UnavailableException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PermanentlyUnavailableException(this, e);
        }
    }


    @Override
    public Class<T> getDeferredClass()
    {
        return proxyClass;
    }


    @Override
    public String toString()
    {
        return String.format("Deferred<MBeanProxy>{on=%s, object=%s, class=%s}",
                             deferredJMXConnector,
                             objectName,
                             proxyClass);
    }
}
