/*
 * File: RemoteApplicationBuildersTest.java
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

import com.oracle.tools.Options;
import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.SimpleRemoteApplicationBuilder;
import com.oracle.tools.runtime.remote.java.RemoteJavaApplicationBuilder;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link RemoteApplicationBuilders} class.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class RemoteApplicationBuildersTest
{
    @Test
    public void shouldGetCorrectBuilderForRemotePlatformAndNonJavaApplication() throws Exception
    {
        Platform platform = new RemotePlatform(InetAddress.getLocalHost(), "user", null);
        Options  options  = new Options();

        options.add(RemoteApplicationBuilders.remote());

        ApplicationBuilder.Supplier supplier = options.get(ApplicationBuilder.Supplier.class);

        assertThat(supplier, is(notNullValue()));

        ApplicationBuilder builder = supplier.getApplicationBuilder(platform, SimpleApplication.class);

        assertThat(builder, is(instanceOf(SimpleRemoteApplicationBuilder.class)));
        assertThat(builder.getPlatform(), is(sameInstance(platform)));
    }


    @Test
    public void shouldGetCorrectBuilderForRemotePlatformAndJavaApplication() throws Exception
    {
        Platform platform = new RemotePlatform(InetAddress.getLocalHost(), "user", null);
        Options  options  = new Options();

        options.add(RemoteApplicationBuilders.remote());

        ApplicationBuilder.Supplier supplier = options.get(ApplicationBuilder.Supplier.class);

        assertThat(supplier, is(notNullValue()));

        ApplicationBuilder builder = supplier.getApplicationBuilder(platform, JavaApplication.class);

        assertThat(builder, is(instanceOf(RemoteJavaApplicationBuilder.class)));
        assertThat(builder.getPlatform(), is(sameInstance(platform)));
    }
}
