/*
 * File: CoherenceCluster.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.AbstractAssembly;

import com.tangosol.util.UID;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by brianoliver on 5/22/14.
 */
public class CoherenceCluster extends AbstractAssembly<CoherenceClusterMember>
{
    /**
     * Constructs a {@link CoherenceCluster} given a list of {@link CoherenceClusterMember}s.
     *
     * @param members  the {@link CoherenceClusterMember}s
     */
    public CoherenceCluster(List<? extends CoherenceClusterMember> members)
    {
        super(members);
    }


    /**
     * Obtains the current number of {@link CoherenceClusterMember}s in the {@link CoherenceCluster}.
     *
     * @return the current number of {@link CoherenceClusterMember}s
     */
    public int getClusterSize()
    {
        Iterator<CoherenceClusterMember> members = iterator();

        return members.hasNext() ? members.next().getClusterSize() : 0;
    }


    /**
     * Obtains the member {@link UID}s for the {@link Cluster}.
     *
     * @return  a {@link Set} of {@link UID}, one for each {@link ClusterMember}
     */
    public Set<UID> getClusterMemberUIDs()
    {
        Iterator<CoherenceClusterMember> members = iterator();

        return members.hasNext() ? members.next().getClusterMemberUIDs() : new TreeSet<UID>();
    }
}
