/*
 * File: EchoApp.java
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

package com.oracle.tools.runtime.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A simple application to use to run functional tests
 * on {@link com.oracle.tools.runtime.ApplicationConsole}
 * implementations.
 * </p>
 * The main method of the class reads from {@link System#in} and echos the
 * Strings read to {@link System#out}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author  Jonathan Knight
 */
public class EchoApp
{
    public static void main(String[] args) throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String         line   = reader.readLine();
        while (line != null)
        {
            if ("quit".equalsIgnoreCase(line.trim()))
            {
                break;
            }

            System.out.println("Echo: " + line);
            line   = reader.readLine();
        }
        System.out.flush();
    }
}
