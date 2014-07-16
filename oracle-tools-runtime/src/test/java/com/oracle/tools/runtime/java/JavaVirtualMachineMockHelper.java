/*
 * File: JavaVirtualMachineMockHelper.java
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

import java.lang.reflect.Field;

import static org.mockito.Mockito.spy;

/**
 * A helper class that mocks out the {@link JavaVirtualMachine#INSTANCE}
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JavaVirtualMachineMockHelper
{
    private static JavaVirtualMachine savedJVM;

    /**
     * Mock out the {@link JavaVirtualMachine#INSTANCE} so we can
     * mock its methods
     */
    public static void mockJavaVirtualMachine() throws Exception
    {
        if (JavaVirtualMachine.getInstance().getClass().getCanonicalName().contains("$$"))
        {
            // We've already mocked the JavaVirtualMachine.INSTANCE
            return;
        }

        Class clazz         = JavaVirtualMachine.class;
        Field instanceField = clazz.getDeclaredField("INSTANCE");

        savedJVM = JavaVirtualMachine.getInstance();

        JavaVirtualMachine mock = spy(savedJVM);

        instanceField.setAccessible(true);
        instanceField.set(null, mock);
    }


    /**
     * Restore the {@link JavaVirtualMachine#INSTANCE}
     */
    public static void restoreJavaVirtualMachine() throws Exception
    {
        if (savedJVM == null)
        {
            return;
        }

        Class clazz         = JavaVirtualMachine.class;
        Field instanceField = clazz.getDeclaredField("INSTANCE");

        instanceField.setAccessible(true);
        instanceField.set(null, savedJVM);
    }
}
