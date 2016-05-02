/*
 * File: SimpleRemoteApplicationLauncher.java
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

package com.oracle.bedrock.runtime.remote;

import com.oracle.bedrock.runtime.SimpleApplication;
import com.oracle.bedrock.Options;

/**
 * A simple implementation of a {@link ApplicationLauncher}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SimpleRemoteApplicationLauncher extends AbstractRemoteApplicationLauncher<SimpleApplication>
{
    /**
     * Constructs a {@link SimpleRemoteApplicationLauncher}.
     *
     */
    public SimpleRemoteApplicationLauncher()
    {
    }


    @Override
    protected void onLaunching(Options options)
    {
        // by default there's nothing to do on launching
    }


    @Override
    protected void onLaunched(SimpleApplication application,
                              Options           options)
    {
        // by default there's nothing to do once launched
    }
}
