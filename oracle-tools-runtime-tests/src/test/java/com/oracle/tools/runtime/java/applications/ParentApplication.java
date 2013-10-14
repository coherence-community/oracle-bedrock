/*
 * File: ParentApplication.java
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

package com.oracle.tools.runtime.java.applications;

import com.oracle.tools.runtime.ApplicationConsole;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.NativeJavaApplicationBuilder;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;

import java.io.IOException;

/**
 * An application that starts a {@link com.oracle.tools.runtime.java.applications.ChildApplication}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ParentApplication
{
    /**
     * Entry Point of the Application
     *
     * @param arguments
     */
    public static void main(String[] arguments) throws IOException, InterruptedException
    {
        System.out.printf("%s started\n", ParentApplication.class.getName());

        System.out.printf("server.address  : %s\n", System.getProperty("server.address"));
        System.out.printf("server.port     : %s\n", System.getProperty("server.port"));
        System.out.printf("orphan.children : %s\n", System.getProperty("orphan.children"));

        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(ChildApplication.class.getName());

        schema.setSystemProperty("server.address", System.getProperty("server.address"));
        schema.setSystemProperty("server.port", System.getProperty("server.port"));

        NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
            new NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

        builder.setOrphansPermitted(Boolean.getBoolean("orphan.children"));

        ApplicationConsole    console     = new SystemApplicationConsole();
        SimpleJavaApplication application = builder.realize(schema, "client", console);

        application.waitFor();
    }
}
