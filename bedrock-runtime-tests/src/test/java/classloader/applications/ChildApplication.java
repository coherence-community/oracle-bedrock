/*
 * File: ChildApplication.java
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

package classloader.applications;

import com.oracle.bedrock.runtime.Settings;
import com.oracle.bedrock.runtime.concurrent.RemoteChannelSerializer;
import com.oracle.bedrock.runtime.concurrent.socket.SocketBasedRemoteChannelClient;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.concurrent.TimeUnit;

/**
 * An application that is started by a {@link ParentApplication}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ChildApplication
{
    /**
     * Entry Point of the Application
     *
     * @param arguments
     */
    public static void main(String[] arguments) throws UnknownHostException, Exception
    {
        System.out.printf("%s started\n", ChildApplication.class.getName());

        String serializerName = System.getProperty(Settings.CHANNEL_SERIALIZER);

        System.out.printf("server.address: %s\n", System.getProperty("server.address"));
        System.out.printf("server.port   : %s\n", System.getProperty("server.port"));
        System.out.printf("serializer    : %s\n", serializerName);

        RemoteChannelSerializer serializer = null;
        if (serializerName != null)
        {
            Class<?> clz = Class.forName(serializerName);
            serializer = (RemoteChannelSerializer) clz.getDeclaredConstructor().newInstance();
        }

        System.out.printf("Connecting to the specified server");

        SocketBasedRemoteChannelClient channel =
            new SocketBasedRemoteChannelClient(InetAddress.getByName(System.getProperty("server.address")),
                                    Integer.getInteger("server.port"), serializer);

        channel.open();

        System.out.printf("Connected... now sleeping");

        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

        System.out.printf("Finished sleeping... now disconnecting");

        channel.close();

        System.out.printf("Disconnecting from the specified server");
    }
}
