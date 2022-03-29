/*
 * File: AbstractCoherenceSessionTest.java
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

package com.oracle.bedrock.runtime.coherence;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.coherence.callables.GetClusterSize;
import com.oracle.bedrock.runtime.coherence.callables.GetLocalMemberId;
import com.oracle.bedrock.runtime.coherence.options.ClusterPort;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.testsupport.junit.AbstractTest;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Functional Tests for {@link CoherenceSession}s.
 * <p>
 * Copyright (c) 2021. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 */
public class CoherenceSessionIT extends AbstractTest
{
    /**
     * Obtains the {@link Platform} to use when realizing applications.
     */
    public Platform getPlatform()
    {
        return LocalPlatform.get();
    }

    @Test
    public void shouldGetDefaultSession()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           Console.system()))
        {
            Eventually.assertThat(server, new GetLocalMemberId(), is(1));
            Eventually.assertThat(server, new GetClusterSize(), is(1));

            Session session = server.getSession();
            assertThat(session, is(notNullValue()));
            assertThat(session.isActive(), is(true));
            assertThat(session.getScopeName(), is(Coherence.DEFAULT_SCOPE));
        }
    }

    @Test
    public void shouldGetCacheFromDefaultSession()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           Console.system()))
        {
            Eventually.assertThat(server, new GetLocalMemberId(), is(1));
            Eventually.assertThat(server, new GetClusterSize(), is(1));

            Session session = server.getSession();
            assertThat(session, is(notNullValue()));

            NamedCache<String, String> sessionCache = session.getCache("test");
            NamedCache<String, String> cache        = server.getCache("test");

            assertThat(sessionCache, is(notNullValue()));
            assertThat(cache, is(notNullValue()));

            sessionCache.put("key-1", "value-1");
            assertThat(cache.get("key-1"), is("value-1"));
        }
    }

    @Test
    public void shouldGetSystemSession()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           Console.system()))
        {
            Eventually.assertThat(server, new GetLocalMemberId(), is(1));
            Eventually.assertThat(server, new GetClusterSize(), is(1));

            Session session = server.getSession(Coherence.SYSTEM_SESSION);
            assertThat(session, is(notNullValue()));
            assertThat(session.isActive(), is(true));
            assertThat(session.getScopeName(), is(Coherence.SYSTEM_SESSION));
        }
    }


    @Test
    public void shouldGetCacheFromSystemSession()
    {
        Platform platform = getPlatform();

        try (CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                           ClusterPort.automatic(),
                                                           LocalHost.only(),
                                                           Console.system()))
        {
            Eventually.assertThat(server, new GetLocalMemberId(), is(1));
            Eventually.assertThat(server, new GetClusterSize(), is(1));

            Session session = server.getSession(Coherence.SYSTEM_SESSION);
            assertThat(session, is(notNullValue()));

            NamedCache<String, String> sessionCache = session.getCache("sys$config-test");
            NamedCache<String, String> cache        = server.getCache(Coherence.SYSTEM_SESSION, "sys$config-test");

            assertThat(sessionCache, is(notNullValue()));
            assertThat(cache, is(notNullValue()));

            sessionCache.put("key-1", "value-1");
            assertThat(cache.get("key-1"), is("value-1"));

            NamedCache<String, String> defaultCache = server.getCache("sys$config-test");
            assertThat(defaultCache.get("key-1"), is(nullValue()));
        }
    }
}
