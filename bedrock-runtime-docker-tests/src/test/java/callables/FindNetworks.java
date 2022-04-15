/*
 * File: FindNetworks.java
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

package callables;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link RemoteCallable} to find the network interfaces for a Java application.
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class FindNetworks implements RemoteCallable<Map<String,List<InetAddress>>>
{
    /**
     * A singleton instance of {@link FindNetworks}.
     */
    public static final FindNetworks INSTANCE = new FindNetworks();


    @Override
    public Map<String,List<InetAddress>> call() throws Exception
    {
        Map<String,List<InetAddress>> results = new HashMap<>();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements())
        {
            NetworkInterface  networkInterface = interfaces.nextElement();
            List<InetAddress> addressList      = new ArrayList<>();

            networkInterface.getInterfaceAddresses().forEach((ia) -> addressList.add(ia.getAddress()));

            results.put(networkInterface.getDisplayName(), addressList);
        }

        return results;
    }
}
