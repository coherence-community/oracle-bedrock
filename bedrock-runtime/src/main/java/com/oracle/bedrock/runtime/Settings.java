/*
 * File: Settings.java
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

import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.java.container.Container;

import java.util.Properties;

/**
 * Provides a means to determine runtime settings, typically overridden by
 * the environment or system properties.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public class Settings
{
    /**
     * The Java System (boolean) Property to override the ability to output diagnostics.
     */
    public static final String IS_DIAGNOSTICS_ENABLED = "bedrock.runtime.diagnostics";

    /**
     * The Java System (boolean) Property determine if a runtime process can be left
     * running (ie: orphaned) when the process that started it terminates (for any reason).
     */
    public static final String ORPHANABLE = "bedrock.runtime.orphanable";

    /**
     * The Java System (String) Property that specifies the URI of the host and
     * port of the process that started a process (ie: the parent address).
     */
    public static final String PARENT_URI = "bedrock.runtime.parent";

    /**
     * The Java System (String) Property that specifies the name of the serializer class to use.
     */
    public static final String CHANNEL_SERIALIZER = "bedrock.runtime.channel.serializer";


    /**
     * Determines if diagnostics are enabled.
     *
     * @param isDiagnosticsEnabled  this value is returned when the {@link #IS_DIAGNOSTICS_ENABLED}
     *                              hasn't been defined.
     *
     * @return <code>true</code> if diagnostics should be output
     */
    public static boolean isDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        Properties properties = Container.getPlatformScope().getProperties();

        Object     oValue     = properties.get(IS_DIAGNOSTICS_ENABLED);

        if (oValue instanceof String)
        {
            String value = (String) oValue;

            value = value.trim().toLowerCase();

            return value.equals("true") || value.equals("on");
        }
        else
        {
            return isDiagnosticsEnabled;
        }

    }
}
