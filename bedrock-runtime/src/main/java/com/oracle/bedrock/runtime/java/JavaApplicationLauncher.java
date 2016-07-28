/*
 * File: JavaApplicationLauncher.java
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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.ApplicationLauncher;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.options.DisplayName;

/**
 * A {@link JavaApplicationLauncher} is Java specific {@link ApplicationLauncher}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public interface JavaApplicationLauncher<A extends JavaApplication> extends ApplicationLauncher<A>
{
    @Override
    default DisplayName getDisplayName(OptionsByType optionsByType)
    {
        ClassName className = optionsByType.get(ClassName.class);

        if (className == null)
        {
            return optionsByType.get(DisplayName.class);
        }
        else
        {
            // determine the short class name of the class we're launching (as a possible default)
            String shortClassName = className.getName();
            int    lastDot        = shortClassName.lastIndexOf(".");

            shortClassName = lastDot <= 0 ? shortClassName : shortClassName.substring(lastDot + 1);

            if (shortClassName.isEmpty())
            {
                return optionsByType.get(DisplayName.class);
            }
            else
            {
                return optionsByType.getOrSetDefault(DisplayName.class, DisplayName.of(shortClassName));
            }
        }
    }
}
