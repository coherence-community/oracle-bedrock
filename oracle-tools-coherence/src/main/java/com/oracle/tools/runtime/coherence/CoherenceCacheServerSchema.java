/*
 * File: CoherenceCacheServerSchema.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.java.JavaProcess;

import java.util.Properties;

/**
 * Defines a schema encapsulating configuration and operational settings
 * required for establishing an Oracle Coherence Cache Server, those of which are
 * represented at runtime by {@link CoherenceCacheServer}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CoherenceCacheServerSchema
    extends AbstractCoherenceClusterMemberSchema<CoherenceCacheServer, CoherenceCacheServerSchema>
{
    /**
     * Constructs a default {@link CoherenceCacheServerSchema}.
     */
    public CoherenceCacheServerSchema()
    {
        super(CoherenceCacheServer.class, DEFAULT_CACHE_SERVER_CLASSNAME);
    }


    /**
     * Constructs a {@link CoherenceCacheServerSchema} with a custom main application class name,
     * using the class path of the executing application.
     *
     * @param applicationClassName  the fully qualified class name of the main application
     */
    public CoherenceCacheServerSchema(String applicationClassName)
    {
        super(CoherenceCacheServer.class, applicationClassName);
    }


    /**
     * Constructs a {@link CoherenceCacheServerSchema} with a custom main application class name,
     * but using the specific class path.
     *
     * @param applicationClassName  the fully qualified class name of the main application
     * @param classPath             the class path for the Java application.
     */
    public CoherenceCacheServerSchema(String applicationClassName,
                                      String classPath)
    {
        super(CoherenceCacheServer.class, applicationClassName, classPath);
    }


    /**
     * Constructs a {@link CoherenceCacheServerSchema}.
     *
     * @param executableName        the executable name to run Java (typically just "java")
     * @param applicationClassName  the fully qualified class name of the main application
     * @param classPath             the class path for the Java application
     */
    public CoherenceCacheServerSchema(String executableName,
                                      String applicationClassName,
                                      String classPath)
    {
        super(CoherenceCacheServer.class, executableName, applicationClassName, classPath);
    }


    @Override
    public CoherenceCacheServer createJavaApplication(JavaProcess        process,
                                                      String             name,
                                                      ApplicationConsole console,
                                                      Properties         environmentVariables,
                                                      Properties         systemProperties)
    {
        return new CoherenceCacheServer(process,
                                        name,
                                        console,
                                        environmentVariables,
                                        systemProperties,
                                        isDiagnosticsEnabled(),
                                        getDefaultTimeout(),
                                        getDefaultTimeoutUnits(),
                                        getLifecycleInterceptors());
    }
}
