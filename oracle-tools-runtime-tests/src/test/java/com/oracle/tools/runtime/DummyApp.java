/*
 * File: DummyApp.java
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

package com.oracle.tools.runtime;

import java.util.Properties;

/**
 * A dummy application class that is started by various tests
 * and writes things to System.out so that they can be asserted
 * by the tests.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 *
 * @author Jonathan Knight
 */
public class DummyApp
{
    /**
     * Field description
     */
    public static String[] args;

    /**
     * Field description
     */
    public static Properties properties;

    /**
     * Field description
     */
    public static boolean stopCalled = false;


    /**
     * Method description
     *
     * @param args
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        stopCalled    = false;
        DummyApp.args = args;

        for (String arg : args)
        {
            System.out.print(arg + ",");
        }

        System.out.println();

        DummyApp.properties = new Properties();

        for (String name : System.getProperties().stringPropertyNames())
        {
            if (name.startsWith("test.prop."))
            {
                System.out.print(name + "=" + System.getProperty(name) + ",");
                properties.setProperty(name, System.getProperty(name));
            }
        }

        System.out.println();
    }


    /**
     * Method description
     */
    public static void stop()
    {
        stopCalled = true;
        System.out.println("Stop called");
    }


    /**
     * Method description
     *
     * @return
     */
    public static String[] getArgs()
    {
        return args;
    }


    /**
     * Method description
     *
     * @return
     */
    public static Properties getProperties()
    {
        return properties;
    }


    /**
     * Method description
     *
     * @return
     */
    public static boolean wasStopCalled()
    {
        return stopCalled;
    }
}
