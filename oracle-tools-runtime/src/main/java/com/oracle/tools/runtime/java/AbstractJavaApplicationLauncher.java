/*
 * File: AbstractJavaApplicationLauncher.java
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

import com.oracle.tools.runtime.AbstractApplicationLauncher;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.java.options.ClassName;

import com.oracle.tools.runtime.options.DisplayName;

/**
 * An {@link AbstractJavaApplicationLauncher} is the base implementation for {@link JavaApplicationLauncher}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractJavaApplicationLauncher<A extends JavaApplication, P extends Platform>
    extends AbstractApplicationLauncher<A, P> implements JavaApplicationLauncher<A, P>
{
    /**
     * Constructs a {@link AbstractJavaApplicationLauncher}.
     *
     * @param platform  the {@link Platform} on which an {@link Application} will be launched
     */
    public AbstractJavaApplicationLauncher(P platform)
    {
        super(platform);
    }


    @Override
    protected DisplayName getDisplayName(Options options)
    {
        ClassName className = options.get(ClassName.class);

        if (className == null)
        {
            return options.get(DisplayName.class);
        }
        else
        {
            // determine the short class name of the class we're launching (as a possible default)
            String shortClassName = className.getName();
            int    lastDot        = shortClassName.lastIndexOf(".");

            shortClassName = lastDot <= 0 ? shortClassName : shortClassName.substring(lastDot + 1);

            if (shortClassName.isEmpty())
            {
                return options.get(DisplayName.class);
            }
            else
            {
                return options.getOrDefault(DisplayName.class, DisplayName.of(shortClassName));
            }
        }
    }
}
