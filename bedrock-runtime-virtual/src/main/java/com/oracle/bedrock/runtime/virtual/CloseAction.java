/*
 * File: CloseAction.java
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

package com.oracle.bedrock.runtime.virtual;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;

/**
 * An enum of different options for closing a Virtual Machine.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public enum CloseAction implements Option
{
    /**
     * When close is called on the {@link VirtualPlatform} do nothing
     */
    None,

    /**
     * When close is called on the {@link VirtualPlatform} shut down and destroy the VM
     */
    Destroy,

    /**
     * When close is called on the {@link VirtualPlatform} simulate pressing the power button
     */
    PowerButton,

    /**
     * When close is called on the {@link VirtualPlatform} save the state of the VM and close
     */
    SaveState,

    /**
     * When close is called on the {@link VirtualPlatform} shutdown the VM
     */
    @OptionsByType.Default
    Shutdown
}
