/*
 * File: JavaApplicationRunner.java
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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.Settings;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteChannelListener;
import com.oracle.bedrock.runtime.concurrent.socket.SocketBasedRemoteChannelClient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to run {@link JavaApplication}s that connect back to their "parent"
 * application so that they may be remotely controlled, request further information
 * or may detect when they become orphaned (their parent is killed, destroyed or
 * otherwise terminates).
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public class JavaApplicationRunner
{
    /**
     * The {@link RemoteChannel} for the launched {@link JavaApplication}.
     */
    public static SocketBasedRemoteChannelClient channel = null;


    /**
     * {@link JavaApplicationRunner} entry point.
     * <p>
     * Usage:  java JavaApplicationRunner ApplicationClassName [args]
     * <p>
     * Where:  ApplicationClassName is the fully-qualified-class-name of the
     *                              application to run (this class must
     *                              have a standard main method declaration).
     *
     *         args is a space separated list of arguments to pass to the
     *              application main method.
     *
     * @param arguments  the arguments for the {@link JavaApplicationRunner}
     */
    public static void main(String[] arguments)
    {
        if (arguments.length == 0)
        {
            System.out.println("JavaApplicationRunner: No application (fqcn) was specified to start. An application must be specified as an argument.");
            Runtime.getRuntime().halt(1);
        }
        else
        {
            final String  parent       = System.getProperty(Settings.PARENT_URI, null);
            final boolean isOrphanable = Boolean.getBoolean(Settings.ORPHANABLE);

            if (parent == null)
            {
                System.out.println("JavaApplicationRunner: No parent URI was specified.  The parent URI must be specified to start the application.");

                Runtime.getRuntime().halt(1);
            }

            if (arguments.length >= 1)
            {
                String applicationClassName = arguments[0];

                // create the arguments for the application
                String[] applicationArguments = new String[arguments.length - 1];

                System.arraycopy(arguments, 1, applicationArguments, 0, arguments.length - 1);

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

                // attempt to connect to the parent application
                try
                {
                    URI parentURI = new URI(parent);

                    // find the InetAddress of the host on which the parent is running
                    InetAddress inetAddress = InetAddress.getByName(parentURI.getHost());

                    // establish a RemoteExecutorClient to handle and send requests to the parent
                    channel = new SocketBasedRemoteChannelClient(inetAddress, parentURI.getPort());

                    channel.addListener(new RemoteChannelListener()
                                        {
                                            @Override
                                            public void onOpened(RemoteChannel channel)
                                            {
                                                // connected to the parent!
                                            }

                                            @Override
                                            public void onClosed(RemoteChannel channel)
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
                    channel.open();
                }
                catch (URISyntaxException e)
                {
                    System.out.println("JavaApplicationRunner: The specified parent URI [" + parent + "] is invalid");
                    e.printStackTrace(System.out);
                }
                catch (UnknownHostException e)
                {
                    System.out.println("JavaApplicationRunner: The specified parent address is unknown");
                    e.printStackTrace(System.out);
                }
                catch (IOException e)
                {
                    System.out.println("JavaApplicationRunner: Failed to open a connection to parent");
                    e.printStackTrace(System.out);

                    if (channel != null)
                    {
                        channel.close();
                        channel = null;
                    }
                }

                if (channel != null)
                {
                    // start the application
                    try
                    {
                        // attempt to load the application class
                        Class<?> applicationClass = Class.forName(applicationClassName);

                        // now launch the application
                        Method mainMethod = applicationClass.getMethod("main", String[].class);

                        mainMethod.invoke(null, new Object[] {applicationArguments});
                    }
                    catch (ClassNotFoundException e)
                    {
                        System.out.println("JavaApplicationRunner: Could not load the class " + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                    catch (NoSuchMethodException e)
                    {
                        System.out.println("JavaApplicationRunner: Could not locate a main method for "
                                           + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                    catch (IllegalAccessException e)
                    {
                        System.out.println("JavaApplicationRunner: Could not access the main method for "
                                           + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                    catch (InvocationTargetException e)
                    {
                        System.out.println("JavaApplicationRunner: Failed to invoke the main method for "
                                           + applicationClassName);
                        e.printStackTrace(System.out);
                    }
                }
            }
        }
    }
}
