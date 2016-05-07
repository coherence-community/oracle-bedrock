/*
 * File: DefaultScope.java
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

package com.oracle.bedrock.runtime.java.container;

import com.oracle.bedrock.annotations.Internal;

/**
 * A {@link Scope} to use in a Container when a {@link ContainerScope} could
 * not be determined.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Internal
public class DefaultScope extends AbstractContainerScope
{
    /**
     * Constructs a {@link DefaultScope}.
     *
     * @param platformScope  the {@link PlatformScope} on which the {@link DefaultScope}
     *                       will be based.
     */
    public DefaultScope(PlatformScope platformScope)
    {
        super("(Default)", platformScope.getProperties(), platformScope.getAvailablePorts(), null);

        stdout = platformScope.getStandardOutput();
        stderr = platformScope.getStandardError();
        stdin  = platformScope.getStandardInput();
    }
}
