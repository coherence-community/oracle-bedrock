/*
 * File: SocketEchoServer.java
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

package applications;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.java.JavaApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple application that listens on a socket and echoes back anything received.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SocketEchoServer
{
    /**
     * The System property to set to the port number that the server should listen on.
     */
    public static final String PROPERTY_SERVER_PORT = "server.listen.port";

    /**
     * The {@link IsListening} singleton instance to use to check the application
     * is listening.
     */
    public static final IsListening IS_LISTENING = new IsListening();

    /**
     * An {@link AtomicBoolean} set to <code>true</code> when the application is listening.
     */
    private static final AtomicBoolean listening = new AtomicBoolean(false);


    /**
     * The application main method that opens a socket and echos back whatever it receives.
     *
     * @param args  the main arguments
     *
     * @throws IOException  if an exception occurs opening the sockets
     */
    public static void main(String[] args) throws IOException
    {
        String serverPortProperty = System.getProperty(PROPERTY_SERVER_PORT);

        if (serverPortProperty == null || serverPortProperty.trim().isEmpty())
        {
            System.err.println("System property " + PROPERTY_SERVER_PORT + " not set");
            System.exit(1);
        }

        System.out.println("SocketEchoServer: port=" + serverPortProperty);

        int portNumber = Integer.parseInt(serverPortProperty);

        System.out.println("SocketEchoServer: opening socket");


        try (ServerSocket   serverSocket = new ServerSocket(Integer.parseInt(serverPortProperty)))
        {
            System.out.println("SocketEchoServer: listening...");
            listening.set(true);

            try (Socket         clientSocket = serverSocket.accept();
                 PrintWriter    out          = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in           = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())))
            {
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                {
                    System.out.println("Received: " + inputLine);
                    out.println(inputLine);
                    System.out.println("Echoed: " + inputLine);
                }

                System.out.println("SocketEchoServer: exiting");
            }
            catch (IOException e)
            {
                System.err.println("Exception caught when trying to listen on port "
                                   + portNumber + " or listening for a connection");

                System.err.println(e.getMessage());

                System.exit(1);
            }
        }
    }


    /**
     * A {@link RemoteCallable} to use to check that a
     * {@link SocketEchoServer} is listening.
     */
    public static class IsListening implements RemoteCallable<Boolean>
    {
        @Override
        public Boolean call() throws Exception
        {
            System.out.println("Check listening " + SocketEchoServer.listening.get());
            return SocketEchoServer.listening.get();
        }
    }
}
