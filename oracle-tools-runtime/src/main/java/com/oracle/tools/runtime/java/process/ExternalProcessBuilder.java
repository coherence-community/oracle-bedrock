/*
 * File: ExternalProcessBuilder.java
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

package com.oracle.tools.runtime.java.process;

import java.io.IOException;
import java.util.Properties;

/**
 * An {@link ExternalProcessBuilder} is a {@link JavaProcessBuilder} that
 * creates separate non-child Java {@link Process}es from the currently
 * executing process.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class ExternalProcessBuilder extends AbstractJavaProcessBuilder
{
    /**
     * Constructs an {@link ExternalProcessBuilder}.
     *
     * @param executableName        the name of the jvm executable (typically "java")
     * @param applicationClassName  the name of the application class
     */
    public ExternalProcessBuilder(String executableName,
                                  String applicationClassName)
    {
        super(executableName, applicationClassName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Process realize() throws IOException
    {
        Properties systemProperties = getSystemProperties();

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            String propertyValue = systemProperties.getProperty(propertyName);

            getCommands().add("-D" + propertyName + (propertyValue.isEmpty() ? "" : "=" + propertyValue));
        }

        // add the applicationClassName to the command for the process
        getCommands().add(getApplicationClassName());

        // add the arguments to the command for the process
        for (String argument : getArguments())
        {
            getCommands().add(argument);
        }

        // start the process
        return super.realize();
    }
}
