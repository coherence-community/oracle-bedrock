/*
 * File: DummyClassPathApp.java
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

import com.oracle.tools.runtime.java.virtualization.VirtualizationClassLoader;

import java.io.File;
import java.net.URL;

/**
 * A dummy application class that is started by various tests
 * and writes things to System.out so that they can be asserted
 * by the tests.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DummyClassPathApp
{
    private static URL[] classPath;


    /**
     * Method description
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            ClassLoader classLoader = DummyClassPathApp.class.getClassLoader();

            if (classLoader instanceof VirtualizationClassLoader)
            {
                classPath = ((VirtualizationClassLoader) classLoader).getClassPath();

                for (URL url : classPath)
                {
                    System.out.println(url.toExternalForm());
                }
            }
            else
            {
                String cp = System.getProperty("java.class.path");

                for (String element : cp.split(File.pathSeparator))
                {
                    System.out.println(element);
                }
            }

            Class.forName(args[0]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Method description
     *
     * @return
     */
    public static URL[] getClassPath()
    {
        return classPath;
    }
}
