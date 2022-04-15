/*
 * File: CoherenceClusterOrchestration.java
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

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.coherence.CoherenceCluster;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.ClusterPort;
import com.oracle.bedrock.runtime.coherence.options.LocalStorage;
import com.oracle.bedrock.runtime.coherence.options.Multicast;
import com.oracle.bedrock.runtime.coherence.options.RoleName;
import com.oracle.bedrock.runtime.java.options.Headless;
import com.oracle.bedrock.runtime.java.options.HeapSize;
import com.oracle.bedrock.runtime.java.options.HotSpot;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.ApplicationClosingBehavior;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.util.Capture;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Properties;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.core.Is.is;

/**
 * A JUnit {@link ExternalResource} to represent and orchestrate configuring,
 * establishing, accessing and tearing down a Oracle Coherence Cluster, with
 * support for requesting various ConfigurableCacheFactory implementations to
 * interact with the orchestrated Coherence Cluster.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 * <p>
 * <strong>DEPRECATED:</strong> Please consider migrating to use the {@link CoherenceClusterResource} instead. This
 * class may be removed in the next major release.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 * @author Harvey Raja
 * @author Aleks Seovic
 */
@Deprecated
public class CoherenceClusterOrchestration extends ExternalResource
{
    /**
     * The number of storage enable members that will be started in the cluster.
     */
    private int storageMemberCount = 2;

    /**
     * The {@link LocalPlatform} on which the Coherence Cluster will be orchestrated.
     */
    private final LocalPlatform platform;

    /**
     * The {@link OptionsByType} to use as a basis for constructing a variety of {@link CoherenceClusterMember}s.
     */
    private final OptionsByType commonMemberOptions;

    /**
     * The {@link OptionsByType} to use as a basis for constructing storage-enabled {@link CoherenceClusterMember}s.
     */
    private final OptionsByType storageMemberOptions;

    /**
     * The {@link OptionsByType} to use when creating the {@link CoherenceCluster}.
     */
    private OptionsByType clusterCreationOptions;

    /**
     * The {@link OptionsByType} to use when closing the {@link CoherenceCluster}.
     */
    private OptionsByType clusterClosingOptions;

    /**
     * The {@link CoherenceCluster} established by the orchestrator.
     */
    private CoherenceCluster cluster;

    /**
     * The original system properties to be restored when the orchestration is complete.
     */
    private Properties systemProperties;

    /**
     * The {@link ConfigurableCacheFactory} sessions that have been
     * created against the orchestrated {@link CoherenceCluster}.
     */
    private final HashMap<SessionBuilder, ConfigurableCacheFactory> sessions;

    /**
     * The cluster port.
     */
    private Capture<Integer> clusterPort;


    /**
     * Constructs a {@link CoherenceClusterOrchestration}.
     */
    public CoherenceClusterOrchestration()
    {
        // we're going to orchestrate the cluster using the LocalPlatform
        this.platform = LocalPlatform.get();

        // establish a Cluster port
        this.clusterPort = new Capture<>(platform.getAvailablePorts());

        // establish a common member options on which to base storage enabled and proxy members
        this.commonMemberOptions = OptionsByType.empty();

        // establish the common server schema address and port details
        String hostAddress = platform.getLoopbackAddress().getHostAddress();

        // TODO: remove when NameService respects localhost system property
        // this.commonMemberOptions.add(LocalHost.of(hostAddress));
        this.commonMemberOptions.add(ClusterPort.of(clusterPort));
        this.commonMemberOptions.add(Multicast.ttl(0));

        // we also define proxy configuration
        this.commonMemberOptions.add(SystemProperty.of("tangosol.coherence.extend.address", hostAddress));
        this.commonMemberOptions.add(SystemProperty.of("tangosol.coherence.extend.port", clusterPort));
        this.commonMemberOptions.add(SystemProperty.of("tangosol.coherence.extend.enabled", true));

        // establish default java process configuration
        this.commonMemberOptions.add(Headless.enabled());
        this.commonMemberOptions.add(HotSpot.Mode.SERVER);
        this.commonMemberOptions.add(HeapSize.of(256, HeapSize.Units.MB, 1024, HeapSize.Units.MB));

        // by default we'll use the SystemApplicationConsole
        this.commonMemberOptions.add(Console.system());

        // by default we don't have any special options for storage members
        this.storageMemberOptions = OptionsByType.empty();

        // by default we don't have a cluster
        this.cluster = null;

        // by default the creation and closing options aren't set
        this.clusterCreationOptions = OptionsByType.empty();
        this.clusterClosingOptions  = OptionsByType.empty();

        // by default we have no sessions
        this.sessions = new HashMap<>();
    }


    @Override
    public Statement apply(Statement   base,
                           Description description)
    {
        // automatically set the cluster name to the test class name
        // if the cluster name isn't configured
        if (commonMemberOptions.get(ClusterName.class) == null)
        {
            commonMemberOptions.add(ClusterName.of(description.getClassName()));
        }

        return super.apply(base, description);
    }


