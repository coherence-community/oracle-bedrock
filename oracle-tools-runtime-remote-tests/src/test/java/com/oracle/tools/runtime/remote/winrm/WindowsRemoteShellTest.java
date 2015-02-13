/*
 * File: WindowsRemoteShellTest.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.options.EnvironmentVariables;

import com.oracle.tools.runtime.remote.Authentication;
import com.oracle.tools.runtime.remote.Password;
import com.oracle.tools.runtime.remote.RemoteApplicationEnvironment;
import com.oracle.tools.runtime.remote.RemoteApplicationProcess;
import com.oracle.tools.runtime.remote.SimpleRemoteApplicationEnvironment;
import com.oracle.tools.runtime.remote.http.HttpBasedAuthentication;

import org.junit.Test;

import org.mockito.InOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.Properties;

/**
 * Tests for {@link WindowsRemoteShell}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsRemoteShellTest
{
    /**
     *
     */
    @Test
    public void shouldCreateWindowsSession() throws Exception
    {
        String                  userName       = "Bob";
        HttpBasedAuthentication authentication = new Password("secret");
        String                  hostName       = "foo.com";
        int                     port           = 1234;

        WindowsRemoteShell      shell          = new WindowsRemoteShell(userName, authentication, hostName, port);

        WindowsSession          session        = shell.createSession();

        assertThat(session, is(notNullValue()));

        SoapConnection soapConnection = session.getSoapConnection();

        assertThat(soapConnection.getUrl().getHost(), is("foo.com"));
        assertThat(soapConnection.getUrl().getPort(), is(1234));
        assertThat(soapConnection.getUserName(), is("Bob"));
        assertThat(soapConnection.getAuthentication(), is(sameInstance(authentication)));
    }


    /**
     *
     */
    @Test
    public void shouldMakeDirectories() throws Exception
    {
        String             userName       = "Bob";
        Authentication     authentication = new Password("secret");
        String             hostName       = "foo.com";
        int                port           = 1234;
        Options            options        = new Options();
        WindowsSession     session        = mock(WindowsSession.class);

        WindowsRemoteShell shell = new WindowsRemoteShellStub(userName, authentication, hostName, port, session);

        shell.makeDirectories("dir1\\dir2", options);

        InOrder inOrder = inOrder(session);

        inOrder.verify(session).connect();
        inOrder.verify(session).execute(eq("mkdir"),
                                        eq(Arrays.asList("dir1\\dir2")),
                                        any(InputStream.class),
                                        any(OutputStream.class),
                                        any(OutputStream.class));
    }


    /**
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void shouldRealizeRemoteApplicationProcess() throws Exception
    {
        String                       userName       = "Bob";
        Authentication               authentication = new Password("secret");
        String                       hostName       = "foo.com";
        int                          port           = 1234;
        WindowsSession               session        = mock(WindowsSession.class);

        Platform                     platform       = mock(Platform.class);

        EnvironmentVariables         envVars = EnvironmentVariables.custom().set("Key1", "Val1").set("Key2", "Val2");
        Options                      options        = new Options(envVars);

        SimpleApplicationSchema schema = new SimpleApplicationSchema("App").addArgument("Arg1").addArgument("Arg2");

        RemoteApplicationEnvironment environment    = new SimpleRemoteApplicationEnvironment(schema, platform, options);

        WindowsRemoteShell shell = new WindowsRemoteShellStub(userName, authentication, hostName, port, session);

        Properties                   envVariables   = environment.getRemoteEnvironmentVariables();

        RemoteApplicationProcess     process = shell.realize(schema, "App-1", platform, environment, "Dir-1", options);

        assertThat(process, is(notNullValue()));

        InOrder inOrder = inOrder(session);

        inOrder.verify(session).connect("Dir-1", envVariables);
        inOrder.verify(session).execute(eq("App"),
                                        eq(Arrays.asList("Arg1", "Arg2")),
                                        any(InputStream.class),
                                        any(OutputStream.class),
                                        any(OutputStream.class));
    }


    /**
     * A stub class {@link WindowsRemoteShell} to use for testing that
     * can be provided with a {@link WindowsSession} to use.
     */
    public static class WindowsRemoteShellStub extends WindowsRemoteShell
    {
        private WindowsSession session;


        /**
         */
        public WindowsRemoteShellStub(String         userName,
                                      Authentication authentication,
                                      String         hostName,
                                      int            port,
                                      WindowsSession session)
        {
            super(userName, authentication, hostName, port);

            this.session = session;
        }


        @Override
        protected WindowsSession createSession()
        {
            return session;
        }
    }
}
