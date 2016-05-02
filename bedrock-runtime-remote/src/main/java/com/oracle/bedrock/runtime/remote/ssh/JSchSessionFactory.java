/*
 * File: JSchSessionFactory.java
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

package com.oracle.bedrock.runtime.remote.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.oracle.bedrock.runtime.remote.Authentication;
import com.oracle.bedrock.runtime.remote.options.StrictHostChecking;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.options.Timeout;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A factory that can produce connected instances of
 * a JSch {@link Session}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JSchSessionFactory
{
    /**
     * The {@link JSch} framework.
     */
    protected JSch jsch;

    /**
     * Create a {@link JSchSessionFactory}.
     */
    public JSchSessionFactory()
    {
        this(new JSch());
    }

    /**
     * Create a {@link JSchSessionFactory} using the specified
     * {@link JSch} framework.
     *
     * @param jsch  the {@link JSch} framework to use
     */
    public JSchSessionFactory(JSch jsch)
    {
        this.jsch = jsch;
    }

    /**
     * Create a JSch {@link Session} connected to the specified remote host.
     *
     * @param userName        the user name to use to connect to the specified host
     * @param authentication  the {@link Authentication} method to use to authenticate the user
     * @param hostName        the host name of the remote host to connect to
     * @param port            the port on the remote host to connect to
     * @param socketFactory   the {@link JSchSocketFactory} to use
     * @param options         the {@link Options} to use to control the session
     *
     * @return a {@link Session} connected to the specified remote host
     *
     * @throws JSchException if an error occurs creating the {@link Session}
     */
    public Session createSession(String hostName, int port, String userName, Authentication authentication,
                                 JSchSocketFactory socketFactory, Options options) throws JSchException
    {
        // allow the authentication to configure the framework
        if (authentication instanceof JSchBasedAuthentication)
        {
            ((JSchBasedAuthentication) authentication).configureFramework(jsch);
        }

        // create the remote session
        Session session = jsch.getSession(userName, hostName, port);

        // establish the specialized socket factory for the session
        session.setSocketFactory(socketFactory);

        // the session should not cause the JVM not to exit
        session.setDaemonThread(true);

        // determine the timeout
        Timeout timeout   = options.getOrDefault(Timeout.class, Timeout.autoDetect());
        int     timeoutMS = (int) timeout.getDuration().to(TimeUnit.MILLISECONDS);

        // set the default session timeouts (in milliseconds)
        session.setTimeout(timeoutMS);

        // allow the authentication to configure the session
        if (authentication instanceof JSchBasedAuthentication)
        {
            ((JSchBasedAuthentication) authentication).configureSession(session);
        }

        // ----- configure the session channel properties -----
        Properties config = new Properties();

        // are we to use strict-host-checking? (when it's not defined it's enabled it by default)
        StrictHostChecking strictHostChecking = options.get(StrictHostChecking.class);

        config.put("StrictHostKeyChecking", strictHostChecking.isEnabled() ? "yes" : "no");
        session.setConfig(config);

        // connect the session
        session.connect();

        return session;
    }
}
