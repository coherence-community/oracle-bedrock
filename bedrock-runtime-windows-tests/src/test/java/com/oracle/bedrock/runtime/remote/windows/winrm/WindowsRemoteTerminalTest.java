/*
 * File: WindowsRemoteTerminalTest.java
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

package com.oracle.bedrock.runtime.remote.windows.winrm;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.remote.Authentication;
import com.oracle.bedrock.runtime.remote.Password;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.http.HttpBasedAuthentication;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WindowsRemoteTerminal}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsRemoteTerminalTest
{
    @Test
    public void shouldCreateWindowsSession() throws Exception
    {
        String                  userName       = "Bob";
        HttpBasedAuthentication authentication = new Password("secret");
        String                  hostName       = "foo.com";
        int                     port           = 1234;
        RemotePlatform          platform       = mock(RemotePlatform.class);

        when(platform.getUserName()).thenReturn(userName);
        when(platform.getAuthentication()).thenReturn(authentication);
        when(platform.getAddress()).thenReturn(InetAddress.getByName(hostName));
        when(platform.getPort()).thenReturn(port);

        WindowsRemoteTerminal shell   = new WindowsRemoteTerminal(platform);

        WindowsSession        session = shell.createSession();

        assertThat(session, is(notNullValue()));

        SoapConnection soapConnection = session.getSoapConnection();

        assertThat(soapConnection.getUrl().getHost(), is("foo.com"));
        assertThat(soapConnection.getUrl().getPort(), is(1234));
        assertThat(soapConnection.getUserName(), is("Bob"));
        assertThat(soapConnection.getAuthentication(), is(sameInstance(authentication)));
    }


    @Test
    public void shouldMakeDirectories() throws Exception
    {
        String         userName       = "Bob";
        Authentication authentication = new Password("secret");
        String         hostName       = "foo.com";
        int            port           = 1234;
        OptionsByType  optionsByType  = OptionsByType.empty();
        WindowsSession session        = mock(WindowsSession.class);

        RemotePlatform platform       = mock(RemotePlatform.class);

        when(platform.getUserName()).thenReturn(userName);
        when(platform.getAuthentication()).thenReturn(authentication);
        when(platform.getAddress()).thenReturn(InetAddress.getByName(hostName));
        when(platform.getPort()).thenReturn(port);

        WindowsRemoteTerminal shell = new WindowsRemoteTerminalStub(platform, session);

        shell.makeDirectories("dir1\\dir2", optionsByType);

        InOrder inOrder = inOrder(session);

        inOrder.verify(session).connect();
        inOrder.verify(session).execute(eq("mkdir"),
                                        eq(Arrays.asList("dir1\\dir2")),
                                        any(InputStream.class),
                                        any(OutputStream.class),
                                        any(OutputStream.class));
    }


    /**
     * A stub class {@link WindowsRemoteTerminal} to use for testing that
     * can be provided with a {@link WindowsSession} to use.
     */
    public static class WindowsRemoteTerminalStub extends WindowsRemoteTerminal
    {
        private WindowsSession session;


        /**
         */
        public WindowsRemoteTerminalStub(RemotePlatform platform,
                                         WindowsSession session)
        {
            super(platform);

            this.session = session;
        }


        @Override
        protected WindowsSession createSession()
        {
            return session;
        }
    }
}
