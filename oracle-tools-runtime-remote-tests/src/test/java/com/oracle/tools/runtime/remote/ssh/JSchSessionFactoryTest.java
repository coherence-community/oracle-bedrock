/*
 * File: JSchSessionFactoryTest.java
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

package com.oracle.tools.runtime.remote.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import com.oracle.tools.Options;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.runtime.remote.AbstractRemoteTest;
import com.oracle.tools.runtime.remote.Authentication;
import com.oracle.tools.runtime.remote.SecureKeys;
import com.oracle.tools.runtime.remote.options.StrictHostChecking;

import org.junit.Assume;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import java.io.File;

import java.util.Properties;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Functional tests for {@link JSchSessionFactory}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JSchSessionFactoryTest extends AbstractRemoteTest
{

    @Test
    public void shouldObtainSession() throws Exception
    {
        Assume.assumeThat("Test ignored as private key file does not exist", privateKeyFileExists(), is(true));

        JSchSocketFactory  socketFactory = new JSchSocketFactory();
        Options            options       = new Options(StrictHostChecking.disabled());
        JSchSessionFactory factory       = new JSchSessionFactory();
        Session            session       = null;

        try
        {
            session = factory.createSession(getRemoteHostName(), 22, getRemoteUserName(),
                                            getRemoteAuthentication(), socketFactory, options);

            assertThat(session.isConnected(), is(true));
        }
        finally
        {
            if (session != null)
            {
                session.disconnect();
            }
        }
    }

    @Test
    public void shouldConfigureAuthentication() throws Exception
    {
        JSchSocketFactory       socketFactory = new JSchSocketFactory();
        String                  hostName      = "test.oracle.com";
        int                     port          = 1234;
        String                  userName      = "Larry";
        JSch                    jSch          = mock(JSch.class);
        Options                 options       = new Options();
        JSchBasedAuthentication auth          = mock(JSchBasedAuthentication.class);
        Session                 session       = mock(Session.class);

        when(jSch.getSession(userName, hostName, port)).thenReturn(session);

        JSchSessionFactory factory = new JSchSessionFactory(jSch);

        Session            result  = factory.createSession(hostName, port, userName, auth, socketFactory, options);

        assertThat(result, is(sameInstance(session)));

        verify(auth).configureFramework(jSch);
        verify(auth).configureSession(session);
    }

    @Test
    public void shouldConfigureSession() throws Exception
    {
        JSchSocketFactory       socketFactory = new JSchSocketFactory();
        String                  hostName      = "test.oracle.com";
        int                     port          = 1234;
        String                  userName      = "Larry";
        JSch                    jSch          = mock(JSch.class);
        Options                 options       = new Options();
        JSchBasedAuthentication auth          = mock(JSchBasedAuthentication.class);
        Session                 session       = mock(Session.class);

        when(jSch.getSession(userName, hostName, port)).thenReturn(session);

        JSchSessionFactory factory = new JSchSessionFactory(jSch);

        Session            result  = factory.createSession(hostName, port, userName, auth, socketFactory, options);

        assertThat(result, is(sameInstance(session)));

        verify(session).setSocketFactory(same(socketFactory));
        verify(session).setDaemonThread(true);
        verify(session).setTimeout((int) Timeout.autoDetect().to(TimeUnit.MILLISECONDS));
        verify(session).connect();
    }

    @Test
    public void shouldEnableStrictHostCheckingByDefault() throws Exception
    {
        JSchSocketFactory       socketFactory = new JSchSocketFactory();
        String                  hostName      = "test.oracle.com";
        int                     port          = 1234;
        String                  userName      = "Larry";
        JSch                    jSch          = mock(JSch.class);
        Options                 options       = new Options();
        JSchBasedAuthentication auth          = mock(JSchBasedAuthentication.class);
        Session                 session       = mock(Session.class);

        when(jSch.getSession(userName, hostName, port)).thenReturn(session);

        JSchSessionFactory factory = new JSchSessionFactory(jSch);

        Session            result  = factory.createSession(hostName, port, userName, auth, socketFactory, options);

        assertThat(result, is(sameInstance(session)));

        ArgumentCaptor<Properties> configCaptor = ArgumentCaptor.forClass(Properties.class);
        verify(session).setConfig(configCaptor.capture());

        Properties configuration = configCaptor.getValue();
        assertThat(configuration.getProperty("StrictHostKeyChecking"), is("yes"));
    }

    @Test
    public void shouldEnableStrictHostChecking() throws Exception
    {
        JSchSocketFactory       socketFactory = new JSchSocketFactory();
        String                  hostName      = "test.oracle.com";
        int                     port          = 1234;
        String                  userName      = "Larry";
        JSch                    jSch          = mock(JSch.class);
        Options                 options       = new Options(StrictHostChecking.enabled());
        JSchBasedAuthentication auth          = mock(JSchBasedAuthentication.class);
        Session                 session       = mock(Session.class);

        when(jSch.getSession(userName, hostName, port)).thenReturn(session);

        JSchSessionFactory factory = new JSchSessionFactory(jSch);

        Session            result  = factory.createSession(hostName, port, userName, auth, socketFactory, options);

        assertThat(result, is(sameInstance(session)));

        ArgumentCaptor<Properties> configCaptor = ArgumentCaptor.forClass(Properties.class);
        verify(session).setConfig(configCaptor.capture());

        Properties configuration = configCaptor.getValue();
        assertThat(configuration.getProperty("StrictHostKeyChecking"), is("yes"));
    }

    @Test
    public void shouldDisableStrictHostChecking() throws Exception
    {
        JSchSocketFactory       socketFactory = new JSchSocketFactory();
        String                  hostName      = "test.oracle.com";
        int                     port          = 1234;
        String                  userName      = "Larry";
        JSch                    jSch          = mock(JSch.class);
        Options                 options       = new Options(StrictHostChecking.disabled());
        JSchBasedAuthentication auth          = mock(JSchBasedAuthentication.class);
        Session                 session       = mock(Session.class);

        when(jSch.getSession(userName, hostName, port)).thenReturn(session);

        JSchSessionFactory factory = new JSchSessionFactory(jSch);

        Session            result  = factory.createSession(hostName, port, userName, auth, socketFactory, options);

        assertThat(result, is(sameInstance(session)));

        ArgumentCaptor<Properties> configCaptor = ArgumentCaptor.forClass(Properties.class);
        verify(session).setConfig(configCaptor.capture());

        Properties configuration = configCaptor.getValue();
        assertThat(configuration.getProperty("StrictHostKeyChecking"), is("no"));
    }
}
