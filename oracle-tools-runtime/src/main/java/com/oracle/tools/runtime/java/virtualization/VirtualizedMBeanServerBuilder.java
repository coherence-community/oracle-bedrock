/*
 * File: VirtualizedMBeanServerBuilder.java
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

package com.oracle.tools.runtime.java.virtualization;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.oracle.tools.runtime.java.JavaApplication.*;

/**
 * A {@link VirtualizedMBeanServerBuilder} is an {@link MBeanServerBuilder}
 * that isolates {@link MBeanServer}s with in a {@link VirtualizedSystem}.
 * <p>
 * Additionally, a {@link VirtualizedMBeanServerBuilder} automatically
 * manages the creation of {@link JMXConnectorServer}s when remote JMX
 * has been enabled.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class VirtualizedMBeanServerBuilder extends MBeanServerBuilder
{
    /**
     * The {@link URL} formate for JMX RMI connections.
     */
    public static final String JMX_RMI_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";

    /**
     * The jmx.remote.x.password.file property.
     */
    public static final String PROPERTY_JMX_REMOTE_PASSWORD_FILE = "jmx.remote.x.password.file";

    /**
     * The jmx.remote.x.access.file property.
     */
    public static final String PROPERTY_JMX_REMOTE_ACCESS_FILE = "jmx.remote.x.access.file";

    /**
     * The jmx.remote.rmi.client.socket.factory property.
     */
    public static final String PROPERTY_JMX_REMOTE_RMI_CLIENT_SOCKET_FACTORY = "jmx.remote.rmi.client.socket.factory";

    /**
     * The jmx.remote.rmi.server.socket.factory property.
     */
    public static final String PROPERTY_JMX_REMOTE_RMI_SERVER_SOCKET_FACTORY = "jmx.remote.rmi.server.socket.factory";

    /**
     * The javax.management.builder.initial property.
     */
    public static final String PROPERTY_JMX_MBEAN_SERVER_BUILDER = "javax.management.builder.initial";

    /**
     * The remote jmx password file.
     */
    public static final String DEFAULT_JMXREMOTE_PASSWORD_FILE = "/lib/management/jmxremote.password";

    /**
     * The remote jmx access file
     */
    public static final String DEFAULT_JMXREMOTE_ACCESS_FILE = "/lib/management/jmxremote.access";

    /**
     * The RMI Client Socket Factory class name.
     */
    public static final String DEFAULT_RMICLIENT_SOCKET_FACTORY = "javax.rmi.ssl.SslRMIClientSocketFactory";

    /**
     * The RMI Server Socket Factory class name.
     */
    public static final String DEFAULT_RMISERVER_SOCKET_FACTORY = "javax.rmi.ssl.SslRMIServerSocketFactory";

    /**
     * The class name of the {@link MBeanServerBuilder} prior to virtualizing.
     */
    private final String m_mBeanServerBuilderClassName;

    /**
     * The {@link ConnectorFactory} for the {@link VirtualizedMBeanServerFinder}.
     */
    private final ConnectorFactory m_jmxConnectorFactory;

    /**
     * The {@link RmiRegistryFactory} for the {@link VirtualizedMBeanServerFinder}.
     */
    private final RmiRegistryFactory m_rmiRegistryFactory;

    /**
     * The {@link AvailablePortIterator} to use for creating
     * remote jmx server connectors.
     */
    private AvailablePortIterator m_availablePortIterator;

    /**
     * The current {@link MBeanServer}s created by this
     * {@link VirtualizedMBeanServerBuilder} by domain name.
     */
    private Map<String, MBeanServer> m_mBeanServers;

    /**
     * The current {@link JMXConnectorServer}s by {@link MBeanServer}.
     */
    private Map<MBeanServer, JMXConnectorServer> m_jmxConnectorServers;


    /**
     * Constructs a {@link VirtualizedMBeanServerBuilder}.
     *
     * @param availablePortIterator  the {@link AvailablePortIterator} to use for
     *                               acquiring ports for server connectors
     */
    public VirtualizedMBeanServerBuilder(AvailablePortIterator availablePortIterator)
    {
        this(new DefaultConnectorFactory(), new DefaultRmiRegistryFactory(), availablePortIterator);
    }


    /**
     * Constructs a {@link VirtualizedMBeanServerBuilder}.
     *
     * @param availablePortIterator  the {@link AvailablePortIterator} to use for
     *                               acquiring ports for server connectors
     */
    public VirtualizedMBeanServerBuilder(ConnectorFactory      jmxConnectorFactory,
                                         RmiRegistryFactory    rmiRegistryFactory,
                                         AvailablePortIterator availablePortIterator)
    {
        m_mBeanServerBuilderClassName = System.getProperties().containsKey(PROPERTY_JMX_MBEAN_SERVER_BUILDER)
                                        ? System.getProperty(PROPERTY_JMX_MBEAN_SERVER_BUILDER)
                                        : MBeanServerBuilder.class.getCanonicalName();

        m_mBeanServers          = new HashMap<String, MBeanServer>();
        m_jmxConnectorServers   = new HashMap<MBeanServer, JMXConnectorServer>();

        m_jmxConnectorFactory   = jmxConnectorFactory;
        m_rmiRegistryFactory    = rmiRegistryFactory;
        m_availablePortIterator = availablePortIterator;
    }


    /**
     * Obtains the name of the {@link MBeanServerBuilder} class that
     * was defined prior to this {@link VirtualizedMBeanServerBuilder} being
     * created.
     *
     * @return  the {@link MBeanServerBuilder} class name
     */
    public String getPreviousMBeanServerBuilderClassName()
    {
        return m_mBeanServerBuilderClassName;
    }


    /**
     * Obtains the {@link MBeanServer} registered with the specified domain.
     * <p>
     * @param domain  the domain of the {@link MBeanServer} in the {@link Virtualization}.
     *
     * @return the {@link MBeanServer} or <code>null</code> if not associated
     *         with a domain
     */
    public synchronized MBeanServer getMBeanServer(String domain)
    {
        return m_mBeanServers.get(domain);
    }


    /**
     * Obtains the {@link JMXConnectorServer} that was started for the specified
     * {@link MBeanServer}.
     *
     * @param mBeanServer  the {@link MBeanServer}
     *
     * @return the {@link JMXConnectorServer} or <code>null</code> if the
     *         {@link MBeanServer} is unknown to this builder
     */
    public synchronized JMXConnectorServer getJMXConnectorServer(MBeanServer mBeanServer)
    {
        // determine the "real" MBeanServer if this one is a DelegatingMBeanServer
        mBeanServer = mBeanServer instanceof DelegatingMBeanServer
                      ? getMBeanServer(((DelegatingMBeanServer) mBeanServer).getDomain()) : mBeanServer;

        return m_jmxConnectorServers.get(mBeanServer);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized MBeanServer newMBeanServer(String              domain,
                                                   MBeanServer         outer,
                                                   MBeanServerDelegate delegate)
    {
        // attempt to locate the mbean server locally
        MBeanServer mBeanServer = m_mBeanServers.get(domain);

        if (mBeanServer == null)
        {
            // delegate the creation of the server to the super-class
            mBeanServer = super.newMBeanServer(domain, outer, delegate);

            // save the mBeanServer (it's scoped by this builder)
            m_mBeanServers.put(domain, mBeanServer);

            // establish an server connector for the mbean server whem remote jmx is enabled
            boolean isRemoteJMXEnabled = System.getProperties().containsKey(SUN_MANAGEMENT_JMXREMOTE)
                                         &&!"false".equalsIgnoreCase(System.getProperty(SUN_MANAGEMENT_JMXREMOTE));

            if (isRemoteJMXEnabled)
            {
                createJMXConnectorServer(mBeanServer, System.getProperties());
            }
        }

        return new DelegatingMBeanServer(domain);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MBeanServerDelegate newMBeanServerDelegate()
    {
        // always have the super-class provide the delegate
        return super.newMBeanServerDelegate();
    }


    /**
     * Creates a {@link JMXConnectorServer} for the specified {@link MBeanServer}
     * using the provided connection properties.
     *
     * @param mBeanServer  the {@link MBeanServer}
     * @param properties   the {@link Properties} for the connection
     *
     * @return a {@link JMXConnectorServer}
     */
    JMXConnectorServer createJMXConnectorServer(MBeanServer mBeanServer,
                                                Properties  properties)
    {
        try
        {
            String hostName;

            if (properties.containsKey(JAVA_RMI_SERVER_HOSTNAME))
            {
                hostName = properties.getProperty(JAVA_RMI_SERVER_HOSTNAME);
            }
            else
            {
                hostName = InetAddress.getLocalHost().getHostName();
            }

            int port;

            if (properties.containsKey(SUN_MANAGEMENT_JMXREMOTE_PORT))
            {
                port = Integer.parseInt(properties.getProperty(SUN_MANAGEMENT_JMXREMOTE_PORT));
            }
            else
            {
                port = m_availablePortIterator.next();
                properties.setProperty(SUN_MANAGEMENT_JMXREMOTE_PORT, String.valueOf(port));
            }

            m_rmiRegistryFactory.createRegistry(port);

            String remoteJMXConnectionUrl = String.format(JMX_RMI_URL_FORMAT, hostName, port);

            properties.setProperty(SUN_MANAGEMENT_JMXREMOTE_URL, remoteJMXConnectionUrl);

            JMXServiceURL       url         = new JMXServiceURL(remoteJMXConnectionUrl);
            Map<String, Object> environment = new HashMap<String, Object>();

            if (Boolean.parseBoolean(properties.getProperty(SUN_MANAGEMENT_JMXREMOTE_AUTHENTICATE)))
            {
                String javaHome     = System.getProperty(JAVA_HOME);
                String authFileName = properties.getProperty(SUN_MANAGEMENT_JMXREMOTE_PASSWORD_FILE);

                if (authFileName == null)
                {
                    authFileName = javaHome + DEFAULT_JMXREMOTE_PASSWORD_FILE;
                }

                if (authFileName != null)
                {
                    environment.put(PROPERTY_JMX_REMOTE_PASSWORD_FILE, authFileName);
                }

                String accessFileName = properties.getProperty(SUN_MANAGEMENT_JMXREMOTE_ACCESS_FILE);

                if (accessFileName == null)
                {
                    accessFileName = javaHome + DEFAULT_JMXREMOTE_ACCESS_FILE;
                }

                if (accessFileName != null)
                {
                    environment.put(PROPERTY_JMX_REMOTE_ACCESS_FILE, accessFileName);
                }
            }

            if (Boolean.parseBoolean(properties.getProperty(SUN_MANAGEMENT_JMXREMOTE_SSL)))
            {
                try
                {
                    environment.put(PROPERTY_JMX_REMOTE_RMI_CLIENT_SOCKET_FACTORY,
                                    Class.forName(DEFAULT_RMICLIENT_SOCKET_FACTORY).newInstance());
                    environment.put(PROPERTY_JMX_REMOTE_RMI_SERVER_SOCKET_FACTORY,
                                    Class.forName(DEFAULT_RMISERVER_SOCKET_FACTORY).newInstance());
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("JMXConnectorServer not started. SSL security requires the Java Dynamic Management Kit or Java 1.5.",
                                               e);
                }
            }

            // create the connector
            JMXConnectorServer connector = m_jmxConnectorFactory.createJMXConnectorServer(url,
                                                                                          environment,
                                                                                          mBeanServer);

            // remember the connector so we can clean up in the future
            m_jmxConnectorServers.put(mBeanServer, connector);

            // start server connector so external jmx management can work
            connector.start();

            return connector;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not start JMXConnectorServer", e);
        }
    }


    /**
     * A {@link JMXConnectorFactory} defines factory methods to acquire
     * JMX Connectors for clients and servers.
     */
    public static interface ConnectorFactory
    {
        /**
         * Creates a new {@link JMXConnectorServer}.
         *
         * @param url          the {@link JMXServiceURL}
         * @param environment  the environment variables for the connector
         * @param mBeanServer  the {@link MBeanServer} to which to connect
         *
         * @return a new {@link JMXConnectorServer}
         *
         * @throws IOException
         */
        public JMXConnectorServer createJMXConnectorServer(JMXServiceURL  url,
                                                           Map<String, ?> environment,
                                                           MBeanServer    mBeanServer) throws IOException;


        /**
         * Creates a new {@link JMXConnector}.
         *
         * @param url          the {@link JMXServiceURL}
         * @param environment  the environment variables for the connector
         *
         * @return
         *
         * @throws IOException
         */
        public JMXConnector createJMXConnector(JMXServiceURL  url,
                                               Map<String, ?> environment) throws IOException;
    }


    /**
     * A {@link RmiRegistryFactory} provides the ability to create Rmi {@link Registry}s.
     */
    public static interface RmiRegistryFactory
    {
        /**
         * Attempt to create an Rmi {@link Registry} on the specified port.
         *
         * @param port  the port number to use
         *
         * @throws RemoteException  if the {@link Registry} could not be created
         */
        public void createRegistry(int port) throws RemoteException;
    }


    /**
     * The default implementation of the {@link ConnectorFactory}.
     */
    public static class DefaultConnectorFactory implements ConnectorFactory
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public JMXConnectorServer createJMXConnectorServer(JMXServiceURL  url,
                                                           Map<String, ?> environment,
                                                           MBeanServer    mBeanServer) throws IOException
        {
            return JMXConnectorServerFactory.newJMXConnectorServer(url, environment, mBeanServer);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public JMXConnector createJMXConnector(JMXServiceURL  url,
                                               Map<String, ?> environment) throws IOException
        {
            return JMXConnectorFactory.newJMXConnector(url, environment);
        }
    }


    /**
     * The default implementation of an {@link RmiRegistryFactory}.
     */
    public static class DefaultRmiRegistryFactory implements RmiRegistryFactory
    {
        /**
         * {@inheritDoc}
         */
        public void createRegistry(int port) throws RemoteException
        {
            try
            {
                LocateRegistry.createRegistry(port);
            }
            catch (ExportException _)
            {
                // Ignored as the most likely cause is that we
                // have already bound a registry to this port
            }
        }
    }
}
