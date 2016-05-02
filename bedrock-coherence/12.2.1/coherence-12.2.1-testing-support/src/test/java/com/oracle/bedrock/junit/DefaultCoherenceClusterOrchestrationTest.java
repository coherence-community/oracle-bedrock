/*
 * File: DefaultCoherenceClusterOrchestrationTest.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.deferred.Eventually;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.not;

import static org.hamcrest.core.Is.is;

/**
 * Functional Tests for the {@link CoherenceClusterOrchestration}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DefaultCoherenceClusterOrchestrationTest
{
    /**
     * A {@link CoherenceClusterOrchestration} for a default {@link CoherenceCluster}.
     */
    @Rule
    public CoherenceClusterOrchestration orchestration = new CoherenceClusterOrchestration();


    /**
     * Ensure that a default cluster is formed by a {@link CoherenceClusterOrchestration}.
     */
    @Test
    public void shouldFormDefaultCluster()
    {
        Assert.assertThat(orchestration.getCluster().getClusterSize(), is(3));
    }


    /**
     * Ensure that a {@link StorageDisabledMember} session can be created against the
     * {@link CoherenceClusterOrchestration}.
     */
    @Test
    public void shouldCreateStorageDisabledMemberSession()
    {
        ConfigurableCacheFactory session = orchestration.getSessionFor(SessionBuilders.storageDisabledMember());

        NamedCache               cache   = session.ensureCache("dist-example", null);

        Eventually.assertThat(orchestration.getCluster().getClusterSize(), is(4));

        cache.put("message", "hello world");

        Eventually.assertThat(invoking(orchestration.getCluster().getCache("dist-example")).get("message"),
                              is((Object) "hello world"));
    }


    /**
     * Ensure that a {@link ExtendClient} session can be created against the
     * {@link CoherenceClusterOrchestration}.
     */
    @Test
    public void shouldCreateExtendClientSession()
    {
        ConfigurableCacheFactory session = orchestration.getSessionFor(SessionBuilders.extendClient("coherence-cache-config.xml"));

        NamedCache               cache   = session.ensureCache("dist-example", null);

        Assert.assertThat(orchestration.getCluster().getClusterSize(), is(3));

        cache.put("message", "hello world");

        Eventually.assertThat(invoking(orchestration.getCluster().getCache("dist-example")).get("message"),
                              is((Object) "hello world"));
    }


    /**
     * Ensure that an {@link CoherenceClusterOrchestration} will return the same Session when using
     * the same {@link SessionBuilder}.
     */
    @Test()
    public void shouldReturnSameSessionForSameSessionBuilder()
    {
        ConfigurableCacheFactory cacheFactory1 = orchestration.getSessionFor(SessionBuilders.storageDisabledMember());

        ConfigurableCacheFactory cacheFactory2 = orchestration.getSessionFor(SessionBuilders.storageDisabledMember());

        ConfigurableCacheFactory cacheFactory3 =
            orchestration.getSessionFor(SessionBuilders.extendClient("coherence-cache-config.xml"));

        ConfigurableCacheFactory cacheFactory4 =
            orchestration.getSessionFor(SessionBuilders.extendClient("coherence-cache-config.xml"));

        Assert.assertThat(cacheFactory1, is(cacheFactory2));
        Assert.assertThat(cacheFactory3, is(cacheFactory4));
        Assert.assertThat(cacheFactory1, is(not(cacheFactory3)));
    }
}
