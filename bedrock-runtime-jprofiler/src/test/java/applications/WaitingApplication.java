/*
 * File: WaitingApplication.java
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

import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;

import java.io.IOException;

/**
 * A simple application that waits on a monitor lock until told not to.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WaitingApplication
{

    /**
     * The monitor that this application sychronizes on to wait.
     */
    public static final Object MONITOR = new Object();

    public static boolean sleep = true;

    /**
     * Entry Point of the Application
     *
     * @param arguments  programme arguments
     */
    public static void main(String[] arguments) throws IOException, InterruptedException
    {
        System.out.printf("%s started\n", WaitingApplication.class.getName());

        System.out.printf("Using java.home: %s\n", System.getProperty("java.home"));

        System.out.println("Now waiting...");

        if (sleep)
        {
            synchronized (MONITOR)
            {
                while (sleep)
                {
                    MONITOR.wait();
                }
            }
        }

        System.out.println("Finished sleeping... now terminating");
    }

    public static class Terminate implements RemoteRunnable
    {
        @Override
        public void run()
        {
            synchronized (WaitingApplication.MONITOR)
            {
                WaitingApplication.sleep = false;
                WaitingApplication.MONITOR.notifyAll();
            }

        }
    }
}
