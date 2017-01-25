/*
 * File: SessionBuilder.java
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

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.tangosol.net.ConfigurableCacheFactory;

/**
 * A mechanism to build local Coherence Session (represented as a {@link ConfigurableCacheFactory}), typically
 * for a {@link CoherenceClusterOrchestration} or {@link CoherenceClusterResource}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface SessionBuilder
{
    /**
     * Creates a {@link ConfigurableCacheFactory} for a Coherence Session.
     *
     * @param platform       the {@link LocalPlatform} on which the {@link ConfigurableCacheFactory} will be established
     * @param cluster        the {@link CoherenceCluster} for which the session will be created
     * @param optionsByType  the {@link OptionsByType}s provided to all of the {@link CoherenceClusterMember}s
     *                       when establishing the {@link CoherenceCluster}
     *
     * @return a {@link ConfigurableCacheFactory}
     */
    ConfigurableCacheFactory build(LocalPlatform platform,
                                   CoherenceCluster cluster,
                                   OptionsByType optionsByType);
}
