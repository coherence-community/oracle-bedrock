/*
 * File: Constants.java
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

package com.oracle.tools.runtime.network;

/**
 * Typical Network {@link Constants}.
 * <p>
 * Copyright (c) 2011-2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Constants
{
    /**
     * Obtains the correct localhost address for the platform, which
     * may depend on the operating system and version of Java.
     *
     * @return the localhost string for the platform
     */
    public static String getLocalHost()
    {
        if (System.getProperties().containsKey("tangosol.coherence.localhost"))
        {
            return System.getProperty("tangosol.coherence.localhost");
        }
        else if (System.getProperty("java.version").startsWith("1.6"))
        {
            return "127.0.0.1";
        }
        else
        {
            return "localhost";
        }
    }
}
