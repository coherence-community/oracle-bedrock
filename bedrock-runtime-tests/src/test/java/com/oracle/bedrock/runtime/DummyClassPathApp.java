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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.container.ContainerClassLoader;

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
    /**
     * The Main Method.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            ClassLoader classLoader = DummyClassPathApp.class.getClassLoader();

            ClassPath classPath;

            if (classLoader instanceof ContainerClassLoader)
            {
                classPath = ((ContainerClassLoader) classLoader).getClassPath();
            }
            else
            {
                classPath = ClassPath.ofSystem();
            }

            for (String path : classPath)
            {
                System.out.println(path);
            }

            Class.forName(args[0]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
