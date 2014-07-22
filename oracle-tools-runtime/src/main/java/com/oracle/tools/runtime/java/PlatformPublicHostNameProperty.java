/*
 * File: PlatformPublicHostNameProperty.java
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

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.PlatformAware;

/**
 * Instances of this class can be used to provide the
 * public host name of a {@link Platform} as a system
 * property in a {@link com.oracle.tools.runtime.PropertiesBuilder}.
 *
 * @author Jonathan Knight
 */
public class PlatformPublicHostNameProperty implements PlatformAware
{
    /** The current {@link com.oracle.tools.runtime.Platform} */
    private Platform platform;


    @Override
    public void setPlatform(Platform platform)
    {
        this.platform = platform;
    }


    @Override
    public String toString()
    {
        return platform != null
               ? platform.getPublicInetAddress().getHostName()
               : LocalPlatform.getInstance().getHostName();
    }
}
