/*
 * File: SimpleJavaApplicationSchema.java
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

import com.oracle.tools.Options;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.Platform;

import java.util.Properties;

/**
 * A {@link SimpleJavaApplicationSchema} is a {@link JavaApplicationSchema}
 * for simple {@link SimpleJavaApplication}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleJavaApplicationSchema
    extends AbstractJavaApplicationSchema<SimpleJavaApplication, SimpleJavaApplicationSchema>
{
    /**
     * Constructs a {@link SimpleJavaApplicationSchema} based on
     * a {@link JavaApplicationSchema}.
     *
     * @param schema  the {@link JavaApplicationSchema}
     */
    public SimpleJavaApplicationSchema(JavaApplicationSchema schema)
    {
        super(schema);
    }


    /**
     * Constructs a {@link SimpleJavaApplicationSchema} based on
     * a {@link SimpleJavaApplicationSchema}.
     *
     * @param schema  the {@link SimpleJavaApplicationSchema}
     */
    public SimpleJavaApplicationSchema(SimpleJavaApplicationSchema schema)
    {
        super(schema);
    }


    /**
     * Construct a {@link SimpleJavaApplicationSchema} with a given application
     * class name, defaulting to use the class path of the executing application.
     *
     * @param applicationClassName  the fully qualified class name of the Java application
     */
    public SimpleJavaApplicationSchema(String applicationClassName)
    {
        super(applicationClassName);
    }


    /**
     * Construct a {@link SimpleJavaApplicationSchema}.
     *
     * @param applicationClassName  the fully qualified class name of the Java application
     * @param classPath             the class path for the Java application
     */
    public SimpleJavaApplicationSchema(String applicationClassName,
                                       String classPath)
    {
        super(applicationClassName, classPath);
    }


    @Override
    public SimpleJavaApplication createJavaApplication(JavaApplicationProcess process,
                                                       String                 displayName,
                                                       Platform               platform,
                                                       Options                options,
                                                       ApplicationConsole     console,
                                                       Properties             environmentVariables,
                                                       Properties             systemProperties,
                                                       int                    remoteDebuggingPort)
    {
        SimpleJavaApplicationRuntime environment = new SimpleJavaApplicationRuntime(displayName,
                                                                                    platform,
                                                                                    options,
                                                                                    process,
                                                                                    console,
                                                                                    environmentVariables,
                                                                                    systemProperties,
                                                                                    remoteDebuggingPort);

        return new SimpleJavaApplication(environment, this.getApplicationListeners());
    }


    @Override
    public void configureDefaults()
    {
        // we don't establish any defaults
    }


    @Override
    public Class<SimpleJavaApplication> getApplicationClass()
    {
        return SimpleJavaApplication.class;
    }
}
