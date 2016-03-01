/*
 * File: RemoteApplicationBuilders.java
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

package com.oracle.tools.runtime.remote.options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.options.ApplicationBuilders;
import com.oracle.tools.runtime.remote.AbstractRemoteApplicationBuilder;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.SimpleRemoteApplicationBuilder;
import com.oracle.tools.runtime.remote.java.RemoteJavaApplicationBuilder;

/**
 * A factory class that can create instances of an {@link ApplicationBuilder.Supplier}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class RemoteApplicationBuilders extends ApplicationBuilders
{
    /**
     * Create an {@link ApplicationBuilder.Supplier} that will supply an {@link ApplicationBuilder}
     * for building {@link Application}s that run on a {@link RemotePlatform}.
     *
     * @return  an {@link ApplicationBuilder.Supplier} that will supply an {@link ApplicationBuilder}
     *          for building {@link Application}s that run on a {@link RemotePlatform}
     */
    public static <A extends Application> ApplicationBuilder.Supplier<A, RemotePlatform> remote()
    {
        return new ApplicationBuilder.Supplier<A, RemotePlatform>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public ApplicationBuilder<A, RemotePlatform> getApplicationBuilder(RemotePlatform platform,
                                                                              Class<? extends A> applicationClass)
            {
                AbstractRemoteApplicationBuilder builder;

                if (JavaApplication.class.isAssignableFrom(applicationClass))
                {
                    builder = new RemoteJavaApplicationBuilder(platform);
                }
                else
                {
                    builder = new SimpleRemoteApplicationBuilder(platform);
                }

                return builder;
            }
        };
    }
}
