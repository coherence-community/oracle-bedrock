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
import com.oracle.tools.deferred.PermanentlyUnavailableException;
import com.oracle.tools.deferred.TemporarilyUnavailableException;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

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
    private String jmxConnectionURL;

    /**
     * The {@link JMXConnectorBuilder} for the specified url.
     */
    private JMXConnectorBuilder jmxConnectorBuilder;


    /**
     * Constructs a {@link DeferredJMXConnector}.
     * <p>
     * (defaults to use a {@link StandardJMXConnectorBuilder})
     *
     * @param jmxConnectionURL  the JMX connection url
     */
    public DeferredJMXConnector(String jmxConnectionURL)
    {
        this.jmxConnectionURL = jmxConnectionURL;
        jmxConnectorBuilder   = new StandardJMXConnectorBuilder();
    }


    /**
     * Constructs a {@link DeferredJMXConnector}.
     *
     * @param jmxConnectionURL     the JMX connection url
     * @param jmxConnectorBuilder  the {@link JMXConnectorBuilder} to realize
     *                          {@link JMXConnector}s
     */
    public DeferredJMXConnector(String              jmxConnectionURL,
                                JMXConnectorBuilder jmxConnectorBuilder)
    {
        this.jmxConnectionURL    = jmxConnectionURL;
        this.jmxConnectorBuilder = jmxConnectorBuilder;
    }


    /**
     * Obtain the JMX Connector URL for the {@link DeferredJMXConnector}.
     *
     * @return  the url for the {@link JMXConnector}
     */
    public String getJMXConnectionURL()
    {
        return jmxConnectionURL;
    }


    @Override
    public JMXConnector get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
    {
        String              username    = "";
        String              password    = "";

        Map<String, Object> env         = new HashMap<String, Object>();
        String[]            credentials = new String[] {username, password};

        env.put(JMXConnector.CREDENTIALS, credentials);

        try
        {
            JMXConnector connector;

            connector = jmxConnectorBuilder.realize(new JMXServiceURL(jmxConnectionURL), env);
            connector.connect();

            return connector;
        }
        catch (IOException e)
        {
            throw new PermanentlyUnavailableException(this, e);
        }
        catch (Exception e)
        {
            // we assume any exception means we should retry
            throw new TemporarilyUnavailableException(this, e);
        }
    }


    @Override
    public Class<JMXConnector> getDeferredClass()
    {
        return JMXConnector.class;
    }


    @Override
    public String toString()
    {
        return String.format("Deferred<JMXConnector>{%s}", jmxConnectionURL);
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
        @Override
        public JMXConnector realize(JMXServiceURL  url,
                                    Map<String, ?> env) throws IOException
        {
            return JMXConnectorFactory.newJMXConnector(url, env);
        }
    }
}
