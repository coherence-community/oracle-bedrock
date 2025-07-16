/*
 * File: ApplicationLauncher.java
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

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.runtime.options.DisplayName;

/**
 * An internal mechanism to launch {@link Application}s for a specific type of
 * {@link Platform} based on a set of {@link Option}s.
 * <p>
 * Developers must never use implementations of this class.  Instead
 * they should use instances of specific {@link Platform}s.
 * <p>
 * {@link ApplicationLauncher}s are single-use-only in that they may only be
 * used to launch a single {@link Application}.  Any attempt to launch two or more
 * {@link Application}s using a single {@link ApplicationLauncher} instance is
 * undefined.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application}s the {@link ApplicationLauncher} will launch
 */
@Internal
public interface ApplicationLauncher<A extends Application>
        extends Option
{
    /**
     * Launches an {@link Application} on the {@link Platform} using the provided {@link OptionsByType}s.
     *
     * @param platform       the {@link Platform} the {@link Application} is being launched on
     * @param metaClass      the {@link MetaClass} of the {@link Application}
     * @param optionsByType  the {@link OptionsByType} for launching the {@link Application}
     *
     * @return an {@link Application} representing the application launched by the {@link ApplicationLauncher}
     *
     * @throws RuntimeException when a problem occurs while launching the application
     */
    A launch(Platform      platform,
             MetaClass<A>  metaClass,
             OptionsByType optionsByType);


    /**
     * Obtain the {@link DisplayName} for the {@link Application}, defaulting to something sensible
     * based on the provided {@link OptionsByType} if not defined.
     *
     * @param optionsByType  the {@link OptionsByType}
     *
     * @return a {@link DisplayName}
     */
    default DisplayName getDisplayName(OptionsByType optionsByType)
    {
        return optionsByType.get(DisplayName.class);
    }
}
