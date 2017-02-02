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

package com.oracle.bedrock.runtime.coherence;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.options.Decorations;
import com.oracle.bedrock.runtime.AbstractAssembly;
import com.oracle.bedrock.runtime.Assembly;
import com.oracle.bedrock.runtime.coherence.callables.GetAutoStartServiceNames;
import com.oracle.bedrock.runtime.coherence.callables.GetServiceStatus;
import com.oracle.bedrock.runtime.coherence.callables.IsServiceStorageEnabled;
import com.oracle.bedrock.runtime.concurrent.options.Caching;
import com.oracle.bedrock.util.Trilean;
import com.tangosol.net.NamedCache;
import com.tangosol.util.UID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import static com.oracle.bedrock.deferred.DeferredHelper.ensure;
import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static com.oracle.bedrock.predicate.Predicates.contains;
import static com.oracle.bedrock.predicate.Predicates.doesNotContain;
import static com.oracle.bedrock.predicate.Predicates.greaterThan;

/**
 * An {@link Assembly} that represents a collection of {@link CoherenceClusterMember}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CoherenceCluster extends AbstractAssembly<CoherenceClusterMember>
{
    /**
     * Constructs a {@link CoherenceCluster}.
     *
     * @param optionsByType  the {@link OptionsByType} for the {@link CoherenceCluster}
     */
    public CoherenceCluster(OptionsByType optionsByType)
    {
        super(optionsByType);
    }


    /**
     * Obtains the current number of {@link CoherenceClusterMember}s in the underlying
     * {@link CoherenceCluster} by asking a {@link CoherenceClusterMember}.
     *
     * @return the current number of {@link CoherenceClusterMember}s
     */
    public int getClusterSize()
    {
        Iterator<CoherenceClusterMember> members = iterator();

        return members.hasNext() ? members.next().getClusterSize() : 0;
    }


    /**
     * Obtains the member {@link UID}s for the {@link CoherenceCluster}.
     *
     * @return  a {@link Set} of {@link UID}, one for each {@link CoherenceClusterMember}
     */
    public Set<UID> getClusterMemberUIDs()
    {
        Iterator<CoherenceClusterMember> members = iterator();

        return members.hasNext() ? members.next().getClusterMemberUIDs() : new TreeSet<UID>();
    }


    /**
     * Obtains a proxy of the specified {@link NamedCache} available in the
     * {@link CoherenceCluster}.
     *
     * @param cacheName  the name of the {@link NamedCache}
     *
     * @return  a proxy to the {@link NamedCache}
     */
    public NamedCache getCache(String cacheName)
    {
        Iterator<CoherenceClusterMember> members = iterator();

        return members.hasNext() ? members.next().getCache(cacheName) : null;
    }


    @Override
    protected void onRelaunching(CoherenceClusterMember member,
                                 OptionsByType          optionsByType)
    {
        // get the current MemberUID and record it (or make the application remember it)
        UID memberUID = member.getLocalMemberUID();

        // add the member as a decoration to the OptionsByType
        optionsByType.add(Decoration.of(memberUID));

        // notify the assembly of the change
        onChanged(optionsByType);
    }


    @Override
    protected void onRelaunched(CoherenceClusterMember original,
                                CoherenceClusterMember restarted,
                                OptionsByType          optionsByType)
    {
        // ensure that the original member UID is no longer in the cluster
        Decorations decorations       = optionsByType.get(Decorations.class);

        UID         originalMemberUID = decorations.get(UID.class);

        if (originalMemberUID != null)
        {
            // ensure that the restarted member is in the member set of the cluster
            ensure(eventually(invoking(this).getClusterMemberUIDs()), doesNotContain(originalMemberUID));
        }

        // ensure the restarted member has joined the cluster
        // (without doing this the local member id returned below may be different from
        // the one when the member joins the cluster)
        ensure(eventually(invoking(restarted).getClusterSize()), greaterThan(1));

        // determine the UID of the restarted member
        UID restartedMemberUID = restarted.getLocalMemberUID();

        // ensure that the restarted member is in the member set of the cluster
        ensure(eventually(invoking(this).getClusterMemberUIDs()), contains(restartedMemberUID));

        // notify the assembly of the change
        onChanged(optionsByType);
    }


    /**
     * Useful {@link Predicate}s for a {@link CoherenceCluster}.
     */
    public interface Predicates
    {
        /**
         * A {@link Predicate} to determine if all of the services of
         * {@link CoherenceClusterMember}s are safe.
         *
         * @return  a {@link Predicate}
         */
        static Predicate<CoherenceCluster> autoStartServicesSafe()
        {
            return (cluster) -> {

                // determine the number of each auto start service is defined by the cluster
                       HashMap<String, Integer> serviceCountMap = new HashMap<>();

                       for (CoherenceClusterMember member : cluster)
                       {
                           Set<String> serviceNames = member.invoke(new GetAutoStartServiceNames(), Caching.enabled());

                           for (String serviceName : serviceNames)
                           {
                               // determine if the service is storage enabled?
                               Trilean trilean = member.invoke(new IsServiceStorageEnabled(serviceName));

                               // only adjust the service count when it's not storage disabled
                               // (ie: we count storage enabled and unknown service types)
                               int adjust = trilean == Trilean.FALSE ? 0 : 1;

                               serviceCountMap.compute(serviceName,
                                                       (name, count) -> count == null ? adjust : count + adjust);
                           }
                       }

                       // ensure the autostart services defined by each member are safe
                       // according to the number of required services
                       for (CoherenceClusterMember member : cluster)
                       {
                           Set<String> serviceNames = member.invoke(new GetAutoStartServiceNames(), Caching.enabled());

                           for (String serviceName : serviceNames)
                           {
                               int count = serviceCountMap.get(serviceName);
                        
                               if (count > 1)
                               {
                                   ServiceStatus status = member.invoke(new GetServiceStatus(serviceName));

                                   if (status == null
                                       || status == ServiceStatus.ENDANGERED
                                       || status == ServiceStatus.ORPHANED
                                       || status == ServiceStatus.STOPPED
                                       || status == ServiceStatus.UNKNOWN)
                                   {
                                       return false;
                                   }
                               }
                               else if (count == 1)
                               {
                                   ServiceStatus status = member.invoke(new GetServiceStatus(serviceName));

                                   if (status == null
                                       || status == ServiceStatus.STOPPED
                                       || status == ServiceStatus.UNKNOWN)
                                   {
                                       return false;
                                   }
                               }
                           }
                       }

                       return true;
                   };
        }
    }
}
