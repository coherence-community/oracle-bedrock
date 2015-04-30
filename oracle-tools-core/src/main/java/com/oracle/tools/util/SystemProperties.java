/*
 * File: SystemProperties.java
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

package com.oracle.tools.util;

import java.util.Iterator;
import java.util.Properties;

/**
 * A helper class for working with {@link System} {@link Properties}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SystemProperties
{
    /**
     * Creates a snapshot (copy) of the existing system properties.
     *
     * @return a {@link Properties} collection representing a snapshot of
     *         the current system properties
     */
    public static synchronized Properties createSnapshot()
    {
        // make a copy of the system properties as they are now
        Properties properties = new Properties();

        // NOTE: we access/copy/modify individual properties one at a time here
        // instead of replacing the entire collection as we can't be guaranteed
        // some other part of the testing infrastructure/rules etc have replaced
        // the properties implementation
        for (String name : System.getProperties().stringPropertyNames())
        {
            String value = System.getProperty(name);

            properties.setProperty(name, value);
        }

        return properties;
    }


    /**
     * Replaces the current system properties with those defined by the specified
     * {@link Properties} collection.
     *
     * @param properties  the properties to replace the current system properties
     */
    public static synchronized void replaceWith(Properties properties)
    {
        // remove all of the system properties not in the original properties
        // (and update those that have changed)

        // NOTE: we access/copy/modify individual properties one at a time here
        // instead of replacing the entire collection as we can't be guaranteed
        // some other part of the testing infrastructure/rules etc have replaced
        // the properties implementation
        for (String name : System.getProperties().stringPropertyNames())
        {
            if (properties.containsKey(name))
            {
                String value         = System.getProperty(name);
                String originalValue = properties.getProperty(name);

                if (!originalValue.equals(value))
                {
                    System.getProperties().setProperty(name, originalValue);
                }
            }
            else
            {
                System.getProperties().remove(name);
            }
        }
    }
}
