/*
 * File: SessionExists.java
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
import com.tangosol.net.Session;

/**
 * A {@link RemoteCallable} to determine whether a given {@link Session}
 * exists on a remote cluster member.
 */
public class SessionExists
        implements RemoteCallable<Boolean>
{
    private final String coherenceName;

    private final String sessionName;

    public SessionExists()
    {
        this(Coherence.DEFAULT_NAME, Coherence.DEFAULT_NAME);
    }

    public SessionExists(String sessionName)
    {
        this(Coherence.DEFAULT_NAME, sessionName);
    }

    public SessionExists(String coherenceName, String sessionName)
    {
        this.coherenceName = coherenceName == null ? Coherence.DEFAULT_NAME : coherenceName;
        this.sessionName = sessionName == null ? Coherence.DEFAULT_NAME : sessionName;
    }

    @Override
    public Boolean call() throws Exception
    {
        return Coherence.getInstances()
                        .stream()
                        .filter(c -> c.getName().equals(coherenceName))
                        .map(c -> c.hasSession(sessionName))
                        .findFirst()
                        .orElse(false);
    }
}
