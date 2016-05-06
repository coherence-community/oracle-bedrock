/*
 * File: ListNets.java
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

package com.oracle.bedrock.runtime.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import static java.lang.System.out;

/**
 * Class description
 *
 * @version        Enter version here..., 14/11/10
 * @author         Enter your name here...
 */
public class ListNets
{
    public static void main(String args[]) throws SocketException
    {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(nets))
        {
            displayInterfaceInformation(netint);
        }
    }


    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException
    {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        out.printf("Index: %d\n", netint.getIndex());
        out.printf("Virtual: %s\n", netint.isVirtual());
        out.printf("Loopback: %s\n", netint.isLoopback());
        out.printf("Running: %s\n", netint.isUp());
        out.printf("Point-To-Point: %s\n", netint.isPointToPoint());
        out.printf("Parent: %s\n", netint.getParent());

        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();

        for (InetAddress inetAddress : Collections.list(inetAddresses))
        {
            out.printf("InetAddress: %s\n", inetAddress);
        }

        out.printf("\n");
    }
}