    @Override
    protected void before() throws Throwable
    {
        // take a snapshot of the system properties
        systemProperties = com.oracle.bedrock.util.SystemProperties.createSnapshot();

        // establish a CoherenceClusterBuilder with the required configuration
        CoherenceClusterBuilder clusterBuilder = new CoherenceClusterBuilder();

        // define the options for the storage enabled members of the cluster
        OptionsByType storageServerOptions = createStorageEnabledMemberOptions();

        storageServerOptions.addAll(clusterCreationOptions);

        clusterBuilder.include(storageMemberCount, CoherenceClusterMember.class, storageServerOptions.asArray());

        // establish the cluster
        cluster = clusterBuilder.build();

        // let's ensure that we don't have a local cluster member
        CacheFactory.setCacheFactoryBuilder(null);

        CacheFactory.shutdown();

        // let the super-class perform it's initialization
        super.before();
    }


    @Override
    protected void after()
    {
        // clean up the sessions
        synchronized (sessions)
        {
            for (ConfigurableCacheFactory session : sessions.values())
            {
                session.dispose();

                CacheFactory.getCacheFactoryBuilder().release(session);
            }
        }

        CacheFactory.shutdown();

        CacheFactory.setCacheFactoryBuilder(null);

        // close the cluster
        cluster.close(clusterClosingOptions.asArray());

        // restore the system properties
        com.oracle.bedrock.util.SystemProperties.replaceWith(systemProperties);

        // let the super-class perform it's cleanup as well
        super.after();
    }


    protected OptionsByType createStorageEnabledMemberOptions()
    {
        // define the options for the storage enabled members of the cluster
        OptionsByType optionsByType = OptionsByType.of(commonMemberOptions);

        optionsByType.add(DisplayName.of("storage"));
        optionsByType.add(RoleName.of("storage"));
        optionsByType.add(LocalStorage.enabled());

        optionsByType.addAll(storageMemberOptions);

        return optionsByType;
    }


    /**
     * Sets the {@link Option}s to be used when closing the {@link CoherenceCluster}.
     *
     * @param options the {@link Option}s
     *
     * @return the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     *
     * @see ApplicationClosingBehavior
     */
    public CoherenceClusterOrchestration withClosingOptions(Option... options)
    {
        this.clusterClosingOptions = OptionsByType.of(options);

        return this;
    }


    /**
     * Sets the {@link Option}s to be passed to the {@link CoherenceClusterBuilder} when building the
     * {@link CoherenceCluster}.
     *
     * @param options the {@link Option}s
     *
     * @return the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     */
    public CoherenceClusterOrchestration withBuilderOptions(Option... options)
    {
        this.clusterCreationOptions = OptionsByType.of(options);

        return this;
    }


    /**
     * Obtains the {@link CoherenceCluster} that was established by the orchestration.
     *
     * @return the {@link CoherenceCluster}
     */
    public CoherenceCluster getCluster()
    {
        return cluster;
    }


    /**
     * Obtains the {@link LocalPlatform} on which the {@link CoherenceClusterOrchestration} will
     * create sessions.
     * <p>
     * NOTE: The orchestrated {@link CoherenceCluster} may be orchestrated on one or more other
     * {@link Platform}s.
     *
     * @return the {@link LocalPlatform}
     */
    public LocalPlatform getLocalPlatform()
    {
        return platform;
    }


    /**
     * Adds the specified {@link Option}s to the {@link CoherenceClusterOrchestration} for
     * launching all {@link CoherenceClusterMember}s.
     *
     * @param options  the {@link Option}s
     *
     * @return  the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration withOptions(Option... options)
    {
        commonMemberOptions.addAll(options);

        return this;
    }


    /**
     * Adds the specified {@link Option}s to the {@link CoherenceClusterOrchestration} for
     * launching all storage-enabled {@link CoherenceClusterMember}s.
     *
     * @param options  the {@link Option}s for storage-enabled {@link CoherenceClusterMember}s
     *
     * @return  the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration withStorageMemberOptions(Option... options)
    {
        storageMemberOptions.addAll(options);

        return this;
    }


    /**
     * Obtains a session (represented as a {@link ConfigurableCacheFactory} against the
     * orchestrated Coherence Cluster for interacting with Coherence.
     * <p>
     * Only a single session may be created by a {@link CoherenceClusterOrchestration}
     * against an orchestrated {@link CoherenceCluster}.
     * <p>
     * Attempts to request a session multiple times with the same {@link SessionBuilder}
     * will return the same session.
     *
     * @param builder the builder for the specific type of session
     *
     * @return a {@link ConfigurableCacheFactory} representing the Coherence Session.
     *
     * @throws IllegalStateException when an attempt to request sessions for
     *                               different {@link SessionBuilder}s is made
     */
    public synchronized ConfigurableCacheFactory getSessionFor(SessionBuilder builder)
    {
        // restore the system properties (as the session needs to start cleanly)
        com.oracle.bedrock.util.SystemProperties.replaceWith(systemProperties);

        ConfigurableCacheFactory session = sessions.get(builder);

        if (session == null)
        {
            OptionsByType optionsByType = OptionsByType.of(commonMemberOptions);

            optionsByType.add(RoleName.of("client"));
            optionsByType.add(LocalStorage.disabled());

            session = builder.build(platform, getCluster(), optionsByType);

            sessions.put(builder, session);
        }

        return session;
    }


    /**
     * Sets the number of storage enabled members that will be started in the cluster.
     *
     * @param count the number of storage enabled members
     *
     * @return the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration setStorageMemberCount(int count)
    {
        storageMemberCount = count;

        return this;
    }
}
