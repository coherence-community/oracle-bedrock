/*
 * File: DoStuff.java
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

package com.oracle.tools.runtime.virtual.vagrant;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * A utility class used in the Vagrant functional tests.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DoStuff
{
    public static void main(String[] args)
    {
        System.out.println("Args: " + Arrays.toString(args));

        Properties      properties = System.getProperties();
        TreeSet<String> names      = new TreeSet<String>(properties.stringPropertyNames());

        System.out.println("Properties:");

        for (String name : names)
        {
            System.out.println(name + "=" + properties.getProperty(name));
        }

        System.out.println("Environment:");

        for (Map.Entry<String, String> entry : System.getenv().entrySet())
        {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        System.out.println("Networks:");

        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements())
            {
                NetworkInterface net = interfaces.nextElement();

                System.out.println(net.getName() + " " + Collections.list(net.getInetAddresses()));
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
    }
}
