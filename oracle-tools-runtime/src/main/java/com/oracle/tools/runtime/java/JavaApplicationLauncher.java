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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import java.util.concurrent.atomic.AtomicBoolean;

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
            System.out
                .println("JavaApplicationLauncher: No application (fqcn) was specified to start. An application must be specified as an argument.");
            Runtime.getRuntime().halt(1);
        }
        else
        {
            final String  parent       = System.getProperty(Settings.PARENT_URI, null);
            final boolean isOrphanable = Boolean.getBoolean(Settings.ORPHANABLE);

            // a flag indicating if this application is in the process of
            // shutting down naturally
            // (System.exit(...) or main has finished and shutdown hooks have started)
            final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

            // add a shutdown hook so we can tell when the application is
            // terminating due to natural causes
            // (like it's finished or a System.exit(...) was called)
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    isShuttingDown.set(true);
                }
            });

            if (parent == null)
            {
                System.out
                    .println("JavaApplicationLauncher: No parent URI was specified.  The parent URI must be specified to start the application.");

                Runtime.getRuntime().halt(1);
            }
            else if (arguments.length >= 1)
            {
                String               applicationClassName = arguments[0];

                RemoteExecutorClient remoteExecutor       = null;

                // attempt to connect to the parent application
                try
                {
                    URI parentURI = new URI(parent);

                    // find the InetAddress of the host on which the parent is running
                    InetAddress inetAddress = InetAddress.getByName(parentURI.getHost());

                    // establish a RemoteExecutorClient to handle and send requests to the parent
                    remoteExecutor = new RemoteExecutorClient(inetAddress, parentURI.getPort());

                    remoteExecutor.addListener(new RemoteExecutorListener()
                    {
                        @Override
                        public void onOpened(RemoteExecutor executor)
                        {
                            // connected to the parent!
                        }

                        @Override
                        public void onClosed(RemoteExecutor executor)
                        {
                            // disconnected from the parent so terminate
                            // (if we're not orphanable)
                            if (!isOrphanable)
                            {
                                Runtime.getRuntime().halt(2);
                            }
                        }
                    });

                    // connect to the parent
                    remoteExecutor.open();
                }
                catch (URISyntaxException e)
                {
                    System.out.println("JavaApplicationLauncher: The specified parent URI [" + parent + "] is invalid");
                    e.printStackTrace(System.out);
                }
                catch (UnknownHostException e)
                {
                    System.out.println("JavaApplicationLauncher: The specified parent address is unknown");
                    e.printStackTrace(System.out);
                }
                catch (IOException e)
                {
                    System.out.println("JavaApplicationLauncher: Failed to open a connection to parent");
                    e.printStackTrace(System.out);

                    if (remoteExecutor != null)
                    {
                        remoteExecutor.close();
                        remoteExecutor = null;
                    }
                }

                if (remoteExecutor != null)
                {
                    // start the application
                    try
                    {
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
                        e.printStackTrace(System.out);
                    }
                    catch (NoSuchMethodException e)
                    {
                        System.out.println("JavaApplicationLauncher: Could not locate a main method for "
                                           + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                    catch (IllegalAccessException e)
                    {
                        System.out.println("JavaApplicationLauncher: Could not access the main method for "
                                           + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                    catch (InvocationTargetException e)
                    {
                        System.out.println("JavaApplicationLauncher: Failed to invoke the main method for "
                                           + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                }
            }
        }
    }
}
