/*
 * File: JavaApplicationRunnerTest.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.concurrent.RemoteChannel;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteChannelServer;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for {@link JavaApplicationRunner}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JavaApplicationRunnerTest
{
    @Test
    public void shouldInjectEventChannel() throws Exception
    {
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer())
        {
            server.open();

            InetAddress           parentAddress = InetAddress.getLoopbackAddress();
            String                parentURI     = "//" + parentAddress.getHostAddress() + ":" + server.getPort();
            JavaApplicationRunner runner        = new JavaApplicationRunner(parentURI, true);

            runner.run(TestApp.class.getName(), new String[0]);

            assertThat(TestApp.channel, is(notNullValue()));
        }
    }


    public static class TestApp
    {
        @RemoteChannel.Inject
        public static RemoteChannel channel;

        public static void main(String[] args) throws Exception
        {
        }
    }
}
