/*
 * File: SystemPropertyMutatingApplication.java
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

import java.io.IOException;

/**
 * A simple application to set the value of a system property.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SystemPropertyMutatingApplication
{
    /**
     * A simple application to set the value of a specified system property to a particular value.
     *
     * @param args  arg[0] is the name of the property to set and arg[1] is the value to set.
     */
    public static void main(String[] args) throws IOException
    {
        String name  = args[0];
        String value = args[1];

        System.out.println("Existing: " + System.getProperty(name));

        System.setProperty(name, value);

        System.out.println("Now: " + System.getProperty(name));

        System.out.println("(now waiting for some input to terminate)");

        System.in.read();
    }
}
