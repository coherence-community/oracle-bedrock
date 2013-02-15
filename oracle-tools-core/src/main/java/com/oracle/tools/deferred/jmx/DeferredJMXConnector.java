/*
 * File: DeferredJMXConnector.java
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
import com.oracle.tools.deferred.ObjectNotAvailableException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link DeferredJMXConnector} is a {@link Deferred} for a
 * {@link JMXConnector}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class DeferredJMXConnector implements Deferred<JMXConnector>
{
    /**
     * The url that specifies the JMX connection.
     */
    private String m_jmxConnectionURL;

    /**
     * The {@link JMXConnectorBuilder} for the specified url.
     */
    private JMXConnectorBuilder m_jmxConnectorBuilder;


    /**
     * Constructs a {@link DeferredJMXConnector}.
     * <p>
     * (defaults to use a {@link StandardJMXConnectorBuilder})
     *
     * @param jmxConnectionURL  the JMX connection url
     */
    public DeferredJMXConnector(String jmxConnectionURL)
    {
        m_jmxConnectionURL    = jmxConnectionURL;
        m_jmxConnectorBuilder = new StandardJMXConnectorBuilder();
    }


    /**
     * Constructs a {@link DeferredJMXConnector}.
     *
     * @param jmxConnectionURL  the JMX connection url
     * @param builder           the {@link JMXConnectorBuilder} to realize
     *                          {@link JMXConnector}s
     */
    public DeferredJMXConnector(String              jmxConnectionURL,
                                JMXConnectorBuilder builder)
    {
        m_jmxConnectionURL    = jmxConnectionURL;
        m_jmxConnectorBuilder = builder;
    }


    /**
     * Obtain the JMX Connector URL for the {@link DeferredJMXConnector}.
     *
     * @return  the url for the {@link JMXConnector}
     */
    public String getJMXConnectionURL()
    {
        return m_jmxConnectionURL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JMXConnector get() throws ObjectNotAvailableException
    {
        String              username    = "";
        String              password    = "";

        Map<String, Object> env         = new HashMap<String, Object>();
        String[]            credentials = new String[] {username, password};

        env.put(JMXConnector.CREDENTIALS, credentials);

        try
        {
            JMXConnector connector;

            connector = m_jmxConnectorBuilder.realize(new JMXServiceURL(m_jmxConnectionURL), env);
            connector.connect();

            return connector;
        }
        catch (IOException e)
        {
            return null;
        }
        catch (Exception e)
        {
            // it's important to re-throw as a runtime as it allows
            // Ensured and Supervised resources to re-try.
            throw new RuntimeException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<JMXConnector> getDeferredClass()
    {
        return JMXConnector.class;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("Deferred<JMXConnector>{%s}", m_jmxConnectionURL);
    }


    /**
     * A {@link JMXConnectorBuilder} provides a mechanism to realize a
     * {@link JMXConnector} when required.
     */
    public static interface JMXConnectorBuilder
    {
        /**
         * Realize a {@link JMXConnector} given the specified {@link JMXServiceURL}
         * and environment.
         *
         * @param url  the {@link JMXServiceURL}
         * @param env  the environment
         *
         * @return an initialized {@link JMXConnector}
         *
         * @throws IOException  should the {@link JMXConnector} fail to be realized
         */
        public JMXConnector realize(JMXServiceURL  url,
                                    Map<String, ?> env) throws IOException;
    }


    /**
     * A {@link StandardJMXConnectorBuilder} is a {@link JMXConnectorBuilder}
     * implementation that uses standard JMX methods to create a {@link JMXConnector}.
     */
    public static class StandardJMXConnectorBuilder implements JMXConnectorBuilder
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public JMXConnector realize(JMXServiceURL  url,
                                    Map<String, ?> env) throws IOException
        {
            return JMXConnectorFactory.newJMXConnector(url, env);
        }
    }
}
