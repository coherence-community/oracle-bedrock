/*
 * File: RestartCoherenceClusterMemberAction.java
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

package com.oracle.tools.runtime.coherence.actions;

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.deferred.DeferredPredicate;
import com.oracle.tools.predicate.Predicate;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.actions.CustomAction;
import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.options.Discriminator;
import com.oracle.tools.runtime.options.DisplayName;
import com.tangosol.util.UID;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.predicate.Predicates.contains;
import static com.oracle.tools.predicate.Predicates.doesNotContain;
import static com.oracle.tools.predicate.Predicates.greaterThan;
import static com.oracle.tools.predicate.Predicates.is;

/**
 * A {@link CustomAction} to close a {@link CoherenceClusterMember} that is defined as part
 * of a {@link CoherenceCluster} and then launch a new {@link CoherenceClusterMember} given
 * a specified {@link Options}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RestartCoherenceClusterMemberAction implements CustomAction<CoherenceClusterMember, CoherenceCluster>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(RestartCoherenceClusterMemberAction.class.getName());

    /**
     * The prefix / {@link DisplayName} of the {@link CoherenceClusterMember} to close.
     */
    private String prefix;

    /**
     * The optional {@link Predicate} that must be satisfied before a {@link CoherenceClusterMember} is closed.
     */
    private Predicate<CoherenceClusterMember> closePredicate;

    /**
     * The {@link Platform} to launch the new {@link CoherenceClusterMember}.
     */
    private Platform platform;

    /**
     * The {@link Option}s to use when realizing the new {@link CoherenceClusterMember}.
     */
    private Option[] options;


    /**
     * Constructs a {@link RestartCoherenceClusterMemberAction}.
     *
     * @param prefix          the prefix / {@link DisplayName} of {@link CoherenceClusterMember} to close
     * @param closePredicate  the optional {@link Predicate} that must be satisfied before restarting a
     *                        {@link CoherenceClusterMember} (may be <code>null</code>)
     * @param platform        the {@link Platform} to launch new {@link CoherenceClusterMember}s on
     * @param options         the {@link Option}s to use when realizing the new {@link CoherenceClusterMember}
     */
    public RestartCoherenceClusterMemberAction(String                            prefix,
                                               Predicate<CoherenceClusterMember> closePredicate,
                                               Platform                          platform,
                                               Option...                         options)
    {
        this.prefix = prefix;
        this.closePredicate = closePredicate == null
                              ? com.oracle.tools.predicate.Predicates.<CoherenceClusterMember>always() : closePredicate;
        this.platform = platform;
        this.options  = options;
    }


    @Override
    public void perform(final CoherenceCluster cluster)
    {
        // obtain an iterator over the candidate cluster members
        Iterator<CoherenceClusterMember> clusterMembers = cluster.getAll(prefix).iterator();

        if (clusterMembers.hasNext())
        {
            // assume we want to launch a new member
            boolean launchNewMember = true;

            // choose the first cluster member from the candidates
            CoherenceClusterMember member    = clusterMembers.next();
            UID                    memberUID = member.getLocalMemberUID();

            // we'll use the same ClusterMember name for the new ClusterMember
            String        name          = member.getName();

            DisplayName   displayName   = member.getOptions().get(DisplayName.class);
            Discriminator discriminator = member.getOptions().get(Discriminator.class);

            // close the ClusterMember
            try
            {
                if (LOGGER.isLoggable(Level.INFO))
                {
                    LOGGER.info("Closing cluster member [" + name + " #" + member.getLocalMemberId() + "]");
                }

                // ensure that the predicate is satisfied (using a deferred)
                ensure(new DeferredPredicate<>(member, closePredicate));

                if (cluster.remove(member))
                {
                    member.close();
                }
                else
                {
                    launchNewMember = false;
                }
            }
            catch (Exception e)
            {
                LOGGER.log(Level.WARNING,
                           "Failed to close member [" + member + "].  Skipping restarting another member",
                           e);

                launchNewMember = false;
            }

            if (launchNewMember)
            {
                try
                {
                    if (LOGGER.isLoggable(Level.INFO))
                    {
                        LOGGER.info("Creating a new cluster member [" + name + "]");
                    }

                    synchronized (cluster)
                    {
                        if (cluster.isClosed())
                        {
                            LOGGER.warning("Abandoning creation of the new cluster member as the Cluster is now closed");
                        }
                        else
                        {
                            // ensure that the cluster no longer contains the closed member
                            ensure(eventually(invoking(cluster).getClusterMemberUIDs()), doesNotContain(memberUID));

                            // ensure that the predicate is satisfied (using a deferred)
                            ensure(new DeferredPredicate<>(clusterMembers.next(), closePredicate));

                            // establish the launch options
                            Options launchOptions = new Options(options);

                            launchOptions.addIfAbsent(displayName);
                            launchOptions.addIfAbsent(discriminator);

                            // start a new ClusterMember (with the same name as the old member)
                            member = platform.launch(CoherenceClusterMember.class, launchOptions.asArray());

                            // ensure that the new member has joined the cluster
                            ensure(eventually(invoking(member).getClusterSize()), is(greaterThan(1)));

                            memberUID = member.getLocalMemberUID();

                            // add the new ClusterMember into the Cluster
                            cluster.add(member);

                            // ensure that the new member is a member of the cluster
                            ensure(eventually(invoking(cluster).getClusterMemberUIDs()), contains(memberUID));

                            if (LOGGER.isLoggable(Level.INFO))
                            {
                                LOGGER.info("Created new cluster member [" + name + " #" + member.getLocalMemberId()
                                            + "]");
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.WARNING, "Failed to create a new member", e);
                }
            }
        }
        else
        {
            LOGGER.severe("Failed to restart a cluster member as there were no cluster members with a prefix ["
                          + prefix + "]");
        }
    }
}
