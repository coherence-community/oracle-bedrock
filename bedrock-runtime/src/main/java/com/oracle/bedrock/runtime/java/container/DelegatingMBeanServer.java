/*
 * File: DelegatingMBeanServer.java
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

package com.oracle.bedrock.runtime.java.container;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.util.Set;

/**
 * A {@link DelegatingMBeanServer} is an {@link MBeanServer} implementation
 * that delegates calls to the appropriate {@link MBeanServer} of a
 * {@link Scope}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DelegatingMBeanServer implements MBeanServer
{
    /**
     * The domain of the {@link DelegatingMBeanServer}.
     */
    private String domain;


    /**
     * Constructs a {@link DelegatingMBeanServer}.
     *
     * @param domain  the domain
     */
    public DelegatingMBeanServer(String domain)
    {
        this.domain = domain;
    }


    /**
     * Obtains the domain onto which the {@link DelegatingMBeanServer}
     * is delegating.
     *
     * @return  the domain of the {@link DelegatingMBeanServer}.
     */
    public String getDomain()
    {
        return domain;
    }


    /**
     * Obtains the {@link MBeanServer} for the specified domain from the
     * {@link Container}.
     *
     * @return  the {@link MBeanServer} delegate
     */
    private MBeanServer getDelegate()
    {
        ContainerScope scope = Container.getContainerScope();
        ContainerMBeanServerBuilder builder = scope == null
                                              ? Container.getDefaultScope().getMBeanServerBuilder()
                                              : scope.getMBeanServerBuilder();
        MBeanServer mBeanServer = builder.getMBeanServer(domain);

        if (mBeanServer == null)
        {
            mBeanServer = builder.newMBeanServer(domain, null, builder.newMBeanServerDelegate());
        }

        return mBeanServer;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addNotificationListener(ObjectName           paramObjectName,
                                        NotificationListener paramNotificationListener,
                                        NotificationFilter   paramNotificationFilter,
                                        Object               paramObject) throws InstanceNotFoundException
    {
        getDelegate().addNotificationListener(paramObjectName,
                                              paramNotificationListener,
                                              paramNotificationFilter,
                                              paramObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addNotificationListener(ObjectName         paramObjectName1,
                                        ObjectName         paramObjectName2,
                                        NotificationFilter paramNotificationFilter,
                                        Object             paramObject) throws InstanceNotFoundException
    {
        getDelegate().addNotificationListener(paramObjectName1, paramObjectName2, paramNotificationFilter, paramObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInstance createMBean(String     paramString,
                                      ObjectName paramObjectName)
                                          throws ReflectionException, InstanceAlreadyExistsException,
                                                 MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        return getDelegate().createMBean(paramString, paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInstance createMBean(String     paramString,
                                      ObjectName paramObjectName,
                                      Object[]   paramArrayOfObject,
                                      String[]   paramArrayOfString)
                                          throws ReflectionException, InstanceAlreadyExistsException,
                                                 MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        return getDelegate().createMBean(paramString, paramObjectName, paramArrayOfObject, paramArrayOfString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInstance createMBean(String     paramString,
                                      ObjectName paramObjectName1,
                                      ObjectName paramObjectName2)
                                          throws ReflectionException, InstanceAlreadyExistsException,
                                                 MBeanRegistrationException, MBeanException,
                                                 NotCompliantMBeanException, InstanceNotFoundException
    {
        return getDelegate().createMBean(paramString, paramObjectName1, paramObjectName2);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInstance createMBean(String     paramString,
                                      ObjectName paramObjectName1,
                                      ObjectName paramObjectName2,
                                      Object[]   paramArrayOfObject,
                                      String[]   paramArrayOfString)
                                          throws ReflectionException, InstanceAlreadyExistsException,
                                                 MBeanRegistrationException, MBeanException,
                                                 NotCompliantMBeanException, InstanceNotFoundException
    {
        return getDelegate().createMBean(paramString,
                                         paramObjectName1,
                                         paramObjectName2,
                                         paramArrayOfObject,
                                         paramArrayOfString);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public ObjectInputStream deserialize(ObjectName paramObjectName,
                                         byte[]     paramArrayOfByte) throws InstanceNotFoundException, OperationsException
    {
        return getDelegate().deserialize(paramObjectName, paramArrayOfByte);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public ObjectInputStream deserialize(String paramString,
                                         byte[] paramArrayOfByte) throws OperationsException, ReflectionException
    {
        return getDelegate().deserialize(paramString, paramArrayOfByte);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public ObjectInputStream deserialize(String     paramString,
                                         ObjectName paramObjectName,
                                         byte[]     paramArrayOfByte)
                                             throws InstanceNotFoundException, OperationsException, ReflectionException
    {
        return getDelegate().deserialize(paramString, paramObjectName, paramArrayOfByte);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(ObjectName paramObjectName,
                               String     paramString)
                                   throws MBeanException, AttributeNotFoundException, InstanceNotFoundException,
                                          ReflectionException
    {
        return getDelegate().getAttribute(paramObjectName, paramString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeList getAttributes(ObjectName paramObjectName,
                                       String[]   paramArrayOfString)
                                           throws InstanceNotFoundException, ReflectionException
    {
        return getDelegate().getAttributes(paramObjectName, paramArrayOfString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader(ObjectName paramObjectName) throws InstanceNotFoundException
    {
        return getDelegate().getClassLoader(paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoaderFor(ObjectName paramObjectName) throws InstanceNotFoundException
    {
        return getDelegate().getClassLoaderFor(paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        return getDelegate().getClassLoaderRepository();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultDomain()
    {
        return getDelegate().getDefaultDomain();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDomains()
    {
        return getDelegate().getDomains();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getMBeanCount()
    {
        return getDelegate().getMBeanCount();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanInfo getMBeanInfo(ObjectName paramObjectName)
        throws InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        return getDelegate().getMBeanInfo(paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInstance getObjectInstance(ObjectName paramObjectName) throws InstanceNotFoundException
    {
        return getDelegate().getObjectInstance(paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object instantiate(String paramString) throws ReflectionException, MBeanException
    {
        return getDelegate().instantiate(paramString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object instantiate(String   paramString,
                              Object[] paramArrayOfObject,
                              String[] paramArrayOfString) throws ReflectionException, MBeanException
    {
        return getDelegate().instantiate(paramString, paramArrayOfObject, paramArrayOfString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object instantiate(String     paramString,
                              ObjectName paramObjectName)
                                  throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        return getDelegate().instantiate(paramString, paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object instantiate(String     paramString,
                              ObjectName paramObjectName,
                              Object[]   paramArrayOfObject,
                              String[]   paramArrayOfString)
                                  throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        return getDelegate().instantiate(paramString, paramObjectName, paramArrayOfObject, paramArrayOfString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(ObjectName paramObjectName,
                         String     paramString,
                         Object[]   paramArrayOfObject,
                         String[]   paramArrayOfString)
                             throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        return getDelegate().invoke(paramObjectName, paramString, paramArrayOfObject, paramArrayOfString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInstanceOf(ObjectName paramObjectName,
                                String     paramString) throws InstanceNotFoundException
    {
        return getDelegate().isInstanceOf(paramObjectName, paramString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRegistered(ObjectName paramObjectName)
    {
        return getDelegate().isRegistered(paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName paramObjectName,
                                           QueryExp   paramQueryExp)
    {
        return getDelegate().queryMBeans(paramObjectName, paramQueryExp);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ObjectName> queryNames(ObjectName paramObjectName,
                                      QueryExp   paramQueryExp)
    {
        return getDelegate().queryNames(paramObjectName, paramQueryExp);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectInstance registerMBean(Object     paramObject,
                                        ObjectName paramObjectName)
                                            throws InstanceAlreadyExistsException, MBeanRegistrationException,
                                                   NotCompliantMBeanException
    {
        return getDelegate().registerMBean(paramObject, paramObjectName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotificationListener(ObjectName           paramObjectName,
                                           NotificationListener paramNotificationListener)
                                               throws InstanceNotFoundException, ListenerNotFoundException
    {
        getDelegate().removeNotificationListener(paramObjectName, paramNotificationListener);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotificationListener(ObjectName           paramObjectName,
                                           NotificationListener paramNotificationListener,
                                           NotificationFilter   paramNotificationFilter,
                                           Object               paramObject)
                                               throws InstanceNotFoundException, ListenerNotFoundException
    {
        getDelegate().removeNotificationListener(paramObjectName,
                                                 paramNotificationListener,
                                                 paramNotificationFilter,
                                                 paramObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotificationListener(ObjectName paramObjectName1,
                                           ObjectName paramObjectName2)
                                               throws InstanceNotFoundException, ListenerNotFoundException
    {
        getDelegate().removeNotificationListener(paramObjectName1, paramObjectName2);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotificationListener(ObjectName         paramObjectName1,
                                           ObjectName         paramObjectName2,
                                           NotificationFilter paramNotificationFilter,
                                           Object             paramObject)
                                               throws InstanceNotFoundException, ListenerNotFoundException
    {
        getDelegate().removeNotificationListener(paramObjectName1,
                                                 paramObjectName2,
                                                 paramNotificationFilter,
                                                 paramObject);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(ObjectName paramObjectName,
                             Attribute  paramAttribute)
                                 throws InstanceNotFoundException, AttributeNotFoundException,
                                        InvalidAttributeValueException, MBeanException, ReflectionException
    {
        getDelegate().setAttribute(paramObjectName, paramAttribute);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeList setAttributes(ObjectName    paramObjectName,
                                       AttributeList paramAttributeList)
                                           throws InstanceNotFoundException, ReflectionException
    {
        return getDelegate().setAttributes(paramObjectName, paramAttributeList);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterMBean(ObjectName paramObjectName) throws InstanceNotFoundException, MBeanRegistrationException
    {
        getDelegate().unregisterMBean(paramObjectName);
    }
}
