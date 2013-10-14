/*
 * File: JavaApplicationLauncher.java
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

import com.oracle.tools.runtime.Settings;

import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteExecutorListener;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorClient;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Used to launch {@link JavaApplication}s that connect back to their "parent"
 * application so that they may be remotely controlled, request further information
 * or may detect when they become orphaned (their parent is killed, destroyed or
 * otherwise terminates).
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JavaApplicationLauncher
{
    /**
     * {@link JavaApplicationLauncher} entry point.
     * <p>
     * Usage:  java JavaApplicationLauncher ApplicationClassName [args]
     * <p>
     * Where:  ApplicationClassName is the fully-qualified-class-name of the
     *                              application to run (this class must
     *                              have a standard main method declaration).
     *
     *         args is a space separated list of arguments to pass to the
     *              application main method.
     *
     * @param arguments  the arguments for the {@link JavaApplicationLauncher}
     */
    public static void main(String[] arguments)
    {
        if (arguments.length == 0)
        {
            // TODO: output we need at least one argument
            System.exit(-1);
        }
        else
        {
            final String  address      = System.getProperty(Settings.PARENT_ADDRESS, null);
            final Integer port         = Integer.getInteger(Settings.PARENT_PORT, null);
            final boolean isOrphanable = Boolean.getBoolean(Settings.ORPHANABLE);

            if (address == null)
            {
                // TODO: address can't be null
                System.exit(-1);
            }
            else if (port == null)
            {
                // TODO: port must be real
                System.exit(-1);
            }
            else if (arguments.length >= 1)
            {
                String applicationClassName = arguments[0];

                try
                {
                    InetAddress          inetAddress    = InetAddress.getByName(address);
                    RemoteExecutorClient remoteExecutor = new RemoteExecutorClient(inetAddress, port);

                    remoteExecutor.addListener(new RemoteExecutorListener()
                    {
                        @Override
                        public void onOpened(RemoteExecutor executor)
                        {
                            // TODO: connected to the parent!
                        }

                        @Override
                        public void onClosed(RemoteExecutor executor)
                        {
                            // TODO: disconnected from the parent!
                            if (!isOrphanable)
                            {
                                System.exit(-2);
                            }
                        }
                    });

                    remoteExecutor.open();

                    // attempt to load the application class
                    Class<?> applicationClass = Class.forName(applicationClassName);

                    // create the real arguments for the application
                    String[] realArguments = new String[arguments.length - 1];

                    for (int i = 1; i < arguments.length; i++)
                    {
                        realArguments[i - 1] = arguments[i];
                    }

                    // now launch the application
                    Method mainMethod = applicationClass.getMethod("main", String[].class);

                    mainMethod.invoke(null, new Object[] {realArguments});

                }
                catch (ClassNotFoundException e)
                {
                    System.out.println("JavaApplicationLauncher: Could not load the class " + applicationClassName);
                    System.out.println(e);
                }
                catch (NoSuchMethodException e)
                {
                    System.out.println("JavaApplicationLauncher: Could not locate a main method for "
                                       + applicationClassName);
                    System.out.println(e);
                }
                catch (IllegalAccessException e)
                {
                    System.out.println("JavaApplicationLauncher: Could not access the main method for "
                                       + applicationClassName);
                    System.out.println(e);
                }
                catch (InvocationTargetException e)
                {
                    System.out.println("JavaApplicationLauncher: Failed to invoke the main method for "
                                       + applicationClassName);
                    System.out.println(e);
                }
                catch (UnknownHostException e)
                {
                    System.out.println("JavaApplicationLauncher: The specified parent address is unknown");
                    System.out.println(e);
                }
                catch (IOException e)
                {
                    System.out.println("JavaApplicationLauncher: Failed to open a connection to parent");
                    System.out.println(e);
                }
            }
        }
    }
}
