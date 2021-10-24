/*
 * File: GetSession.java
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

package com.oracle.bedrock.runtime.coherence.callables;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

/**
 * A {@link RemoteCallable} to obtain a {@link NamedCache} from a specific {@link Session}.
 */
@SuppressWarnings("rawtypes")
public class GetSessionCache
        implements RemoteCallable<NamedCache>
{
    /**
     * The name of the {@link Coherence} instance.
     */
    private final String coherenceName;

    /**
     * The name of the {@link Session} instance.
     */
    private final String sessionName;

    /**
     * The name of the {@link NamedCache}.
     */
    private final String cacheName;

    public GetSessionCache(String coherenceName, String sessionName, String cacheName)
    {
        this.coherenceName = coherenceName == null ? Coherence.DEFAULT_NAME : coherenceName;
        this.sessionName   = sessionName == null ? Coherence.DEFAULT_NAME : sessionName;
        this.cacheName = cacheName;
    }

    @Override
    public NamedCache call() throws Exception {
        return Coherence.getInstance(coherenceName).getSession(sessionName).getCache(cacheName);
    }
}
